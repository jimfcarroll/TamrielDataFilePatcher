package database;

import java.util.HashMap;
import java.util.Map;

import database.table.Table;
import database.where.AlwaysTrueComparison;
import database.where.Comparison;
import database.where.WhereEvaluator;

public class Update implements Statement {
    private final Table table;
    private Comparison whereComparison;
    private final Map<String, String> updateValues;

    public Update(final net.sf.jsqlparser.statement.update.Update rawUpdate) {
        table = Tables.getTable(rawUpdate.getTables().get(0).getName());

        if(rawUpdate.getWhere() == null) {
            whereComparison = new AlwaysTrueComparison();
        } else {
            final WhereEvaluator whereEvaluator = new WhereEvaluator();
            whereComparison = whereEvaluator.evaluateWhere(rawUpdate.getWhere());
        }

        updateValues = new HashMap<>();
        for(int i = 0; i < rawUpdate.getColumns().size(); i++) {
            final String key = rawUpdate.getColumns().get(i).getColumnName();
            final String value = rawUpdate.getExpressions().get(i).toString();
            updateValues.put(key, value);
        }
    }

    @Override
    public Table getTable() {
        return table;
    }

    public Comparison getWhereComparison() {
        return whereComparison;
    }

    public Map<String, String> getUpdateValues() {
        return updateValues;
    }
}
