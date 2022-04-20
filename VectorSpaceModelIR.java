/*
    This program implements the Vector Space Information 
    Retrieval model, demonstrated on the Cranfield corpus.

    Authors: Abelson Abueg
    Date Created: 4 Apr 2022
    Last Updated: 19 Apr 2022
*/

// Java
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;

// OpenNLP Stemmer
import opennlp.tools.stemmer.PorterStemmer;

public class VectorSpaceModelIR {
    /*
     *
     * CONSTRUCTOR AND CLASS METHODS
     * 
     */

    // TreeMap<DocID, Title>
    private TreeMap<Integer, String> documents;

    // TreeMap<Term, TreeMap<DocID, Title Term Frequency>>
    private TreeMap<String, TreeMap<Integer, Integer>> termTitleFreq;

    // TreeMap<Term, TreeMap<DocID, Abstract Term Freq>>
    private TreeMap<String, TreeMap<Integer, Integer>> termAbstractFreq;

    // TreeMap<DocID, TreeMap<Term, TF-IDF Weight>>
    private TreeMap<Integer, TreeMap<String, Double>> docTitleWeights;
    private TreeMap<Integer, TreeMap<String, Double>> docAbstractWeights;

    // TreeMap<Final Cosine Similarity Scores, DocID>
    private TreeMap<Double, Integer> finalCosineSimilarityScores;

    // TreeMap<QueryID, Query>
    private TreeMap<String, String> queryList;

