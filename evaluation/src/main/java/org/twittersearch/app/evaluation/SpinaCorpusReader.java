package org.twittersearch.app.evaluation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

public class SpinaCorpusReader {
	SpinaCorpus corpus;

	public SpinaCorpusReader(String fileNameIndex, String fileNameGoldStandard) {
		this.readIndex(fileNameIndex);
		this.readGoldStandard(fileNameGoldStandard);
	}
	
	public SpinaCorpusReader(String fileNameIndex, String fileNameGoldStandard, String fileNameAnnotations) {
		this.corpus = new SpinaCorpus();
		
		this.readIndex(fileNameIndex);
		this.readGoldStandard(fileNameGoldStandard);	
		this.readAnnotations(fileNameAnnotations);
	}
	
	protected void readIndex(String fileName) {
		Map<String, String> index = new HashMap<String, String>();
		try {
			CSVReader reader = new CSVReader(new FileReader(fileName), '\t');
			String[] nextLine;
			try {
				while ((nextLine = reader.readNext()) != null) {
					index.put(nextLine[0], nextLine[1]);
				}
			} catch (IOException e) {
				System.out.println("Failed to read next line in SpinaCorpus!");
				e.printStackTrace(); 
			}
		} catch (FileNotFoundException e) {
			System.out.println("The given file does not exist!");
			e.printStackTrace();
		}
		
		this.corpus.setIndex(index);
	}
	
	protected void readGoldStandard(String fileName) {
		Map<String, List<String>> goldStandard = new HashMap<String, List<String>>();
		try {
			CSVReader reader = new CSVReader(new FileReader(fileName), ' ');
			String[] nextLine;
			try {
				while ((nextLine = reader.readNext()) != null) {
					if (nextLine.length == 4) {
						if (!goldStandard.containsKey(this.corpus.getIndex().get(nextLine[0]))) {
							goldStandard.put(nextLine[0], new LinkedList<String>());
						} 
						goldStandard.get(nextLine[0]).add(nextLine[2]);
					} else {
						System.out.println("Invalid Line: " + nextLine);
					}
				}
			} catch (IOException e) {
				System.out.println("Failed to read next line in SpinaCorpus!");
				e.printStackTrace(); 
			}
		} catch (FileNotFoundException e) {
			System.out.println("The given file does not exist!");
			e.printStackTrace();
		}
		
		
	}
	
	protected void readAnnotations(String fileName) {
		Map<String, Map<String, String[]>> annotations = new HashMap<String, Map<String, String[]>>();
		try {
			CSVReader reader = new CSVReader(new FileReader(fileName), '\t', '\"', 1);
			String[] nextLine;
			try {
				while ((nextLine = reader.readNext()) != null) {
					if (nextLine.length == 5) {
						if (!annotations.containsKey(nextLine[0])) {
							annotations.put(nextLine[0], new HashMap<String, String[]>());
						} 
						annotations.get(nextLine[0]).put(nextLine[1], new String[]{nextLine[2], nextLine[3], nextLine[4]});
					} else {
						System.out.println("Invalid Line: " + nextLine);
					}
				}
			} catch (IOException e) {
				System.out.println("Failed to read next line in SpinaCorpus!");
				e.printStackTrace(); 
			}
		} catch (FileNotFoundException e) {
			System.out.println("The given file does not exist!");
			e.printStackTrace();
		}
	}
}
