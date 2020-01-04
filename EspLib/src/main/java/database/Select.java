package database;

import java.util.LinkedList;
import java.util.List;

import database.table.Table;
import database.where.AlwaysTrueComparison;
import database.where.Comparison;
import database.where.WhereEvaluator;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

public class Select extends SelectVisitorAdapter implements SelectItemVisitor, Statement {
	private List<String> rawSelectItems;
	private Table table;
	private Columns columns;
	private Comparison whereComparison;

	public Select(net.sf.jsqlparser.statement.select.Select rawSelect) {
		rawSelectItems = new LinkedList<String>();
		table = null;
		columns = null;
		whereComparison = null;
		
		SelectBody selectBody = rawSelect.getSelectBody();
		selectBody.accept(this);
	}

	@Override
	public void visit(PlainSelect arg0) {
		table = Tables.getTable(arg0.getFromItem().toString());
		
		if (table == null) {
			System.out.println("This table does not exist.");
			return;
		}
		
		List<SelectItem> selectItemList = arg0.getSelectItems();
		for (SelectItem selectItem: selectItemList) {
			selectItem.accept(this);
		}
		columns = table.getColumns(this.rawSelectItems.toArray(new String[this.rawSelectItems.size()]));
		
		if (arg0.getWhere() == null) {
			whereComparison = new AlwaysTrueComparison();
		} else {
			WhereEvaluator whereEvaluator = new WhereEvaluator();
			whereComparison = whereEvaluator.evaluateWhere(arg0.getWhere());
		}
	}

	@Override
	public void visit(AllColumns arg0) {
		rawSelectItems.add("*");
	}

	@Override
	public void visit(AllTableColumns arg0) {
		rawSelectItems.add(arg0.toString());
	}

	@Override
	public void visit(SelectExpressionItem arg0) {
		rawSelectItems.add(arg0.toString());
	}

	public Table getTable() {
		return table;
	}

	public Columns getColumns() {
		return columns;
	}

	public Comparison getWhereComparison() {
		return whereComparison;
	}
	
}
