package com.socket;

import java.io.File;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class indexedSearch {
	static SimpleSearch srch = new SimpleSearch();

	public static void main(String[] args) throws Exception {

		File indexDir = new File("index/");
		String msg = "select * from sampleddb.demo";
		String query = EncodeDecode.encode(msg);

		long startTime = System.nanoTime();
		indexedSearch searcher = new indexedSearch();
		searcher.searchIndex(indexDir, query);
		long stopTime = System.nanoTime();
		float timeElapsed = (float) (stopTime - startTime) / 1000000000;
		System.out.println("The Execution time is : " + (stopTime - startTime)
				+ " nanoseconds (= " + timeElapsed + " sec).");

	}
	//public int

	public int searchIndex(File indexDir, String queryStr) throws Exception {

		// System.out.println("Query is :" + queryStr);
		// if (indexDir.isDirectory()) {
		// if (indexDir.list().length > 0) {
		
		SimplerFileIndexer index = new SimplerFileIndexer();
		index.call();
		// }
		// }
		Directory directory = FSDirectory.open(indexDir);
		IndexSearcher searcher = new IndexSearcher(directory);
		QueryParser parser = new QueryParser(Version.LUCENE_30, "contents",
				new WhitespaceAnalyzer());
		Query query = parser.parse(QueryParser.escape(queryStr));
		IndexReader ir = IndexReader.open(directory);
		int count = 0;
		// System.out.println("Query after parsing is : " + query);
		TermDocs termDocs = ir.termDocs(new Term("contents", queryStr));

		while (termDocs.next()) {
			count += termDocs.freq();
		}
		TopDocs topDocs = searcher.search(query, 100000);
		System.out.println("The query: \"" + EncodeDecode.decode(queryStr)
				+ "\" has been found " + count + " times.");
		return count;
	}

}