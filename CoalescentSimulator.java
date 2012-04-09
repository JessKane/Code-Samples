package btry4820.problemset2;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * The theory behind this simulator is that given a set of nodes, each one
 * will coalesce (one node merging with the other) at an exponentially 
 * distributed time. Therefore, given a set of nodes (or samples), this
 * simulator works backwards to construct a tree, eventually resulting in a 
 * single most recent common ancestor.
 * 
 * Trees are stored as arrays of string arrays, with each array containing 
 * a time since the last coalescent event and the list of nodes remaining. 
 * So, a tree with 3 nodes might look like this:
 * 
 *  0	1	2	3	
 *  0.6482022762398866	23	1	
 *  0.6377970883211118	123
 * 
 * @author Jessica Kane
 */

public class CoalescentSimulator {

	/**
	 * Recursive factorial function. Makes the math in the main function look simpler.
	 */
	public static double factorial(int n) {
		if(n<=1)     // base case
			return 1;
		else
			return n*factorial(n-1);
	}

	/**
	 * Loads genealogy from a file.
	 * 
	 * @throws FileNotFoundException 
	 */
	public static String[][] load(String filename) {
		ArrayList<String[]> geneologyList = new ArrayList<String[]>();
		try {
			Scanner scanner = new Scanner(new FileInputStream(filename));
			while (scanner.hasNextLine()) {
				String gen = scanner.nextLine();
				String values[] = gen.split("\t");
				geneologyList.add(values);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String[][] geneListArray = Arrays.copyOf(geneologyList.toArray(), 
				geneologyList.size(), String[][].class);
		String geneology[][] = geneListArray;
		return geneology;
	}

	/**
	 * Saves genealogy to a file.
	 * 
	 * @param filename 
	 * @param geneology
	 */
	public static void save(String geneology[][], String filename) {
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			for (String[] gens: geneology) {
				if (gens != null) {
					for (String value: gens) {
						out.write (value + "\t");
					}
					out.write("\n");
				}
			}	    	
			out.close();
			fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}

	/**
	 * Prints a genealogy to console. 
	 */
	public static void print(String geneology[][]) {
		for (String[] gens: geneology) {
			if (gens != null) {
				for (String value: gens) {
					System.out.print (value + "\t");
				}
			}
			System.out.print("\n");
		}	    	
	}
	
	/**
	 * Find the time of most recent common ancestor (TMRCA)
	 */
	public static double TMRCA(String[][] tree) {
		double sum = 0;
		for (String[] generation: tree) {
			if (generation != null) {
				sum += Double.parseDouble(generation[0]);
			}
		}
		return sum;
	}
	

	/**
	 * Simulates a gene genealogy using a coalescent-based approach.
	 */
	public static String[][] simulate(int sampleSize) {
		int nodeNum = sampleSize;  	
		String[][] genealogy = new String[nodeNum][];

		// set up geneology so that the first item in the array is the time, and the 
		// others are simply a number representing a node
		genealogy[0] = new String[nodeNum + 1];
		genealogy[0][0] = "0";    // represents the time
		for (int i=1; i<nodeNum+1; i++) {
			genealogy[0][i] = "" + i;
		}
		double distParam = factorial(nodeNum)/(factorial(nodeNum-2)*2);

		int genCounter = 0;
		for (int i=nodeNum; i>1; i--) {
			// pick a random time (distributed exponentially)
			double time = Math.log((1-Math.random())/distParam)/(-distParam);

			// pick two random nodes to coalesce
			int node1 = 1 + (int)(Math.random()*((i-1)+1));
			int node2 = node1;
			while(node1 == node2) {
				node2 = 1 + (int)(Math.random()*((i-1)+1));
			}

			// record this generation's information
			ArrayList<String> newRecord = new ArrayList<String>();
			newRecord.add("" + time);
			newRecord.add(genealogy[genCounter][node1] + genealogy[genCounter][node2]);
			// coalesced nodes are stored as the node numbers concatenated together,
			// for ease of tracking
			for (int j=1; j<i+1; j++) {
				if ((j != node1) && (j != node2)) {
					newRecord.add(genealogy[genCounter][j]); 
				}
			}

			String[] arrayRecord = Arrays.copyOf(newRecord.toArray(), 
					newRecord.size(), String[].class);
			genealogy[genCounter+1] = arrayRecord; 

			distParam = factorial(i+1)/(factorial(i-1)*2);
			genCounter ++;	
		}		    

		return genealogy;  	
	}

	/**
	 * Main method, run simulations from here. 
	 */
	public static void main(String[] args) {
		// Checking that saving and loading works
		String[][] tree = simulate(3);
		print(tree);
		save(tree, "A1");
     	String[][] treeReLoaded = load("A1");
		print(treeReLoaded);  // print another copy of the same tree

		// Calculate average TMRCA over 20 simulations
		double[] TMRCAs = new double[20];
		double TMRCAAvg = 0;
		for (int i=0; i<20; i++) {
			String[][] newTree = simulate (10);
			double TMRCA = TMRCA(newTree);
			TMRCAs[i] = TMRCA;
			TMRCAAvg += TMRCA;
		}
		
		TMRCAAvg = TMRCAAvg/TMRCAs.length;
		System.out.println(TMRCAAvg);

		// How fast can I process a sample size of 100?
		System.out.println("start");
		simulate(100);
		System.out.println("done");
		
	}
}
