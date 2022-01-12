import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * @authors: David Ouma and Michael Moyo, PS5 COSC 10 '21S
 * Sudi
 *Date: 24/05/2021
 */

public class Sudi {

    ArrayList<ArrayList<String[]>> wordPieces;
    ArrayList<ArrayList<String[]>> tagPieces;
    private Map<String, Map<String, Double>> tMatrix;
    private Map<String, Map<String, Double>> eMatrix;
    private Map<String, Map<String, Integer>> MapofWords;
    private Map<String, Map<String, Integer>> MapofTags;


    public Sudi() {

        tMatrix = new HashMap<String, Map<String, Double>>();
        eMatrix = new HashMap<String, Map<String, Double>>();

        MapofWords = new HashMap<String, Map<String, Integer>>();
        MapofTags = new HashMap<String, Map<String, Integer>>();

    }

    /**
     * This method tests the Viterbi algorithm on a desired section of Brown
     * @param pieces number of pieces
     */
    public double testAllLines(int pieces) throws Exception {

        double accuracyScore = 0.0;
        int counter = 0;
        wordPieces = new ArrayList<ArrayList<String[]>>(pieces);
        tagPieces = new ArrayList<ArrayList<String[]>>(pieces);

        for (int i = 0; i < pieces; i ++) {//Place Holder Lists
            ArrayList<String[]> wordPHolder = new ArrayList<String[]>();
            wordPieces.add(wordPHolder);
            ArrayList<String[]> tagPHolder = new ArrayList<String[]>();
            tagPieces.add(tagPHolder);
        }

        //Reading words and tags from files
        BufferedReader words = new BufferedReader(new FileReader("inputs/texts/brown-train-sentences.txt"));
        BufferedReader tags = new BufferedReader(new FileReader("inputs/texts/brown-train-tags.txt"));

        String tagLine = new String();//Takes each line in tags' file
        String wordLine = new String();//Takes each line in words' file

        do{//Read through the words and tags
            wordPieces.get(counter % pieces).add(wordLine.split(" "));
            tagPieces.get(counter % pieces).add(tagLine.split(" "));
            counter ++;

        }while ((wordLine = words.readLine()) != null && (tagLine = tags.readLine()) != null);

        //Close files
        tags.close();
        words.close();

        //Loops through the pieces
        for (int i = 0; i < pieces; i++) { //loop through each piece
            ignore(i); //ignore this piece
            eMatrix = generateEMatrix();
            tMatrix = generateTMatrix();
            accuracyScore += performanceTest(i);
        }
        return (accuracyScore / pieces); //Accuracy score
    }

    /**
     * Tests Viterbi algorithm method
     * @param pieces desired pieces
     * @param lines desired lines
     */
    public double testLinesPieces(int pieces, int lines) throws Exception {

        int counter = 0;
        double accuracyScore = 0.0;

        wordPieces = new ArrayList<ArrayList<String[]>>(pieces);
        tagPieces = new ArrayList<ArrayList<String[]>>(pieces);

        for (int i = 0; i < pieces; i ++) {//Place Holder Lists
            ArrayList<String[]> wordPHolder = new ArrayList<String[]>();
            wordPieces.add(wordPHolder);
            ArrayList<String[]> tagPHolder = new ArrayList<String[]>();
            tagPieces.add(tagPHolder);
        }

        //Reading words and tags from files
        BufferedReader words = new BufferedReader(new FileReader("inputs/texts/brown-train-sentences.txt"));
        BufferedReader tags = new BufferedReader(new FileReader("inputs/texts/brown-train-tags.txt"));

        String tagLine = new String();//Takes each line in tags' file
        String wordLine = new String();//Takes each line in words' file

        do{//Read through the words and tags

            wordPieces.get(counter % pieces).add(wordLine.split(" "));
            tagPieces.get(counter % pieces).add(tagLine.split(" "));
            counter ++;

        }while (counter < lines && (wordLine = words.readLine()) != null && (tagLine = tags.readLine()) != null);

        //Close files
        tags.close();
        words.close();

        //Loops through the pieces
        for (int i = 0; i < pieces; i++) { //loop through each piece
            ignore(i); //ignore this piece
            tMatrix = generateTMatrix();
            eMatrix = generateEMatrix();
            accuracyScore += performanceTest(i);
        }
        return (accuracyScore / pieces); //Accuracy Score
    }

