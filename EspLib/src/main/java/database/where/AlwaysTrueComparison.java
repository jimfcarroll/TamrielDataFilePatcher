package database.where;

import database.Row;

public class AlwaysTrueComparison implements Comparison {

	@Override
	public boolean testConditionOnRow(Row row) {
		return true;
	}

}
