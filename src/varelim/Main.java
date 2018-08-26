package varelim;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Main class to read in a network, add queries and observed variables, and
 * eliminate variables.
 * 
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali,
 *         Leonieke van den Bulk
 */

public class Main {
	private final static String networkName = "cancer.bif"; // The network to be read in (format from
																// http://www.bnlearn.com/bnrepository/)

	public static void main(String[] args) {

		// Read in the network
		Networkreader reader = new Networkreader(networkName);

		// Get the variables and probabilities of the network
		ArrayList<Variable> variables = reader.getVs();
		ArrayList<Table> Ps = reader.getPs();

		// Print variables and probabilities
		reader.printNetwork(variables, Ps);

		// Ask user for query and heuristic
		reader.askForQuery();

		// Turn this on if you want to experiment with different heuristics for bonus
		// points (you need to implement the heuristics yourself)
		// reader.askForHeuristic();
		// String heuristic = reader.getHeuristic();

		Variable queryVariable = reader.getQueriedVariable();

		// Ask user for observed variables
		reader.askForObservedVariables();
		ArrayList<Variable> observedVariables = reader.getObservedVariables();

		// Print the query and observed variables
		reader.printQueryAndObserved(queryVariable, observedVariables);

		// PUT YOUR CODE FOR THE VARIABLE ELIMINATION ALGORITHM HERE
		System.out.println("-----------------------");
		// turn tables into factors
		ArrayList<Factor> factors = new ArrayList<>();
		for (int i = 0; i < Ps.size(); i++) {
			factors.add(new Factor(Ps.get(i), "Factor#" + i));
		}
		System.out.println("remove false notions of observed variables in probability tables");
		removeContradictoryNotions(observedVariables, factors);
		// print result
		// printFactors(factors);
		// create elimination order
		System.out.println("create elimination ordering");
		ArrayList<Variable> order = fillOrder(variables, queryVariable);
		// remove query variable from elimination order
		// eliminate with elimination order
		for (Variable elimVar : order) {
			System.out.println("Going to eliminate: " + elimVar.getName());
			// get the factor involved for this elimination
			ArrayList<Factor> involvedFactors = new ArrayList<>();
			for (Factor factor : factors) {
				ArrayList<Variable> involvedVariables = factor.getNodes();
				if (involvedVariables.contains(elimVar)) {
					involvedFactors.add(factor);
				}
			}
			System.out.println("amount of factors involved: " + involvedFactors.size());
			// multiply factors
			Factor newFactor = involvedFactors.remove(0);
			factors.remove(newFactor);
			while (involvedFactors.size() > 0) {
				Factor otherFactor = involvedFactors.remove(0);
				factors.remove(otherFactor);
				newFactor = multiplyFactors(otherFactor, newFactor, elimVar);
			}
			factors.add(newFactor);
			System.out.println();
			System.out.println("---------------------------------- \n FACTOR BEFORE SUMMING OUT ELIMVAR");
			System.out.println(newFactor);
			sumOutVariable(newFactor, elimVar);
			System.out.println("\n FACTOR AFTER\n");
			System.out.println(newFactor);
			System.out.println("----------------------------------");
		}
		Factor result = factors.remove(0);
		while (factors.size() > 0) {
			result = multiplyFactors(result, factors.remove(0), queryVariable);
		}
		factors.add(result);
		normalizeFactor(result);
		System.out.println("\n RESULT:");
		printFactors(factors);
	}
	
	/**
	 * make sure the probabilities of a factor add up to 1
	 * @param factor
	 */
	private static void normalizeFactor(Factor factor) {
		double total = 0.0;
		for (ProbRow row : factor.getTable()) {
			total += row.getProb();
		}
		for (ProbRow row : factor.getTable()) {
			double oldProb = row.getProb();
			double newProb = oldProb / total * 100;
			row.setProb(newProb);
		}
	}

	/**
	 * Create an elimination order
	 * @param variables all variables
	 * @param queryVariable
	 * @return ArrayList of variables
	 */
	private static ArrayList<Variable> fillOrder(ArrayList<Variable> variables, Variable queryVariable) {
		final ArrayList<Variable> order = new ArrayList<>(variables);
		order.sort(new Comparator<Variable>() {

			@Override
			public int compare(Variable o1, Variable o2) {
				int descendants1 = 0;
				int descendants2 = 0;
				for (Variable var : order) {
					if (var.getParents().contains(o1)) {
						descendants1++;
					} else if (var.getParents().contains(o2)) {
						descendants2++;
					}
				}
				int parents1 = o1.getNrOfParents();
				int parents2 = o2.getNrOfParents();
				if (descendants1 == 0) {
					return -1;
				} else if (descendants2 == 0) {
					return 1;
				} else if (parents1 == parents2) {
					return 0;
				} else {
					return parents1 > parents2 ? 1 : -1;
				}
			}
		});
		order.remove(queryVariable);
		System.out.println(" ORDER: ");
		for (Variable var : order) {
			System.out.print(var.getName() + ", ");
		}
		System.out.println();
		return order;
	}