    /**
     * Separates sections for testing and tagging
     * @param leave		this is the index that needs to be left out
     */
    private void ignore(int leave) {
        for (int i = 0; i < wordPieces.size(); i ++) {
            if (i != leave) {
                for (int j = 0; j < wordPieces.get(i).size(); j ++) {
                    wordstofMapofTags(wordPieces.get(i).get(j), tagPieces.get(i).get(j));
                    nextTagLink(tagPieces.get(i).get(j));
                }
            }
        }
    }


    /**
     *  Checks accuracy by comparing the predicted tags with the actual tags for a particular string
     * @param startTest
     */
    private double performanceTest(int startTest) {

        double accuracy = 0;

        for (int i = 0; i < tagPieces.get(startTest).size(); i ++) {

            String[] predictedTags = ViterbiAlgo(wordPieces.get(startTest).get(i));
            String[] actualTags = tagPieces.get(startTest).get(i);

            double percentage = 0;

            for (int j = 0; j < predictedTags.length; j ++) {
                if (actualTags[j].equalsIgnoreCase(predictedTags[j])) { percentage ++; }
            }
            accuracy += (percentage/actualTags.length);
        }
        return accuracy/tagPieces.get(startTest).size();
    }

    /**
     * Link tags to their next tags
     * @param tags
     */
    private void nextTagLink(String[] tags) {
        prevTagLink("START", tags[0]);
        for (int i = 1; i < tags.length; i ++) { prevTagLink(tags[i-1], tags[i]); }
    }

    /**
     * This method puts the current tag into the map of the previous tag and increases its frequency
     * @param previousTag
     * @param currentTag
     */
    private void prevTagLink(String previousTag, String currentTag) {

        if (MapofTags.containsKey(previousTag)) {
            if (MapofTags.get(previousTag).containsKey(currentTag)) { MapofTags.get(previousTag).put(currentTag, MapofTags.get(previousTag).get(currentTag) + 1); }
            else { MapofTags.get(previousTag).put(currentTag, 1); }
        }
        else {
            Map<String, Integer> nextTag = new HashMap<String, Integer>();
            nextTag.put(currentTag, 1);
            MapofTags.put(previousTag, nextTag);
        }
    }

    /**
     * map the words according to their tags
     * @param words
     * @param tags
     */
    private void wordstofMapofTags(String[] words, String[] tags) {
        for (int i = 0; i < words.length; i ++) {

            if (MapofWords.containsKey(tags[i])) {
                if (MapofWords.get(tags[i]).containsKey(words[i])) { MapofWords.get(tags[i]).put(words[i], MapofWords.get(tags[i]).get(words[i]) + 1); }
                else { MapofWords.get(tags[i]).put(words[i], 1); }
            }
            else {
                Map<String, Integer> wordForTag = new HashMap<String, Integer>();
                wordForTag.put(words[i], 1);
                MapofWords.put(tags[i], wordForTag);
            }
        }
    }

    /**
     * Calculates the log probability
     */
    private Map<String, Map<String, Double>> generateEMatrix() {

        int denom;
        Map<String, Map<String, Double>> eMatrixTemp = new HashMap<String, Map<String, Double>>();

        for(String state: MapofWords.keySet()) {
            denom = 0;
            for(Integer freqs : MapofWords.get(state).values()) { denom += freqs; }

            Map<String, Double> element = new HashMap<String, Double>();

            for(String word : MapofWords.get(state).keySet()) {
                double eProb = Math.log((double) MapofWords.get(state).get(word) / denom);
                element.put(word, eProb);
            }
            eMatrixTemp.put(state, element);
        }
        return eMatrixTemp;
    }

