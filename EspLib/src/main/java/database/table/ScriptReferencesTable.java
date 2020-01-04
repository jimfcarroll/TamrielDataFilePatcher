package database.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import database.Columns;
import database.Row;
import database.where.Comparison;
import model.Record;
import model.SubRecord;

public class ScriptReferencesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"owningObject", "script"},
			new Columns.Type[] {Columns.Type.text, Columns.Type.text},
			new boolean[] {false, true}
	);

	@Override
	public List<Row> insert(List<Record> records, List<Row> rows) {
		System.out.println("This table does not support insertion.");
		return null;
	}

	@Override
	public List<Row> select(List<Record> records, Columns selectedColumns, Comparison whereComparison) {
		return executeRUD(records, selectedColumns, whereComparison, null, false);
	}

	@Override
	public List<Row> update(List<Record> records, Comparison whereComparison, Map<String, String> updateValues) {
		return executeRUD(records, allColumns, whereComparison, updateValues, false);
	}

	@Override
	public List<Row> delete(List<Record> records, Comparison whereComparison) {
		return executeRUD(records, allColumns, whereComparison, null, true);
	}
	
	private List<Row> executeRUD(List<Record> records, Columns selectedColumns, Comparison whereComparison, Map<String,String> updateValues, boolean delete) {
		List<Row> returnRows = new ArrayList<Row>();
		
		for (int i1 = 0; i1 < records.size(); i1++) {
			Record record = records.get(i1);
			
			String[] rowValues = new String[2];
			
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				if (subRecord.getName().equals("NAME")) {
					rowValues[0] = subRecord.getData().getValueAsString();
				}
				
				else if (subRecord.getName().equals("SCRI")) {
					rowValues[1] = subRecord.getData().getValueAsString();
					
					if (rowValues[0] != null) {
						Row newRow = new Row(allColumns, rowValues);
						
						if (whereComparison.testConditionOnRow(newRow)) {
							
							if (updateValues != null) {
								if (updateValues.containsKey("script")) {
									subRecord.getData().setValueByString(updateValues.get("script"));
									rowValues[1] = subRecord.getData().getValueAsString();
								}
							}
							
							if (delete) {
								record.getSubRecords().remove(i2);
								i2--;
							}
							
							if (selectedColumns != allColumns) {
								newRow = newRow.createFractionRow(selectedColumns);
							}
							returnRows.add(newRow);
						}
					}
					
					rowValues = Arrays.copyOf(rowValues, rowValues.length);
				}
				
			}
			
		}
		
		return returnRows;
	}

	@Override
	public String getName() {
		return "scriptReferences";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}

}
