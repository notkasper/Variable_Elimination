package varelim;

import java.util.ArrayList;
/**
 * Represents a row of probabilities
 * 
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 */
public class ProbRow {
	private double prob;
	private ArrayList<String> values;
	private Variable node;
	private ArrayList<Variable> parents;

	/**
	 * Constructor
	 * @param node a variable 
	 * @param prob probability belonging to this row of values of the node
	 * @param values values of all the variables (node+parents) in the row, of which the value of the node itself is always last
	 * @param parents the parent variables
	 */
	public ProbRow(Variable node, double prob, ArrayList<String> values, ArrayList<Variable> parents) {
		this.prob = prob;
		this.values = values;
		this.node = node;
		this.parents = parents;
	}
	
	public ProbRow(ProbRow row) {
		this.prob = row.getProb();
		this.values = new ArrayList<>(row.getValues());
		this.node = row.getNode();
		this.parents = new ArrayList<>(row.getParents());
	}

	/**
	 * Check whether two have ProbRows have the same parents
	 * @param pr a probability row
	 * @return True if this probRow and pr have the same parents
	 */
	public boolean sameParentsValues(ProbRow pr) {
		return this.values.equals(pr.values);
	}

	/**
	 * Getter of the probability
	 * @return the probability of the node.
	 */
	public double getProb() {
		return prob;
	}
	
	public void setProb(double prob) {
		this.prob = prob;
	}

	/**
	 * Getter of the node.
	 * @return node given the probabilities.
	 */
	public Variable getNode() {
		return node;
	}
	
	/**
	 * Getter of the values.
	 * @return ArrayList<String> of values of the probability row
	 */
	public ArrayList<String> getValues() {
		return values;
	}
	
	/**
	 * Transform probabilities to string.
	 */
	public String toString() {
		String valuesString = "";
		for(int i = 0; i < values.size()-1; i++){
			valuesString = valuesString + values.get(i) + ", ";
		}
		valuesString = valuesString + values.get(values.size()-1);
		return valuesString + " | " + Double.toString(prob);
	}
	
	/**
	 * Getter of the parents.
	 * @return the parents of the node given the probabilities.
	 */
	public ArrayList<Variable> getParents(){
		return parents;
	}
}