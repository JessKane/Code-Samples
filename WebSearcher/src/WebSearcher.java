package Homework3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Jama.Matrix;

public class WebSearcher {

	ArrayList<String> testDataLinks = null;
	Matrix linkMatrix = null;
	Matrix pageRankVector = null;
	HashMap<String, ArrayList<String>> anchorTextList = null;
	HashMap<String, HashMap<String, String>> index = null;
	HashMap<String, String> snippetList = null;

	public WebSearcher (String filename) {
		testDataLinks = new ArrayList<String>();
		anchorTextList = new HashMap<String, ArrayList<String>>();
		index = new HashMap<String, HashMap<String, String>>();
		snippetList = new HashMap<String, String>();

		// part 1 - extract hyperlinks
		this.setTestDataHyperlinks(filename);
		this.processTestDataHyperlinks();
		// part 2 - pagerank
		this.getPageRankVector();
		// part 3 - index
		// index partly constructed in part 1 to avoid making more JSoup calls
		this.constructIndexAndMetaData();

	}

	// ------------------- Extracting Hyperlinks -------------------

	/**
	 * Read in data from the original list of hyperlinks
	 * 
	 * @param filename
	 */
	public void setTestDataHyperlinks (String filename) {
		testDataLinks.clear();
		try {
			Scanner scanner = new Scanner(new FileInputStream(filename));
			while (scanner.hasNextLine()) {
				String[] linksAndIds = scanner.nextLine().split(",");
				if (linksAndIds.length > 1) {
					testDataLinks.add(linksAndIds[1]);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a list of all links in each of the pages listed in the original list of hyperlinks. Also, starts
	 * the index for part 3, and gets snippets for part 4, so that the page doesn't need to be re-opened 
	 * (it's a big bottleneck).
	 */
	public void processTestDataHyperlinks () {
		index.clear();

		// get list of links in pages
		ArrayList<String[]> outgoingLinks = new ArrayList<String[]>();

		ArrayList<String> blacklist = new ArrayList<String>();

		for (String link: testDataLinks) {			
			Document doc = null;

			try {
				doc = Jsoup.connect(link)
						.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
						.get();
			} catch (IOException e) {
				System.out.println("JSoup could not resolve hyperlink: " + link + " . ");
				blacklist.add(link);
			}


			if (doc != null) {
				// get text
				Elements pageLinks = doc.select("a[href]");
				for (Element pageLink: pageLinks) {
					if ((testDataLinks.contains(pageLink.absUrl("href"))) && (pageLink.absUrl("href") != link)) {
						String[] linkData = {link, pageLink.absUrl("href"), pageLink.text()};
						outgoingLinks.add(linkData);
					}
				}

				// get title
				HashMap<String, String> pageData = new HashMap<String, String>();
				pageData.put("title", doc.title());
				index.put(link, pageData);

				// get snippit
				snippetList.put(link, doc.body().text().substring(0, 70));
			}
		}

		for (String link: blacklist) {
			testDataLinks.remove(link);
		}

		// populate matrix
		setLinkMatrix(outgoingLinks);

		// get anchor text list
		setAnchorTextList(outgoingLinks);
	}

	/**
	 * Set up the link matrix (just an incidence matrix)
	 * 
	 * @param outgoingLinks
	 */
	public void setLinkMatrix (ArrayList<String[]> outgoingLinks) {
		linkMatrix = new Matrix(testDataLinks.size(), testDataLinks.size());
		for (int i = 0; i < testDataLinks.size(); i++) {
			String link = testDataLinks.get(i);
			for (String[] outgoingLink: outgoingLinks) {
				if (link.equals(outgoingLink[0])) {
					int citingIndex = i;
					int citedIndex = testDataLinks.indexOf(outgoingLink[1]);
					linkMatrix.set(citedIndex, citingIndex, 1);     //doubles automatically ignored 
				}
			}
		}
	}

	/**
	 * Set up the anchor text map
	 * 
	 * @param outgoingLinks
	 */
	public void setAnchorTextList(ArrayList<String[]> outgoingLinks) {
		anchorTextList.clear();
		for (String[] outgoingLink: outgoingLinks) {
			if (anchorTextList.containsKey(outgoingLink[1])) {
				ArrayList<String> linkTexts = anchorTextList.get(outgoingLink[1]);
				linkTexts.add(outgoingLink[2]);
			} else {
				ArrayList<String> linkTexts = new ArrayList<String>();
				linkTexts.add(outgoingLink[2]);
				anchorTextList.put(outgoingLink[1], linkTexts);
			}
		}
	}

	// ------------------- Pagerank -------------------

	public void getPageRankVector() {
		normalizeLinkMatrix();

		pageRankVector = new Matrix(testDataLinks.size(), 1, 1.0);

		calculateConvergingVector(0.85);

	}

	/**
	 * Normalize the term matrix by the number of links in each page. Also accounts for dead ends.
	 */
	public void normalizeLinkMatrix() {
		for (int i= 0; i < linkMatrix.getColumnDimension(); i ++) {
			int linkCount = 0;

			for (int j= 0; j < linkMatrix.getRowDimension(); j ++) {
				if (linkMatrix.get(j, i) == 1) {
					linkCount ++;
				}
			}

			for (int j= 0; j < linkMatrix.getRowDimension(); j ++) {
				if (linkCount == 0) {
					linkMatrix.set(j, i, 1.0/linkMatrix.getRowDimension());
				} else if (linkMatrix.get(j, i) == 1) {
					linkMatrix.set(j, i, 1.0/linkCount);
				}
			}
		}
	}

	/**
	 * Algorithm: 		w[k] = dBw[k-1]+(1-d)z[0]
	 * 
	 * @param dampening
	 */
	public void calculateConvergingVector(double dampening) {
		// algorithm prep (these are constant, no need to recalculate them every time)
		Matrix dB = linkMatrix.times(dampening);
		Matrix z0 = new Matrix (testDataLinks.size(), 1, 1.0);
		Matrix d1z0 = z0.times(1-dampening);

		// iteration
		boolean converged = false;
		double convergenceFactor = 0.00001; // how close is close enough?
		int k = 0;

		while (!converged) {
			Matrix newPageRankVector = (dB.times(pageRankVector)).plus(d1z0);

			boolean same = true;
			for (int i = 0; i < testDataLinks.size(); i ++) {
				if (!((pageRankVector.get(i,0) + convergenceFactor  > newPageRankVector.get(i,0)) 
						&& (pageRankVector.get(i,0) - convergenceFactor  < newPageRankVector.get(i,0))) ) {
					same = false;
				}
			}

			if (same) {
				converged = true;
			} else if (k > 200) {
				converged = true; 		// a failsafe, to avoid infinite loops if the convergenceFactor is too small or there's a large oscillating pattern
			}

			pageRankVector = newPageRankVector;
			k++;
		}
	}

	// ------------------- Index -------------------

	public void constructIndexAndMetaData() {
		HashMap<String, HashMap<String, String>> metadataInfo = new HashMap<String, HashMap<String, String>>();

		for (int i = 0; i < testDataLinks.size(); i++) {

			// Anchor text
			ArrayList<String> anchorText = anchorTextList.get(testDataLinks.get(i));
			String fullText = "";
			ArrayList<String> words = new ArrayList<String>();
			if (anchorText != null) {
				for (String text: anchorText) {
					text = text.toLowerCase();
					fullText += " " + text;
					text = text.replaceAll("[^a-zA-Z\\d\\s]", "");
					String[] anchorWords = text.split(" "); 
					for (String word: anchorWords) {
						if (!words.contains(word)) {
							words.add(word);
						} 
					}
				}
			}

			String metadataWords = "";
			for (String word: words) {
				metadataWords += " " + word;
			}

			// Finish the index
			HashMap<String, String> indexMap = index.get(testDataLinks.get(i));
			indexMap.put("anchorText", fullText);

			// Get rest of metadata info
			HashMap<String, String> metadataEntry = new HashMap<String, String>();
			metadataEntry.put("anchorTextWords", metadataWords);
			metadataEntry.put("pageRank", Double.toString(pageRankVector.get(i, 0)));
			metadataEntry.put("id", Integer.toString(i));

			metadataInfo.put(testDataLinks.get(i), metadataEntry);

		}

		writeMetaDataFile(metadataInfo);
	}

	public void writeMetaDataFile(HashMap<String, HashMap<String, String>> metadataInfo) {
		try {
			FileWriter fstream = new FileWriter("metadata.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			for (String link: testDataLinks) {
				HashMap<String, String> metadataEntry = metadataInfo.get(link);
				out.write(metadataEntry.get("id") + ": " + link + "\n");
				out.write("PageRank: " + metadataEntry.get("pageRank") + "\n");
				out.write("Anchor Text Terms: " + metadataEntry.get("anchorTextWords") + "\n");
				out.write("\n");
			}	    	
			out.close();
			fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}

	// ------------------- Searching -------------------

	public void processQuery (String query) {
		query = query.toLowerCase();
		ArrayList<Integer> hits = new ArrayList<Integer>(); 
		for (int i = 0; i < testDataLinks.size(); i++) {
			HashMap<String,String> indexEntry = index.get(testDataLinks.get(i));
			if (indexEntry.get("title").toLowerCase().contains(query) || indexEntry.get("anchorText").contains(query)) {
				hits.add(i);
			}
		}

		if (hits.size() < 1) {
			System.out.println("Query did not find any results.");
		} else {
			TreeMap<Double, Integer> pageRankHits = orderByPageRank(hits);
			System.out.println("\n");
			for (Map.Entry<Double, Integer> pageRankEntry: pageRankHits.descendingMap().entrySet()) {
				System.out.println(index.get(testDataLinks.get(pageRankEntry.getValue())).get("title"));
				System.out.println(testDataLinks.get(pageRankEntry.getValue()));
				System.out.println("PageRank: " + pageRankEntry.getKey());
				System.out.println("Excerpt: " + snippetList.get(testDataLinks.get(pageRankEntry.getValue())));
				System.out.println("-------------------------------------");
			}
		}
	}


	public TreeMap<Double, Integer> orderByPageRank (ArrayList<Integer> hits) {
		TreeMap<Double, Integer> pageRankResults = new TreeMap<Double, Integer>();
		for (Integer id: hits) {
			pageRankResults.put(pageRankVector.get(id, 0), id);
		}

		return pageRankResults;
	}

	public static void main (String[] args) {
		String filename;
		if (args.length < 1) {
			filename = "data/test3.txt";
		} else {
			filename = args[0];
		}

		WebSearcher webSearcher = new WebSearcher(filename);

		// console input
		System.out.println("Enter a query here : ");
		boolean running = true;
		while (running) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String query = reader.readLine();
				if (query.equals("ZZZ")) {
					running = false;
				} else {
					webSearcher.processQuery(query);
					System.out.println("\nEnter a query here : ");
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

	}
}
