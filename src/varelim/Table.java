package varelim;

import java.util.ArrayList;

/**
 * Class to represent the Table Object consisting of probability rows
 * 
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 */

public class Table {

	private ArrayList<ProbRow> table;
	private Variable node;
	private ArrayList<Variable> parents;
	
	/**
	 * Constructor of the class.
	 * @param table made out of probability rows (ProbRows)
	 * @param node belonging to the current probability table
	 * @param parents belonging to the current probability table
	 */
	public Table(ArrayList<ProbRow> table, Variable node, ArrayList<Variable> parents) {
		this.table = table;
		this.node = node;
		this.parents = parents;
	}
	
	public Table(Table table) {
		this.table = new ArrayList<>();
		for (ProbRow row : table.getTable()) {
			this.table.add(new ProbRow(row));
		}
		this.node = new Variable(table.getNode());
		this.parents = new ArrayList<>();
		for (Variable var : table.getParents()) {
			this.parents.add(new Variable(var));
		}
	}

	/**
	 * Getter of the table made out of ProbRows
	 * @return table
	 */
	public ArrayList<ProbRow> getTable() {
		return table;
	}
	
	 /**
	 * Getter of the node that belongs to the probability table
	 * @return the node
	 */
	public Variable getNode() {
		return node;
	}

	/**
	 * Getter of the parents that belong to the probability table
	 * @return the parents
	 */
	public ArrayList<Variable> getParents() {
		return parents;
	}

	/**
	  * Gets the i'th element from the ArrayList of ProbRows
	  * @param i index
	  * @return i'th ProbRow in Table
	  */
	public ProbRow get(int i) {
		return table.get(i);
	}
	
	/**
	 * Returns the size of the Table (amount of probability rows)
	 * @return size of Table
	 */
	public int size() {
		return table.size();
	}
}