    /**
     * Calculates the log probability
     */
    private Map<String, Map<String, Double>> generateTMatrix() {

        int denom;
        Map<String, Map<String, Double>> tMatrixTemp = new HashMap<String, Map<String, Double>>();

        for(String state : MapofTags.keySet()) {
            denom = 0;
            for(Integer frequencies : MapofTags.get(state).values()) { denom+= frequencies; }

            Map<String, Double> element = new HashMap<String, Double>();

            for(String nextState : MapofTags.get(state).keySet()) {
                double tProb = Math.log((double) MapofTags.get(state).get(nextState) / denom);
                element.put(nextState, tProb);
            }
            tMatrixTemp.put(state, element);
        }
        return tMatrixTemp;
    }


    private double getEmission(String nextState, String word) {
        if(eMatrix.get(nextState).containsKey(word)) { return eMatrix.get(nextState).get(word); }
        else { return -1000.0; }
    }

    private double getTransition(String state, String nextState) {
        if(tMatrix.get(state).containsKey(nextState)) { return tMatrix.get(state).get(nextState); }
        else { return -1000.0; }
    }

    /**
     * Viterbi Algorithm method
     * @param observations  string that contains the text for testing
     * @return tags according to training data
     */
    private String[] ViterbiAlgo(String[] observations) {

        ArrayList<Map<String, String>> backTrace = new ArrayList<Map<String, String>>();

        Map<String, Double> states = new HashMap<String, Double>();
        states.put("START", (double) 0);

        for(String word : observations) {

            Map<String, Double> scores = new HashMap<String, Double>();
            Map<String, String> backPath = new HashMap<String, String>();

            for(String state: states.keySet()) {

                if(MapofTags.containsKey(state)) {

                    for(String nextState: MapofTags.get(state).keySet()) {

                        double score = states.get(state) + getTransition(state, nextState) + getEmission(nextState, word);

                        if(!scores.containsKey(nextState) || score > scores.get(nextState)) {
                            scores.put(nextState, score);
                            backPath.put(nextState, state);
                        }
                    }
                }
            }
            backTrace.add(backPath);
            states = scores;
        }
        String[] predictedTags = new String[observations.length];
        String maxKey = null;
        Double maxValue = -1000.0;
        for(String state : states.keySet()) {
            if(states.get(state) > maxValue) {
                maxValue = states.get(state);
                maxKey = state;
            }
        }
        predictedTags[predictedTags.length - 1] = maxKey;
        for(int i = predictedTags.length - 2; i > -1; i --) { predictedTags[i] = backTrace.get(i + 1).get(predictedTags[i+1]); }
        return predictedTags;
    }

    private void quickTrain() throws Exception {

        //File Readers
        BufferedReader words = new BufferedReader(new FileReader("inputs/texts/brown-train-sentences.txt"));
        BufferedReader tags = new BufferedReader(new FileReader("inputs/texts/brown-train-tags.txt"));

        String wordLine = new String(); //line in file
        String tagLine = new String();  //tag in file

        String[] wordTokens;	//list of words keeping track of indices
        String[] tagTokens;	//list of tags  keeping track of indices to compare with wordlist

        String start = "START";
        Map<String, Integer> map = new HashMap<String, Integer>();
        MapofTags.put(start, map);

        do{
            wordTokens = wordLine.split(" ");
            tagTokens = tagLine.split(" ");
            nextTagLink(tagTokens);
            wordstofMapofTags(wordTokens, tagTokens);

        }while ((wordLine = words.readLine()) != null && (tagLine = tags.readLine()) != null);

        //close files
        words.close();
        tags.close();

        generateTMatrix();
        generateEMatrix();

    }

