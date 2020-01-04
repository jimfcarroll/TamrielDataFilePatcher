package database.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.Columns;
import database.Row;
import database.where.Comparison;
import model.Record;
import model.SubRecord;

public class FileReferencesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"owningObject", "file", "type"},
			new Columns.Type[] {Columns.Type.text, Columns.Type.text, Columns.Type.text},
			new boolean[] {false, true, false}
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
		System.out.println("This table does not support deletion.");
		return null;
	}
	
	private List<Row> executeRUD(List<Record> records, Columns selectedColumns, Comparison whereComparison, Map<String,String> updateValues, boolean delete) {
		List<Row> returnRows = new ArrayList<Row>();
		
		for (int i1 = 0; i1 < records.size(); i1++) {
			Record record = records.get(i1);
			
			String[] rowValues = new String[3];
			SubRecord fileSubRecord = null;
			
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				if (subRecord.getName().equals("NAME")) {
					rowValues[0] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("MODL")) {
					rowValues[1] = subRecord.getData().getValueAsString();
					rowValues[2] = "model";
					fileSubRecord = subRecord;
				} else if (subRecord.getName().equals("ITEX")) {
					rowValues[1] = subRecord.getData().getValueAsString();
					rowValues[2] = "icon";
					fileSubRecord = subRecord;
				} else if (subRecord.getName().equals("DATA") && record.getName().equals("LTEX")) {
					rowValues[1] = subRecord.getData().getValueAsString();
					rowValues[2] = "texture";
					fileSubRecord = subRecord;
				} else if (subRecord.getName().equals("FNAM") && record.getName().equals("SOUN")) {
					rowValues[1] = subRecord.getData().getValueAsString();
					rowValues[2] = "sound";
					fileSubRecord = subRecord;
				}
				
				if (rowValues[0] != null && rowValues[1] != null && rowValues[2] != null) {
					Row newRow = new Row(allColumns, rowValues);
					
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("file")) {
								fileSubRecord.getData().setValueByString(updateValues.get("file"));
								rowValues[1] = fileSubRecord.getData().getValueAsString();
							}
						}
						
						if (selectedColumns != allColumns) {
							newRow = newRow.createFractionRow(selectedColumns);
						}
						returnRows.add(newRow);
					}
					
					rowValues = new String[] {rowValues[0], null, null};
				}
				
			}
		
		}
		
		return returnRows;
	}

	@Override
	public String getName() {
		return "fileReferences";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}

}