	/**
	 * multiply two factors
	 * @param f1 a factor
	 * @param f2 a factor
	 * @param elimVar the elimination variable
	 * @return the result of the factor multiplication
	 */
	private static Factor multiplyFactors(Factor f1, Factor f2, Variable elimVar) {
		// put smallest factor first
		if (f1.getNodes().size() > f2.getNodes().size()) {
			Factor temp = f2;
			f2 = f1;
			f1 = temp;
		}
		System.out.println("\n START OF MULTIPLICATION");
		System.out.println("\n" + f1 + "\n" + "\n *");
		System.out.println(f2);
		// combine all variable
		ArrayList<Variable> combined = new ArrayList<>(f1.getNodes());
		combined.addAll(f2.getNodes());
		// get unique variables
		ArrayList<Variable> uniques = new ArrayList<>();
		for (Variable var : combined) {
			boolean found = false;
			for (Variable unique : uniques) {
				if (unique.getName().equals(var.getName())) {
					found = true;
				}
			}
			if (!found) {
				uniques.add(var);
			}
		}
		// multiply probability rows with equal value for the elimination variable
		for (int j = 0; j < f1.getTable().size(); j ++) {
			ProbRow row1 = f1.getTable().get(j);
			String varName = elimVar.getName();
			// get index of the elimination variable so we can check its value
			int indexOf1 = getIndexOfVariableName(f1.getNodes(), varName);
			int indexOf2 = getIndexOfVariableName(f2.getNodes(), varName);
			// the value of the elimination variable in the first factor
			String value1 = row1.getValues().get(indexOf1);
			for (int i = 0; i < f2.getTable().size(); i++) {
				ProbRow row2 = f2.getTable().get(i);
				// the value of the elimination variable in the second factor
				String value2 = row2.getValues().get(indexOf2);
				// if the values are the same, we should multiply the probabilities
				if (value1.equals(value2)) {
					double prob1 = row1.getProb();
					double prob2 = row2.getProb();
					double newProb = prob1 * prob2;
					row2.setProb(newProb);
				}
			}
		}
		System.out.println("\n RESULT OF MULTIPLICATION");
		System.out.println(f2);
		return f2;
	}
	
	private static int getIndexOfVariableName(ArrayList<Variable> variables, String varName) {
		int index = -1;
		for (int i = 0; i < variables.size(); i++) {
			Variable var = variables.get(i);
			if (var.getName().equals(varName)) {
				index = i;
			}
		}
		return index;
	}

	private static void sumOutVariable(Factor factor, Variable var) {
		// find index of the elimination variable
		int indexOf = factor.getNodes().indexOf(var);
		factor.getNodes().remove(indexOf);
		// remove the elimination variable from each probability row
		for (ProbRow row : factor.getTable()) {
			row.getValues().remove(indexOf);
		}
		// because we cannot remove rows in the loop, we keep track of which ones have already been used in the ignore list
		ArrayList<ProbRow> ignore = new ArrayList<>();
		for (ProbRow row1 : factor.getTable()) {
			if (!ignore.contains(row1)) {
				ArrayList<String> values = row1.getValues();
				// check for any matching values, and add them together
				for (ProbRow row2 : factor.getTable()) {
						ArrayList<String> values2 = row2.getValues();
						if (values.equals(values2) && row2 != row1) {
							row1.setProb(row1.getProb() + row2.getProb());
							ignore.add(row2);
					}
				}
			}
		}
		// remove all the rows in the ignore list, this leaves just the "new" probability rows in the factor
		for (ProbRow row : ignore) {
			factor.getTable().remove(row);
		}
	}

	private static void printFactors(ArrayList<Factor> factors) {
		for (Factor factor : factors) {
			System.out.println(factor + "\n");
		}
	}

	private static void removeContradictoryNotions(ArrayList<Variable> observedVariables,
			ArrayList<Factor> probabilityTables) {
		for (Variable observedVariable : observedVariables) {
			// loop through all tables
			for (Factor factor : probabilityTables) {
				// remove all contradictory notions of the observed variable
				if (factor.getNodes().contains(observedVariable)) {
					ArrayList<ProbRow> rows = factor.getTable();
					// keep a list of ProbRow objects so we can remove them later
					ArrayList<ProbRow> rowsToRemove = new ArrayList<>();
					ArrayList<Variable> involvedVariables = factor.getNodes();
					// for each row check if it contains a contradictory value
					for (ProbRow row : rows) {
						int indexOf = involvedVariables.indexOf(observedVariable);
						String observedValue = observedVariable.getValue();
						if (!row.getValues().get(indexOf).equals(observedValue)) {
							rowsToRemove.add(row);
						}
					}
					// remove all rows that contains contradictory observed values
					for (ProbRow row : rowsToRemove) {
						rows.remove(row);
					}
				}
			}
		}
	}

}