    public void testingVertibi(){

        //hardcoding the Transmission map- from tag to tag
        Map<String, Double> m1 = new HashMap<>();
        m1.put("CNJ",0.0 );
        tMatrix.put("#",m1);

        Map<String, Double> m2 = new HashMap<>();
        m2.put("N",7.0 );
        tMatrix.put("#",m2);

        Map<String, Double> m3 = new HashMap<>();
        m3.put("NP",3.0 );
        tMatrix.put("#",m3);

        Map<String, Double> m4 = new HashMap<>();
        m4.put("V",0.0 );
        tMatrix.put("#",m1);

        //
        Map<String, Double> m21 = new HashMap<>();
        m21.put("CNJ",0.0 );
        tMatrix.put("CNJ",m21);

        Map<String, Double> m22 = new HashMap<>();
        m22.put("N",4.0 );
        tMatrix.put("CNJ",m22);

        Map<String, Double> m23 = new HashMap<>();
        m23.put("NP",2.0 );
        tMatrix.put("CNJ",m23);

        Map<String, Double> m24 = new HashMap<>();
        m24.put("V",4.0 );
        tMatrix.put("CNJ",m24);

        //
        Map<String, Double> m31 = new HashMap<>();
        m31.put("CNJ",2.0 );
        tMatrix.put("N",m31);

        Map<String, Double> m32 = new HashMap<>();
        m32.put("N",0.0 );
        tMatrix.put("N",m32);

        Map<String, Double> m33 = new HashMap<>();
        m33.put("NP",0.0 );
        tMatrix.put("N",m33);

        Map<String, Double> m34 = new HashMap<>();
        m34.put("V",8.0 );
        tMatrix.put("N",m34);

        //
        Map<String, Double> m41 = new HashMap<>();
        m41.put("CNJ",2.0 );
        tMatrix.put("NP",m41);

        Map<String, Double> m42 = new HashMap<>();
        m42.put("N",0.0 );
        tMatrix.put("NP",m42);

        Map<String, Double> m43 = new HashMap<>();
        m43.put("NP",0.0 );
        tMatrix.put("NP",m43);

        Map<String, Double> m44 = new HashMap<>();
        m44.put("V",4.0 );
        tMatrix.put("NP",m44);

        //
        Map<String, Double> m51 = new HashMap<>();
        m51.put("CNJ",2.0 );
        tMatrix.put("V",m51);

        Map<String, Double> m52 = new HashMap<>();
        m52.put("N",4.0 );
        tMatrix.put("V",m52);

        Map<String, Double> m53 = new HashMap<>();
        m53.put("NP",2.0 );
        tMatrix.put("V",m53);

        Map<String, Double> m54 = new HashMap<>();
        m54.put("V",0.0 );
        tMatrix.put("V",m54);


        //hard coding emisiion map
        Map<String, Double> n1 = new HashMap<>();
        n1.put("and",10.0 );
        eMatrix.put("CNJ",n1);

        Map<String, Double> n2 = new HashMap<>();
        n2.put("cat",0.0 );
        eMatrix.put("CNJ",n2);

        Map<String, Double> n3 = new HashMap<>();
        n3.put("chase",0.0 );
        eMatrix.put("CNJ",n3);

        Map<String, Double> n4 = new HashMap<>();
        n4.put("dog",0.0 );
        eMatrix.put("CNJ",n4);

        Map<String, Double> n5 = new HashMap<>();
        n5.put("get",0.0 );
        eMatrix.put("CNJ",n5);

        Map<String, Double> n6 = new HashMap<>();
        n6.put("watch",0.0 );
        eMatrix.put("CNJ",n6);

        //
        Map<String, Double> n21 = new HashMap<>();
        n21.put("and",0.0 );
        eMatrix.put("N",n21);

        Map<String, Double> n22 = new HashMap<>();
        n22.put("cat",4.0 );
        eMatrix.put("N",n22);

        Map<String, Double> n23 = new HashMap<>();
        n23.put("chase",0.0 );
        eMatrix.put("N",n23);

        Map<String, Double> n24 = new HashMap<>();
        n24.put("dog",4.0 );
        eMatrix.put("N",n24);

        Map<String, Double> n25 = new HashMap<>();
        n25.put("get",0.0 );
        eMatrix.put("N",n25);

        Map<String, Double> n26 = new HashMap<>();
        n26.put("watch",2.0 );
        eMatrix.put("N",n26);


        //
        Map<String, Double> n31 = new HashMap<>();
        n31.put("and",0.0 );
        eMatrix.put("NP",n31);

        Map<String, Double> n32 = new HashMap<>();
        n32.put("cat",0.0 );
        eMatrix.put("NP",n32);

        Map<String, Double> n33 = new HashMap<>();
        n33.put("chase",10.0 );
        eMatrix.put("NP",n33);

        Map<String, Double> n34 = new HashMap<>();
        n34.put("dog",0.0 );
        eMatrix.put("NP",n34);

        Map<String, Double> n35 = new HashMap<>();
        n35.put("get",0.0 );
        eMatrix.put("NP",n35);

        Map<String, Double> n36 = new HashMap<>();
        n36.put("watch",0.0 );
        eMatrix.put("NP",n36);

        //
        Map<String, Double> n41 = new HashMap<>();
        n41.put("and",0.0 );
        eMatrix.put("V",n41);

        Map<String, Double> n42 = new HashMap<>();
        n42.put("cat",0.0 );
        eMatrix.put("V",n42);

        Map<String, Double> n43 = new HashMap<>();
        n43.put("chase",3.0 );
        eMatrix.put("V",n43);

        Map<String, Double> n44 = new HashMap<>();
        n44.put("dog",0.0 );
        eMatrix.put("V",n44);

        Map<String, Double> n45 = new HashMap<>();
        n45.put("get",1.0 );
        eMatrix.put("V",n45);

        Map<String, Double> n46 = new HashMap<>();
        n46.put("watch",6.0 );
        eMatrix.put("V",n46);

        //calling the vertibi algorithm
        String sentence = "chase watch dog chase watch";
        String[] words = sentence.split(" ");

        String[] tags= ViterbiAlgo(words);
        System.out.println("[NP, V, N, V, N]");

        for(int i =0; i< tags.length; i++){

            System.out.println(tags[i]);

        }


    }

