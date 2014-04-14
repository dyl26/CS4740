package project1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Project1 {
	

	
	//Data structures to hold the word occurrences for the unigrams, bigrams, and trigrams
	static HashMap<String,Integer> wordCount = new HashMap<String,Integer>(); //unigram word counter
	static HashMap<String, HashMap<String,Integer>> biWordCount = new HashMap<String, HashMap<String, Integer>>(); //biword counter
	static HashMap<String, HashMap<String, HashMap<String,Integer>>> triWordCount = new HashMap<String, HashMap<String, HashMap<String,Integer>>>();//first string is w_n-2, second string is w_n-1, last string is w_n
	
	//Data structures to hold the values of the unigrams, bigrams, and trigrams
	static HashMap<String,Double> unigrams;
	static HashMap<String,HashMap<String,Double>> bigrams;
	static HashMap<String,HashMap<String,HashMap<String,Double>>> trigrams;
	
	static int corpusCounter = 1; //counts how many words are in the corpus
	
	//helper data structure for random sentence generation
	static HashMap<String,Integer> wordsAfter = new HashMap<String,Integer>();
	static int numBigrams = 0;
	

	/**Runs the training of a corpus, calculates its unigram, bigram, and trigram values. 
	 * Generates three random sentences, and gets the perplexity scores of the kjbible.test and reviews.test given the training model
	 * NOTE: either lines 38 and 39 must be uncommented, or lines 40 and 41. These tell which training model to run */
	public static void main(String[] args) throws IOException {

		
		//Preprocess TRAINING files:  -Can only run one of these at a time!!!!
		//String processedFile = preprocessBible("bible_corpus/kjbible.train", "outputFiles/processedText.txt");
		//System.out.println("Training using corpus: kjbible.train\n");
		String processedFile = preProcessHotel("HotelReviews/reviews.train","outputFiles/result.txt");
		System.out.println("Training using corpus: reviews.train\n");
		
		//Preprocess TEST files
		String testBibleText = preprocessBible("bible_corpus/kjbible.test", "outputFiles/processedTestBible.txt");
		String testHotelText = preProcessHotel("HotelReviews/reviews.test", "outputFiles/processedTestHotel.txt");

				
		/**Creation of unigram and bigram model */	
		
		FileWriter fWriter = new FileWriter("outputFiles/checking.txt"); //used for debugging purposes.
		FileWriter bigramWriter = new FileWriter("outputFiles/bigrams.txt");
		FileWriter trigramWriter = new FileWriter("outputFiles/trigrams.txt");


			
		String[] splitLine = new String[2]; //this is used for cases when there are more than 2 sentences in a line


		String sentence = "";

		
		//Read in the preprocessed file
		BufferedReader fread = new BufferedReader(new FileReader(processedFile));
		
		int lineNum = 0;

		while (fread.ready()){
			String line =fread.readLine();
			if (line.startsWith("|")){
				line = line.substring(2);
			}

			if (line.contains("|")){    //identifier for end of sentence

				//split the sentence such that the current sentence is finished.
				splitLine = splitSentence(line);
				sentence = sentence+" "+splitLine[0];
				
				//break the sentence into words
				String[] wordsNoPipe = getWords(sentence);
				ArrayList<String> words = new ArrayList<String>(); //Structure to hold each word in a sentence
				
				words.add("|"); //add | to mark the beginning of each broken up sentence
						
				if (wordsNoPipe[0]!="|"){
					words.add(wordsNoPipe[0]);
					corpusCounter = corpusCounter+1;
				}
				for (int i=1; i<wordsNoPipe.length; i++){

					words.add(wordsNoPipe[i]); //add the rest of the words to the words array
					if (wordsNoPipe[i]!=""){
						//increase the corpus count by 1
						corpusCounter = corpusCounter + 1;
					}
				}
				
				//write each broken up sentence to a text file for debugging purposes
				fWriter.write(words.toString()+"\n"); 
				
				for (int n=0; n<words.size(); n++){
					
					//update unigram counter
					if (wordCount.containsKey(words.get(n))){
						Integer val = wordCount.get(words.get(n));
						wordCount.put(words.get(n),val+1);
					}
					else {
						wordCount.put(words.get(n), 1);
					}

					//update bigram counter
					if (n>0){
						if (biWordCount.containsKey(words.get(n))){ //if map contains word
							HashMap<String,Integer> prevMap = biWordCount.get(words.get(n)); 
							if (prevMap.containsKey(words.get(n-1))){ //if inner map contains the previous word
								Integer value = prevMap.get(words.get(n-1)); 
								prevMap.put(words.get(n-1), value+1);
							}
							else { //if inner map doesn't contain previous word
								prevMap.put(words.get(n-1),1);
							}
						}
						else { //if outer map doesn't contain word
							HashMap<String, Integer> innerMap = new HashMap<String,Integer>();
							innerMap.put(words.get(n-1), 1);
							biWordCount.put(words.get(n), innerMap);
						}
					}
					//update trigram counter
					if (n>1){
						//word n-2 not in outermost map
						if(!triWordCount.containsKey(words.get(n-2))){
							//create all 3 levels
							
							HashMap<String,Integer>innermostMap = new HashMap<String,Integer>();
							innermostMap.put(words.get(n), 1);
							HashMap<String, HashMap<String,Integer>> middleMap = new HashMap<String, HashMap<String,Integer>>();
							middleMap.put(words.get(n-1), innermostMap);
							triWordCount.put(words.get(n-2), middleMap);
						}
						else{
							HashMap<String, HashMap<String,Integer>> realMiddleMap = triWordCount.get(words.get(n-2));
							//word n-1 not in middle map

							if(!realMiddleMap.containsKey(words.get(n-1))){
								//create new inner map and update upper levels
								
								HashMap<String,Integer>innermostMap = new HashMap<String,Integer>();
								innermostMap.put(words.get(n), 1);

								realMiddleMap.put(words.get(n-1),innermostMap);

								triWordCount.put(words.get(n-2), realMiddleMap); //update uppermost map
								
							}
							else{
								HashMap<String,Integer> realInnermostMap = realMiddleMap.get(words.get(n-1));
								//word n not in innermost map
								if(!realInnermostMap.containsKey(words.get(n))){
									//add to innermost map and update upper levels
									
									realInnermostMap.put(words.get(n),1);
									realMiddleMap.put(words.get(n-1), realInnermostMap);
									triWordCount.put(words.get(n-2), realMiddleMap);
								}
								else{
									//word n in innermost map
									int count = realInnermostMap.get(words.get(n));
									
									//update count in innermost map and then update upper maps
									
									realInnermostMap.put(words.get(n), count+1);
									realMiddleMap.put(words.get(n-1), realInnermostMap);
									triWordCount.put(words.get(n-2), realMiddleMap);
								}
							}
						}
					}
					
				}
				
				lineNum = lineNum+1;;
				
				for(String twoWordBefore : triWordCount.keySet()){
					HashMap<String, HashMap<String,Integer>> listOfBeforeWords = triWordCount.get(twoWordBefore);
					for(String wordBefore : listOfBeforeWords.keySet()){
						HashMap<String,Integer> listOfWords = listOfBeforeWords.get(wordBefore);
						for(String word : listOfWords.keySet()){
							fWriter.write(twoWordBefore+" "+wordBefore+" "+word+" "+listOfWords.get(word)+"\n");
						}
					}
				}

				
				//If multiple sentences are present on a single line, process each one of them
				while (splitLine[1].contains("|")){
					splitLine = splitSentence(splitLine[1]);
					String[] newWordsNoPipe = getWords(splitLine[0]);
					
					ArrayList<String> newWords = new ArrayList<String>();
					newWords.add("|");
					
					for (String w: newWordsNoPipe){
						newWords.add(w);
						if (w!=""){
							corpusCounter = corpusCounter + 1;
						}
					}
					
					fWriter.write(newWords.toString()+"\n");
					
					for (int n=0; n<newWords.size(); n++){
						//fWriter.write(newWords.get(n)+"\n");
						//update unigram counter
						if (wordCount.containsKey(newWords.get(n))){
							Integer val = wordCount.get(newWords.get(n));
							wordCount.put(newWords.get(n),val+1);
						}
						else {
							wordCount.put(newWords.get(n), 1);
						}

						//update bigram counter
						if (n>0){
							if (biWordCount.containsKey(newWords.get(n))){ //if map contains word
								HashMap<String,Integer> prevMap = biWordCount.get(newWords.get(n)); 
								if (prevMap.containsKey(newWords.get(n-1))){ //if inner map contains the previous word
									Integer value = prevMap.get(newWords.get(n-1)); 
									prevMap.put(newWords.get(n-1), value+1);
								}
								else { //if inner map doesn't contain previous word
									prevMap.put(newWords.get(n-1),1);
								}
							}
							else { //if outer map doesn't contain word
								HashMap<String, Integer> innerMap = new HashMap<String,Integer>();
								innerMap.put(newWords.get(n-1), 1);
								biWordCount.put(newWords.get(n), innerMap);
							}
						}
						
						//update trigram counter
						if (n>1){
							//word n-2 not in outermost map
							if(!triWordCount.containsKey(newWords.get(n-2))){
								//create all 3 levels
								
								HashMap<String,Integer>innermostMap = new HashMap<String,Integer>();
								innermostMap.put(newWords.get(n), 1);
								HashMap<String, HashMap<String,Integer>> middleMap = new HashMap<String, HashMap<String,Integer>>();
								middleMap.put(newWords.get(n-1), innermostMap);
								triWordCount.put(newWords.get(n-2), middleMap);
							}
							else{
								HashMap<String, HashMap<String,Integer>> realMiddleMap = triWordCount.get(newWords.get(n-2));
								//word n-1 not in middle map
								if(!realMiddleMap.containsKey(newWords.get(n-1))){
									//create new inner map and update upper levels
									
									HashMap<String,Integer>innermostMap = new HashMap<String,Integer>();
									innermostMap.put(newWords.get(n), 1);

									realMiddleMap.put(newWords.get(n-1),innermostMap);

									triWordCount.put(newWords.get(n-2), realMiddleMap); //update uppermost map									
								}
								else{
									HashMap<String,Integer> realInnermostMap = realMiddleMap.get(newWords.get(n-1));
									//word n not in innermost map
									if(!realInnermostMap.containsKey(newWords.get(n))){
										//add to innermost map and update upper levels
										
										realInnermostMap.put(newWords.get(n),1);
										realMiddleMap.put(newWords.get(n-1), realInnermostMap);
										triWordCount.put(newWords.get(n-2), realMiddleMap);
									}
									else{
										//word n in innermost map
										int count = realInnermostMap.get(newWords.get(n));
										
										//update count in innermost map and then update upper maps
										
										realInnermostMap.put(newWords.get(n), count+1);
										realMiddleMap.put(newWords.get(n-1), realInnermostMap);
										triWordCount.put(newWords.get(n-2), realMiddleMap);
									}
								}
							}
						}
					}
					
				}
				
				sentence = splitLine[1];
			}
			else {
				sentence = sentence+" "+line;
			}

		}

				
		//Make the unigrams, bigrams, and trigrams
		wordCount.put("|", wordCount.get("|")/2+1); //need to update the sentence delimeter word count to avoid double counting.
		unigrams = unigrams(wordCount, corpusCounter);
		bigrams = bigrams(biWordCount, wordCount, corpusCounter,bigramWriter);
		trigrams = trigrams(triWordCount, biWordCount, corpusCounter, trigramWriter);
		fread.close();
		fWriter.close();
		bigramWriter.close();
		trigramWriter.close();

		
		//Make the random sentences
		HashMap<String,HashMap<String,Double>> fixedBigram = fixHashMap(bigrams);

		String unigramSentence = makeUnigramSentence(unigrams);
		String bigramSentence = makeBigramSentence(fixedBigram);
		String trigramSentence = makeTrigramSentence(trigrams,fixedBigram);
		
		System.out.println("Randomly generated unigram sentence: "+unigramSentence+"\n");
		
		System.out.println("Randomly generated bigram sentence: "+bigramSentence+"\n");
		
		System.out.println("Randomly generated trigram sentence: "+trigramSentence);
		
		
		//Calculating Perplexity Part!!!!!!!!!!!!!!
		double[] uniCounts = calcUniNValues();
		double[] biCounts = calcBiNValues();
		double[] triCounts = calcTriNValues();
		
		System.out.println("\n");

		//Calculate unigram perplexities
		double bibleUniPerplex = unigramPerplexity(testBibleText, uniCounts);
		System.out.println("Bible Test unigram perplexity: "+bibleUniPerplex);
		double hotelUniPerplex = unigramPerplexity(testHotelText, uniCounts);
		System.out.println("Hotel Test unigram perplexity: "+hotelUniPerplex);
		
		
		System.out.println("\n");

		//Calculate bigram perplexities
		double bibleBiPerplex = bigramPerplexity(testBibleText, biCounts, uniCounts);
		System.out.println("Bible Test bigram perplexity: "+bibleBiPerplex);
		double hotelBiPerplex = bigramPerplexity(testHotelText, biCounts, uniCounts);
		System.out.println("Hotel Test bigram perplexity: "+hotelBiPerplex);
		
		System.out.println("\n");

		//Calculate trigram perplexities
		double bibleTriPerplex = trigramPerplexity(testBibleText, triCounts, biCounts);
		System.out.println("Bible Test trigram perplexity: "+bibleTriPerplex);
		double hotelTriPerplex = trigramPerplexity(testHotelText, triCounts, biCounts);
		System.out.println("Hotel Test trigram perplexity: "+hotelTriPerplex);
		
		
	}
	
	
	
	
	
	

	public static HashMap<String,Double> unigrams(HashMap<String,Integer> wordCounts, int corpusCount) {
		HashMap <String, Double> unigram = new HashMap<String, Double>();

		for(String word : wordCounts.keySet()){
			//System.out.println(wordCounts.get(word));
			double value = (double) wordCounts.get(word)/(double)corpusCount;
			//System.out.println("Unigram value for "+word+" = "+value);
			unigram.put(word, value);
		}

		return unigram;
	}
	

	
	public static String[] splitSentence(String line){
		String[] splitLine = new String[2];
		int pipe = line.indexOf("|");
		splitLine[0] = line.substring(0, pipe+1);
		splitLine[1] = line.substring(pipe+1, line.length());			
		return splitLine;
	}
	
	public static String[] getWords(String sentence){
		//Process the sentence.
		sentence = sentence.replaceAll("\\s+"," ");
		sentence = sentence.trim();
		return sentence.split(" ");
	}
	
	public static String preprocessBible (String fileName, String processedFileName) throws IOException{
		BufferedReader fread = new BufferedReader(new FileReader(fileName));
		FileWriter fWriter = new FileWriter(processedFileName);
		
		//Preprocessing text
		String verses = "\\d+:\\d+";
		String endSentence = "[\\.\\?!]";
		String midPunct = "[:,();\"]";
		String newLine = "\\n";
		String tags = "<.*>";
		
		while (fread.ready()){
			String line =fread.readLine();
			
			//process each line of text
			line = line.toLowerCase();
			line = line.replaceAll(verses,"");
			line = line.replaceAll(tags,"");
			line = line.replaceAll(midPunct, "");
			line = line.replace('-', ' ');
			line = line.replaceAll(endSentence," |\n");
			line = line.replaceAll(newLine, "");
			
			fWriter.write(line+" ");
		}
		fread.close();
		fWriter.close();
		return processedFileName;
	}
	
	public static String preProcessHotel(String inputFile, String outputFile) throws IOException {
		
		BufferedReader f = new BufferedReader(new FileReader(inputFile));
		FileWriter w = new FileWriter(outputFile);

		String begin = "\\d,\\d,";
		String endSentence = "[\\.\\?!]";
		String midPunct = "[:,();\"@]";

		
		while(f.ready()){
			String line = f.readLine();
			line = line.toLowerCase();
			line = line.replaceAll(begin, "| ");
			line = line.replaceAll(endSentence, " |");
			line = line.replaceAll(midPunct, "");
			line = line.trim();
		
			if (line.contains("|")){
				if (line.charAt(line.length()-1)== '|'){
					line=line.substring(0,line.length()-1);
				}
				w.write(line);
			}		
		}
		w.write("|");
		w.close();
		f.close();
		
		return outputFile;
	}
	
	public static HashMap<String, HashMap<String,Double>> bigrams(HashMap<String, HashMap<String, Integer>> biWordCounts, HashMap<String,Integer> uniWordCounts, int corpusCount, FileWriter bigramWriter) throws IOException {
		
		HashMap <String, HashMap<String,Double>> bigram = new HashMap<String, HashMap<String, Double>>();
		
		for(String word : biWordCounts.keySet()){

			HashMap<String, Integer> listOfBeforeWords = biWordCounts.get(word);
			
			for (String wordBefore : listOfBeforeWords.keySet()){
				HashMap <String, Double> temp = new HashMap<String, Double>();
							
				double value = (double) listOfBeforeWords.get(wordBefore)/(double)uniWordCounts.get(wordBefore);				
				//System.out.println("Bigram value for "+word+" | "+wordBefore+" = "+value);
				bigramWriter.write(word+" | "+wordBefore+"\n");
				if(bigram.containsKey(word)){
					HashMap <String,Double> beforeWords = bigram.get(word);
					beforeWords.put(wordBefore, value);
					bigram.put(word, beforeWords);
				}
				else{
					temp.put(wordBefore,value);
					bigram.put(word, temp);
					numBigrams++;
				}
			}
		}

		return bigram;
	}
	
	public static HashMap<String, HashMap<String, HashMap<String,Double>>> trigrams(HashMap<String, HashMap<String, HashMap<String,Integer>>> triWordCounts, HashMap<String, HashMap<String,Integer>> biWordCounts, int corpusCount, FileWriter trigramWriter) throws IOException{
		//bigramCounts should be of form word => wordBefore => Integer
		HashMap<String, HashMap<String, HashMap<String,Double>>> trigram = new HashMap<String, HashMap<String, HashMap<String,Double>>>();
		
		for(String twoWordBefore : triWordCounts.keySet()){
			HashMap<String, HashMap<String,Integer>> listOfBeforeWords = triWordCounts.get(twoWordBefore);
			for(String wordBefore : listOfBeforeWords.keySet()){
				HashMap<String,Integer> listOfWords = listOfBeforeWords.get(wordBefore);				
				for(String word : listOfWords.keySet()){
					HashMap<String,Integer> temp = biWordCounts.get(wordBefore);
					Integer biWordCount = temp.get(twoWordBefore);
					double value = (double)listOfWords.get(word)/(double)biWordCount;
					//System.out.println("Trigram value for "+word+" | "+twoWordBefore+" "+wordBefore+" = "+value);
					trigramWriter.write(word+" | "+twoWordBefore+" "+wordBefore+"\n");
					//word n-2 not in outermost map
					if(!trigram.containsKey(twoWordBefore)){
						//create all 3 levels
						HashMap<String,Double>innermostMap = new HashMap<String,Double>();
						innermostMap.put(word, value);
						HashMap<String, HashMap<String,Double>> middleMap = new HashMap<String, HashMap<String,Double>>();
						middleMap.put(wordBefore, innermostMap);
						trigram.put(twoWordBefore, middleMap);
					}
					else{
						HashMap<String, HashMap<String,Double>> realMiddleMap = trigram.get(twoWordBefore);
						//word n-1 not in middle map
						if(!realMiddleMap.containsKey(wordBefore)){
							//create new inner map and update upper levels
							HashMap<String,Double>innermostMap = new HashMap<String,Double>();
							innermostMap.put(word, value);
							realMiddleMap.put(wordBefore,innermostMap);
							trigram.put(twoWordBefore, realMiddleMap); //update uppermost map
						}
						else{
							HashMap<String,Double> realInnermostMap = realMiddleMap.get(wordBefore);
							//word n not in innermost map
							if(!realInnermostMap.containsKey(word)){
								//add to innermost map and update upper levels
								realInnermostMap.put(word,value);
								realMiddleMap.put(wordBefore, realInnermostMap);
								trigram.put(twoWordBefore, realMiddleMap);
							}
							else{
								//word n in innermost map
								System.out.println("ISSUE: THERE SHOULD NOT BE DUPLICATES IN THE TRIGRAM HASHMAP");
							}
						}
					}
				}
			}
		}
		return trigram;
	}
	
	public static String makeUnigramSentence(HashMap<String,Double> wordCountProb){
		//make sentence of length numWords using probabilities from wordCountProb
		
		int size = wordCountProb.size();
		Object[][] ranges = new Object[size][2]; //for each row, first col is word, second col is range
		String sentence = "| ";
		
		double prevEntry = 0.0;
		int i = 0;
		
		boolean foundPipe = false;
		
		for(Map.Entry entry : wordCountProb.entrySet()){
			Object word = (String) entry.getKey();
			double prob = (Double) entry.getValue();
			ranges[i][0] = word;
			ranges[i][1] = prob + prevEntry;
			prevEntry = prob + prevEntry;
			i++;
		}
		
		//for(int wordNum=0; wordNum<numWords; wordNum++){
		while(!foundPipe){
			double rand = Math.random();
			boolean found = false;
			int j = 0;
						
			if(rand > (Double)ranges[ranges.length-1][1]){
				//if random number is larger than any of the ranges, return the last word
				found = true;
				sentence += (String)ranges[ranges.length-1][0]+" ";
			}
			
			while(!found && j<ranges.length){
				if(rand < (Double)ranges[j][1]){
					if(ranges[j][0].equals("|")){
						//keep | out of the middle of the sentence
						/*rand = Math.random();
						j= 0;
						*/
						found = true;
						sentence += (String)ranges[j][0];
						foundPipe = true;
					}
					else{
						found = true;
						sentence += (String)ranges[j][0]+" ";
					}
				}
				else{
					//System.out.println("Not found "+rand);
				}
				j++;
			}
		}
		
		//sentence += " |";
		return sentence;
	}


	public static String makeBigramSentence(HashMap<String,HashMap<String,Double>> wordCountProb) throws IOException{
		//make sentence of length numWords using probabilities from wordCountProb
		
		FileWriter bigram3 = new FileWriter("outputFiles/bigrams3.txt");
		
		
		//int size = numBigrams;
		HashMap <String,Object[][]> ranges = new HashMap <String,Object[][]>(); 
			//String is WordBefore
			//for each row, first col is word, second col is range
		String sentence = "| ";
		
		double prevEntry = 0.0;
		int i = 0;
		String prevWord = "|";
		boolean foundPipe = false;
		
		for(String wordBefore : wordCountProb.keySet()){
			HashMap<String, Double> listOfWords = wordCountProb.get(wordBefore);
			
			int size = wordsAfter.get(wordBefore);
			Object[][] temp = new Object[size][2];
			
			for(String word: listOfWords.keySet()){
				double prob = listOfWords.get(word);
				temp[i][0] = word;
				temp[i][1] = prob+prevEntry;
				prevEntry = prob+prevEntry;
				i++;
				ranges.put(wordBefore,temp);
				bigram3.write(word+" | "+wordBefore+"\n");				
			}
			
			prevEntry = 0.0;
			i = 0;
		}
		
		//for(int wordNum=0; wordNum<numWords; wordNum++){
		while(!foundPipe){
			double rand = Math.random();
			boolean found = false;
			int j = 0;	
			
			Object[][] rangeTable = ranges.get(prevWord);
			
			
			if(rand > (Double)rangeTable[rangeTable.length-1][1]){
				//if random number is larger than any of the ranges, return the last word
				found = true;
				sentence += (String)rangeTable[rangeTable.length-1][0]+" ";
				prevWord = (String)rangeTable[rangeTable.length-1][0];
			}
			
			while(!found && j<rangeTable.length){
				if(rand < (Double)rangeTable[j][1]){
					if(rangeTable[j][0].equals("|")){
						//keep | out of the middle of the sentence
						/*rand = Math.random();
						j= 0;*/
						found = true;
						sentence += (String)rangeTable[j][0];
						prevWord = (String)rangeTable[j][0];
						foundPipe = true;
					}
					else{
						found = true;
						sentence += (String)rangeTable[j][0]+" ";
						prevWord = (String)rangeTable[j][0];
					}
				}
				else{
					//System.out.println("Not found "+rand);
				}
				j++;
			}
		}
		
		//sentence += " |";
		bigram3.close();
		return sentence;
	}
	
	public static String makeBigramSentence(HashMap<String,HashMap<String,Double>> wordCountProb, int numWords) throws IOException{
		//make sentence of length numWords (not counting starting |) using probabilities from wordCountProb
		
		FileWriter bigram3 = new FileWriter("outputFiles/bigrams3.txt");
				
		HashMap <String,Object[][]> ranges = new HashMap <String,Object[][]>(); 
			//String is WordBefore
			//for each row, first col is word, second col is range
		String sentence = "| ";
		
		double prevEntry = 0.0;
		int i = 0;
		String prevWord = "|";
		
		for(String wordBefore : wordCountProb.keySet()){
			HashMap<String, Double> listOfWords = wordCountProb.get(wordBefore);
			int size = wordsAfter.get(wordBefore);
			Object[][] temp = new Object[size][2];			
			for(String word: listOfWords.keySet()){
				double prob = listOfWords.get(word);
				temp[i][0] = word;
				temp[i][1] = prob+prevEntry;
				prevEntry = prob+prevEntry;
				i++;
				ranges.put(wordBefore,temp);
				bigram3.write(word+" | "+wordBefore+"\n");			
			}
			prevEntry = 0.0;
			i = 0;
		}
		
		for(int wordNum=0; wordNum<numWords; wordNum++){
		
			double rand = Math.random();
			boolean found = false;
			int j = 0;	
			
			Object[][] rangeTable = ranges.get(prevWord);
			
			//System.out.println(ranges.get(prevWord).toString());
			
			if(rand > (Double)rangeTable[rangeTable.length-1][1]){
				//if random number is larger than any of the ranges, return the last word
				found = true;
				sentence += (String)rangeTable[rangeTable.length-1][0]+" ";
				prevWord = (String)rangeTable[rangeTable.length-1][0];
			}
			
			while(!found && j<rangeTable.length){
				if(rand < (Double)rangeTable[j][1]){
					if(rangeTable[j][0].equals("|")){
						//keep | out of the middle of the sentence
						rand = Math.random();
						j= 0;
						found = true;
						sentence += (String)rangeTable[j][0];
						prevWord = (String)rangeTable[j][0];
					}
					else{
						found = true;
						sentence += (String)rangeTable[j][0]+" ";
						prevWord = (String)rangeTable[j][0];
					}
				}
				else{
					//System.out.println("Not found "+rand);
				}
				j++;
			}
		}
		
		//sentence += " |";
		bigram3.close();
		return sentence;
	}
	
	public static String makeTrigramSentence(HashMap<String,HashMap<String,HashMap<String,Double>>> triWordCountProb, HashMap<String,HashMap<String,Double>> biWordCountProb) throws IOException{
		//make sentence of length numWords using probabilities from wordCountProb
		
		FileWriter trigram3 = new FileWriter("outputFiles/trigrams3.txt");
		
		
		//int size = numBigrams;
		HashMap <String,HashMap<String,ArrayList<ArrayList<Object>>>> ranges = new HashMap <String,HashMap<String,ArrayList<ArrayList<Object>>>>(); 
			//String is twoWordBefore, then wordBefore
			//for each row, first col is word, second col is range
		double prevEntry = 0.0;
		int i = 0;
		
		for(String twoWordBefore : triWordCountProb.keySet()){
			HashMap<String, HashMap<String,Double>> listOfBeforeWords = triWordCountProb.get(twoWordBefore);
			
			for(String wordBefore : listOfBeforeWords.keySet()){
				HashMap<String,Double> listOfWords = listOfBeforeWords.get(wordBefore);
				int size = wordsAfter.get(wordBefore);

				//System.out.println("phrase: "+twoWordBefore+" "+wordBefore+" "+" size: "+size);
				ArrayList<ArrayList<Object>> temp = new ArrayList<ArrayList<Object>>();
				
				for(String word: listOfWords.keySet()){
					double prob = listOfWords.get(word);
					//System.out.println("word: "+word);
					temp.add(new ArrayList<Object>());
					temp.get(i).add(word);
					temp.get(i).add(prob+prevEntry);
					prevEntry = prob+prevEntry;
					i++;
					trigram3.write(word+" | "+twoWordBefore+" "+wordBefore+"\n");
				}
				
				prevEntry = 0.0;
				i = 0;
				//all words for a given wordBefore should be in temp now
				//update upper hashtables
				HashMap<String,ArrayList<ArrayList<Object>>> innerMap = new HashMap<String,ArrayList<ArrayList<Object>>>();
				innerMap.put(wordBefore,temp);
				
				if(!ranges.containsKey(twoWordBefore)){
					//twoWordBefore not in hashmap -> add it and all sub-HashMaps
					ranges.put(twoWordBefore,innerMap);
				}
				else{
					//twoWordBefore IS in hashmap -> update it with sub-HashMaps
					HashMap<String,ArrayList<ArrayList<Object>>> originalWordBeforeMap = ranges.get(twoWordBefore);
					originalWordBeforeMap.put(wordBefore,temp);
					ranges.put(twoWordBefore, originalWordBeforeMap);
				}
			}
			
		}
		trigram3.close();
		
		boolean foundPipe = false;
		String sentence = makeBigramSentence(biWordCountProb, 1);
		String[] sentenceWords = sentence.split(" ");
		String twoWordBefore = sentenceWords[0];
		String prevWord = sentenceWords[1];
		
		//for(int wordNum=0; wordNum<numWords; wordNum++){
		while(!foundPipe){
			double rand = Math.random();
			boolean found = false;
			int j = 0;	
			
			HashMap<String,ArrayList<ArrayList<Object>>>innerMap = ranges.get(twoWordBefore);
			ArrayList<ArrayList<Object>> rangeTable = new ArrayList<ArrayList<Object>>();
			
			rangeTable = innerMap.get(prevWord);
			
			
			if(rand > (Double)rangeTable.get(rangeTable.size()-1).get(1)){
				//if random number is larger than any of the ranges, return the last word
				found = true;
				sentence += (String)rangeTable.get(rangeTable.size()-1).get(0)+" ";
				twoWordBefore = prevWord;
				prevWord = (String)rangeTable.get(rangeTable.size()-1).get(0);
			}
			
			while(!found && j<rangeTable.size()){
				if(rand < (Double)rangeTable.get(j).get(1)){
					if(rangeTable.get(j).get(0).equals("|")){
						//keep | out of the middle of the sentence
						/*rand = Math.random();
						j= 0;*/
						found = true;
						sentence += (String)rangeTable.get(j).get(0);
						twoWordBefore = prevWord;
						prevWord = (String)rangeTable.get(j).get(0);
						foundPipe = true;
					}
					else{
						found = true;
						sentence += (String)rangeTable.get(j).get(0)+" ";
						twoWordBefore = prevWord;
						prevWord = (String)rangeTable.get(j).get(0);
					}
				}
				else{
					//System.out.println("Not found "+rand);
				}
				j++;
			}
		}
		
		return sentence;
	}

	public static HashMap<String,HashMap<String,Double>> fixHashMap(HashMap<String,HashMap<String,Double>> origHashMap) throws IOException{
		//returns hashmap in form wordBefore => (word,prob)
		FileWriter bigram2 = new FileWriter("outputFiles/bigrams2.txt");
		
		HashMap<String,HashMap<String,Double>> newHash = new HashMap<String, HashMap<String, Double>>();
		//first string is wordBefore, second string is word
		
		for(String word : origHashMap.keySet()){

			HashMap<String, Double> listOfBeforeWords = origHashMap.get(word);
			//System.out.println(listOfBeforeWords.toString());
			
			//System.out.println(listOfBeforeWords.toString());
			
			for (String wordBefore : listOfBeforeWords.keySet()){
				//System.out.println(wordBefore);
				
				double value = listOfBeforeWords.get(wordBefore);
				
				if(newHash.containsKey(wordBefore)){ //if newHash already has this wordBefore
					//System.out.println("old wordBefore in newHash");
					HashMap <String,Double> words = newHash.get(wordBefore); //hashmap of all words this word has come before
					//System.out.println(wordBefore+" "+words.toString());
					words.put(word, value);
					newHash.put(wordBefore, words);
					//System.out.println(wordBefore+" "+words.toString());
					int count = wordsAfter.get(wordBefore);
					wordsAfter.put(wordBefore, count+1);
					bigram2.write(word+" | "+wordBefore+"\n");
				}
				else{ //newHash does not have wordBefore yet, make new hashtable
					HashMap <String, Double> temp = new HashMap<String, Double>();
					
					temp.put(word,value); //create new words hashtable
					newHash.put(wordBefore, temp);
					if(wordsAfter.containsKey(wordBefore)){
						int count = wordsAfter.get(wordBefore);
						wordsAfter.put(wordBefore, count+1);
						System.out.println(wordBefore+" is wonky");
					}
					else{
						wordsAfter.put(wordBefore, 1);
					}
					bigram2.write(word+" | "+wordBefore+"\n");
				}
			}
			
		}
		bigram2.close();
		return newHash;
	}
	
	public static double[] calcUniNValues(){
		double[] uniNCounts = new double[6]; //Smoothing only affects counts of 5 or less.
		for (String w: wordCount.keySet()){
			if (wordCount.get(w)<6){
				uniNCounts[wordCount.get(w)]++;
			}
		}
		//set the value for unknown unigrams
		uniNCounts[0] = (double)corpusCounter/(double)1000;  //assume that an unknown word occurs after every 1000
		return uniNCounts;
	}
	
	public static double[] calcBiNValues(){
		double[] biNCounts = new double[6]; //Smoothing only affects counts of 5 or less.
		for (String w: biWordCount.keySet()){
			for (String b : biWordCount.get(w).keySet()){
				//get the value,
				if (biWordCount.get(w).get(b)<6){
					biNCounts[biWordCount.get(w).get(b)]++;
				}
			}
		}
		//set the value for unknown bigrams
		biNCounts[0] = 1.0; //assume that an unknown bigram combination occurs only once.
		return biNCounts;
	}
	
	public static double[] calcTriNValues(){
		double[] triNCounts = new double[6];
		for (String wn2: triWordCount.keySet()){ //get all the wn-1 words
			for (String wn1: triWordCount.get(wn2).keySet()){ //get all the wn words
				for (String wn: triWordCount.get(wn2).get(wn1).keySet()){
					if (triWordCount.get(wn2).get(wn1).get(wn)<6){
						triNCounts[triWordCount.get(wn2).get(wn1).get(wn)]++;
					}
					
				}
			}
			
		}
		//set the c value for unknown trigrams
		triNCounts[0] = 1000; // assumes that an unknown bigram combination occurs only once.
		return triNCounts;
	}
	
	//Returns new c value for Unigrams
	public static double smoothUniWord(String word, double[] nCounts){
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
	
	//Returns new c value for bigrams
	public static double smoothBiWords(String word, String wordBefore, double[] nCounts){
		
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
	
	//Returns new c value for trigrams . Note word is the current word, wn1 is the word before, and wn2 is the 2 words before
	public static double smoothTriWords(String word, String wn1, String wn2, double[] triNCounts){
		
		//if there is an unknown word
		if (!triWordCount.containsKey(word) || !triWordCount.get(word).containsKey(wn1) || !triWordCount.get(word).get(wn1).containsKey(wn2)){
			return triNCounts[0];
		}
		else {
			int c = triWordCount.get(word).get(wn1).get(wn2);
			if (c<triNCounts.length-1){
				if (triNCounts[c+1]==0){
					return c;
				}
				return ((double)c+1.0)*((double)triNCounts[c+1]/(double)triNCounts[c]);
			}
			else {
				return c;
			}
		}
		
	}
	
	//input is a processed bible or hotel review
	public static double unigramPerplexity(String testSet, double[] nCounts) throws IOException{
		double logPart = 0;
		double N=0;
		
		BufferedReader reader = new BufferedReader(new FileReader(testSet));
		
		while(reader.ready()){
			String line = reader.readLine();
			if (line.startsWith("|")){
				line = line.substring(2);
			}
			String[] words = getWords(line);
			for (String w: words){
				
				double p = Math.log10((double)corpusCounter/(double)smoothUniWord(w, nCounts)); //smoothing for unigram word
				logPart = logPart+p;
				N++;
			}
		}
		reader.close();
		double logPrep =logPart/N;
		return Math.pow(10, logPrep);
	}
	
	
	//Perplexity formula = (log(1/P(w2|w1)+ ...+ log(1/P(wn|wn-1))/N
	
		public static double bigramPerplexity(String testSet, double[] biNCounts, double[] uniNCounts) throws IOException{
			double logSum = 0;
			String previousWord = "|";
			double N=0;
		
			BufferedReader reader = new BufferedReader(new FileReader(testSet));
			
			while(reader.ready()){
				String line = reader.readLine();
				if (line.startsWith("|")){
					line = line.substring(2);
				}
				
				String[] words = getWords(line);
				//for each word, get its probability take its log and add to logSum
				for (String w: words){
					N++;
					double p = Math.log10((double)smoothUniWord(previousWord, uniNCounts)/(double)smoothBiWords(w, previousWord, biNCounts)); //smoothing
					logSum = logSum+p;
					previousWord = w;
				}
			}
			reader.close();
			double logPrep =logSum/(double)N;
			
			return Math.pow(10, logPrep);
		}
		
		public static double trigramPerplexity(String testSet, double[] triNCounts, double[] biNCounts) throws IOException{
			double logSum = 0;
			double N=0;
			String wn2 = "|";
			String wn1;
		
			BufferedReader reader = new BufferedReader(new FileReader(testSet));
			
			while(reader.ready()){
				String line = reader.readLine();
				if (line.startsWith("|")){
					line = line.substring(2);
				}
				
				String[] words = getWords(line);
				wn1 = words[0];
				
				//for each word, get its probability take its log and add to logSum
				for (String w: words){
					N++;
					double p = Math.log10((double)smoothBiWords(wn1, wn2, biNCounts)/(double)smoothTriWords(w,wn1,wn2, triNCounts)); //smoothing
					logSum = logSum+p;
					wn2 = wn1;
					wn1 = w;
				}
			}
			reader.close();
			double logPrep =logSum/(double)N;
			
			return Math.pow(10, logPrep);
		}

}
