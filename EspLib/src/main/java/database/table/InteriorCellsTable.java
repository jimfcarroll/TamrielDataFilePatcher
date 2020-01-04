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

public class InteriorCellsTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {
					"name",
					"ambientRed",
					"ambientGreen",
					"ambientBlue",
					"sunlightRed",
					"sunlightGreen",
					"sunlightBlue",
					"fogRed",
					"fogGreen",
					"fogBlue",
					"fogDensity",
					"hasWater",
					"waterHeight",
					"illegalToSleepHere",
					"behaveLikeExterior",
					"region",
					"objectCount"
					},
			new Columns.Type[] {
					Columns.Type.text,
					Columns.Type.integer,
					Columns.Type.integer,
					Columns.Type.integer,
					Columns.Type.integer,
					Columns.Type.integer,
					Columns.Type.integer,
					Columns.Type.integer,
					Columns.Type.integer,
					Columns.Type.integer,
					Columns.Type.number,
					Columns.Type.integer,
					Columns.Type.number,
					Columns.Type.integer,
					Columns.Type.integer,
					Columns.Type.text,
					Columns.Type.integer
					},
			new boolean[] {
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					true,
					false
					}
	);

	@Override
	public List<Row> insert(List<Record> records, List<Row> rows) {
		System.out.println("This table does not support insertion of rows yet.");
		return null;
	}

	@Override
	public List<Row> select(List<Record> records, Columns selectedColumns, Comparison whereComparison) {
		return executeRUD(records, selectedColumns, whereComparison, null, false);
	}

	@Override
	public List<Row> update(List<Record> records, Comparison whereComparison, Map<String, String> updateValues) {
		if (allColumns.containsNonUpdatableFields(updateValues)) {
			System.out.println("objectCount may not be updated.");
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
			boolean noInteriorCell = false;
			boolean refsStarted = false;
			int objectCount = 0;
			
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				//FRMR is the first entry of the references in a cell.
				if (subRecord.getName().equals("FRMR")) {
					objectCount++;
					if (!refsStarted) {
						refsStarted = true;
					}
				}
				
				if (refsStarted) {
					continue;
				}
				
				if (subRecord.getName().equals("NAME")) {
					rowValues[allColumns.getPositionOf("name")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("DATA")) {
					SubRecordDataComposed composedSubRecordData = (SubRecordDataComposed) subRecord.getData();
					int flags = composedSubRecordData.getValueAsIntOf("flags");
					
					if ((flags & 0x01) == 0x00) {
						noInteriorCell = true;
						break;
					}
					
					if ((flags & 0x02) == 0x02) {
						rowValues[allColumns.getPositionOf("hasWater")] = "1";
					} else {
						rowValues[allColumns.getPositionOf("hasWater")] = "0";
					}
					
					if ((flags & 0x04) == 0x04) {
						rowValues[allColumns.getPositionOf("illegalToSleepHere")] = "1";
					} else {
						rowValues[allColumns.getPositionOf("illegalToSleepHere")] = "0";
					}
					
					if ((flags & 0x80) == 0x80) {
						rowValues[allColumns.getPositionOf("behaveLikeExterior")] = "1";
					} else {
						rowValues[allColumns.getPositionOf("behaveLikeExterior")] = "0";
					}
				} else if (subRecord.getName().equals("RGNN")) {
					rowValues[allColumns.getPositionOf("region")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("WHGT")) {
					rowValues[allColumns.getPositionOf("waterHeight")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("AMBI")) {
					SubRecordDataComposed composedSubRecordData = (SubRecordDataComposed) subRecord.getData();
					
					int ambientColorInt = composedSubRecordData.getValueAsIntOf("ambientColor");
					int ambientRed = ambientColorInt & 0xFF;
					int ambientGreen = (ambientColorInt >> 8) & 0xFF;
					int ambientBlue = (ambientColorInt >> 16) & 0xFF;
					
					rowValues[allColumns.getPositionOf("ambientRed")] = String.valueOf(ambientRed);
					rowValues[allColumns.getPositionOf("ambientGreen")] = String.valueOf(ambientGreen);
					rowValues[allColumns.getPositionOf("ambientBlue")] = String.valueOf(ambientBlue);
					
					int sunlightColorInt = composedSubRecordData.getValueAsIntOf("sunlightColor");
					int sunlightRed = sunlightColorInt & 0xFF;
					int sunlightGreen = (sunlightColorInt >> 8) & 0xFF;
					int sunlightBlue = (sunlightColorInt >> 16) & 0xFF;
					
					rowValues[allColumns.getPositionOf("sunlightRed")] = String.valueOf(sunlightRed);
					rowValues[allColumns.getPositionOf("sunlightGreen")] = String.valueOf(sunlightGreen);
					rowValues[allColumns.getPositionOf("sunlightBlue")] = String.valueOf(sunlightBlue);
					
					int fogColorInt = composedSubRecordData.getValueAsIntOf("fogColor");
					int fogRed = fogColorInt & 0xFF;
					int fogGreen = (fogColorInt >> 8) & 0xFF;
					int fogBlue = (fogColorInt >> 16) & 0xFF;
					
					rowValues[allColumns.getPositionOf("fogRed")] = String.valueOf(fogRed);
					rowValues[allColumns.getPositionOf("fogGreen")] = String.valueOf(fogGreen);
					rowValues[allColumns.getPositionOf("fogBlue")] = String.valueOf(fogBlue);
					rowValues[allColumns.getPositionOf("fogDensity")] = composedSubRecordData.getValueAsStringOf("fogDensity");
				}
			}
			
			if (noInteriorCell) {
				continue;
			}
			
			rowValues[allColumns.getPositionOf("objectCount")] = String.valueOf(objectCount);
			Row newRow = new Row(allColumns, rowValues);
			
			if (whereComparison.testConditionOnRow(newRow)) {
				
				if (updateValues != null) {
					Map<String, Float> variables = new HashMap<String, Float>();
					variables.put("ambientRed", Float.parseFloat(rowValues[allColumns.getPositionOf("ambientRed")]));
					variables.put("ambientGreen", Float.parseFloat(rowValues[allColumns.getPositionOf("ambientGreen")]));
					variables.put("ambientBlue", Float.parseFloat(rowValues[allColumns.getPositionOf("ambientBlue")]));
					variables.put("sunlightRed", Float.parseFloat(rowValues[allColumns.getPositionOf("sunlightRed")]));
					variables.put("sunlightGreen", Float.parseFloat(rowValues[allColumns.getPositionOf("sunlightGreen")]));
					variables.put("sunlightBlue", Float.parseFloat(rowValues[allColumns.getPositionOf("sunlightBlue")]));
					variables.put("fogRed", Float.parseFloat(rowValues[allColumns.getPositionOf("fogRed")]));
					variables.put("fogGreen", Float.parseFloat(rowValues[allColumns.getPositionOf("fogGreen")]));
					variables.put("fogBlue", Float.parseFloat(rowValues[allColumns.getPositionOf("fogBlue")]));
					variables.put("fogDensity", Float.parseFloat(rowValues[allColumns.getPositionOf("fogDensity")]));
					variables.put("hasWater", Float.parseFloat(rowValues[allColumns.getPositionOf("hasWater")]));
					variables.put("waterHeight", Float.parseFloat(rowValues[allColumns.getPositionOf("waterHeight")]));
					variables.put("illegalToSleepHere", Float.parseFloat(rowValues[allColumns.getPositionOf("illegalToSleepHere")]));
					variables.put("behaveLikeExterior", Float.parseFloat(rowValues[allColumns.getPositionOf("behaveLikeExterior")]));
					variables.put("objectCount", Float.parseFloat(rowValues[allColumns.getPositionOf("objectCount")]));
					
					Map<String, String> stringVariables = new HashMap<String, String>();
					stringVariables.put("name", rowValues[allColumns.getPositionOf("name")]);
					stringVariables.put("region", rowValues[allColumns.getPositionOf("region")]);
					
					//sometimes there is no region entry in an interior cell. this variable is to check if there is
					boolean regionUpdated = false;
					
					for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
						SubRecord subRecord = record.getSubRecords().get(i2);
						
						//FRMR is the first entry of the references in a cell.
						if (subRecord.getName().equals("FRMR")) {
							break;
						}
						
						if (subRecord.getName().equals("NAME") && updateValues.containsKey("name")) {
							String newName = ModelFunctions.evaluateStringExpression(updateValues.get("name"), stringVariables);
							((SubRecordDataString)subRecord.getData()).setValue(newName);
							rowValues[allColumns.getPositionOf("name")] = newName;
							
						} else if (subRecord.getName().equals("DATA")) {
							SubRecordDataComposed composedSubRecordData = (SubRecordDataComposed) subRecord.getData();
							int binaryFlags = composedSubRecordData.getValueAsIntOf("flags");
							boolean flagUpdated = false;
							
							if (updateValues.containsKey("hasWater")) {
								int newValue = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("hasWater"), variables));
								
								if (newValue < 1) {
									binaryFlags = binaryFlags & 0xFD;
									rowValues[allColumns.getPositionOf("hasWater")] = "0";
								} else {
									binaryFlags = binaryFlags | 0x02;
									rowValues[allColumns.getPositionOf("hasWater")] = "1";
								}
								
								flagUpdated = true;
							}
							
							if (updateValues.containsKey("illegalToSleepHere")) {
								int newValue = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("illegalToSleepHere"), variables));
								
								if (newValue < 1) {
									binaryFlags = binaryFlags & 0xFB;
									rowValues[allColumns.getPositionOf("illegalToSleepHere")] = "0";
								} else {
									binaryFlags = binaryFlags | 0x04;
									rowValues[allColumns.getPositionOf("illegalToSleepHere")] = "1";
								}
								
								flagUpdated = true;
							}
							
							if (updateValues.containsKey("behaveLikeExterior")) {
								int newValue = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("behaveLikeExterior"), variables));
								
								if (newValue < 1) {
									binaryFlags = binaryFlags & 0x7F;
									rowValues[allColumns.getPositionOf("behaveLikeExterior")] = "0";
								} else {
									binaryFlags = binaryFlags | 0x80;
									rowValues[allColumns.getPositionOf("behaveLikeExterior")] = "1";
								}
								
								flagUpdated = true;
							}
							
							if (flagUpdated) {
								composedSubRecordData.setValueByStringFor(String.valueOf(binaryFlags), "flags");
							}
						} else if (subRecord.getName().equals("RGNN") && updateValues.containsKey("region")) {
							String newRegion = ModelFunctions.evaluateStringExpression(updateValues.get("region"), stringVariables);
							((SubRecordDataString)subRecord.getData()).setValue(newRegion);
							rowValues[allColumns.getPositionOf("region")] = newRegion;
							
							//if the region is set to empty, remove it
							if (subRecord.getData().getValueAsString().equals("")) {
								record.getSubRecords().remove(i2);
								i2--;
							}
							
							regionUpdated = true;
							
						} else if (subRecord.getName().equals("WHGT") && updateValues.containsKey("waterHeight")) {
							float newValue = ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("waterHeight"), variables);
							subRecord.getData().setValueByString(String.valueOf(newValue));
							rowValues[allColumns.getPositionOf("waterHeight")] = String.valueOf(newValue);
							
						} else if (subRecord.getName().equals("AMBI")) {
							SubRecordDataComposed composedSubRecordData = (SubRecordDataComposed) subRecord.getData();
							
							
							// ambient color
							boolean ambientColorUpdated = false;
							int ambientColorInt = composedSubRecordData.getValueAsIntOf("ambientColor");
							
							if (updateValues.containsKey("ambientRed")) {
								int newRed = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("ambientRed"), variables));
								newRed = ModelFunctions.truncateInt(newRed, 0, 255);
								ambientColorUpdated = true;
								
								ambientColorInt = (ambientColorInt & 0xFFFFFF00) | newRed;
								rowValues[allColumns.getPositionOf("ambientRed")] = String.valueOf(newRed);
							}
							
							if (updateValues.containsKey("ambientGreen")) {
								int newGreen = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("ambientGreen"), variables));
								newGreen = ModelFunctions.truncateInt(newGreen, 0, 255);
								ambientColorUpdated = true;
								
								ambientColorInt = (ambientColorInt & 0xFFFF00FF) | (newGreen << 8);
								rowValues[allColumns.getPositionOf("ambientGreen")] = String.valueOf(newGreen);
							}
							
							if (updateValues.containsKey("ambientBlue")) {
								int newBlue = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("ambientBlue"), variables));
								newBlue = ModelFunctions.truncateInt(newBlue, 0, 255);
								ambientColorUpdated = true;
								
								ambientColorInt = (ambientColorInt & 0xFF00FFFF) | (newBlue << 16);
								rowValues[allColumns.getPositionOf("ambientBlue")] = String.valueOf(newBlue);
							}
							
							if (ambientColorUpdated) {
								composedSubRecordData.setValueByStringFor(String.valueOf(ambientColorInt), "ambientColor");
							}
							
							
							// sunlight color
							boolean sunlightColorUpdated = false;
							int sunlightColorInt = composedSubRecordData.getValueAsIntOf("sunlightColor");
							
							if (updateValues.containsKey("sunlightRed")) {
								int newRed = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("sunlightRed"), variables));
								newRed = ModelFunctions.truncateInt(newRed, 0, 255);
								sunlightColorUpdated = true;
								
								sunlightColorInt = (sunlightColorInt & 0xFFFFFF00) | newRed;
								rowValues[allColumns.getPositionOf("sunlightRed")] = String.valueOf(newRed);
							}
							
							if (updateValues.containsKey("sunlightGreen")) {
								int newGreen = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("sunlightGreen"), variables));
								newGreen = ModelFunctions.truncateInt(newGreen, 0, 255);
								sunlightColorUpdated = true;
								
								sunlightColorInt = (sunlightColorInt & 0xFFFF00FF) | (newGreen << 8);
								rowValues[allColumns.getPositionOf("sunlightGreen")] = String.valueOf(newGreen);
							}
							
							if (updateValues.containsKey("sunlightBlue")) {
								int newBlue = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("sunlightBlue"), variables));
								newBlue = ModelFunctions.truncateInt(newBlue, 0, 255);
								sunlightColorUpdated = true;
								
								sunlightColorInt = (sunlightColorInt & 0xFF00FFFF) | (newBlue << 16);
								rowValues[allColumns.getPositionOf("sunlightBlue")] = String.valueOf(newBlue);
							}
							
							if (sunlightColorUpdated) {
								composedSubRecordData.setValueByStringFor(String.valueOf(sunlightColorInt),"sunlightColor");
							}
							
							
							//fog color
							boolean fogColorUpdated = false;
							int fogColorInt = composedSubRecordData.getValueAsIntOf("fogColor");
							
							if (updateValues.containsKey("fogRed")) {
								int newRed = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("fogRed"), variables));
								newRed = ModelFunctions.truncateInt(newRed, 0, 255);
								fogColorUpdated = true;
								
								fogColorInt = (fogColorInt & 0xFFFFFF00) | newRed;
								rowValues[allColumns.getPositionOf("fogRed")] = String.valueOf(newRed);
							}
							
							if (updateValues.containsKey("fogGreen")) {
								int newGreen = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("fogGreen"), variables));
								newGreen = ModelFunctions.truncateInt(newGreen, 0, 255);
								fogColorUpdated = true;
								
								fogColorInt = (fogColorInt & 0xFFFF00FF) | (newGreen << 8);
								rowValues[allColumns.getPositionOf("fogGreen")] = String.valueOf(newGreen);
							}
							
							if (updateValues.containsKey("fogBlue")) {
								int newBlue = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("fogBlue"), variables));
								newBlue = ModelFunctions.truncateInt(newBlue, 0, 255);
								fogColorUpdated = true;
								
								fogColorInt = (fogColorInt & 0xFF00FFFF) | (newBlue << 16);
								rowValues[allColumns.getPositionOf("fogBlue")] = String.valueOf(newBlue);
							}
							
							if (fogColorUpdated) {
								composedSubRecordData.setValueByStringFor(String.valueOf(fogColorInt),"fogColor");
							}
							
							
							//fog density
							if (updateValues.containsKey("fogDensity")) {
								float newValue = ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("fogDensity"), variables);
								composedSubRecordData.setValueByStringFor(String.valueOf(newValue),"fogDensity");
								
								rowValues[allColumns.getPositionOf("fogDensity")] = String.valueOf(newValue);
							}
						}
					}
					
					//now check whether the region value could be set or if there was no existing region entry that could be updated.
					if (regionUpdated == false && updateValues.containsKey("region")) {
						String newValue = ModelFunctions.evaluateStringExpression(updateValues.get("region"), stringVariables);
						
						SubRecordDataString subRecordData = new SubRecordDataString(newValue, "region", 0, 999);
						SubRecord newSubRecord = new SubRecord("RGNN", subRecordData, record);
						record.getSubRecords().add(2, newSubRecord);
						rowValues[allColumns.getPositionOf("region")] = newValue;
					}
				}
				
				
				if (delete) {
					//search for a pathgrid record belonging to this interior cell
					boolean pathgridFound = false;
					for (int i2 = 0; i2 < records.size(); i2++) {
						Record searchRecord = records.get(i2);
						
						if (!searchRecord.getName().equals("PGRD")) {
							continue;
						}
						
						for (SubRecord nextSubRecord : searchRecord.getSubRecords()) {
							if (nextSubRecord.getName().equals("NAME")) {
								SubRecordDataString stringSubRecordData = (SubRecordDataString)nextSubRecord.getData();
								
								if (stringSubRecordData.getValueAsString().equals(rowValues[allColumns.getPositionOf("name")])) {
									records.remove(i2);
									pathgridFound = true;
									
									if (i2 < i1) {
										i1--;
									}
								}
								break;
							}
						}
						
						if (pathgridFound) {
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
		return "interiorCells";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}

}
