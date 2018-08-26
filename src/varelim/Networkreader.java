package varelim;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Class that reads in a network and puts the variables and probabilities at the
 * right places.
 * 
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 */

public class Networkreader {

	private ArrayList<Variable> Vs = new ArrayList<Variable>();
	private ArrayList<Table> Ps = new ArrayList<Table>();
	private ArrayList<ProbRow> probRows;
	private String varName;
	private String probName;
	private ArrayList<Variable> parents = new ArrayList<Variable>();
	private Variable query = null;
	private ArrayList<Variable> obs = new ArrayList<Variable>();
	private String line;
	private int nrOfRows;
	private String heuristic;
	private Scanner scan;

	/**
	 * Constructor reads in the data file and adds the variables and its
	 * probabilities to the designated arrayLists.
	 * 
	 * @param file, the name of the .bif file that contains the network.
	 */
	public Networkreader(String file) {
		BufferedReader br = null;
		try {
			String cur; // Keeping track of current line observed by BufferedReader
			br = new BufferedReader(new FileReader(file)); 
			try {
				while ((cur = br.readLine()) != null) { 
					if (cur.contains("variable")) { 
						//Add variable to the list
						varName = cur.substring(9, cur.length() - 2);
						cur = br.readLine();
						ArrayList<String> values = searchForValues(cur);
						Vs.add(new Variable(varName, values));
					}
					if (cur.contains("{")) { 
						parents = new ArrayList<Variable>();
					}
					if (cur.contains("probability")) { 
						// Conditional to check for parents of selected variable
						searchForParents(cur);
					}
					if (cur.contains("table")) { 
						//Conditional to find probabilities of 1 row and add Probabilities to
						// probability list
						ArrayList<ProbRow> currentProbRows = searchForProbs(cur);
						for(ProbRow p : currentProbRows) {
							probRows.add(p);
						}
						Table table = new Table(probRows, getByName(probName), parents);
						Ps.add(table);
					}
					if (cur.contains(")") && cur.contains("(") && !cur.contains("prob")) {
						// Conditional to find probabilities of more than 1 row;
						// add probabilities to probability list
						ArrayList<ProbRow> currentProbRows = searchForProbs(cur);
						for(ProbRow p : currentProbRows) {
							probRows.add(p);
						}
						if (probRows.size() == nrOfRows) {
							Table table = new Table(probRows, getByName(probName), parents);
							Ps.add(table);
						}
					}
				}
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
			System.out.println("This file does not exist.");
			System.exit(0);
		}
	}

	/**
	 * Searches for a row of probabilities in a string
	 * 
	 * @param a string s
	 * @return a ProbRow
	 */
	public ArrayList<ProbRow> searchForProbs(String s) {
		Variable node = null;
		ArrayList<ProbRow> currentProbRows = new ArrayList<ProbRow>();
		int beginIndex = s.indexOf(')') + 2;
		if (s.contains("table")) {
			beginIndex = s.indexOf('e') + 2;
		}

		int endIndex = s.length() - 1;
		String subString = s.substring(beginIndex, endIndex);
		String[] probsString = subString.split(", ");
		double[] probs = new double[probsString.length];
		for (int i = 0; i < probsString.length; i++) {
			probs[i] = Double.parseDouble(probsString[i]);
		}

		if (!s.contains("table")) {
			ArrayList<String> parentsValues = new ArrayList<String>();
			ArrayList<String> nodeValues = new ArrayList<String>();
			beginIndex = s.indexOf('(') + 1;
			endIndex = s.indexOf(')');
			subString = s.substring(beginIndex, endIndex);
			String[] stringValues = subString.split(", ");
			for(String value : stringValues) {
				parentsValues.add(value);
			}
			for(Variable v : Vs) {
				if(probName.equals(v.getName())) {
					node = v;
					nodeValues = v.getValues();
				}
			}
			for(int i=0; i<probs.length; i++) {
				parentsValues.add((String) nodeValues.get(i));
				ArrayList<String> currentVal = new ArrayList<String>(parentsValues);
				currentProbRows.add(new ProbRow(node, probs[i], currentVal, parents));
				parentsValues.remove(parentsValues.size()-1);
			}
		}
		else {
			ArrayList<String> values = new ArrayList<String>();
			ArrayList<String> nodeValues = new ArrayList<String>();
			for(Variable v : Vs) {
				if(probName.equals(v.getName())) {
					node = v;
					values = v.getValues();
				}
			}
			for(int i=0; i<probs.length; i++) {
				nodeValues.add(values.get(i));
				ArrayList<String> currentVal = new ArrayList<String>(nodeValues);
				ProbRow prob = new ProbRow(node, probs[i], currentVal, parents);
				currentProbRows.add(prob);
				nodeValues.clear();
			}
		}

		return currentProbRows;
	}

	/**
	 * Searches for values in a string
	 * 
	 * @param a string s
	 * @return a list of values
	 */
	public ArrayList<String> searchForValues(String s) {
		int beginIndex = s.indexOf('{') + 2;
		int endIndex = s.length() - 3;
		String subString = s.substring(beginIndex, endIndex);
		String[] valueArray = subString.split(", ");
		return new ArrayList<String>(Arrays.asList(valueArray));
	}

	/**
	 * Method to check parents of chosen variable.
	 * 
	 * @param cur, which gives the current line.
	 */
	public void searchForParents(String cur) {
		if (cur.contains("|")) { // Variable has parents
			extractParents(cur);
		} else { // Variable has no parents
			probName = cur.substring(14, cur.length() - 4);
			for(Variable v : Vs) {
				if(probName.equals(v.getName())) {
					nrOfRows = v.getNumberOfValues();
				}
			}
		}
		probRows = new ArrayList<ProbRow>();
	}

	/**
	 * Gets a variable from variable Vs when the name is given
	 * 
	 * @param name
	 * @return variable with name as name
	 */
	private Variable getByName(String name) {
		Variable var = null;
		for (int i = 0; i < Vs.size(); i++) {
			if (Vs.get(i).getName().equals(name))
				var = Vs.get(i);
		}
		return var;
	}

	/**
	 * Extracts parents and puts them in a list of parents of that node.
	 * 
	 * @param cur, a string to extract from
	 */
	public void extractParents(String cur) {
		probName = cur.substring(14, cur.indexOf("|") - 1);
		Variable var = getByName(probName);
		String sub = cur.substring(cur.indexOf('|') + 2, cur.indexOf(')') - 1);
		while (sub.contains(",")) { // Variable has more parents
			String current = sub.substring(0, sub.indexOf(','));
			sub = sub.substring(sub.indexOf(',') + 2);
			for (int i = 0; i < Vs.size(); i++) {
				if (Vs.get(i).getName().equals(current)) {
					parents.add(Vs.get(i)); // Add parent to list
				}
			}
		}
		if (!sub.contains(",")) { // Variable has no more parents
			for (int i = 0; i < Vs.size(); i++) {
				if (Vs.get(i).getName().equals(sub)) {
					parents.add(Vs.get(i)); //
				}
			}
		}

		var.setParents(parents);
		nrOfRows = computeNrOfRows(probName);

	}

	/**
	 * Computes the number of rows needed given the current parents
	 * 
	 * @return the number of rows
	 */
	private int computeNrOfRows(String probName) {
		int fac = 1;
		for (int i = 0; i < parents.size(); i++) {
			fac = fac * parents.get(i).getNumberOfValues();
		}
		for(Variable v : Vs) {
			if(probName.equals(v.getName())) {
				fac = fac * v.getNumberOfValues();
			}
		}
		return fac;
	}

	/**
	 * Asks for a query from the user.
	 */
	public void askForQuery() {
		System.out.println("\nWhich variable(s) do you want to query? Please enter in the number of the variable.");
		for (int i = 0; i < Vs.size(); i++) {
			System.out.println("Variable " + i + ": " + Vs.get(i).getName());
		}
		scan = new Scanner(System.in);
		line = scan.nextLine();
		if (line.isEmpty()) {
			System.out.println("You have not chosen a query value. Please choose a query value.");
			askForQuery();
		}
		try {
			int queriedVar = Integer.parseInt(line);
			if (queriedVar >= 0 && queriedVar < Vs.size()) {
				query = Vs.get(queriedVar);
			} else {
				System.out.println("This is not a correct index. Please choose an index between " + 0 + " and "
						+ (Vs.size() - 1) + ".");
				askForQuery();
			}
		} catch (NumberFormatException ex) {
			System.out.println("This is not a correct index. Please choose an index between " + 0 + " and "
					+ (Vs.size() - 1) + ".");
			askForQuery();
		}
	}

	/**
	 * Ask the user for observed variables in the network.
	 */
	public void askForObservedVariables() {

		obs.clear();
		System.out.println("Which variable(s) do you want to observe? Please enter in the number of the variable, \n"
				+ "followed by a comma and the value of the observed variable. Do not use spaces. \n"
				+ "If you want to query multiple variables, delimit them with a ';' and no spaces.\n"
				+ "Example: '2,True;3,False'");
		for (int i = 0; i < Vs.size(); i++) {
			String values = "";
			for (int j = 0; j < Vs.get(i).getNumberOfValues() - 1; j++) {
				values = values + Vs.get(i).getValues().get(j) + ", ";
			}
			values = values + Vs.get(i).getValues().get(Vs.get(i).getNumberOfValues() - 1);

			System.out.println("Variable " + i + ": " + Vs.get(i).getName() + " - " + values);
		}
		scan = new Scanner(System.in);
		line = scan.nextLine();
		if (line.isEmpty()) {
		} else {
			if (!line.contains(",")) {
				System.out.println("You did not enter a comma between values. Please try again");
				askForObservedVariables();
				return;
			} else {
				while (line.contains(";")) { // Multiple observed variables
					try {
						int queriedVar = Integer.parseInt(line.substring(0, line.indexOf(",")));
						String bool = line.substring(line.indexOf(",") + 1, line.indexOf(";"));
						changeVariableToObserved(queriedVar, bool);
						line = line.substring(line.indexOf(";") + 1); // Continue
																		// with
																		// next
																		// observed
																		// variable.
					} catch (NumberFormatException ex) {
						System.out.println("This is not a correct input. Please choose an index between " + 0 + " and "
								+ (Vs.size() - 1) + ".");
						askForObservedVariables();
						return;
					}
				}
				if (!line.contains(";")) { // Only one observed variable
					try {

						int queriedVar = Integer.parseInt(line.substring(0, line.indexOf(",")));
						String bool = line.substring(line.indexOf(",") + 1);
						changeVariableToObserved(queriedVar, bool);
					} catch (NumberFormatException ex) {
						System.out.println("This is not a correct input. Please choose an index between " + 0 + " and "
								+ (Vs.size() - 1) + ".");
						askForObservedVariables();
					}
				}
			}
		}
	}

	/**
	 * Checks whether a number and value represent a valid observed value or not and if so, adds it to the
	 * observed list. If not, asks again for new input.
	 */
	public void changeVariableToObserved(int queriedVar, String value) {
		Variable ob;
		if (queriedVar >= 0 && queriedVar < Vs.size()) {
			ob = Vs.get(queriedVar);
			if (ob.isValueOf(value)) {
				ob.setValue(value);
				ob.setObserved(true);
			}

			else {
				System.out.println("Apparently you did not fill in the value correctly. You typed: \"" + value
						+ "\"Please try again");
				askForObservedVariables();
				return;
			}
			obs.add(ob); // Adding observed variable and it's value to list.
		} else {
			System.out.println("You have chosen an incorrect index. Please choose an index between " + 0 + " and "
					+ (Vs.size() - 1));
			askForObservedVariables();
		}
	}

	/**
	 * Print the network that was read-in (by printing the variables, parents and probabilities).
	 * 
	 * @param The list of variables.
	 * @param The list of probabilities.
	 */
	public void printNetwork(ArrayList<Variable> Vs, ArrayList<Table> Ps) {
		System.out.println("The variables:");
		for (int i = 0; i < Vs.size(); i++) {
			String values = "";
			for (int j = 0; j < Vs.get(i).getNumberOfValues() - 1; j++) {
				values = values + Vs.get(i).getValues().get(j) + ", ";
			}
			values = values + Vs.get(i).getValues().get(Vs.get(i).getNumberOfValues() - 1);
			System.out.println((i + 1) + ") " + Vs.get(i).getName() + " - " + values); // Printing
																						// the
																						// variables.
		}
		System.out.println("\nThe probabilities:");
		for (int i = 0; i < Ps.size(); i++) {
			if (Vs.get(i).getNrOfParents() != 0) {
				for (int j = 0; j < Vs.get(i).getParents().size(); j++) {
					System.out.println(Ps.get(i).get(0).getNode().getName() + " has parent "
							+ Vs.get(i).getParents().get(j).getName()); // Printing
																		// the
																		// probabilities.
				}
			} else {
				System.out.println(Ps.get(i).get(0).getNode().getName() + " has no parents.");
			}

			Table probs = Ps.get(i);
			for (int l = 0; l < probs.size(); l++) {
				System.out.println(probs.get(l));
			}
			System.out.println();
		}
	}

	/**
	 * Prints the query and observed variables given in by the user.
	 * 
	 * @param The query variable(s).
	 * @param The observed variable(s).
	 */
	public void printQueryAndObserved(Variable query, ArrayList<Variable> Obs) {
		System.out.println("\nThe queried variable(s) is/are: "); // Printing
																	// the
																	// queried
																	// variables.
		System.out.println(query.getName());
		if (!Obs.isEmpty()) {
			System.out.println("The observed variable(s) is/are: "); // Printing
																		// the
																		// observed
																		// variables.
			for (int m = 0; m < Obs.size(); m++) {
				System.out.println(Obs.get(m).getName());
				System.out.println("This variable has the value: " + Obs.get(m).getValue());
			}
		}
	}
	
	/**
	 * Asks for a heuristic
	 */
	public void askForHeuristic() {
		System.out.println("Supply a heuristic. Input 1 for least-incoming, 2 for fewest-factors and enter for random");
		scan = new Scanner(System.in);
		line = scan.nextLine();
		if (line.isEmpty()) {
			heuristic = "empty";
			System.out.println("You have chosen for random");
		} else if (line.equals("1")) {
			heuristic = "least-incoming";
			System.out.println("You have chosen for least-incoming");
		} else if (line.equals("2")) {
			heuristic = "fewest-factors";
			System.out.println("You have chosen for fewest-factors");
		} else {
			System.out.println(line + " is not an option. Please try again");
			askForHeuristic();
		}
		scan.close();
	}

	/**
	 * Getter of the observed variables.
	 * 
	 * @return a list of observed variables given by the user.
	 */
	public ArrayList<Variable> getObservedVariables() {
		return obs;
	}

	/**
	 * Getter of the queried variables.
	 * 
	 * @return the variable the user wants to query.
	 */
	public Variable getQueriedVariable() {
		return query;
	}

	/**
	 * Getter of the variables in the network.
	 * 
	 * @return the list of variables in the network.
	 */
	public ArrayList<Variable> getVs() {
		return Vs;
	}

	/**
	 * Getter of the probabilities in the network.
	 * 
	 * @return the list of probabilities in the network.
	 */
	public ArrayList<Table> getPs() {
		return Ps;
	}

	/**
	 * Getter of the heuristic.
	 * 
	 * @return the name of the heuristic.
	 */
	public String getHeuristic() {
		return heuristic;
	}
}