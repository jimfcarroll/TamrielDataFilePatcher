package database.where;


import database.Columns;
import database.Row;

public interface Comparison {
	public boolean testConditionOnRow(Row row);
}
