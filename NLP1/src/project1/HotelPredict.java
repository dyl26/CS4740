package project1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HotelPredict {

	/**
	 * @param args
	 * @throws IOException 
	 */
	static HashMap<String, Integer> countT = new HashMap<String, Integer>();
	static HashMap<String, Integer> countF = new HashMap<String, Integer>();
    static HashMap<String, HashMap<String,Integer >> bicountT = new HashMap<String, HashMap<String,Integer >>(); 
    static HashMap<String, HashMap<String,Integer >> bicountF = new HashMap<String, HashMap<String,Integer >>(); 
    
	public static void main(String[] args) throws IOException {
		
		String[] sortedFiles = sortHotelReview("HotelReviews/reviews.train","outputFiles/truth.txt","outputFiles/notTruth.txt");
		ArrayList<String> Twords = tokenize(sortedFiles[0]);
		//System.out.println("size of Twords="+Twords.size());
		ArrayList<String> notTwords = tokenize(sortedFiles[1]);
		
		countT = countWord(Twords);
        bicountT = countBiWord(Twords); 
                
        countF = countWord(notTwords);
        bicountF = countBiWord(notTwords); 
                       
        predict("kaggle_data_file.txt","outputFiles/kaggle_predict.txt");
        
        //System.out.println(predictCount[0]+predictCount[1]);
   
		}
	   
	    /** this method read the input hotel file, preprocess the hotel reviews, and sorts the review based on truthfulness,
       * write the sorted review into two files, then return a string array containing the two file names*/
	public static String[] sortHotelReview(String inputFile, String truthful, String notTruthful) throws IOException {
		BufferedReader hotel = new BufferedReader(new FileReader(inputFile));
		FileWriter w1 = new FileWriter(truthful);
		FileWriter w0 = new FileWriter(notTruthful);
        String[] result= new String[2];
		String begin = "\\d,\\d,";
		String endSentence = "[\\.\\?!]";
		String midPunct = "[:,();\"@]";

		
		while(hotel.ready()){
			String line = hotel.readLine();
			String isTruthful = line.substring(0,1);
			line = line.toLowerCase();
			line = line.replaceAll(begin, "| ");
			line = line.replaceAll(endSentence, " |");
			line = line.replaceAll(midPunct, "");
			line = line.trim();
		
			if (line.contains("|")){
				//System.out.println("1="+ line);
				if (line.charAt(line.length()-1)== '|'){
					line=line.substring(0,line.length()-1);
					//System.out.println(line);
				}
				if(isTruthful.equals("1")){
					w1.write(line );
				}
				else{
				w0.write(line);
				}
			}		
		}
		w1.write(" |");
		w0.write(" |");
		w1.close();
		w0.close();
		hotel.close();
		
		result[0] = truthful;
		result[1] = notTruthful;
		
		return result;
	}
	
	/** tokenize the input preprocessed txt file into an arraylist of words */
	public static ArrayList<String> tokenize(String input) throws IOException{
		BufferedReader file = new BufferedReader(new FileReader(input));
		String[] wordsInLine = new String[0];
		ArrayList<String> newWords = new ArrayList<String>();
		int numberOfWords =0;
		while (file.ready()){
			String line = file.readLine();
			wordsInLine = getWords(line);
			 }
		
		for (String word: wordsInLine){
			
			if(newWords.isEmpty()){
				newWords.add(word);
			    numberOfWords++;
			}
			else {
				if ( !(word.equals("|") ) || !(newWords.get(newWords.size()-1).equals("|"))){
			     newWords.add(word);
			     numberOfWords++;
				}
			}
		}
		
		//System.out.println("# of words="+ numberOfWords);
		return newWords;
		
	}
	/** count unigram counters*/
	public static HashMap<String, Integer> countWord(ArrayList<String> wordlist){
		HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
		for (String word: wordlist){
			//update unigram counter
			if (wordCount.containsKey(word)){
				Integer count = wordCount.get(word);
				wordCount.put(word,count+1);
		      }
			else {
				wordCount.put(word, 1);
			  }
	      }
		return wordCount;
	}
	
	/**this method counts bi-gram counters, returns a hashmap with the <word, <previousWord, count of word|previous word>>*/
    public static HashMap<String, HashMap<String,Integer> > countBiWord(ArrayList<String> wordlist){
    	HashMap<String, HashMap<String,Integer>> biWordCount = new HashMap<String, HashMap<String, Integer>>();
    	for (int n=0; n<wordlist.size(); n++){
    		if (n>0){ //update biWordCount
				if (biWordCount.containsKey(wordlist.get(n))){ //if map contains word
					HashMap<String,Integer> prevMap = biWordCount.get(wordlist.get(n)); 
					if (prevMap.containsKey(wordlist.get(n-1))){ //if inner map contains the previous word
						Integer value = prevMap.get(wordlist.get(n-1)); 
						prevMap.put(wordlist.get(n-1), value+1);
					}
					else { //if inner map doesn't contain previous word
						prevMap.put(wordlist.get(n-1),1);
					}
				}
				else { //if outer map doesn't contain word
					HashMap<String, Integer> innerMap = new HashMap<String,Integer>();
					innerMap.put(wordlist.get(n-1), 1);
					biWordCount.put(wordlist.get(n), innerMap);
				}
			}
    	}
    	return biWordCount;
    }
	
                    
    /**predict the thuthfulness of each review. return an array of counts of predicted truthful and not truthful. In the mean time,
     * write the preditcion into a txt file ("kaggle_predicted.txt") as instructed. */
    public static String predict(String testFile, String predictFile) throws IOException{
    	BufferedReader file = new BufferedReader(new FileReader(testFile));    
    	
    	FileWriter w = new FileWriter(predictFile);
    	ArrayList<String> predictionList = new ArrayList<String>();
    	int lineCounter =-1;
    	
    	String begin = "\\d,\\d,";   	    	
    	String testBegin = "\\?,";
		String endSentence = "[\\.\\?!]";
		String midPunct = "[:,();\"@]";
		w. write("Id,Label" + '\n');
		System.out.println("Id,Label" + '\n');
		while(file.ready()){ //preprocess the file
			String line = file.readLine();
						
			line = line.toLowerCase();
			line = line.replaceAll(testBegin, "| ");
			line = line.replaceAll(endSentence, " |");
			line = line.replaceAll(midPunct, "");
			line = line.trim();
						
			if(line.contains("|")){
				String[] wordsInLine = getWords(line);
				lineCounter++;
				ArrayList<String> wordList = new ArrayList<String>();
				for(int i=0; i<wordsInLine.length; i++){
					wordList.add(wordsInLine[i]); 
				}
				//calculate the number of word that appear less than 5 times
				double[] uniNValT = calcUniNValues(countT);
				double[] uniNValF = calcUniNValues(countF);
				double[] biNValT = calcBiNValues(bicountT);
				double[] biNValF = calcBiNValues(bicountF);
				//calculate perplexity based on two models				
				double perpT = bigramPerplexity(wordList,countT,bicountT,biNValT, uniNValT); //calculate perplexity based on the true model
				double perpF = bigramPerplexity(wordList,countF,bicountF,biNValF, uniNValF); // calculate perplexity based on the fake model
				String isTruth = (perpT < perpF) ? "1" : "0"; // compare the two perplexities and determine the fitness, choose the the model that gives lower perplexity
				predictionList.add(isTruth);
				w.write(lineCounter+ ","+ isTruth + '\n');
				System.out.println(lineCounter+ ","+ isTruth + '\n');
			} else{
				predictionList.add(null); 
			}
			
		} // end of while loop
		
		w.close();
		// sort perplexity into two arraylists based on truthfulness
		
		return predictFile;
		}
      
    /** split a string into small stings by space and return an array of strings*/
    public static String[] getWords(String sentence) {
		//Process the sentence.
		sentence = sentence.replaceAll("\\s+"," ");
		sentence = sentence.trim();
		//writer.write(sentence+"\n");
		return sentence.split(" ");
	}
	
    /**calculate bigram numbers that is less than 5 times, return an array of numbers of words have 0,1,2,3,4,5,6 counts*/
   public static double[] calcBiNValues(HashMap<String, HashMap<String, Integer>> biCount){
	double[] nCounts = new double[6]; //Smoothing only affects counts of 5 or less.
	int corpusCounter =0;
	for (String w: biCount.keySet()){
		for (String b : biCount.get(w).keySet()){
			corpusCounter += biCount.get(w).get(b);
			//get the value,
			if (biCount.get(w).get(b)<6){
				nCounts[biCount.get(w).get(b)]++;
			}
		}
	 }
		
	nCounts[0] = nCounts[1]/(double)corpusCounter;
	return nCounts;
   }

   /**calculate unigram numbers that is less than 5 times, return an array of numbers of words have 0,1,2,3,4,5,6 counts*/
  public static double[] calcUniNValues(HashMap<String,Integer> wordCount){
	int corpusCounter =0;
	double[] nCounts = new double[6]; //Smoothing only affects counts of 5 or less.
	for (String w: wordCount.keySet()){
		corpusCounter += wordCount.get(w);
		if (wordCount.get(w)<6){
			nCounts[wordCount.get(w)]++;
		}
	}
	
	nCounts[0] = nCounts[1]/(double)corpusCounter;
	return nCounts;
}
  
  /**calculate the unigram perplexity based on give unigram model*/
  public static double unigramPerplexity(ArrayList<String> test, HashMap<String, Integer> wordCount, double[] nCounts) throws IOException{
		double logPart = 0;
		double N=0;
		int corpusCounter = test.size();
		
		for (String w: test){
				
			double p = Math.log10((double)corpusCounter/(double)smoothUniWord(w, nCounts, wordCount)); //smoothing for unigram word
			logPart = logPart+p;
			N++;
			}
		
		
		double logPrep =logPart/N;
		return Math.pow(10, logPrep);
	}

/**calculate bigram perplexity based on the given unigram and bigram */
public static double bigramPerplexity(ArrayList<String> test, HashMap<String, Integer> wordCount,HashMap<String, HashMap<String,Integer>> biWordCount, double[] biNCounts, double[] uniNCounts) throws IOException{
	double logSum = 0;
	String previousWord = "|";
	double N=0;

		//for each word, get its probability take its log and add to logSum
		for (String w: test){
			N++;
			//if wn-1 is unknown or wn is unknown:
			if (!wordCount.containsKey(previousWord) || !wordCount.containsKey(w)){
				double p= Math.log10(1000); //smoothing;
				logSum = logSum+p;
				previousWord = w;
			}

			else{
				double p = Math.log10((double)smoothUniWord(previousWord, uniNCounts, wordCount)/(double)smoothBiWords(w, previousWord, biNCounts, biWordCount)); //smoothing
				logSum = logSum+p;
				previousWord = w;
			}
		}
	
	double logPrep =logSum/(double)N;
	
	return Math.pow(10, logPrep);
}

//Returns new c value for Unigrams
public static double smoothUniWord(String word, double[] nCounts, HashMap<String, Integer> wordCount){
	if (!wordCount.containsKey(word)){
		return nCounts[0];
	}
	int c = wordCount.get(word);
	if (c<nCounts.length-1){
		if (nCounts[c+1]==0){
			return c;
		}			
		double updatedC = ((double)c+1.0)*((double)nCounts[c+1]/(double)nCounts[c]);
		return updatedC;
	}
	else {
		return (double)c;
	}
}

/**Returns new c value for bigrams*/
	public static double smoothBiWords(String word, String wordBefore, double[] nCounts, HashMap<String, HashMap<String,Integer>> biWordCount){
		
		//if there is an unknown word
		if (!biWordCount.containsKey(word) || !biWordCount.get(word).containsKey(wordBefore)){
			return nCounts[0];
		}
		else {
			int c = biWordCount.get(word).get(wordBefore);
			if (c<nCounts.length-1){
				
				if (nCounts[c+1]==0){
					return c;
				}
				
				return ((double)c+1.0)*((double)nCounts[c+1]/(double)nCounts[c]);
			}
			else {
				return c;
			}
		}
	}
}





