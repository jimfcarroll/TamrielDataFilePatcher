package database.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.Columns;
import database.Row;
import database.where.Comparison;
import model.Record;
import model.SubRecord;

public class BodypartReferencesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"owningObject", "bodypart"},
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
		System.out.println("This table does not support deletion.");
		return null;
	}
	
	private List<Row> executeRUD(List<Record> records, Columns selectedColumns, Comparison whereComparison, Map<String,String> updateValues, boolean delete) {
		List<Row> returnRows = new ArrayList<Row>();
		
		for (int i1 = 0; i1 < records.size(); i1++) {
			Record record = records.get(i1);
			
			//only npcs, clothing and armors contain body part references
			if (!record.getName().equals("NPC_") && !record.getName().equals("ARMO") && !record.getName().equals("CLOT")) {
				continue;
			}
			
			String owningObject = null;
			
			for (SubRecord subRecord : record.getSubRecords()) {
				
				if (subRecord.getName().equals("NAME")) {
					owningObject = subRecord.getData().getValueAsString();
					
				} else if (subRecord.getName().equals("BNAM") || subRecord.getName().equals("KNAM") && record.getName().equals("NPC_")) {
					
					String[] rowValues = new String[2];
					rowValues[0] = owningObject; 
					rowValues[1] = subRecord.getData().getValueAsString();
					Row newRow = new Row(allColumns, rowValues);
					
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("bodypart")) {
								subRecord.getData().setValueByString(updateValues.get("bodypart"));
								rowValues[1] = subRecord.getData().getValueAsString();
							}
						}
						
						if (selectedColumns != allColumns) {
							newRow = newRow.createFractionRow(selectedColumns);
						}
						returnRows.add(newRow);
					}
				}
			}
		}
		
		return returnRows;
	}

	@Override
	public String getName() {
		return "bodypartReferences";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}
}
