package database.table;

import java.util.ArrayList;
import java.util.Arrays;
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
import model.subrecorddata.SubRecordData;
import model.subrecorddata.SubRecordDataByte;
import model.subrecorddata.SubRecordDataComposed;
import model.subrecorddata.SubRecordDataFloat;
import model.subrecorddata.SubRecordDataInt;
import model.subrecorddata.SubRecordDataSCVR;
import model.subrecorddata.SubRecordDataString;

public class DialogueResponsesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {
					"topic", "id", "previousResponseId", "followingResponseId",
					"disposition", "gender", "npc", "race",
					"class", "faction", "rank", "cell",
					"playersFaction", "playersRank", "text", "resultText",
					"sound", "journalName", "journalFinish", "journalRestart",
					"func1Type", "func1Func", "func1Value", "func1CompareOp", "func1TargetValue",
					"func2Type", "func2Func", "func2Value", "func2CompareOp", "func2TargetValue",
					"func3Type", "func3Func", "func3Value", "func3CompareOp", "func3TargetValue",
					"func4Type", "func4Func", "func4Value", "func4CompareOp", "func4TargetValue",
					"func5Type", "func5Func", "func5Value", "func5CompareOp", "func5TargetValue",
					"func6Type", "func6Func", "func6Value", "func6CompareOp", "func6TargetValue"
			},
			new Columns.Type[] {
					Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.text,
					Columns.Type.integer, Columns.Type.integer, Columns.Type.text, Columns.Type.text,
					Columns.Type.text, Columns.Type.text, Columns.Type.integer, Columns.Type.text,
					Columns.Type.text, Columns.Type.integer, Columns.Type.text, Columns.Type.text,
					Columns.Type.text, Columns.Type.integer, Columns.Type.integer, Columns.Type.integer,
					Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.number,
					Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.number,
					Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.number,
					Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.number,
					Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.number,
					Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.text, Columns.Type.number
			},
			new boolean[] {
					false, true, true ,true,
					true, true, true, true,
					true, true, true, true,
					true, true, true, true,
					true, true, true, true,
					true, true, true, true, true,
					true, true, true, true, true,
					true, true, true, true, true,
					true, true, true, true, true,
					true, true, true, true, true,
					true, true, true, true, true
			}
	);
	
	private final List<String> infoSubRecordsInOrder = Arrays.asList(new String[] {
			"INAM", "PNAM", "NNAM", "DATA", "ONAM", "RNAM", "CNAM", "FNAM", "ANAM", "DNAM", "NAME", "SNAM",
			"QSTN", "QSTF", "QSTR", "SCVR", "INTV", "FLTV", "BNAM"
	});

	public List<Row> insert(List<Record> records, List<Row> rows) {
		System.out.println("This table does not support insertion of rows yet.");
		return null;
	}
	
	public List<Row> select(List<Record> records, Columns columns, Comparison whereComparison) {
		return this.executeRUD(records, columns, whereComparison, null, false);
	}
	
	public List<Row> update(List<Record> records, Comparison whereComparison, Map<String,String> updateValues) {
		if (allColumns.containsNonUpdatableFields(updateValues)) {
			System.out.println("One or more of the specified columns cannot be updated.");
		}
		
		return this.executeRUD(records, this.getAllColumns(), whereComparison, updateValues, false);
	}
	
	public List<Row> delete(List<Record> records, Comparison whereComparison) {
		return this.executeRUD(records, this.getAllColumns(), whereComparison, null, true);
	}
	
	private List<Row> executeRUD(List<Record> records, Columns selectedColumns, Comparison whereComparison, Map<String,String> updateValues, boolean delete) {
		List<Row> returnRows = new ArrayList<Row>();
		String currentTopic = null;
		
		for (int i1 = 0; i1 < records.size(); i1++) {
			Record record = records.get(i1);
			
			if (record.getName().equals("DIAL")) {
				for (SubRecord subRecord : record.getSubRecords()) {
					if (subRecord.getName().equals("NAME")) {
						currentTopic = subRecord.getData().getValueAsString();
						break;
					}
				}
			}
			
			if (!record.getName().equals("INFO")) {
				continue;
			}
			
			String[] currentDialogueResponseRowValues = allColumns.createEmptyRowValues();
			currentDialogueResponseRowValues[allColumns.getPositionOf("topic")] = currentTopic;
			
			String currentResponseId = null;
			String previousResponseId = null;
			String nextResponseId = null;
			int currentSCVR = 0;
			
			//go trough the subrecords for this dialogue response
			List<SubRecord> allSubRecords = record.getSubRecords();
			for (int i = 0; i < allSubRecords.size(); i++) {
				SubRecord subRecord = allSubRecords.get(i);
				
				if (subRecord.getName().equals("INAM")) {
					currentResponseId = subRecord.getData().getValueAsString();
					currentDialogueResponseRowValues[allColumns.getPositionOf("id")] = currentResponseId;
				} else if (subRecord.getName().equals("PNAM")) {
					previousResponseId = subRecord.getData().getValueAsString();
					currentDialogueResponseRowValues[allColumns.getPositionOf("previousResponseId")] = previousResponseId;
				} else if (subRecord.getName().equals("NNAM")) {
					nextResponseId = subRecord.getData().getValueAsString();
					currentDialogueResponseRowValues[allColumns.getPositionOf("followingResponseId")] = nextResponseId;
				} else if (subRecord.getName().equals("DATA")) {
					SubRecordDataComposed composedData = (SubRecordDataComposed)subRecord.getData();
					currentDialogueResponseRowValues[allColumns.getPositionOf("disposition")] = composedData.getValueAsStringOf("disposition");
					currentDialogueResponseRowValues[allColumns.getPositionOf("rank")] = composedData.getValueAsStringOf("rank");
					currentDialogueResponseRowValues[allColumns.getPositionOf("gender")] = composedData.getValueAsStringOf("gender");
					currentDialogueResponseRowValues[allColumns.getPositionOf("playersRank")] = composedData.getValueAsStringOf("playersRank");
				} else if (subRecord.getName().equals("ONAM")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("npc")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("RNAM")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("race")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("CNAM")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("class")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("FNAM")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("faction")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("ANAM")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("cell")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("DNAM")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("playersFaction")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("NAME")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("text")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("SNAM")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("sound")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("QSTN")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("journalName")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("QSTF")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("journalFinish")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("QSTR")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("journalRestart")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("SCVR")) {
					SubRecordDataSCVR scvrData = (SubRecordDataSCVR)subRecord.getData();
					currentSCVR = scvrData.getNumberIndex()+1;
					currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVR+"Type")] = scvrData.getValueAsStringOf("type");
					currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVR+"Func")] = scvrData.getValueAsStringOf("function");
					currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVR+"Value")] = scvrData.getValueAsStringOf("value");
					currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVR+"CompareOp")] = scvrData.getValueAsStringOf("compareOp");
				} else if (subRecord.getName().equals("INTV") || subRecord.getName().equals("FLTV")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVR+"TargetValue")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("BNAM")) {
					currentDialogueResponseRowValues[allColumns.getPositionOf("resultText")] = subRecord.getData().getValueAsString();
				}
			}
			
			//now see if the rows fit with the whereConditions
			Row newRow = new Row(allColumns,currentDialogueResponseRowValues);
			if (whereComparison.testConditionOnRow(newRow)) {
				
				if (updateValues != null) {
					int currentSCVRUpdate = 0;
					List<String> updatedFields = new LinkedList<String>();
					
					for (int i = 0; i < allSubRecords.size(); i++) {
						SubRecord subRecord = allSubRecords.get(i);
						
						if (subRecord.getName().equals("INAM") && updateValues.containsKey("id")) {
							subRecord.getData().setValueByString(updateValues.get("id"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("id")] = subRecord.getData().getValueAsString();
							updatedFields.add("id");
							
						} else if (subRecord.getName().equals("PNAM") && updateValues.containsKey("previousResponseId")) {
							subRecord.getData().setValueByString(updateValues.get("previousResponseId"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("previousResponseId")] = subRecord.getData().getValueAsString();
							updatedFields.add("previousResponseId");
							
						} else if (subRecord.getName().equals("NNAM") && updateValues.containsKey("followingResponseId")) {
							subRecord.getData().setValueByString(updateValues.get("followingResponseId"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("followingResponseId")] = subRecord.getData().getValueAsString();
							updatedFields.add("followingResponseId");
							
						} else if (subRecord.getName().equals("DATA")) {
							SubRecordDataComposed composedData = (SubRecordDataComposed)subRecord.getData();
							
							if (updateValues.containsKey("disposition")) {
								composedData.setValueByStringFor(updateValues.get("disposition"), "disposition");
								currentDialogueResponseRowValues[allColumns.getPositionOf("disposition")] = composedData.getValueAsStringOf("disposition");
								updatedFields.add("disposition");
							}
							
							if (updateValues.containsKey("rank")) {
								composedData.setValueByStringFor(updateValues.get("rank"), "rank");
								currentDialogueResponseRowValues[allColumns.getPositionOf("rank")] = composedData.getValueAsStringOf("rank");
								updatedFields.add("rank");
							}
							
							if (updateValues.containsKey("gender")) {
								composedData.setValueByStringFor(updateValues.get("gender"), "gender");
								currentDialogueResponseRowValues[allColumns.getPositionOf("gender")] = composedData.getValueAsStringOf("gender");
								updatedFields.add("gender");
							}
							
							if (updateValues.containsKey("playersRank")) {
								composedData.setValueByStringFor(updateValues.get("playersRank"), "playersRank");
								currentDialogueResponseRowValues[allColumns.getPositionOf("playersRank")] = composedData.getValueAsStringOf("playersRank");
								updatedFields.add("playersRank");
							}
							
						} else if (subRecord.getName().equals("ONAM") && updateValues.containsKey("npc")) {
							subRecord.getData().setValueByString(updateValues.get("npc"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("npc")] = subRecord.getData().getValueAsString();
							updatedFields.add("npc");
							//the new value is an empty string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("")) {
								allSubRecords.remove(i);
								i--;
							}
							
						} else if (subRecord.getName().equals("RNAM") && updateValues.containsKey("race")) {
							subRecord.getData().setValueByString(updateValues.get("race"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("race")] = subRecord.getData().getValueAsString();
							updatedFields.add("race");
							//the new value is an empty string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("")) {
								allSubRecords.remove(i);
								i--;
							}
							
						} else if (subRecord.getName().equals("CNAM") && updateValues.containsKey("class")) {
							subRecord.getData().setValueByString(updateValues.get("class"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("class")] = subRecord.getData().getValueAsString();
							updatedFields.add("class");
							//the new value is an empty string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("")) {
								allSubRecords.remove(i);
								i--;
							}
							
						} else if (subRecord.getName().equals("FNAM") && updateValues.containsKey("faction")) {
							subRecord.getData().setValueByString(updateValues.get("faction"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("faction")] = subRecord.getData().getValueAsString();
							updatedFields.add("faction");
							//the new value is an empty string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("")) {
								allSubRecords.remove(i);
								i--;
							}
							
						} else if (subRecord.getName().equals("ANAM") && updateValues.containsKey("cell")) {
							subRecord.getData().setValueByString(updateValues.get("cell"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("cell")] = subRecord.getData().getValueAsString();
							updatedFields.add("cell");
							//the new value is an empty string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("")) {
								allSubRecords.remove(i);
								i--;
							}
							
						} else if (subRecord.getName().equals("DNAM") && updateValues.containsKey("playersFaction")) {
							subRecord.getData().setValueByString(updateValues.get("playersFaction"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("playersFaction")] = subRecord.getData().getValueAsString();
							updatedFields.add("playersFaction");
							//the new value is an empty string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("")) {
								allSubRecords.remove(i);
								i--;
							}
							
						} else if (subRecord.getName().equals("NAME") && updateValues.containsKey("text")) {
							subRecord.getData().setValueByString(updateValues.get("text"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("text")] = subRecord.getData().getValueAsString();
							updatedFields.add("text");
							
						} else if (subRecord.getName().equals("SNAM") && updateValues.containsKey("sound")) {
							subRecord.getData().setValueByString(updateValues.get("sound"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("sound")] = subRecord.getData().getValueAsString();
							updatedFields.add("sound");
							//the new value is an empty string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("")) {
								allSubRecords.remove(i);
								i--;
							}
							
						} else if (subRecord.getName().equals("QSTN") && updateValues.containsKey("journalName")) {
							subRecord.getData().setValueByString(updateValues.get("journalName"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("journalName")] = subRecord.getData().getValueAsString();
							updatedFields.add("journalName");
							//the new value is a 0 string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("0")) {
								allSubRecords.remove(i);
								i--;
							}
						
						} else if (subRecord.getName().equals("QSTF") && updateValues.containsKey("journalFinish")) {
							subRecord.getData().setValueByString(updateValues.get("journalFinish"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("journalFinish")] = subRecord.getData().getValueAsString();
							updatedFields.add("journalFinish");
							//the new value is a 0 string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("0")) {
								allSubRecords.remove(i);
								i--;
							}
						
						} else if (subRecord.getName().equals("QSTR") && updateValues.containsKey("journalRestart")) {
							subRecord.getData().setValueByString(updateValues.get("journalRestart"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("journalRestart")] = subRecord.getData().getValueAsString();
							updatedFields.add("journalRestart");
							//the new value is a 0 string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("0")) {
								allSubRecords.remove(i);
								i--;
							}
						
						} else if (subRecord.getName().equals("SCVR")) {
							SubRecordDataSCVR scvrData = (SubRecordDataSCVR)subRecord.getData();
							currentSCVRUpdate = scvrData.getNumberIndex()+1;
							
							if (updateValues.containsKey("func"+currentSCVRUpdate+"Type")) {
								scvrData.setValueByStringFor(updateValues.get("func"+currentSCVRUpdate+"Type"), "type");
								currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVRUpdate+"Type")] = scvrData.getValueAsStringOf("type");
								updatedFields.add("func"+currentSCVRUpdate+"Type");
								
							}
							if (updateValues.containsKey("func"+currentSCVRUpdate+"Func")) {
								scvrData.setValueByStringFor(updateValues.get("func"+currentSCVRUpdate+"Func"), "function");
								currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVRUpdate+"Func")] = scvrData.getValueAsStringOf("function");
								updatedFields.add("func"+currentSCVRUpdate+"Func");
								
							}
							if (updateValues.containsKey("func"+currentSCVRUpdate+"Value")) {
								scvrData.setValueByStringFor(updateValues.get("func"+currentSCVRUpdate+"Value"), "value");
								currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVRUpdate+"Value")] = scvrData.getValueAsStringOf("value");
								updatedFields.add("func"+currentSCVRUpdate+"Value");
								
							}
							if (updateValues.containsKey("func"+currentSCVRUpdate+"CompareOp")) {
								scvrData.setValueByStringFor(updateValues.get("func"+currentSCVRUpdate+"CompareOp"), "compareOp");
								currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVRUpdate+"CompareOp")] = scvrData.getValueAsStringOf("compareOp");
								updatedFields.add("func"+currentSCVRUpdate+"CompareOp");
								
							}
							
							//wenn alle felder dieses SCVRs auf leer gesetzt sind, kann es entfernt werden
							if (currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVRUpdate+"Type")].equals("") &&
								currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVRUpdate+"Func")].equals("") &&
								currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVRUpdate+"Value")].equals("") &&
								currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVRUpdate+"CompareOp")].equals("")) {
								
								//remove the SCVR subRecord
								allSubRecords.remove(i);
								//remove the target value subrecord (INTV, FLTV) behind it.
								if (allSubRecords.get(i).getName().equals("INTV") || allSubRecords.get(i).getName().equals("FLTV")) {
									currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVR+"TargetValue")] = "0";
									updatedFields.add("func"+currentSCVR+"TargetValue");
									allSubRecords.remove(i);
								}
								i--;
							}
							
						} else if ((subRecord.getName().equals("INTV") || subRecord.getName().equals("FLTV")) && updateValues.containsKey("func"+currentSCVR+"TargetValue")) {
							subRecord.getData().setValueByString(updateValues.get("func"+currentSCVR+"TargetValue"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("func"+currentSCVR+"TargetValue")] = subRecord.getData().getValueAsString();
							updatedFields.add("func"+currentSCVR+"TargetValue");
							
						} else if (subRecord.getName().equals("BNAM") && updateValues.containsKey("resultText")) {
							subRecord.getData().setValueByString(updateValues.get("resultText"));
							currentDialogueResponseRowValues[allColumns.getPositionOf("resultText")] = subRecord.getData().getValueAsString();
							updatedFields.add("resultText");
							//the new value is an empty string, remove the entry altogether.
							if (subRecord.getData().getValueAsString().equals("")) {
								allSubRecords.remove(i);
								i--;
							}
						}
					}
					
					//next see if some of the fields have not been updated due to the respective subrecord not being present in this record
					Map<String, String> variablesString = new HashMap<String, String>();
					Map<String, Float> variablesNumber = new HashMap<String, Float>();
					String resultString = null;
					float resultNumber = 0;
					
					//if new scvrs need to be created, store them here.
					SubRecordDataSCVR[] scvrs = new SubRecordDataSCVR[6];
					float[] scvrTargetValues = new float[6];
					
					for (String key : updateValues.keySet()) {
						if (updatedFields.contains(key)) {
							continue;
						}
						
						if (allColumns.isNumeric(key)) {
							variablesNumber.put(key, new Float(0));
							resultNumber = ModelFunctions.evaluateMathematicalExpression(updateValues.get(key), variablesNumber);
							
							if (resultNumber % 1 == 0) {
								currentDialogueResponseRowValues[allColumns.getPositionOf(key)] = String.valueOf((int)resultNumber);
							} else {
								currentDialogueResponseRowValues[allColumns.getPositionOf(key)] = String.valueOf(resultNumber);
							}
						} else {
							variablesString.put(key, "");
							resultString = ModelFunctions.evaluateStringExpression(updateValues.get(key), variablesString);
							currentDialogueResponseRowValues[allColumns.getPositionOf(key)] = resultString;
						}
						
						SubRecord subRecord = null;
						if (key.equals("npc")) {
							if (resultString.equals("")) {
								continue;
							}
							SubRecordData data = new SubRecordDataString(resultString, key, 1, 32);
							subRecord = new SubRecord("ONAM", data, record);
						
						} else if (key.equals("race")) {
							if (resultString.equals("")) {
								continue;
							}
							SubRecordData data = new SubRecordDataString(resultString, key, 0, 100);
							subRecord = new SubRecord("RNAM", data, record);
							
						} else if (key.equals("class")) {
							if (resultString.equals("")) {
								continue;
							}
							SubRecordData data = new SubRecordDataString(resultString, key, 0, 100);
							subRecord = new SubRecord("CNAM", data, record);
							
						} else if (key.equals("faction")) {
							if (resultString.equals("")) {
								continue;
							}
							SubRecordData data = new SubRecordDataString(resultString, key, 0, 100);
							subRecord = new SubRecord("FNAM", data, record);
							
						} else if (key.equals("cell")) {
							if (resultString.equals("")) {
								continue;
							}
							SubRecordData data = new SubRecordDataString(resultString, key, 0, 100);
							subRecord = new SubRecord("ANAM", data, record);
							
						} else if (key.equals("playersFaction")) {
							if (resultString.equals("")) {
								continue;
							}
							SubRecordData data = new SubRecordDataString(resultString, key, 0, 100);
							subRecord = new SubRecord("DNAM", data, record);
							
						} else if (key.equals("sound")) {
							if (resultString.equals("")) {
								continue;
							}
							SubRecordData data = new SubRecordDataString(resultString, key, 0, 100);
							subRecord = new SubRecord("SNAM", data, record);
							
						} else if (key.equals("journalName")) {
							if (resultNumber == 0) {
								continue;
							}
							SubRecordData data = new SubRecordDataByte((byte)resultNumber, key);
							subRecord = new SubRecord("QSTN", data, record);
							
						} else if (key.equals("journalFinish")) {
							if (resultNumber == 0) {
								continue;
							}
							SubRecordData data = new SubRecordDataByte((byte)resultNumber, key);
							subRecord = new SubRecord("QSTF", data, record);
							
						} else if (key.equals("journalRestart")) {
							if (resultNumber == 0) {
								continue;
							}
							SubRecordData data = new SubRecordDataByte((byte)resultNumber, key);
							subRecord = new SubRecord("QSTR", data, record);
							
						} else if (key.equals("resultText")) {
							if (resultString.equals("")) {
								continue;
							}
							SubRecordData data = new SubRecordDataString(resultString, key, 0, 9999);
							subRecord = new SubRecord("BNAM", data, record);
							
						}
						//SCVR fields and INTV and FLTV need to be treated differently, because they need to be put together in a certain order.
						else if (key.startsWith("func")) {
							int position = Integer.parseInt(key.substring(4, 5)) - 1;
							if (key.endsWith("TargetValue")) {
								scvrTargetValues[position] = resultNumber;
							} else {
								SubRecordDataSCVR scvr = scvrs[position];
								if (scvr == null) {
									scvr = new SubRecordDataSCVR(position);
									scvrs[position] = scvr;
								}
								
								if (key.endsWith("Type")) {
									scvr.setValueByStringFor(updateValues.get(key), "type");
								} else if (key.endsWith("Func")) {
									scvr.setValueByStringFor(updateValues.get(key), "function");
								} else if (key.endsWith("Value")) {
									scvr.setValueByStringFor(updateValues.get(key), "value");
								} else if (key.endsWith("CompareOp")) {
									scvr.setValueByStringFor(updateValues.get(key), "compareOp");
								} else if (key.endsWith("TargetValue")) {
									scvr.setValueByStringFor(updateValues.get(key), "value");
								}
							}
							
							//omit the insertion below, so the scvrs and their target values can be assembled first and inserted in a separate step.
							continue;
						}
						
						insertSubRecordAtRightIndex(subRecord, record);
					}
					
					//now look through the created SCVRs and their targetValues and put them into the right place.
					for (int i = 0; i < scvrs.length; i++) {
						SubRecordDataSCVR scvr = scvrs[i];
						if (scvr == null) {
							continue;
						}
						
						SubRecord targetValueSubRecord;
						if (scvrTargetValues[i] % 1 > 0) {
							//a float number
							SubRecordData targetValueSubRecordData = new SubRecordDataFloat(scvrTargetValues[i], "func"+(i+1)+"TargetValue", 3);
							targetValueSubRecord = new SubRecord("FLTV", targetValueSubRecordData, record);
						} else {
							//an integer
							SubRecordData targetValueSubRecordData = new SubRecordDataInt((int)scvrTargetValues[i], "func"+(i+1)+"TargetValue");
							targetValueSubRecord = new SubRecord("INTV", targetValueSubRecordData, record);
						}
						
						int insertionIndex = findInsertionIndexForSCRV(i, record);
						record.getSubRecords().add(insertionIndex, new SubRecord("SCVR", scvr, record));
						record.getSubRecords().add(insertionIndex+1, targetValueSubRecord);
					}
				}
				
				//Deletion of the current row.
				if (delete) {
					records.remove(i1);
					i1--;
					
					//now onto updating the responses before and after the deleted one.
					for (Record searchRecord: records) {
						if (record.getName().equals("INFO")) {
							for (SubRecord searchSubRecord : searchRecord.getSubRecords()) {
								if (searchSubRecord.getName().equals("NNAM") && searchSubRecord.getData().getValueAsString().equals(currentResponseId)) {
									if (nextResponseId != null) {
										searchSubRecord.getData().setValueByString(ModelFunctions.getEscapedString(nextResponseId));
									}
								} else if (searchSubRecord.getName().equals("PNAM") && searchSubRecord.getData().getValueAsString().equals(currentResponseId)) {
									if (previousResponseId != null) {
										searchSubRecord.getData().setValueByString(ModelFunctions.getEscapedString(previousResponseId));
									}
								}
							}
						}
					}
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
		return "dialogueResponses";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}
	
	private void insertSubRecordAtRightIndex(SubRecord subRecord, Record record) {
		int insertionIndex = findInsertionIndexFor(subRecord.getName(), record);
		record.getSubRecords().add(insertionIndex, subRecord);
	}
	
	private int findInsertionIndexFor(String subRecordName, Record record) {
		int index1 = infoSubRecordsInOrder.indexOf(subRecordName);
		int i = 0;
		for (; i < record.getSubRecords().size(); i++) {
			SubRecord subRecord = record.getSubRecords().get(i);
			int index2 = infoSubRecordsInOrder.indexOf(subRecord.getName());
			if (index2 > index1) {
				break;
			}
		}
		
		return i;
	}
	
	private int findInsertionIndexForSCRV(int index, Record record) {
		int i = 0;
		for (; i < record.getSubRecords().size(); i++) {
			SubRecord subRecord = record.getSubRecords().get(i);
			
			if (subRecord.getName().equals("SCVR")) {
				SubRecordDataSCVR subRecordData = (SubRecordDataSCVR)subRecord.getData();
				if (subRecordData.getNumberIndex() > index) {
					break;
				}
			}
			
			if (subRecord.getName().equals("BNAM")) {
				break;
			}
		}
		
		return i;
	}

}
