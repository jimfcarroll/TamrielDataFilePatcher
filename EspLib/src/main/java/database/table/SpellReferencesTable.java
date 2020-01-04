package database.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.Columns;
import database.Row;
import database.where.Comparison;
import model.Record;
import model.SubRecord;

public class SpellReferencesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"owningObject", "spell", "type"},
			new Columns.Type[] {Columns.Type.text, Columns.Type.text, Columns.Type.text,},
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
		return executeRUD(records, allColumns, whereComparison, null, true);
	}
	
	private List<Row> executeRUD(List<Record> records, Columns selectedColumns, Comparison whereComparison, Map<String,String> updateValues, boolean delete) {
		List<Row> returnRows = new ArrayList<Row>();
		
		for (int i1 = 0; i1 < records.size(); i1++) {
			Record record = records.get(i1);
			
			//only npcs, creatures and cells.
			if (!record.getName().equals("NPC_") && !record.getName().equals("CREA") && !record.getName().equals("CELL") && !record.getName().equals("REFR")) {
				continue;
			}
			
			String owningObject = null;
			
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				if (subRecord.getName().equals("NAME")) {
					owningObject = subRecord.getData().getValueAsString();
					
				} else if (subRecord.getName().equals("NPCS") || (record.getName().equals("CELL") && subRecord.getName().equals("TNAM")) || (record.getName().equals("REFR") && subRecord.getName().equals("WNAM"))) {
					
					String[] rowValues = new String[3];
					rowValues[0] = owningObject; 
					rowValues[1] = subRecord.getData().getValueAsString();
					if (rowValues[1].contains("\0")) {
						rowValues[1] = rowValues[1].substring(0, rowValues[1].indexOf('\0'));
					}
					
					rowValues[2] = subRecord.getName().equals("WNAM") ? "readied spell" : subRecord.getName().equals("NPCS") ? "spell" : "trap";
					Row newRow = new Row(allColumns, rowValues);
					
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("spell")) {
								subRecord.getData().setValueByString(updateValues.get("spell"));
								rowValues[1] = subRecord.getData().getValueAsString();
								
								if (rowValues[1].equals("")) {
									record.getSubRecords().remove(i2);
									i2--;
								}
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
			}
		}
		
		return returnRows;
	}

	@Override
	public String getName() {
		return "spellReferences";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}
}
