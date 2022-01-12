import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author David Ouma & Michael Moyo PS5 21S
 * A java class that performs Part of Speech tagging using the viterbi algorithm.
 * The Hidden Markov Model is trained and tested on the Brown corpus and the results are as follows:
 * Correct Tags:35109
 * Incorrect Tags:1285
 * it also has a console based test method that prompts the user to enter a sentence and it print out the sentence
 * interspersed with the POS tags.
 *
 *
 */

public class Ps5{

    public String start = "#";
    public String currState = start;
    public Map<String, Map<String,Double>> Observations= new HashMap<>();
    public Map<String, Map<String,Double>> Transitions = new HashMap<>();
    public double unseenObs = -5.0;


    /**
     * method to perform Viterbi decoding to find the best sequence of tags for a line
     * @param observations
     * @return String[] of tags
     */
    public String[] viterbiDecoding(String[] observations){

        Set<String> currStates= new HashSet<>();
        currStates.add(start);

        //map for observations and scores
        Map<String, Double> currScores = new HashMap<String,Double>();
        currScores.put(start, (double)0);

        // list of back-pointer maps
        List<Map<String, String>> paths = new ArrayList<>();

        //for i from 0 to # observations - 1
        for(int i=0; i < observations.length; i++){

            Set<String> nextStates = new HashSet<>();
            Map<String, Double> nextScores = new HashMap<>();

            //add a back tracking map to list of Paths
            paths.add(new HashMap<String,String>());

            String currObservation = observations[i];

            //System.out.println("currObs: "+currObservation);

            //for each state in currStates
            for(String currState: currStates){

                //System.out.println("currState: "+ currState );
                //check that the state is in the transitions map first
                if(Transitions.containsKey(currState)){

                    //for each transition from currState -> nextState
                    //loop through the keyset of the inner map
                    for(String nextState: Transitions.get(currState).keySet()){

                        //System.out.println(" ALL nextStates: "+ Transitions.get(currState).keySet());
                        //System.out.println("nextState: "+ nextState);

                        Double nextScore = currScores.get(currState) + Transitions.get(currState).get(nextState);
                        //System.out.println("currScore: "+ currScores.get(currState) + " Trans Score:"+ Transitions.get(currState).get(nextState));

                        //if unseen observation, add constant penalty
                        if(!Observations.get(nextState).containsKey(currObservation)){
                            //System.out.println(" -100 unseen observation score used");
                            nextScore= nextScore+ unseenObs;
                        }

                        //normal observation, get score
                        else{
                            nextScore = nextScore+ Observations.get(nextState).get(currObservation);

                        }

                        //add nextState to nextStates if we have a better next score or it's not in nextScores
                        if(!nextScores.containsKey(nextState) ||nextScore > nextScores.get(nextState)){

                            nextStates.add(nextState);
                            nextScores.put(nextState,nextScore);
                            paths.get(i).put(nextState,currState);

                        }
                    }
                }
            }
            // next observation
            currStates= nextStates;
            currScores = nextScores;

        }
        //System.out.println("CURRStateS: "+ currStates);


        //backtrace starts from the state with the best score for the last
        //observation and works back to the start state

        String winnerState = currStates.iterator().next();
        //compare this state and find the one with the best Score
        for(String state: currStates){

            if(currScores.get(state)> currScores.get(winnerState)){

                winnerState = state;
            }
        }

        //backtracking to get the path
        String[] path = new String[observations.length];
        String trackingState = winnerState;

        //from end to start
        for(int i = observations.length-1; i>=0; i--){

            path[i]= trackingState;
            trackingState = paths.get(i).get(trackingState);
        }
        return path;
    }

