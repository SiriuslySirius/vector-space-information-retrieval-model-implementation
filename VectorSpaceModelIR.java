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

public class VectorSpaceModelIR {
    /*
     *
     * CONSTRUCTOR AND CLASS METHODS
     * 
     */

    // TreeMap<DocID, Title> 
    private TreeMap<Integer, String> documents;

    // TreeMap<Term, TreeMap<DocID, Title TF-IDF>>
    private TreeMap<String, TreeMap<Float, Integer>> termTitleWeights;
    
    //TreeMap<Term, TreeMap<DocID, Abstract TF-IDF>>
    private TreeMap<String, TreeMap<Float, Integer>> termAbstractWeights;
    
    //TreeMap<DocID, TF-IDF Weight>
    private TreeMap<Integer, Float> docTitleWeights;
    private TreeMap<Integer, Float> docAbstractWeights;

    //TreeMap<DocID, Cosine Similarity Scores>
    private TreeMap<Integer, Float> cosineSimilarityScoresTitle;
    private TreeMap<Integer, Float> cosineSimilarityScoresAbstract;

    //TreeMap<Final Cosine Similarity Scores, DocID>
    private TreeMap<Float, Integer> finalCosineSimilarityScores;

    // Default Constructor; it's all you really need.
    public VectorSpaceModelIR() {
        this.documents = new TreeMap<Integer, String>();
        this.termTitleWeights = new TreeMap<String, TreeMap<Float, Integer>>();
        this.termAbstractWeights = new TreeMap<String, TreeMap<Float, Integer>>();
        this.docTitleWeights = new TreeMap<Integer, Float>();
        this.docAbstractWeights = new TreeMap<Integer, Float>();
        this.cosineSimilarityScoresTitle = new TreeMap<Integer, Float>();
        this.cosineSimilarityScoresAbstract = new TreeMap<Integer, Float>();
        this.finalCosineSimilarityScores = new TreeMap<Float, Integer>();
    }

    /*
     *
     * GLOBAL VARIABLES
     *
     */
    // An array to hold Gutenberg corpus file names
    static ArrayList<String> inputFileNames = new ArrayList<String>();

    // To keep count of the amount of files in the corpus provided
    static int fileCount = 0;

    // An array of default output file names for ease of access.
    static String outputFiles[] = new String[] {
            "proximity_query_result.csv",
            "proximity_query_detailed_result.csv",
    };

    static String outputPath = "";

    /*
     *
     * HELPER METHODS
     *
     */

    /*
     * loads all files names in the directory subtree into an array
     * violates good programming practice by accessing a global variable
     * (inputFileNames)
     */
    public static void listFilesInPath(final File path) {
        for (final File fileEntry : path.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesInPath(fileEntry);
            } else if (fileEntry.getName().endsWith((".txt"))) {
                fileCount++;
                inputFileNames.add(fileEntry.getPath());
            }
        }
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

        /*
         * If the files exists, we need to empty them.
         * We will be appending new data into the documents
         */
        for (String file : outputFiles) {
            String path = args[1] + "\\" + args[2].toLowerCase() + "_" + args[3].toLowerCase() + "_" + args[4] + "_"
                    + file;
            try {
                new PrintWriter(path, "UTF-8").close();
            } catch (FileNotFoundException ex) {
                System.err.println(ex);
                System.err.println("\nProgram terminated\n");
                System.exit(1);
            } catch (UnsupportedEncodingException ex) {
                System.err.println(ex);
                System.err.println("\nProgram terminated\n");
                System.exit(1);
            }
        }

        /*
         * Extract input file name from command line arguments
         * This is the name of the file from the Gutenberg corpus
         */
        String inputFileDirName = args[0];
        System.out.println("\nInput files directory path name is: " + inputFileDirName);

        /*
         * Extract output path from command line arguments
         * This is the name of the file from the Gutenberg corpus
         */
        outputPath = args[1];
        System.out.println("Output directory path name is: " + outputPath);

        // Collects file names and write them to
        listFilesInPath(new File(inputFileDirName));
        System.out.println("Number of Gutenberg corpus files: " + fileCount);

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
