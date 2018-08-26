package varelim;

import java.util.ArrayList;

public class Factor {
	
	private ArrayList<ProbRow> table;
	private ArrayList<Variable> nodes;
	
	public Factor(Table table, String name) {
		this.table = table.getTable();
		this.nodes = new ArrayList<>();
		this.nodes.addAll(table.getParents());
		this.nodes.add(table.getNode());
	}
	
	public Factor(Factor factor) {
		this.table = new ArrayList<>(factor.getTable());
		this.nodes = new ArrayList<>(factor.getNodes());
	}
	
	/**
	 * Getter of the table made out of ProbRows
	 * @return table
	 */
	public ArrayList<ProbRow> getTable() {
		return table;
	}
	
	/**
	 * Getter of the nodes that belong to the probability table
	 * @return the nodes
	 */
	public ArrayList<Variable> getNodes() {
		return nodes;
	}
	
	/**
	 * Print factor in a readable format
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('\n');
		for (Variable node : nodes) {
			sb.append(node.getName());
			sb.append(" | ");
		}
		sb.append("prob");
		sb.append("\n-----------------------------");
		for (ProbRow row : table) {
			sb.append('\n');
			sb.append(row);
		}
		return sb.toString();
	}

}