    public void consoleTest(){


        System.out.println("Enter a sentence");

        Scanner input = new Scanner(System.in);
        String sentence = "";
        sentence = input.nextLine();

        String[] words = sentence.split("");

        //call viterbi decoding method
        String[] tags = ViterbiAlgo(words);

        //print word and tags to console
        for(int i=0; i< words.length; i++){
            System.out.println(words[i] + "/"+ tags[i] +" ");

        }
    }

    public static void main(String[] args) throws Exception {
        Sudi s = new Sudi();

        s.testingVertibi();

        s.quickTrain();

        /*String command = "";
        Scanner input = new Scanner(System.in);
        System.out.println("Select which test you want to perform by entering the corresponding letter:");
        System.out.println("a: 1 fold cross-validation using first 500 lines.");
        System.out.println("b: 2 folds cross-validation using first 500 lines.");
        System.out.println("c: 1 fold cross-validation using all lines.");
        System.out.println("d: 2 folds cross-validation using all lines.");
        command = input.nextLine();

        if(command.charAt(0)=='a') { System.out.println("Accuracy is " + s.testLinesPieces(5, 500)); }

        if(command.charAt(0)=='b') { System.out.println("Accuracy is " + s.testLinesPieces(10, 500)); }

        if(command.charAt(0)=='c') { System.out.println("Accuracy is " + s.testAllLines(5)); }

        if(command.charAt(0)=='d') { System.out.println("Accuracy is " + s.testAllLines(10)); }

         */

    }

}