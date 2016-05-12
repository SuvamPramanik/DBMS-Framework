package com.socket;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class SimpleSearch {
	public File writeQuery = new File(
			"C:/Users/spramanik/Desktop/DDBMS-Framework/queryHistory.txt");

	public int searchIndex(String queryStr) throws IOException {
		int count = 0;
		try {
			LineIterator it = FileUtils.lineIterator(writeQuery);
			while (it.hasNext()) {
				String line = it.nextLine().toString();
				if (line.equalsIgnoreCase(queryStr)) {
					count++;
				}
			}
			if (count == 0) {
				System.out.println(queryStr + " is not found!!");
			} else {
				System.out.println(queryStr + " is found " + count
						+ " times!!");
			}

		} catch (IOException e) {
			System.out.println("Sorry, unable to open the file!!!");
		}
		return count;
		
	}
	
	public static void main(String args[]) throws IOException {
		long startTime = System.nanoTime();
		String query = "select * from sampleddb.demo";
		SimpleSearch srch = new SimpleSearch();
		srch.searchIndex(query);
		long stopTime = System.nanoTime();
		float timeElapsed=(float)(stopTime-startTime)/1000000000;
		System.out.println("The Execution time is : " + (stopTime - startTime)
				+ " nanoseconds (= "+ timeElapsed+" sec).");
	}
}
