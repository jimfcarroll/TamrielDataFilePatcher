package database.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.Columns;
import database.Row;
import database.where.Comparison;
import model.Record;
import model.SubRecord;

public class EnchantmentReferencesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"owningObject", "enchantment"},
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
			
			//only weapons, books, clothing and armors contain body part references
			if (!record.getName().equals("WEAP") && !record.getName().equals("ARMO") && !record.getName().equals("CLOT") && !record.getName().equals("BOOK")) {
				continue;
			}
			
			String owningObject = null;
			
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				if (subRecord.getName().equals("NAME")) {
					owningObject = subRecord.getData().getValueAsString();
					
				} else if (subRecord.getName().equals("ENAM")) {
					
					String[] rowValues = new String[2];
					rowValues[0] = owningObject; 
					rowValues[1] = subRecord.getData().getValueAsString();
					Row newRow = new Row(allColumns, rowValues);
					
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("enchantment")) {
								subRecord.getData().setValueByString(updateValues.get("enchantment"));
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
		return "enchantmentReferences";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}
}
