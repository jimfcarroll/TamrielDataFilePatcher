package database.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.Columns;
import database.Row;
import database.where.Comparison;
import model.Record;
import model.SubRecord;
import model.subrecorddata.SubrecordDataNPCO;

public class ItemReferencesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"owningObject", "item", "number", "type"},
			new Columns.Type[] {Columns.Type.text, Columns.Type.text, Columns.Type.integer, Columns.Type.text},
			new boolean[] {false, true, true, false}
	);

	@Override
	public List<Row> insert(List<Record> records, List<Row> rows) {
		System.out.println("This table does not support insertion yet.");
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
			
			String type;
			if (
					record.getName().equals("NPC_") ||
					record.getName().equals("CREA") ||
					record.getName().equals("CONT") ||
					record.getName().equals("CREC") ||
					record.getName().equals("NPCC") ||
					record.getName().equals("CNTC")
				) {
				type = "inventory";
			} else if (record.getName().equals("LEVI")) {
				type = "levelled";
			} else if (record.getName().equals("CELL")) {
				type = "key";
			} else if (record.getName().equals("STLN")) {
				type = "stolenItem";
			} else if (record.getName().equals("PCDT")) {
				type = "equiptApparatus";
			} else {
				type = null;
				continue;
			}
			
			String owningObject = null;
			String[] rowValues = null;
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				if (subRecord.getName().equals("NAME") && !record.getName().equals("PCDT")) {
					
					//only in case of a stolen item, the name-entry refers to the item directly
					if (record.getName().equals("STLN")) {
						rowValues = new String[4];
						rowValues[0] = "player"; 
						rowValues[1] = subRecord.getData().getValueAsString();
						rowValues[2] = "1";
						rowValues[3] = type;
						
						Row newRow = new Row(allColumns, rowValues);
						if (whereComparison.testConditionOnRow(newRow)) {
							if (updateValues != null) {
								if (updateValues.containsKey("item")) {
									subRecord.getData().setValueByString(updateValues.get("item"));
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
						
						rowValues = null;
						
					}
					//otherwise it stands for the id of the itemholder
					else {
						owningObject = subRecord.getData().getValueAsString();
					}
				}
				// an entry in a container or a creature/npc
				else if (subRecord.getName().equals("NPCO") && !record.getName().equals("PCDT")) {
					SubrecordDataNPCO subRecordDataNPCO = (SubrecordDataNPCO) subRecord.getData();
					
					rowValues = new String[4];
					rowValues[0] = owningObject; 
					rowValues[1] = subRecordDataNPCO.getItemAsString();
					rowValues[2] = subRecordDataNPCO.getNumberAsString();
					rowValues[3] = type;
					
					Row newRow = new Row(allColumns, rowValues);
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("item")) {
								subRecordDataNPCO.setItemByString(updateValues.get("item"));
								rowValues[1] = subRecordDataNPCO.getItemAsString();
							}
							
							if (updateValues.containsKey("number")) {
								subRecordDataNPCO.setNumberByString(updateValues.get("number"));
								rowValues[2] = subRecordDataNPCO.getNumberAsString();
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
					
					rowValues = null;
					
				} else if (subRecord.getName().equals("INAM") && record.getName().equals("LEVI")) {
					
					rowValues = new String[4];
					rowValues[0] = owningObject; 
					rowValues[1] = subRecord.getData().getValueAsString();
					rowValues[3] = type;
					
				}
				// an entry in a levelled item list
				else if (subRecord.getName().equals("INTV") && record.getName().equals("LEVI")) {
					
					rowValues[2] = subRecord.getData().getValueAsString();
					
					Row newRow = new Row(allColumns, rowValues);
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("item")) {
								SubRecord previousSubRecord = record.getSubRecords().get(i2-1);
								previousSubRecord.getData().setValueByString(updateValues.get("item"));
								rowValues[1] = previousSubRecord.getData().getValueAsString();
							}
							
							if (updateValues.containsKey("number")) {
								subRecord.getData().setValueByString(updateValues.get("number"));
								rowValues[2] = subRecord.getData().getValueAsString();
							}
						}
						
						if (delete) {
							record.getSubRecords().remove(i2);
							record.getSubRecords().remove(i2-1);
							i2 -= 2;
						}
						
						if (selectedColumns != allColumns) {
							newRow = newRow.createFractionRow(selectedColumns);
						}
						returnRows.add(newRow);
					}
					
					rowValues = null;
					
				
				}
				// a key in a cell
				else if (subRecord.getName().equals("KNAM") && record.getName().equals("CELL")) {
					
					rowValues = new String[4];
					rowValues[0] = owningObject; 
					rowValues[1] = subRecord.getData().getValueAsString();
					rowValues[2] = "1";
					rowValues[3] = type;
					
					Row newRow = new Row(allColumns, rowValues);
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("item")) {
								subRecord.getData().setValueByString(updateValues.get("item"));
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
					
					rowValues = null;
				}
				//an equipt apparatus
				else if ( (subRecord.getName().equals("NAM0") || subRecord.getName().equals("NAM1") || subRecord.getName().equals("NAM2") || subRecord.getName().equals("NAM3")) && record.getName().equals("PCDT")) {
					
					rowValues = new String[4];
					rowValues[0] = "player"; 
					rowValues[1] = subRecord.getData().getValueAsString();
					rowValues[2] = "1";
					rowValues[3] = type;
					
					Row newRow = new Row(allColumns, rowValues);
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("item")) {
								subRecord.getData().setValueByString(updateValues.get("item"));
								rowValues[1] = subRecord.getData().getValueAsString();
							}
						}
						
						if (delete) {
							subRecord.getData().setValueByString("");
							rowValues[1] = subRecord.getData().getValueAsString();
						}
						
						if (selectedColumns != allColumns) {
							newRow = newRow.createFractionRow(selectedColumns);
						}
						returnRows.add(newRow);
					}
					
					rowValues = null;
				}
			}
		}
		
		return returnRows;
	}

	@Override
	public String getName() {
		return "itemReferences";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}

}
