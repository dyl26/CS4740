package project1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class biblePreprocess {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader fread = new BufferedReader(new FileReader("sample.txt"));
		FileWriter fWriter = new FileWriter("processedText.txt");

		HashMap<String,Integer> wordCount = new HashMap<String,Integer>();
		HashMap<String, HashMap<String,Integer>> biWordCount = new HashMap<String, HashMap<String, Integer>>();

		int corpusCounter = 0;

		String verses = "\\d+:\\d+";
		String endSentence = "[\\.\\?!]";
		String midPunct = "[:,();\"]";
		String newLine = "\\n";
		String tags = "<.*>";

		String sentence = "| ";

		while (fread.ready()){
			String line =fread.readLine();
			line = line.toLowerCase();
			line = line.replaceAll(verses,"");
			line = line.replaceAll(tags,"");
			line = line.replaceAll(midPunct, "");
			line = line.replace('-', ' ');
			line = line.replaceAll(endSentence," |");
			line = line.replaceAll(newLine, "");

			if (line.contains("|")){    //end of sentence
				
				int pipe = line.indexOf("|");
				String end = line.substring(0, pipe+1);
				String beg = line.substring(pipe+1, line.length());
				
				sentence = sentence+" "+end;
				
				//split sentence into words
				sentence = sentence.replaceAll("\\s+"," ");
				sentence = sentence.trim();
				fWriter.write(sentence+"\n");
				
				
				
				
				
				///-----> here should be new function.
				
				String[] words = sentence.split(" ");
				for (String w: words){
					//fWriter.write(w+"\n");
					if (w!=""){
						corpusCounter = corpusCounter + 1;
					}
				}
				

				for (int n=0; n<words.length; n++){

					//update unigram counter
					if (wordCount.containsKey(words[n])){
						Integer val = wordCount.get(words[n]);
						wordCount.put(words[n],val+1);
					}
					else {
						wordCount.put(words[n], 1);
					}

					//update bigram counter
					if (n>0){
						if (biWordCount.containsKey(words[n])){ //if map contains word
							HashMap<String,Integer> prevMap = biWordCount.get(words[n]); 
							if (prevMap.containsKey(words[n-1])){ //if inner map contains the previous word
								Integer value = prevMap.get(words[n-1]); 
								prevMap.put(words[n-1], value+1);
							}
							else { //if inner map doesn't contain previous word
								prevMap.put(words[n-1],1);
							}
						}
						else { //if outer map doesn't contain word
							HashMap<String, Integer> innerMap = new HashMap<String,Integer>();
							innerMap.put(words[n-1], 1);
							biWordCount.put(words[n], innerMap);
						}
					}
				}
				sentence = beg;
			}
			else {
				sentence = sentence+" "+line;
			}

		}
		System.out.println(corpusCounter);
		unigrams(wordCount, corpusCounter);
		bigrams(biWordCount, wordCount, corpusCounter);
		fread.close();
		fWriter.close();


	}

	public static HashMap<String,Double> unigrams(HashMap<String,Integer> wordCounts, int corpusCount) {
		HashMap <String, Double> unigram = new HashMap<String, Double>();

		for(String word : wordCounts.keySet()){
			//System.out.println(wordCounts.get(word));
			double value = (double) wordCounts.get(word)/(double)corpusCount;
			System.out.println("Unigram value for "+word+" = "+value);
			unigram.put(word, value);
		}

		return unigram;
	}
	
	public static HashMap<String,Double> bigrams(HashMap<String, HashMap<String, Integer>> biWordCounts, HashMap<String,Integer> uniWordCounts, int corpusCount) {
		HashMap <String, Double> bigram = new HashMap<String, Double>();

		for(String word : biWordCounts.keySet()){

			HashMap<String, Integer> listOfBeforeWords = biWordCounts.get(word);
			
			for (String wordBefore : listOfBeforeWords.keySet()){
							
				double value = (double) listOfBeforeWords.get(wordBefore)/(double)uniWordCounts.get(wordBefore);				
				System.out.println("Bigram value for "+word+" | "+wordBefore+" = "+value);
				bigram.put(word+" | "+wordBefore, value);
			}
		}

		return bigram;
	}
	
}
