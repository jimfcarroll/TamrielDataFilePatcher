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

    public Insert(final net.sf.jsqlparser.statement.insert.Insert rawInsert) {
        table = Tables.getTable(rawInsert.getTable().getName());

        if(table == null) {
            System.out.println("This table does not exist.");
            return;
        }

        final List<Column> rawColumns = rawInsert.getColumns();
        final String[] columnNames = new String[rawColumns.size()];
        for(int i = 0; i < columnNames.length; i++) {
            columnNames[i] = rawColumns.get(i).getColumnName();
        }
        columns = table.getColumns(columnNames);

        rows = new LinkedList<Row>();
        final ItemsList itemsList = rawInsert.getItemsList();
        itemsList.accept(this);
    }

    @Override
    public void visit(final ExpressionList arg0) {
        final String[] values = new String[arg0.getExpressions().size()];

        for(int i = 0; i < arg0.getExpressions().size(); i++) {
            final Expression expression = arg0.getExpressions().get(i);
            values[i] = expression.toString();
        }

        rows.add(new Row(columns, values));
    }

    @Override
    public void visit(final MultiExpressionList arg0) {
        for(final ExpressionList expressionList: arg0.getExprList()) {
            visit(expressionList);
        }
    }

    @Override
    public void visit(final SubSelect arg0) {
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
