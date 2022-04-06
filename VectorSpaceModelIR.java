/*
    This program implements the Vector Space Information 
    Retrieval model, demonstrated on the Cranfield corpus.

    Authors: Abelson Abueg
    Date Created: 4 Apr 2022
    Last Updated: 4 Apr 2022
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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;

// Lucene
import org.apache.lucene.analysis.PorterStemmer;

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

    // TreeMap<DocID, TF-IDF Weight>
    private TreeMap<Integer, Float> docTitleWeights;
    private TreeMap<Integer, Float> docAbstractWeights;

    // TreeMap<DocID, Cosine Similarity Scores>
    private TreeMap<Integer, Float> cosineSimilarityScoresTitle;
    private TreeMap<Integer, Float> cosineSimilarityScoresAbstract;

    // TreeMap<Final Cosine Similarity Scores, DocID>
    private TreeMap<Float, Integer> finalCosineSimilarityScores;

    // Default Constructor; it's all you really need.
    public VectorSpaceModelIR() {
        this.documents = new TreeMap<Integer, String>();
        this.termTitleFreq = new TreeMap<String, TreeMap<Integer, Integer>>();
        this.termAbstractFreq = new TreeMap<String, TreeMap<Integer, Integer>>();
        this.docTitleWeights = new TreeMap<Integer, Float>();
        this.docAbstractWeights = new TreeMap<Integer, Float>();
        this.cosineSimilarityScoresTitle = new TreeMap<Integer, Float>();
        this.cosineSimilarityScoresAbstract = new TreeMap<Integer, Float>();
        this.finalCosineSimilarityScores = new TreeMap<Float, Integer>();
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

            PorterStemmer stemmer = new PorterStemmer();

            line = br.readLine();
            while (line != null) {

                // process the line by extracting words using the wordPattern
                wordMatcher = wordPattern.matcher(line);

                // Will store a cleaner version of line into String ArrayList
                ArrayList<String> cleanLine = new ArrayList<String>();

                if (line.contains(".I")) {
                    docID = Integer.parseInt(line.replaceAll("[^0-9]", ""));

                } 
                else if (line.contains(".T")) {
                    String title = "";
                    while((line = br.readLine()).compareTo(".A") != 0) {
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
                                        //Update the term count from the document.
                                        this.termTitleFreq.get(stemmedTerm).replace(docID, this.termTitleFreq.get(stemmedTerm).get(docID) + 1);
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
                    while((line = br.readLine()).compareTo(".B") != 0) {
                        continue;
                    }

                } else if (line.contains(".B")) {
                    while((line = br.readLine()).compareTo(".W") != 0) {
                        continue;
                    }

                } else if (line.contains(".W")) {
                    while((line = br.readLine()).compareTo(".I") != 0) {
                        while((line = br.readLine()).compareTo(".A") != 0) {
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
                                    // Do stuff
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

    }

    /*
     *
     * Calculate and store Cosine Similarity Scores for each
     * document for both title and abstract
     *
     */

    /*
     *
     * Calculate final Cosine Similarity Scores for each
     * document (Can do it in one method?)
     *
     */

    void calcCSS() {

    }

    /*
     *
     * HELPER METHODS
     *
     */

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

        /*
         * Extract input file name from command line arguments
         * This is the name of the file from the Gutenberg corpus
         */
        String inputFileDirName = args[0];
        System.out.println("\nInput files directory path name is: " + inputFileDirName);

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
        String line, word;

        // Initialize new Positional Index
        PositionalIndex positionalIndex = new PositionalIndex();

        System.out.println("\nBuilding Positional Index...");

        // Process one file at a time
        for (int index = 0; index < fileCount; index++) {
            System.out.println("Processing: " + inputFileNames.get(index));

            // Keep track of document position.
            int docPosition = 0;

            /*
             * Keep track of Doc ID for assignment for building the positional
             * index data. They start at 1.
             */
            int docID = 1 + index;

            /*
             * Open the input file, read one line at a time, extract words
             * in the line, extract characters in a word, write words and
             * character counts to disk files
             */
            try {
                /*
                 * Get a BufferedReader object, which encapsulates
                 * access to a (disk) file
                 */
                br = new BufferedReader(new FileReader(inputFileNames.get(index)));

                /*
                 * As long as we have more lines to process, read a line
                 * the following line is doing two things: makes an assignment
                 * and serves as a boolean expression for while test
                 */
                while ((line = br.readLine()) != null) {
                    // process the line by extracting words using the wordPattern
                    wordMatcher = wordPattern.matcher(line);

                    // Will store a cleaner version of line into String ArrayList
                    ArrayList<String> cleanLine = new ArrayList<String>();

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
                            positionalIndex.updatePositionalIndex(term, docID, ++docPosition);
                        }
                    }
                } // while - Line
            } // try
            catch (IOException ex) {
                System.err.println("File " + inputFileNames.get(index) + " not found. Program terminated.\n");
                System.exit(1);
            }
        } // for -- Process one file at a time

        System.out.println("\nPositional Index Built.");
        System.out.println("\nNow performing proximity search...");
        positionalIndex.proximitySearch(args[2].toLowerCase(), args[3].toLowerCase(), Integer.parseInt(args[4]));

        // End Process Timer
        long endTime = System.nanoTime();
        System.out.println("\nProcess Completed in " +
                (double) (endTime - startTime) / 1_000_000_000 + " seconds.\n");
    } // main()
} // class
