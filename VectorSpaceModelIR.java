/*
    This program implements the Vector Space Information 
    Retrieval model, demonstrated on the Cranfield corpus.

    Authors: Abelson Abueg
    Date Created: 4 Apr 2022
    Last Updated: 13 Apr 2022
*/

// Java
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
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
        this.finalCosineSimilarityScores = new TreeMap<Double, Integer>();
    }

    /*
     * 
     * Read the document and keep track of docID-Titles and
     * term and document frequencies.
     * 
     */
    void BuildData(String inputPath) {

        System.out.println("\nInput file path name is: " + inputPath);

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

                // process the line by extracting words using the wordPattern
                wordMatcher = wordPattern.matcher(line);

                // Will store a cleaner version of line into String ArrayList
                ArrayList<String> cleanLine = new ArrayList<String>();

                if (line.contains(".I")) {
                    docID = Integer.parseInt(line.replaceAll("[^0-9]", ""));

                } else if (line.contains(".T")) {
                    String title = "";
                    while ((line = br.readLine()).compareTo(".A") != 0) {
                        title = title + line + " ";
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
                                // If the term exists in the title term frequency
                                if (this.termTitleFreq.containsKey(stemmedTerm)) {
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
                    title.trim();
                    documents.put(docID, title);

                } else if (line.contains(".A")) {
                    while ((line = br.readLine()).compareTo(".B") != 0) {
                        continue;
                    }

                } else if (line.contains(".B")) {
                    while ((line = br.readLine()).compareTo(".W") != 0) {
                        continue;
                    }

                } else if (line.contains(".W")) {
                    while ((line = br.readLine()).compareTo(".I") != 0) {
                        while ((line = br.readLine()).compareTo(".A") != 0) {
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
                                    // If the term exists in the title term frequency
                                    if (this.termAbstractFreq.containsKey(stemmedTerm)) {
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
     * document for both title and abstract
     * 
     */
    void calcTFXIDF() {
        int collectionSize = this.documents.size();

        // For iterating term level of termTitleFreqEntry
        Set<Map.Entry<String, TreeMap<Integer, Integer>>> termTitleFreqEntry = this.termTitleFreq.entrySet();
        termTitleFreqEntry.forEach(term -> {
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
                if (docTitleWeights.containsKey(docID)) {
                    docTitleWeights.get(docID).put(
                            term.getKey(),
                            (raw_tf > 0 ? 1 + Math.log(raw_tf) : 0) * Math.log(collectionSize / termDocFreq));
                }
                // If the document does not exist in docTitleWeights
                else {
                    TreeMap<String, Double> newTree = new TreeMap<String, Double>();
                    newTree.put(
                            term.getKey(),
                            ((raw_tf > 0 ? 1 + Math.log(raw_tf) : 0) * Math.log(collectionSize / termDocFreq)));
                    docTitleWeights.put(docID, newTree);
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
                if (docAbstractWeights.containsKey(docID)) {
                    docAbstractWeights.get(docID).put(
                            term.getKey(),
                            (raw_tf > 0 ? 1 + Math.log(raw_tf) : 0) * Math.log(collectionSize / termDocFreq));
                }
                // If the document does not exist in docTitleWeights
                else {
                    TreeMap<String, Double> newTree = new TreeMap<String, Double>();
                    newTree.put(
                            term.getKey(),
                            ((raw_tf > 0 ? 1 + Math.log(raw_tf) : 0) * Math.log(collectionSize / termDocFreq)));
                    docAbstractWeights.put(docID, newTree);
                }
            });
        });
    }

    /*
     *
     * Calculate and store Cosine Similarity Scores for each
     * document for both title and abstract then calculate final
     * Cosine Similarity Scores for each document (Can do it in one method?)
     *
     */

    void calcCSS(String query, float boost_a, float boost_b) {

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
            String word = query.substring(wordMatcher.start(), wordMatcher.end());
            cleanQuery.add(stemmer.stem(word.toLowerCase()));
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

        // Get query weights for Title and Abstract
        ArrayList<Double> queryTitleWeightVector = new ArrayList<Double>();
        ArrayList<Double> queryAbstractWeightVector = new ArrayList<Double>();
        Set<Map.Entry<String, Integer>> termQueryFreqEntry = termQueryFreq.entrySet();
        termQueryFreqEntry.forEach(term -> {
            int raw_tf = term.getValue();
            int termDocTitleFreq = this.termTitleFreq.get(term.getKey()).size();
            int termDocAbstractFreq = this.termAbstractFreq.get(term.getKey()).size();

            queryTitleWeightVector
                    .add((raw_tf > 0 ? 1 + Math.log(raw_tf) : 0) * Math.log(collectionSize / termDocTitleFreq));
            queryAbstractWeightVector
                    .add((raw_tf > 0 ? 1 + Math.log(raw_tf) : 0) * Math.log(collectionSize / termDocAbstractFreq));
        });

        // For iterating through documents for accessing multiple HashTrees
        Set<Map.Entry<Integer, String>> docs = this.documents.entrySet();

        // For storing intersection weight vectors for Title and Abstract from
        // termQueryFreq
        TreeMap<Integer, ArrayList<Double>> docsTitleWeightVector = new TreeMap<Integer, ArrayList<Double>>();
        TreeMap<Integer, ArrayList<Double>> docsAbstractWeightVector = new TreeMap<Integer, ArrayList<Double>>();

        // Get the intersection weight vectors for Title and Abstract from termQueryFreq
        docs.forEach(doc -> {
            ArrayList<Double> docTitleWeightVector = new ArrayList<Double>();
            ArrayList<Double> docAbstractWeightVector = new ArrayList<Double>();
            int docID = doc.getKey();
            termQueryFreqEntry.forEach(term -> {
                String raw_term = term.getKey();
                if (docTitleWeights.get(docID).containsKey(raw_term)) {
                    docTitleWeightVector.add(docTitleWeights.get(docID).get(raw_term));
                }
                if (docAbstractWeights.get(docID).containsKey(raw_term)) {
                    docAbstractWeightVector.add(docAbstractWeights.get(docID).get(raw_term));
                }
            });
            docsTitleWeightVector.put(docID, docTitleWeightVector);
            docsAbstractWeightVector.put(docID, docAbstractWeightVector);
        });

        docs.forEach(doc -> {
            int docID = doc.getKey();
            finalCosineSimilarityScores.put(
                    ((boost_a *
                            ((ProdSumTFXIDF(queryTitleWeightVector, docsTitleWeightVector.get(docID)))
                                    / ((Math.sqrt(SumSquaredTFXIDF(queryTitleWeightVector)))
                                            * (Math.sqrt(SumSquaredTFXIDF(docsTitleWeightVector.get(docID)))))))
                            +
                            (boost_b *
                                    (ProdSumTFXIDF(queryAbstractWeightVector, docsAbstractWeightVector.get(docID)))
                                    / ((Math.sqrt(SumSquaredTFXIDF(queryAbstractWeightVector)))
                                            * (Math.sqrt(SumSquaredTFXIDF(docsAbstractWeightVector.get(docID))))))),
                    docID);
        });

    }

    /*
     * Prints out top k results
     */
    void displayTopKDocs(int k) {
        TreeMap<Double, Integer> topKResults = this.finalCosineSimilarityScores.entrySet().stream()
                .limit(k)
                .collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()),
                        Map::putAll);

        System.out.println("Your top " + k + " results:\n");
        System.out.format("%7s %30s", "DocID", "Cosine Similarity Score");

        int rank = 1;
        Set<Map.Entry<Double, Integer>> topKResultsEntry = topKResults.entrySet();

        topKResultsEntry.forEach(result -> {
            System.out.format("%7d %30d", result.getValue(), result.getKey());
            System.out.println();
        });
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

    /*
     *
     * MAIN METHOD
     *
     */
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        int error = 0;

        /*
         * Did the user provide correct number of command line arguments?
         * If not, print message and exit
         */
        if (args.length != 5) {
            System.err.println("\nNumber of command line arguments must be 5");
            System.err.println("You have given " + args.length + " command line arguments");
            System.err.println("Incorrect usage. Program terminated");
            System.err.println(
                    "Correct usage: java PositionalIndex <path-to-input-files> <path-to-output-result-files> <first-word> <second-word> <int-distance-between-words>");
            error = 1;
        }
        if (!(args[2] != null && args[2].matches("^[a-zA-Z]*$"))) {
            System.err.println("Error: <first-word> argument must only have alphabet letters in the input.");
            error = 1;
        }
        if (!(args[3] != null && args[3].matches("^[a-zA-Z]*$"))) {
            System.err.println("Error: <second-word> argument must only have alphabet letters in the input.");
            error = 1;
        }
        if (Integer.parseInt(args[4]) < 1) {
            System.err.println("Error: <int-distance-between-words> argument must be greater than 0.");
            error = 1;
        }
        if (error == 1) {
            System.exit(1);
        }

        // End Process Timer
        long endTime = System.nanoTime();
        System.out.println("\nProcess Completed in " +
                (double) (endTime - startTime) / 1_000_000_000 + " seconds.\n");
    } // main()
} // class