    /**
     * Method to test the Vertibi method on simple hard-coded graphs and input strings
     *     (e.g., from programming drill, along with others you make up).
     */
    public void testingVertibi(){

        Observations = new HashMap<String, Map<String,Double>>();
        Transitions = new HashMap<String, Map<String,Double>>();

        //hardcoding the Transmission map- from tag to tag
        //#
        Map<String, Double> m1 = new HashMap<>();
        m1.put("N",7.0 );
        Transitions.put("#",m1);
        Transitions.get("#").put("NP",3.0 );


        //CNJ
        Map<String, Double> m21 = new HashMap<>();
        m21.put("N",4.0 );
        Transitions.put("CNJ",m21);
        Transitions.get("CNJ").put("NP",2.0 );
        Transitions.get("CNJ").put("V",4.0 );


        //N
        Map<String, Double> m31 = new HashMap<>();
        m31.put("CNJ",2.0 );
        Transitions.put("N",m31);
        Transitions.get("N").put("V",8.0 );



        //NP
        Map<String, Double> m41 = new HashMap<>();
        m41.put("CNJ",2.0 );
        Transitions.put("NP",m41);
        Transitions.get("NP").put("V",8.0 );



        //V
        Map<String, Double> m51 = new HashMap<>();
        m51.put("CNJ",2.0 );
        Transitions.put("V",m51);
        Transitions.get("V").put("N",4.0 );
        Transitions.get("V").put("NP",4.0 );



        //hard coding Observations map
        //CNJ
        Map<String, Double> n1 = new HashMap<>();
        n1.put("and",10.0 );
        Observations.put("CNJ",n1);


        //N
        Map<String, Double> n21 = new HashMap<>();
        n21.put("cat",4.0 );
        Observations.put("N",n21);
        Observations.get("N").put("dog", 4.0);
        Observations.get("N").put("watch", 2.0);

        //NP
        Map<String, Double> n31 = new HashMap<>();
        n31.put("chase",10.0 );
        Observations.put("NP",n31);

        //V
        Map<String, Double> n41 = new HashMap<>();
        n41.put("chase",3.0 );
        Observations.put("V",n41);
        Observations.get("V").put("get", 1.0);
        Observations.get("V").put("watch", 6.0);


        //calling the viterbi algorithm
        String sentence = "chase watch dog chase watch";
        String[] words = sentence.split(" ");

        String[] tags= viterbiDecoding(words);
        System.out.println("Correct order of tags from graph: [NP, V, N, V, N]");
        System.out.println("My order of tags:");

        for(int i =0; i< tags.length; i++){

            System.out.println(tags[i]);
        }
    }


    //Write a method to train a model (observation and transition probabilities)
    // on corresponding lines (sentence and tags) from a pair of training files.

    /**
     * Method to train Hidden Markov Model using pair of training files
     * @param sentencesFilePath
     * @param tagsFilePath
     * @throws IOException
     */

    public void trainModel(String sentencesFilePath, String tagsFilePath) throws IOException {

        Observations = new HashMap<String, Map<String,Double>>();
        Transitions = new HashMap<String, Map<String,Double>>();

        BufferedReader inputSentence = new BufferedReader(new FileReader(sentencesFilePath));
        BufferedReader inputTag = new BufferedReader(new FileReader(tagsFilePath));


        String currState = start;
        String tagLine;
        String sentenceLine;

        Transitions.put(start, new HashMap<String, Double>());


        //read file line by line
        while((sentenceLine = inputSentence.readLine()) != null ){

            currState = start;

            //set to lowercase
            sentenceLine.toLowerCase();
            String[] words= sentenceLine.split(" ");

            tagLine =  inputTag.readLine();
            String[] tagWords = tagLine.split(" ");

            //obtain transitions for each word
            for(int i =0; i< words.length; i++){

                //tags are states, words are the observations
                String observation = words[i];
                String nextState = tagWords[i];

                //adding to Transitions map
                // if the currState is not in the Transitions map as a key, add it
                if(Transitions.containsKey(currState) == false){

                    Transitions.put(currState, new HashMap<String, Double>());
                    Transitions.get(currState).put(nextState, 1.0);

                }
                //if the current state exists but nextstate doesn't exist in inner map, add it
                else if(Transitions.get(currState).containsKey(nextState) == false){

                    Transitions.get(currState).put(nextState,1.0);

                }
                //else increment score and put into transitions map
                else{
                    Double score = 1.0 +Transitions.get(currState).get(nextState) ;
                    Transitions.get(currState).put(nextState,score);
                }

                //adding to observations map
                //same process as observations map
                if(Observations.containsKey(nextState) == false){

                    Observations.put(nextState, new HashMap<String, Double>());
                    Observations.get(nextState).put(observation, 1.0);

                }
                else if(Observations.get(nextState).containsKey(observation) == false){
                    Observations.get(nextState).put(observation, 1.0);
                }
                else{
                    Double score2 = 1.0 +Observations.get(nextState).get(observation) ;
                    Observations.get(nextState).put(observation,score2);
                }
                //mark next state as current state
                currState= nextState;
            }


        }
        inputSentence.close();
        inputTag.close();

        //to calculate the probability,loop through outer map once, inner map twice
        //Observations
        for(String obs: Observations.keySet()) {

            Double rowSum = 0.0;

            //first loop on inner map to find the row sums
            for (String POS : Observations.get(obs).keySet()) {

                rowSum = rowSum + Observations.get(obs).get(POS);
            }
            if (rowSum == 0) {

                System.out.println("Dividing by a zero!");
            }

            //second loop to calculate the probability
            for (String POS : Observations.get(obs).keySet()) {

                //calculate the probability
                Double nextScore = Observations.get(obs).get(POS);
                Double probability = Math.log10(nextScore / rowSum);

                //store probability in map
                Observations.get(obs).put(POS, probability);
            }
        }

        //Tansitions
        for(String trans: Transitions.keySet()){

            Double rowSum2 = 0.0;

            //first loop on inner map to find the row sums
            for(String POS2: Transitions.get(trans).keySet()){

                rowSum2= rowSum2+ Transitions.get(trans).get(POS2);
            }

            if(rowSum2 == 0){

                System.out.println("Dividing by a zero!");
            }

            //second loop to calculate the probability
            for(String POS2: Transitions.get(trans).keySet()){

                //calculate the probability
                Double nextScore2= Transitions.get(trans).get(POS2);
                Double probability2 = Math.log10(nextScore2/rowSum2);

                //store probability in map
                Transitions.get(trans).put(POS2, probability2);
            }
        }
        System.out.println("Model Trained");
    }

