package database.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import database.Columns;
import database.Row;
import database.where.Comparison;
import model.ModelFunctions;
import model.Record;
import model.SubRecord;
import model.subrecorddata.SubRecordDataFloat;
import model.subrecorddata.SubRecordDataInt;
import model.subrecorddata.SubRecordDataObjectCoordinates;
import model.subrecorddata.SubRecordDataString;


public class CellObjectsTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {
					"cell", "cellRegion", "id", "scale",
					"xPos", "yPos", "zPos",
					"xRotate", "yRotate", "zRotate"
			},
			new Columns.Type[] {
					Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.number,
					Columns.Type.number, Columns.Type.number, Columns.Type.number,
					Columns.Type.number, Columns.Type.number, Columns.Type.number
			},
			new boolean[] {
					false, false, true, true,
					true, true, true,
					true, true, true
			}
	);
	
	public List<Row> insert(List<Record> records, List<Row> rows) {
		return this.executeCRUD(records, new Columns(new String[0], new Columns.Type[0]), null, null, rows, false);
	}
	
	public List<Row> select(List<Record> records, Columns columns, Comparison whereComparison) {
		return this.executeCRUD(records, columns, whereComparison, null, null, false);
	}
	
	public List<Row> update(List<Record> records, Comparison whereComparison, Map<String,String> updateValues) {
		if (allColumns.containsNonUpdatableFields(updateValues)) {
			System.out.println("One or more of the specified columns cannot be updated.");
		}
		
		return this.executeCRUD(records, this.getAllColumns(), whereComparison, updateValues, null, false);
	}
	
	public List<Row> delete(List<Record> records, Comparison whereComparison) {
		return this.executeCRUD(records, this.getAllColumns(), whereComparison, null, null, true);
	}
	
	private List<Row> executeCRUD(List<Record> records, Columns selectedColumns, Comparison whereComparison, Map<String,String> updateValues, List<Row> insertRows, boolean delete) {
		List<Row> returnRows = new ArrayList<Row>();
		
		for (Record record : records) {
			if (!record.getName().equals("CELL")) {
				continue;
			}
			
			List<SubRecord> allSubRecords = record.getSubRecords();
			boolean cellObjectsStarted = false;
			int indexOffsetByDeletion = 0;
			int insertRowIndex = 1;
			
			List<SubRecord> subRecordsOfCellObject = new LinkedList<SubRecord>();
			String[] currentCellObjectRowValues = null;
			String currentCellName = null;
			String currentRegionName = "";
			
			for (int i = 0; i < allSubRecords.size(); i++) {
				SubRecord subRecord = allSubRecords.get(i);
				
				//if the references have not begun yet, all fields describe the cell rather than the objects in it.
				if (!cellObjectsStarted) {
					 if (subRecord.getName().equals("NAME")) {
						currentCellName = subRecord.getData().getValueAsString();
						continue;
					} else if (subRecord.getName().equals("RGNN")) {
						currentRegionName = subRecord.getData().getValueAsString();
						continue;
					}
				}
				
				//FRMR is always the first entry of an object in a cell
				if (subRecord.getName().equals("FRMR")) {
					if (!cellObjectsStarted) {
						cellObjectsStarted = true;
					}
					
					//if cellObjects got deleted before this one, the index of following objects needs to be decremented to join up with elements coming before these
					if (indexOffsetByDeletion != 0) {
						subRecord.getData().setValueByString("index-"+indexOffsetByDeletion);
					}
					
					insertRowIndex++;
					subRecordsOfCellObject.clear();
					subRecordsOfCellObject.add(subRecord);
					
					currentCellObjectRowValues = new String[allColumns.getLength()];
					currentCellObjectRowValues[allColumns.getPositionOf("cell")] = currentCellName;
					currentCellObjectRowValues[allColumns.getPositionOf("cellRegion")] = currentRegionName;
					
					continue;
				}
				
				if (cellObjectsStarted) {
					subRecordsOfCellObject.add(subRecord);
					
					if (subRecord.getName().equals("NAME")) {
						currentCellObjectRowValues[allColumns.getPositionOf("id")] = subRecord.getData().getValueAsString();
						
					} else if (subRecord.getName().equals("XSCL")) {
						currentCellObjectRowValues[allColumns.getPositionOf("scale")] = subRecord.getData().getValueAsString();
						
					} else if (subRecord.getName().equals("DATA") || subRecord.getName().equals("DELE")) {
						SubRecordDataObjectCoordinates coordinateData;
						
						if (subRecord.getName().equals("DATA")) {
							coordinateData = (SubRecordDataObjectCoordinates)subRecord.getData();
						} else {
							//if the object was deleted, add fake coordinates, so the row can be processed.
							coordinateData = new SubRecordDataObjectCoordinates(new byte[24]);
						}
						
						currentCellObjectRowValues[allColumns.getPositionOf("xPos")] = ModelFunctions.formatFloatNumber(coordinateData.getxPos(), 3);
						currentCellObjectRowValues[allColumns.getPositionOf("yPos")] = ModelFunctions.formatFloatNumber(coordinateData.getyPos(), 3);
						currentCellObjectRowValues[allColumns.getPositionOf("zPos")] = ModelFunctions.formatFloatNumber(coordinateData.getzPos(), 3);
						currentCellObjectRowValues[allColumns.getPositionOf("xRotate")] = ModelFunctions.formatFloatNumber(coordinateData.getxRotate(), 3);
						currentCellObjectRowValues[allColumns.getPositionOf("yRotate")] = ModelFunctions.formatFloatNumber(coordinateData.getyRotate(), 3);
						currentCellObjectRowValues[allColumns.getPositionOf("zRotate")] = ModelFunctions.formatFloatNumber(coordinateData.getzRotate(), 3);
							
						//if no xscl entry has come before this, set scale to 1.00 manually 
						if (currentCellObjectRowValues[allColumns.getPositionOf("scale")] == null) {
							currentCellObjectRowValues[allColumns.getPositionOf("scale")] = "1.00";
						}
						
						//DATA is always the last entry of an object in a cell.
						//important, the row is initially created with all fields of the table to enable testing against the sql-condition.
						Row newRow = new Row(allColumns, currentCellObjectRowValues);
						if (whereComparison != null && whereComparison.testConditionOnRow(newRow) == true) {
							
							//if the query does not require all columns but only a fraction
							if (selectedColumns != allColumns) {
								newRow = newRow.createFractionRow(selectedColumns);
								currentCellObjectRowValues = newRow.getValues();
							}
							
							//add the new row to the return collection.
							returnRows.add(newRow);
							
							//if the row is supposed to get deleted
							if (delete) {
								for (SubRecord deleteSubRecord: subRecordsOfCellObject) {
									allSubRecords.remove(deleteSubRecord);
									i--;
								}
								indexOffsetByDeletion++;
								insertRowIndex--;
							}
							
							//if values are supposed to get updated
							else if (updateValues != null && updateValues.size() > 0) {
								boolean hasXSCL = false;
								
								for (SubRecord updateSubRecord: subRecordsOfCellObject) {
									if (updateSubRecord.getName().equals("NAME")) {
										if (updateValues.containsKey("id")) {
											updateSubRecord.getData().setValueByString(updateValues.get("id"));
											currentCellObjectRowValues[selectedColumns.getPositionOf("id")] = updateSubRecord.getData().getValueAsString();
											
											//for optimization, if only the id is updated.
											if (updateValues.size() == 1) {
												break;
											}
										}
										
									} else if (updateSubRecord.getName().equals("XSCL")) {
										hasXSCL = true;
										if (updateValues.containsKey("scale")) {
											updateSubRecord.getData().setValueByString(updateValues.get("scale"));
											currentCellObjectRowValues[selectedColumns.getPositionOf("scale")] = updateSubRecord.getData().getValueAsString();
										}
										
									} else if (updateSubRecord.getName().equals("DATA")) {
										SubRecordDataObjectCoordinates updateSubRecordCoordinateData = (SubRecordDataObjectCoordinates)updateSubRecord.getData();
										
										Map<String,Float> variables = new HashMap<String,Float>();
										variables.put("xPos", updateSubRecordCoordinateData.getxPos());
										variables.put("yPos", updateSubRecordCoordinateData.getyPos());
										variables.put("zPos", updateSubRecordCoordinateData.getzPos());
										variables.put("xRotate", updateSubRecordCoordinateData.getxRotate());
										variables.put("yRotate", updateSubRecordCoordinateData.getyRotate());
										variables.put("zRotate", updateSubRecordCoordinateData.getzRotate());
										
										if (updateValues.containsKey("xPos")) {
											updateSubRecordCoordinateData.setxPos(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("xPos"), variables));
											currentCellObjectRowValues[selectedColumns.getPositionOf("xPos")] = ModelFunctions.formatFloatNumber(updateSubRecordCoordinateData.getxPos(), 3);
										}
										if (updateValues.containsKey("yPos")) {
											updateSubRecordCoordinateData.setyPos(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("yPos"), variables));
											currentCellObjectRowValues[selectedColumns.getPositionOf("yPos")] = ModelFunctions.formatFloatNumber(updateSubRecordCoordinateData.getyPos(), 3);
										}
										if (updateValues.containsKey("zPos")) {
											updateSubRecordCoordinateData.setzPos(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("zPos"), variables));
											currentCellObjectRowValues[selectedColumns.getPositionOf("zPos")] = ModelFunctions.formatFloatNumber(updateSubRecordCoordinateData.getzPos(), 3);
										}
										if (updateValues.containsKey("xRotate")) {
											updateSubRecordCoordinateData.setxRotate(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("xRotate"), variables));
											currentCellObjectRowValues[selectedColumns.getPositionOf("xRotate")] = ModelFunctions.formatFloatNumber(updateSubRecordCoordinateData.getxRotate(), 3);
										}
										if (updateValues.containsKey("yRotate")) {
											updateSubRecordCoordinateData.setyRotate(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("yRotate"), variables));
											currentCellObjectRowValues[selectedColumns.getPositionOf("yRotate")] = ModelFunctions.formatFloatNumber(updateSubRecordCoordinateData.getyRotate(), 3);
										}
										if (updateValues.containsKey("zRotate")) {
											updateSubRecordCoordinateData.setzRotate(ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("zRotate"), variables));
											currentCellObjectRowValues[selectedColumns.getPositionOf("zRotate")] = ModelFunctions.formatFloatNumber(updateSubRecordCoordinateData.getzRotate(), 3);
										}
										
										//this cell object does not have an xscl-record assigned to it, but the scale is supposed to get updated
										if (!hasXSCL && updateValues.containsKey("scale")) {
											Map<String,Float> scaleVariable = new HashMap<String,Float>();
											scaleVariable.put("scale", new Float(1));
											float scale = ModelFunctions.evaluateMathematicalExpressionFloat(updateValues.get("scale"), scaleVariable);
											currentCellObjectRowValues[allColumns.getPositionOf("scale")] = ModelFunctions.formatFloatNumber(scale, 2);
											
											//an XSCL needs to be manually added now, since there is none yet. Only objects with scale 1.0 don't need one.
											if (scale != 1f) {
												SubRecord scaleSubRecord = new SubRecord("XSCL", new SubRecordDataFloat(scale,"scale",2), record);
												allSubRecords.add(i, scaleSubRecord);
												i++;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			//after iteration through all cellObjects insert new objects at the found insertIndex
			if (insertRows != null) {
				for (int i = 0; i < insertRows.size(); i++) {
					Row insertRow = insertRows.get(i);
					String[] newCellObjectRowValues = new String[allColumns.getLength()]; 
					
					//if the rows are only inserted into a certain cell, omit the cells that don't match
					
					if (insertRow.hasColumn("cell")) {
						String cell = ModelFunctions.evaluateStringExpression(insertRow.getValueOf("cell"));
						if (!cell.equalsIgnoreCase(currentCellName)) {
							continue;
						}
						
						newCellObjectRowValues[allColumns.getPositionOf("cell")] = currentCellName;
					}
					
					if (insertRow.hasColumn("cellRegion")) {
						String region = ModelFunctions.evaluateStringExpression(insertRow.getValueOf("cellRegion"));
						if (!region.equalsIgnoreCase(currentRegionName)) {
							continue;
						}
						
						newCellObjectRowValues[allColumns.getPositionOf("cellRegion")] = currentRegionName;
					}
					
					//FRMR
					SubRecord indexSubRecord = new SubRecord("FRMR", new SubRecordDataInt(insertRowIndex,"index"), record);
					allSubRecords.add(indexSubRecord);
					insertRowIndex++;
					
					//NAME
					if (!insertRow.hasColumn("id")) {
						System.out.println("an id needs to be specified to create a new cell object");
						return null;
					}
					String id = ModelFunctions.evaluateStringExpression(insertRow.getValueOf("id"));
					SubRecord idSubRecord = new SubRecord("NAME", new SubRecordDataString(id, "id", 1, 32), record);
					allSubRecords.add(idSubRecord);
					newCellObjectRowValues[allColumns.getPositionOf("id")] = insertRow.getValueOf("id");
					
					//XSCL
					float scale;
					if (insertRow.hasColumn("scale")) {
						scale = ModelFunctions.evaluateMathematicalExpressionFloat(insertRow.getValueOf("scale"));
						if (scale != 1f) {
							SubRecord scaleSubRecord = new SubRecord("XSCL", new SubRecordDataFloat(scale,"scale",2), record);
							allSubRecords.add(scaleSubRecord);
						}
					} else {
						scale = 1f;
					}
					newCellObjectRowValues[allColumns.getPositionOf("scale")] = ModelFunctions.formatFloatNumber(scale, 2);
					
					//DATA
					SubRecordDataObjectCoordinates newSubRecordCoordinateData = new SubRecordDataObjectCoordinates(new byte[24]);
					
					if (insertRow.hasColumn("xPos")) {
						newSubRecordCoordinateData.setxPos(ModelFunctions.evaluateMathematicalExpressionFloat(insertRow.getValueOf("xPos")));
					}
					newCellObjectRowValues[allColumns.getPositionOf("xPos")] = ModelFunctions.formatFloatNumber(newSubRecordCoordinateData.getxPos(), 3);
					
					if (insertRow.hasColumn("yPos")) {
						newSubRecordCoordinateData.setyPos(ModelFunctions.evaluateMathematicalExpressionFloat(insertRow.getValueOf("yPos")));
					}
					newCellObjectRowValues[allColumns.getPositionOf("yPos")] = ModelFunctions.formatFloatNumber(newSubRecordCoordinateData.getyPos(), 3);
					
					if (insertRow.hasColumn("zPos")) {
						newSubRecordCoordinateData.setzPos(ModelFunctions.evaluateMathematicalExpressionFloat(insertRow.getValueOf("zPos")));
					}
					newCellObjectRowValues[allColumns.getPositionOf("zPos")] = ModelFunctions.formatFloatNumber(newSubRecordCoordinateData.getzPos(), 3);
					
					if (insertRow.hasColumn("xRotate")) {
						newSubRecordCoordinateData.setxRotate(ModelFunctions.evaluateMathematicalExpressionFloat(insertRow.getValueOf("xRotate")));
					}
					newCellObjectRowValues[allColumns.getPositionOf("xRotate")] = ModelFunctions.formatFloatNumber(newSubRecordCoordinateData.getxRotate(), 3);
					
					if (insertRow.hasColumn("yRotate")) {
						newSubRecordCoordinateData.setyRotate(ModelFunctions.evaluateMathematicalExpressionFloat(insertRow.getValueOf("yRotate")));
					}
					newCellObjectRowValues[allColumns.getPositionOf("yRotate")] = ModelFunctions.formatFloatNumber(newSubRecordCoordinateData.getyRotate(), 3);
					
					if (insertRow.hasColumn("zRotate")) {
						newSubRecordCoordinateData.setzRotate(ModelFunctions.evaluateMathematicalExpressionFloat(insertRow.getValueOf("zRotate")));
					}
					newCellObjectRowValues[allColumns.getPositionOf("zRotate")] = ModelFunctions.formatFloatNumber(newSubRecordCoordinateData.getzRotate(), 3);
					
					SubRecord coordinatesSubRecord = new SubRecord("DATA", newSubRecordCoordinateData, record);
					allSubRecords.add(coordinatesSubRecord);
					
					returnRows.add(new Row(allColumns, newCellObjectRowValues));
				}
			}
		}
		
		return returnRows;
	}
	
	public String getName() {
		return "cellObjects";
	}
	
	public Columns getAllColumns() {
		return allColumns;
	}

}
