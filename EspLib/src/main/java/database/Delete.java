package database;

import database.table.Table;
import database.where.AlwaysTrueComparison;
import database.where.Comparison;
import database.where.WhereEvaluator;

public class Delete implements Statement {
	private Table table;
	private Comparison whereComparison;

	public Delete(net.sf.jsqlparser.statement.delete.Delete rawDelete) {
		table = Tables.getTable(rawDelete.getTable().getName());
		
		if (rawDelete.getWhere() == null) {
			whereComparison = new AlwaysTrueComparison();
		} else {
			WhereEvaluator whereEvaluator = new WhereEvaluator();
			whereComparison = whereEvaluator.evaluateWhere(rawDelete.getWhere());
		}
	}

	public Table getTable() {
		return table;
	}

	public Comparison getWhereComparison() {
		return whereComparison;
	}
}