    /**
     * console-based test method to give the tags from an input line.
     */
    public void consoleTest(){

        while(true){

            System.out.println("Enter a sentence");

            Scanner input = new Scanner(System.in);
            String sentence = "";
            sentence = input.nextLine();

            String[] words = sentence.split(" ");

            //call viterbi decoding method
            String[] tags = viterbiDecoding(words);

            //print word and tags to console
            for(int i=0; i< words.length; i++){
                System.out.println(words[i] + "/"+ tags[i] +" ");
            }
            System.out.println("Done!");
        }
    }



    /**
     * file-based test method to evaluate the performance on a pair of test files
     * @param testSentences
     * @param testTags
     * @throws IOException
     */
    public void performanceTest(String testSentences, String testTags) throws IOException{

        BufferedReader inputSentence = new BufferedReader(new FileReader(testSentences));
        BufferedReader inputTags = new BufferedReader(new FileReader(testTags));
        String sentence;
        String tags;

        List<String[]> sentenceList = new ArrayList<>();
        List<String[]> tagsList = new ArrayList<>();

        //reading in the sentences
        while((sentence = inputSentence.readLine())!= null){
            sentence.toLowerCase();
            String[] words = sentence.split(" ");
            sentenceList.add(words);
        }
        inputSentence.close();

        //reading in the tags
        while((tags = inputTags.readLine())!= null){
            //tags.toLowerCase();
            String[] POS = tags.split(" ");
            tagsList.add(POS);
        }
        inputTags.close();

        int correct = 0;
        int incorrect =0;
        //for each array in List of arrays containing the words
        for(int i=0; i< sentenceList.size(); i++){

            //words in the line
            String[] words = sentenceList.get(i);
            String[] rightTags = tagsList.get(i);
            String[] performanceTags= viterbiDecoding(words);

            //compare length of performance test tags with the right tags passed in

            if(words.length != rightTags.length){

                System.out.println("Right tags: "+ rightTags.length+" words:"+ words.length);
                System.out.println("Mismatch btw tag and word numbers, test failed");
                return;

            }

            if(rightTags.length != performanceTags.length){

                System.out.println("Right tags: "+ rightTags.length+" My Tags:"+ performanceTags.length);
                System.out.println("Mismatch in tag numbers, test failed");
                return;
            }

            //for each performance tag in array, compare with right tags
            for(int k=0; k< performanceTags.length; k++){

                if(rightTags[k].equals(performanceTags[k])){
                    //increment correct counter
                    correct++;
                }
                else{
                    incorrect++;
                }
            }
        }

        //print out results
        System.out.println("Correct Tags:" + correct +"\n");
        System.out.println("Incorrect Tags:" + incorrect +"\n");

    }

    public static void main(String[] args) throws  IOException {

        //testing the algorithm using hard coded graph
        Ps5 HMM = new Ps5();
        HMM.testingVertibi();

        //simple files
        HMM.trainModel("inputs/texts/simple-train-sentences.txt","inputs/texts/simple-train-tags.txt");
        HMM.performanceTest("inputs/texts/simple-test-sentences.txt", "inputs/texts/simple-test-tags.txt");

        //brown files
        HMM.trainModel("inputs/texts/brown-train-sentences.txt","inputs/texts/brown-train-tags.txt");
        HMM.performanceTest("inputs/texts/brown-train-sentences.txt","inputs/texts/brown-train-tags.txt");

        //console test
        HMM.consoleTest();

    }


}
