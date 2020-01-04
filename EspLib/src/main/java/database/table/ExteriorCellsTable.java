package database.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import database.Columns;
import database.Row;
import database.where.Comparison;
import model.ModelFunctions;
import model.Record;
import model.SubRecord;
import model.subrecorddata.SubRecordDataComposed;
import model.subrecorddata.SubRecordDataString;

public class ExteriorCellsTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"name", "gridX", "gridY", "region", "illegalToSleepHere"},
			new Columns.Type[] {Columns.Type.text, Columns.Type.integer, Columns.Type.integer, Columns.Type.text, Columns.Type.integer},
			new boolean[] {true, false, false, true, true}
	);

	@Override
	public List<Row> insert(List<Record> records, List<Row> rows) {
		System.out.println("This table does not support insertion of rows.");
		return null;
	}

	@Override
	public List<Row> select(List<Record> records, Columns selectedColumns, Comparison whereComparison) {
		return executeRUD(records, selectedColumns, whereComparison, null, false);
	}

	@Override
	public List<Row> update(List<Record> records, Comparison whereComparison, Map<String, String> updateValues) {
		if (allColumns.containsNonUpdatableFields(updateValues)) {
			System.out.println("gridX and gridY may not be updated.");
		}
		return executeRUD(records, allColumns, whereComparison, updateValues, false);
	}

	@Override
	public List<Row> delete(List<Record> records, Comparison whereComparison) {
		return executeRUD(records, allColumns, whereComparison, null, true);
	}
	
	private List<Row> executeRUD(List<Record> records, Columns selectedColumns, Comparison whereComparison, Map<String, String> updateValues, boolean delete) {
		List<Row> returnRows = new ArrayList<Row>(100);
		
		for (int i1 = 0; i1 < records.size(); i1++) {
			Record record = records.get(i1);
			
			if (!record.getName().equals("CELL")) {
				continue;
			}
			
			String[] rowValues = allColumns.createEmptyRowValues();
			boolean noExteriorCell = false;
			
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				//FRMR is the first entry of the references in a cell.
				if (subRecord.getName().equals("FRMR")) {
					break;
				}
				
				if (subRecord.getName().equals("NAME")) {
					rowValues[allColumns.getPositionOf("name")] = subRecord.getData().getValueAsString();
					
				} else if (subRecord.getName().equals("RGNN")) {
					rowValues[allColumns.getPositionOf("region")] = subRecord.getData().getValueAsString();
					
				} else if (subRecord.getName().equals("DATA")) {
					SubRecordDataComposed composedSubRecordData = (SubRecordDataComposed) subRecord.getData();
					
					//first ensure that the current cell is an exterior cell
					String flags = composedSubRecordData.getValueAsStringOf("flags");
					if (flags.equals("6")) {
						rowValues[allColumns.getPositionOf("illegalToSleepHere")] = "1";
					} else if (flags.equals("2") || flags.equals("0")) {
						rowValues[allColumns.getPositionOf("illegalToSleepHere")] = "0";
					} else {
						noExteriorCell = true;
						break;
					}
					
					rowValues[allColumns.getPositionOf("gridX")] = composedSubRecordData.getValueAsStringOf("gridX");
					rowValues[allColumns.getPositionOf("gridY")] = composedSubRecordData.getValueAsStringOf("gridY");
				}
			}
			
			if (noExteriorCell) {
				continue;
			}
			
			Row newRow = new Row(allColumns, rowValues);
			if (whereComparison.testConditionOnRow(newRow)) {
				
				if (updateValues != null) {
					//sometimes there is no region entry in an exterior cell. this variable is to check if
					boolean regionUpdated = false;
					
					for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
						SubRecord subRecord = record.getSubRecords().get(i2);
						
						//FRMR is the first entry of the references in a cell.
						if (subRecord.getName().equals("FRMR")) {
							break;
						}
						
						if (subRecord.getName().equals("NAME") && updateValues.containsKey("name")) {
							subRecord.getData().setValueByString(updateValues.get("name"));
							rowValues[allColumns.getPositionOf("name")] = subRecord.getData().getValueAsString();
							
						} else if (subRecord.getName().equals("RGNN") && updateValues.containsKey("region")) {
							subRecord.getData().setValueByString(updateValues.get("region"));
							rowValues[allColumns.getPositionOf("region")] = subRecord.getData().getValueAsString();
							
							//if the region is set to empty, remove it
							if (subRecord.getData().getValueAsString().equals("")) {
								record.getSubRecords().remove(i2);
								i2--;
							}
							regionUpdated = true;
							
						} else if (subRecord.getName().equals("DATA")) {
							SubRecordDataComposed composedSubRecordData = (SubRecordDataComposed) subRecord.getData();
							
							if (updateValues.containsKey("illegalToSleepHere")) {
								Map<String, Float> variables = new HashMap<String, Float>();
								variables.put("illegalToSleepHere", Float.parseFloat(rowValues[allColumns.getPositionOf("illegalToSleepHere")]));
								int newValue = Math.round(ModelFunctions.evaluateMathematicalExpression(updateValues.get("illegalToSleepHere"), variables));
								
								if (newValue < 1) {
									composedSubRecordData.setValueByStringFor("2", "flags");
									rowValues[allColumns.getPositionOf("illegalToSleepHere")] = "0";
								} else {
									composedSubRecordData.setValueByStringFor("6", "flags");
									rowValues[allColumns.getPositionOf("illegalToSleepHere")] = "1";
								}
							}
							
//							if (updateValues.containsKey("gridX")) {
//								composedSubRecordData.setValueByStringFor(updateValues.get("gridX"), "gridX");
//								rowValues[allColumns.getPositionOf("gridX")] = composedSubRecordData.getValueAsStringOf("gridX");
//							}
//							if (updateValues.containsKey("gridY")) {
//								composedSubRecordData.setValueByStringFor(updateValues.get("gridY"), "gridY");
//								rowValues[allColumns.getPositionOf("gridY")] = composedSubRecordData.getValueAsStringOf("gridY");
//							}
						}
					}
					
					//now check whether the region value could be set or if there was no existing region entry that could be updated.
					if (regionUpdated == false && updateValues.containsKey("region")) {
						Map<String, String> variables = new HashMap<String, String>();
						variables.put("region", "");
						String newValue = ModelFunctions.evaluateStringExpression(updateValues.get("region"), variables);
						
						SubRecordDataString subRecordData = new SubRecordDataString(newValue, "region", 0, 999);
						SubRecord newSubRecord = new SubRecord("RGNN", subRecordData, record);
						record.getSubRecords().add(2, newSubRecord);
						rowValues[allColumns.getPositionOf("region")] = newValue;
					}
				}
				
				if (delete) {
					
					//now searching the landscape record for this cell to delete it too
					for (int i2 = 0; i2 < records.size(); i2++) {
						Record searchRecord = records.get(i2);
						
						if (!searchRecord.getName().equals("LAND")) {
							continue;
						}
						
						SubRecord coordinateSubrecord = searchRecord.getSubRecords().get(0);
						SubRecordDataComposed composedSubRecordData = (SubRecordDataComposed) coordinateSubrecord.getData();
						
						if (composedSubRecordData.getValueAsStringOf("gridX").equals(rowValues[allColumns.getPositionOf("gridX")]) &&
							composedSubRecordData.getValueAsStringOf("gridY").equals(rowValues[allColumns.getPositionOf("gridY")])) {
							records.remove(i2);
							
							if (i2 < i1) {
								i1--;
							}
							break;
						}
					}
					
					records.remove(i1);
					i1--;
				}
				
				if (selectedColumns != allColumns) {
					newRow = newRow.createFractionRow(selectedColumns);
				}
				returnRows.add(newRow);
			}
		}
	
		return returnRows;
	}

	@Override
	public String getName() {
		return "exteriorCells";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}

}