    // Stop words from here: https://www.ranks.nl/stopwords
    private ArrayList<String> stopwords = new ArrayList<String>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "arent", "as",
            "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "cant", "cannot",
            "could", "couldnt", "did", "didnt", "do", "does", "doesnt", "doing", "dont", "down", "during", "each",
            "few", "for", "from", "further", "had", "hadnt", "has", "hasnt", "have", "havent", "having", "he", "hed",
            "hell", "hes", "her", "here", "heres", "hers", "herself", "him", "himself", "his", "how", "hows", "i", "id",
            "ill", "im", "ive", "if", "in", "into", "is", "isnt", "it", "its", "its", "itself", "lets", "me", "more",
            "most", "mustnt", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other",
            "ought", "our", "ours 	ourselves", "out", "over", "own", "same", "shant", "she", "shed", "shell", "shes",
            "should", "shouldnt", "so", "some", "such", "than", "that", "thats", "the", "their", "theirs", "them",
            "themselves", "then", "there", "theres", "these", "they", "theyd", "theyll", "theyre", "theyve", "this",
            "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasnt", "we", "wed", "well",
            "were", "weve", "were", "werent", "what", "whats", "when", "whens", "where", "wheres", "which", "while",
            "who", "whos", "whom", "why", "whys", "with", "wont", "would", "wouldnt", "you", "youd", "youll", "youre",
            "youve", "your", "yours", "yourself", "yourselves"));

    // Default Constructor; it's all you really need.
    public VectorSpaceModelIR() {
        // For storing data
        this.documents = new TreeMap<Integer, String>();
        this.termTitleFreq = new TreeMap<String, TreeMap<Integer, Integer>>();
        this.termAbstractFreq = new TreeMap<String, TreeMap<Integer, Integer>>();

        // For storing weights TF-IDF Weights
        this.docTitleWeights = new TreeMap<Integer, TreeMap<String, Double>>();
        this.docAbstractWeights = new TreeMap<Integer, TreeMap<String, Double>>();

        // For storing final Cosine Similarity Scores
        this.finalCosineSimilarityScores = new TreeMap<Double, Integer>(Collections.reverseOrder());

        // For storing QueryID and the Query
        this.queryList = new TreeMap<String, String>();
    }

    /*
     * 
     * Read the document and keep track of docID-Titles and
     * term and document frequencies for Title and Abstract
     * 
     */
    void BuildData(String inputPath) {
        // br for efficiently reading characters from an input stream
        BufferedReader br = null;

        /*
         * wordPattern specifies pattern for words using a regular expression
         * wordMatcher finds words by spotting word patterns with input
         */
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");
        Matcher wordMatcher;

        /*
         * line - a line read from file
         * word - an extracted word from a line
         */
        String line = "", word;

        /*
         * Stores the document ID
         */
        int docID = 0;

        try {
            br = new BufferedReader(new FileReader(inputPath));

            // Stemmer
            PorterStemmer stemmer = new PorterStemmer();

            line = br.readLine();
            while (line != null) {
                // Will store a cleaner version of line into String ArrayList
                ArrayList<String> cleanLine = new ArrayList<String>();

                if (line.contains(".I")) {
                    docID = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                    line = br.readLine();
                } else if (line.contains(".T")) {
                    String title = "";
                    while ((line = br.readLine()).compareTo(".A") != 0) {
                        wordMatcher = wordPattern.matcher(line);
                        // Process one word at a time
                        while (wordMatcher.find()) {
                            // Extract and convert the word to lowercase
                            word = line.substring(wordMatcher.start(), wordMatcher.end());
                            title = title + " " + word;
                            cleanLine.add(word.toLowerCase());
                        } // while - wordMatcher

                        /*
                         * Handles cases if the line is empty
                         *
                         * Without this, it will count empty strings
                         * because cleanLine is originally empty.
                         */
                        if (!cleanLine.isEmpty()) {
                            for (String term : cleanLine) {
                                String stemmedTerm = stemmer.stem(term);

                                // To avoid adding stopwords
                                if (stopwords.contains(term)) {
                                    continue;
                                }
                                // If the term exists in the title term frequency
                                else if (this.termTitleFreq.containsKey(stemmedTerm)) {
                                    // If the document exists in the title term frequency
                                    if (this.termTitleFreq.get(stemmedTerm).containsKey(docID)) {
                                        // Update the term count from the document.
                                        this.termTitleFreq.get(stemmedTerm).replace(docID,
                                                this.termTitleFreq.get(stemmedTerm).get(docID) + 1);
                                    } else {
                                        // Add a new document term frequency
                                        this.termTitleFreq.get(stemmedTerm).put(docID, 1);
                                    }
                                    // If the term doesn't exist.
                                } else {
                                    // Create a new document term frequency holder
                                    TreeMap<Integer, Integer> newTermDocFreqHolder = new TreeMap<>();
                                    // Put in the new document term frequency for DocID
                                    newTermDocFreqHolder.put(docID, 1);
                                    // Insert a new term into termTitleFreq
                                    termTitleFreq.put(stemmedTerm, newTermDocFreqHolder);
                                }
                            }
                        }
                    }

                    // Add the new document into the documents TreeMap
                    documents.put(docID, title.trim());

                } else if (line.contains(".A")) {
                    while ((line = br.readLine()).compareTo(".B") != 0) {
                        continue;
                    }

                } else if (line.contains(".B")) {
                    while ((line = br.readLine()).compareTo(".W") != 0) {
                        continue;
                    }

                } else if (line.contains(".W")) {
                    line = br.readLine();
                    while (!line.contains(".I")) {
                        wordMatcher = wordPattern.matcher(line);
                        // Process one word at a time
                        while (wordMatcher.find()) {
                            // Extract and convert the word to lowercase
                            word = line.substring(wordMatcher.start(), wordMatcher.end());
                            cleanLine.add(word.toLowerCase());
                        } // while - wordMatcher

                        /*
                         * Handles cases if the line is empty
                         *
                         * Without this, it will count empty strings
                         * because cleanLine is originally empty.
                         */
                        if (!cleanLine.isEmpty()) {
                            for (String term : cleanLine) {
                                String stemmedTerm = stemmer.stem(term);

                                // To avoid adding stopwords.
                                if (stopwords.contains(term)) {
                                    continue;
                                }
                                // If the term exists in the title term frequency
                                else if (this.termAbstractFreq.containsKey(stemmedTerm)) {
                                    // If the document exists in the title term frequency
                                    if (this.termAbstractFreq.get(stemmedTerm).containsKey(docID)) {
                                        // Update the term count from the document.
                                        this.termAbstractFreq.get(stemmedTerm).replace(docID,
                                                this.termAbstractFreq.get(stemmedTerm).get(docID) + 1);
                                    } else {
                                        // Add a new document term frequency
                                        this.termAbstractFreq.get(stemmedTerm).put(docID, 1);
                                    }
                                    // If the term doesn't exist.
                                } else {
                                    // Create a new document term frequency holder
                                    TreeMap<Integer, Integer> newTermDocFreqHolder = new TreeMap<>();
                                    // Put in the new document term frequency for DocID
                                    newTermDocFreqHolder.put(docID, 1);
                                    // Insert a new term into termTitleFreq
                                    termAbstractFreq.put(stemmedTerm, newTermDocFreqHolder);
                                }
                            }
                        }

                        line = br.readLine();
                        if (line == null) {
                            break;
                        }
                    }

                }
            }
        } catch (IOException ex) {
            System.err.println("File " + inputPath + " not found. Program terminated.\n");
            System.exit(1);
        }
    }

    /*
     *
     * Calculate and store TF-IDF weights for each term in each
     * document for both Title and Abstract
     * 
     */
    void CalcTFXIDF() {
        int collectionSize = this.documents.size();

        // For iterating term level of termTitleFreqEntry
        Set<Map.Entry<String, TreeMap<Integer, Integer>>> termTitleFreqEntry = this.termTitleFreq.entrySet();
        termTitleFreqEntry.forEach(term -> {
            // Get the document term frequency from the docID TreeMap size
            int termDocFreq = term.getValue().size();
            String termKey = term.getKey();

            // For iterating doc level
            Set<Map.Entry<Integer, Integer>> docEntry = term.getValue().entrySet();
            docEntry.forEach(doc -> {
                // Get the key which is docID
                int docID = doc.getKey();
                // Get the value which is the term frequency of the document
                int raw_tf = doc.getValue();

                // If the document exists in docTitleWeights
                if (this.docTitleWeights.containsKey(docID)) {
                    this.docTitleWeights.get(docID).put(
                            termKey,
                            (raw_tf > 0 ? 1 + Math.log10(raw_tf) : 0) * Math.log10(collectionSize / termDocFreq));
                }
                // If the document does not exist in docTitleWeights
                else {
                    TreeMap<String, Double> newTree = new TreeMap<String, Double>();
                    newTree.put(
                            termKey,
                            ((raw_tf > 0 ? 1 + Math.log10(raw_tf) : 0) * Math.log10(collectionSize / termDocFreq)));
                    this.docTitleWeights.put(docID, newTree);
                }
            });
        });

        Set<Map.Entry<String, TreeMap<Integer, Integer>>> termAbstractFreqEntry = this.termAbstractFreq.entrySet();
        termAbstractFreqEntry.forEach(term -> {
            // Get the document term frequency from the docID TreeMap size
            int termDocFreq = term.getValue().size();

            // For iterating doc level
            Set<Map.Entry<Integer, Integer>> docEntry = term.getValue().entrySet();
            docEntry.forEach(doc -> {
                // Get the key which is docID
                int docID = doc.getKey();
                // Get the value which is the term frequency of the document
                int raw_tf = doc.getValue();

                // If the document exists in docTitleWeights
                if (this.docAbstractWeights.containsKey(docID)) {
                    this.docAbstractWeights.get(docID).put(
                            term.getKey(),
                            (raw_tf > 0 ? 1 + Math.log10(raw_tf) : 0) * Math.log10(collectionSize / termDocFreq));
                }
                // If the document does not exist in docTitleWeights
                else {
                    TreeMap<String, Double> newTree = new TreeMap<String, Double>();
                    newTree.put(
                            term.getKey(),
                            ((raw_tf > 0 ? 1 + Math.log10(raw_tf) : 0) * Math.log10(collectionSize / termDocFreq)));
                    this.docAbstractWeights.put(docID, newTree);
                }
            });
        });
    }

    /*
     *
     * Calculate and store Cosine Similarity Scores for each
     * document for both title and abstract then calculate final
     * Cosine Similarity Score for each document.
     *
     */

    void CalcCSS(String query, float boost_a, float boost_b) {

        /*
         * wordPattern specifies pattern for words using a regular expression
         * wordMatcher finds words by spotting word patterns with input
         * 
         * Process the query by extracting words using the wordPattern
         */
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");
        Matcher wordMatcher = wordPattern.matcher(query);

        // Will store a cleaner version of query into String ArrayList
        ArrayList<String> cleanQuery = new ArrayList<String>();

        // Stemmer
        PorterStemmer stemmer = new PorterStemmer();

        while (wordMatcher.find()) {
            // Extract and convert the word to lowercase
            String word = query.substring(wordMatcher.start(), wordMatcher.end()).toLowerCase();
            // Avoid adding stopwords
            if (stopwords.contains(word)) {
                continue;
            } else {
                cleanQuery.add(stemmer.stem(word));
            }
        } // while - wordMatcher

        // TreeMap<Term, Raw TF>
        TreeMap<String, Integer> termQueryFreq = new TreeMap<String, Integer>();

        // Get the terms and their frequencies from cleanQuery
        for (String term : cleanQuery) {
            if (termQueryFreq.containsKey(term)) {
                termQueryFreq.replace(term, termQueryFreq.get(term) + 1);
            } else {
                termQueryFreq.put(term, 1);
            }
        }

        int collectionSize = this.documents.size();

        /*
         * Get query weights and term intersection for Title and Abstract from the
         * document collection
         */
        TreeMap<String, Double> queryTitleWeights = new TreeMap<String, Double>();
        TreeMap<String, Double> queryAbstractWeights = new TreeMap<String, Double>();
        Set<Map.Entry<String, Integer>> termQueryFreqEntry = termQueryFreq.entrySet();
        termQueryFreqEntry.forEach(term -> {
            int raw_tf = term.getValue();
            String termKey = term.getKey();
            int termDocTitleFreq, termDocAbstractFreq;

            // If the term does exist in the index
            if (this.termTitleFreq.get(termKey) != null) {
                termDocTitleFreq = this.termTitleFreq.get(term.getKey()).size();
                queryTitleWeights.put(termKey,
                        (1 + Math.log(raw_tf)) * Math.log(collectionSize / termDocTitleFreq));
            } else {
                queryTitleWeights.put(termKey, (double) 0);
            }

            // If the term does exist in the index
            if (this.termAbstractFreq.get(termKey) != null) {
                termDocAbstractFreq = this.termAbstractFreq.get(term.getKey()).size();
                queryAbstractWeights.put(termKey,
                        (1 + Math.log(raw_tf)) * Math.log(collectionSize / termDocAbstractFreq));
            } else {
                queryAbstractWeights.put(termKey, (double) 0);
            }
        });

        // For iterating through documents for accessing multiple HashTrees
        Set<Map.Entry<Integer, String>> docs = this.documents.entrySet();

        // Need these for looping for intersection.
        Set<Map.Entry<String, Double>> queryTitleWeightsEntry = queryTitleWeights.entrySet();
        Set<Map.Entry<String, Double>> queryAbstractWeightsEntry = queryAbstractWeights.entrySet();

        // Get the intersection weight vectors for Title and Abstract from query weights
        // for Title and Abstract
        docs.forEach(doc -> {
            ArrayList<Double> docTitleWeightVector = new ArrayList<Double>();
            ArrayList<Double> docAbstractWeightVector = new ArrayList<Double>();
            ArrayList<Double> queryTitleWeightVector = new ArrayList<Double>();
            ArrayList<Double> queryAbstractWeightVector = new ArrayList<Double>();

            int docID = doc.getKey();

            // Title
            queryTitleWeightsEntry.forEach(entry -> {
                String term = entry.getKey();
                if (this.docTitleWeights.get(docID) != null && this.docTitleWeights.get(docID).containsKey(term)) {
                    docTitleWeightVector.add(this.docTitleWeights.get(docID).get(term));
                } else {
                    docTitleWeightVector.add((double) 0);
                }
                queryTitleWeightVector.add(entry.getValue());
            });

            // Abstract
            queryAbstractWeightsEntry.forEach(entry -> {
                String term = entry.getKey();
                if (this.docAbstractWeights.get(docID) != null
                        && this.docAbstractWeights.get(docID).containsKey(term)) {
                    docAbstractWeightVector.add(this.docAbstractWeights.get(docID).get(term));
                } else {
                    docAbstractWeightVector.add((double) 0);
                }
                queryAbstractWeightVector.add(entry.getValue());
            });

            // Get Final Cosine Similarity Scores
            double titleCSSNumerator = ProdSumTFXIDF(queryTitleWeightVector, docTitleWeightVector);
            double titleCSSDenominator = ((Math.sqrt(SumSquaredTFXIDF(queryTitleWeightVector)))
                    * (Math.sqrt(SumSquaredTFXIDF(docTitleWeightVector))));
            double abstractCSSNumerator = ProdSumTFXIDF(queryAbstractWeightVector, docAbstractWeightVector);
            double abstractCSSDenominator = ((Math.sqrt(SumSquaredTFXIDF(queryAbstractWeightVector)))
                    * (Math.sqrt(SumSquaredTFXIDF(docAbstractWeightVector))));
            double finalScore = ((boost_a * (titleCSSDenominator == 0 ? 0 : (titleCSSNumerator / titleCSSDenominator)))
                    + (boost_b * (abstractCSSDenominator == 0 ? 0
                            : (abstractCSSNumerator / abstractCSSDenominator))));

            if (finalScore > 0) {
                finalCosineSimilarityScores.put(finalScore, docID);
            }
        });
    }

    /*
     * Prints out top k results
     */
    void DisplayTopKDocs(int k, String queryID) {
        System.out.println("\nYour top " + k + " results for query " + queryID + ":\n");

        int rank = 1;
        int count = 0;
        for (Map.Entry<Double, Integer> result : this.finalCosineSimilarityScores.entrySet()) {
            if (count == k) {
                break;
            }
            int DocID = result.getValue();
            System.out.println("Title: " + GetTitle(DocID));
            System.out.format("%-4s \t %5s \t %23s\n", "Rank", "DocID", "Cosine Similarity Score");
            System.out.format("%-4d \t %-5s \t %-23f\n", rank, DocID, result.getKey());
            System.out.println();
            rank++;
            count++;
        }
    }

    void BuildQueryList(String queryPath) {
        // br for efficiently reading characters from an input stream
        BufferedReader br = null;

        /*
         * wordPattern specifies pattern for words using a regular expression
         * wordMatcher finds words by spotting word patterns with input
         */
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+");
        Matcher wordMatcher;

        try {
            br = new BufferedReader(new FileReader(queryPath));

            String line = "";
            String word = "";

            line = br.readLine();
            while (line != null) {
                // For storing into queryList
                String query = "";
                String queryID = "";
                if (line.contains(".I")) {
                    queryID = line.replaceAll("[^0-9]", "");
                    line = br.readLine();
                }
                if (line.contains(".W")) {
                    line = br.readLine();
                    while (line != null && !line.contains(".I")) {
                        wordMatcher = wordPattern.matcher(line);
                        // Process one word at a time
                        while (wordMatcher.find()) {
                            // Extract and convert the word to lowercase
                            word = line.substring(wordMatcher.start(), wordMatcher.end());
                            query = query + " " + word;
                        } // while - wordMatcher
                        line = br.readLine();
                    }
                }
                queryList.put(queryID, query.trim());
            }

            br.close();

        } catch (IOException ex) {
            System.err.println("File " + queryPath + " not found. Program terminated.\n");
            System.exit(1);
        }
    }

    String GetQuery(String ID) {
        return this.queryList.get(ID);
    }

    String GetTitle(int ID) {
        return this.documents.get(ID);
    }

    void clearResults() {
        this.finalCosineSimilarityScores.clear();
    }

    /*
     *
     * HELPER METHODS
     *
     */

    double ProdSumTFXIDF(ArrayList<Double> vector_a, ArrayList<Double> vector_b) {
        double sum = 0;
        Iterator<Double> iter_a = vector_a.iterator();
        Iterator<Double> iter_b = vector_b.iterator();

        while (iter_a.hasNext() && iter_b.hasNext()) {
            sum += iter_a.next() * iter_b.next();
        }

        return sum;
    }

    double SumSquaredTFXIDF(ArrayList<Double> vector) {
        double sum = 0;

        for (double value : vector) {
            sum += value * value;
        }

        return sum;
    }

    double NormalizeVector(ArrayList<Double> vector) {
        return Math.sqrt(SumSquaredTFXIDF(vector));
    }

    static double DeltaNanoToSec(long a, long b) {
        return ((double) (a - b) / 1_000_000_000);
    }

    static boolean ValidateYN(String yn) {
        return yn.compareTo("n") == 0 || yn.compareTo("y") == 0;
    }

    static boolean ValidateQueryID(String queryID) {
        Pattern three_digit = Pattern.compile("\\d{3}");
        Matcher matcher = three_digit.matcher(queryID);
        return matcher.find() && queryID.length() == 3;
    }

    /*
     *
     * MAIN METHOD
     *
     */
    public static void main(String[] args) {
        int error = 0;

        /*
         * Did the user provide correct number of command line arguments?
         * If not, print message and exit
         */

        if (args.length != 2) {
            System.err.println("\nNumber of command line arguments must be 2");
            System.err.println("You have given " + args.length + " command line arguments");
            System.err.println("Incorrect usage. Program terminated");
            System.err.println(
                    "Correct usage: java VectorSpaceModelIR <cran.all.1400-filepath> <cran.qry-filepath>");
            error = 1;
        }

        File corpus = new File(args[0]);
        File query = new File(args[1]);

        if (!(corpus.exists() && corpus.isFile() && corpus.getName().compareTo("cran.all.1400") == 0)) {
            System.err.println(
                    "Error: <cran.all.1400-filepath> is not a filepath to the cran.all.1400 corpus file or the file does not exists.");
            error = 1;
        }
        if (!(query.exists() && query.isFile() && query.getName().compareTo("cran.qry") == 0)) {
            System.err.println(
                    "Error: <cran.qry-filepath> is not a filepath to the cran.qry file or the file does not exists.");
            error = 1;
        }
        if (error == 1) {
            System.exit(1);
        }

        System.out.println("\nPlease wait, now processing the cran.all.1400 corpus and cran.qry file...\n");
        long startProcessTime = System.nanoTime();
        VectorSpaceModelIR data = new VectorSpaceModelIR();

        System.out.println("Now building collection title and abstract indexes...");
        data.BuildData(corpus.getPath());
        long checkpoint_BuildData = System.nanoTime();
        System.out.println("Collection title and abstract indexes built in "
                + DeltaNanoToSec(checkpoint_BuildData, startProcessTime) + " seconds\n");

        System.out.println("Now calculating TF-IDF of terms from the title and abstract indexes...");
        data.CalcTFXIDF();
        long checkpoint_CalcTFXIDF = System.nanoTime();
        System.out.println("Collection title and abstract term TF-IDF calculations finished in "
                + DeltaNanoToSec(checkpoint_CalcTFXIDF, checkpoint_BuildData) + " seconds\n");

        System.out.println("Now building Query ID index from cran.qry...");
        data.BuildQueryList(query.getPath());
        long checkpoint_BuildQueryList = System.nanoTime();
        System.out.println("Query ID index built in "
                + DeltaNanoToSec(checkpoint_BuildQueryList, checkpoint_CalcTFXIDF) + " seconds\n");

        System.out.println("Now you can start searching the corpus!\n");

        Scanner input = new Scanner(System.in);
        int count = 1;
        String response, queryID;
        float boostTitle, boostAbstract;
        int numResultsToDisplay = 0;
        boostTitle = boostAbstract = 0;

        while (true) {

            // Asking to search the corpus or search corpus again
            do {
                if (count == 1) {
                    System.out.println("Would you like to search the corpus? (Y/N)");
                } else {
                    data.clearResults();
                    System.out.println("Would you like to continue searching the corpus? (Y/N)");
                }

                System.out.print("Input Y/N: ");
                response = input.nextLine().toLowerCase().trim();
                System.out.println();

                switch (response) {
                    case "n":
                        System.out.println("Program closed.\n");
                        input.close();
                        System.exit(0);
                        break;
                    case "y":
                        break;
                    default:
                        System.out.println("Invalid input. Please try again.\n");
                        break;
                }
            } while (!ValidateYN(response));

            // Asking for the query ID
            do {
                System.out.println("Input the 3-digit query ID from cran.qry.");
                System.out.print("Input 3-digit query ID (###): ");
                queryID = input.nextLine().trim();
                System.out.println();

                if (!ValidateQueryID(queryID)) {
                    System.out.println("Invalid input, try again.\n");
                } else if (data.GetQuery(queryID) == null) {
                    System.out.println("Query ID does not exist, try again.\n");
                    queryID = "";
                }
            } while (!ValidateQueryID(queryID));

            // Asking for boost values.
            do {
                System.out.println("Input two floating point values for title and abstract, respectively.");
                System.out.println("NOTE: The sum of both values must equal 1.");

                try {
                    System.out.print("Input title boost: ");
                    boostTitle = Float.parseFloat(input.nextLine());

                    if (boostTitle > 1 || boostTitle < 0) {
                        System.out.println("\nInvalid input. Title boost value must be from 0 to 1.\n");
                        continue;
                    }

                    System.out.print("Input abstract boost: ");
                    boostAbstract = Float.parseFloat(input.nextLine());

                    if (boostAbstract > 1 || boostAbstract > 0) {
                        System.out.println("\nInvalid input.  Abstract boost value must be from 0 to 1, try again.\n");
                        continue;
                    }

                    System.out.println();
                } catch (NumberFormatException e) {
                    System.out.println("\nInvalid input try again.\n");
                    continue;
                }

                if (boostTitle + boostAbstract != 1) {
                    System.out.println("\nBoost values don't sum to 1, try again.\n");
                    continue;
                }

            } while (boostTitle + boostAbstract != 1);

            System.out.println("\nNow calculating Cosine Similarity Scoring...");
            long startCSS = System.nanoTime();
            data.CalcCSS(data.GetQuery(queryID), boostTitle, boostAbstract);
            long checkpointCalcCSS = System.nanoTime();
            System.out.println("Cosine Similarity Scoring completed in "
                    + DeltaNanoToSec(checkpointCalcCSS, startCSS) + " seconds\n");

            // Asking for number of documents to display on command prompt
            do {
                System.out.println("Input the number of top results you wish to see.");
                System.out.println(
                        "NOTE: 1400 is the max, but you may not be able to see the top results in the console.");

                try {
                    System.out.print("Input # of documents to display: ");
                    numResultsToDisplay = Integer.parseInt(input.nextLine());

                } catch (NumberFormatException e) {
                    System.out.println("\nInvalid input, try again.\n");
                    continue;
                }

                if (numResultsToDisplay <= 0) {
                    System.out.println("\nValue cannot be <= 0, try again.\n");
                    continue;
                } else if (numResultsToDisplay > 1400) {
                    System.out.println("\nValue cannot be > 1400, try again.\n");
                    continue;
                }

            } while (numResultsToDisplay <= 0 || numResultsToDisplay > 1400);

            data.DisplayTopKDocs(numResultsToDisplay, queryID);
            count++;
        }

    } // main()
} // class
