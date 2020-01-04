package database;

import java.util.LinkedList;
import java.util.List;
import database.table.Table;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

public class Insert implements Statement, ItemsListVisitor {
	private Table table;
	private Columns columns;
	private List<Row> rows;

	public Insert(net.sf.jsqlparser.statement.insert.Insert rawInsert) {
		table = Tables.getTable(rawInsert.getTable().getName());
		
		if (table == null) {
			System.out.println("This table does not exist.");
			return;
		}
		
		List<Column> rawColumns = rawInsert.getColumns();
		String[] columnNames = new String[rawColumns.size()];
		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i] = rawColumns.get(i).getColumnName();
		}
		columns = table.getColumns(columnNames);
		
		rows = new LinkedList<Row>();
		ItemsList itemsList = rawInsert.getItemsList();
		itemsList.accept(this);
	}

	@Override
	public void visit(ExpressionList arg0) {
		String[] values = new String[arg0.getExpressions().size()];
		
		for (int i = 0; i < arg0.getExpressions().size(); i++) {
			Expression expression = arg0.getExpressions().get(i);
			values[i] = expression.toString();
		}
		
		rows.add(new Row(columns, values));
	}

	@Override
	public void visit(MultiExpressionList arg0) {
		for (ExpressionList expressionList: arg0.getExprList()) {
			visit(expressionList);
		}
	}

	@Override
	public void visit(SubSelect arg0) {
		System.out.println("There is no support for insertion with subselects yet.");
	}

	public Table getTable() {
		return table;
	}

	public Columns getColumns() {
		return columns;
	}

	public List<Row> getRows() {
		return rows;
	}
}
