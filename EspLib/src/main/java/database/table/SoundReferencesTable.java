package database.table;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.Columns;
import database.Row;
import database.where.Comparison;
import model.ModelFunctions;
import model.Record;
import model.SubRecord;

public class SoundReferencesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"owningObject", "sound", "soundType"},
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
			
			if (!record.getName().equals("SNDG") &&
				!record.getName().equals("DOOR") &&
				!record.getName().equals("LIGH") &&
				!record.getName().equals("REGN")
			) {
				continue;
			}
			
			String[] rowValues = new String[3];
			SubRecord soundSubRecord = null;
			
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				if (subRecord.getName().equals("NAME")) {
					rowValues[0] = subRecord.getData().getValueAsString();
					continue;
				}
				
				//when a fitting subrecord is found
				if (subRecord.getName().equals("SNAM")) {
					soundSubRecord = subRecord;
					
					if (record.getName().equals("REGN")) {
						rowValues = Arrays.copyOf(rowValues, rowValues.length);
						rowValues[1] = ModelFunctions.getStringFromBytes(Arrays.copyOfRange(subRecord.getData().getRawData(), 0, 32));
						if (rowValues[1].contains("\0")) {
							rowValues[1] = rowValues[1].substring(0, rowValues[1].indexOf('\0'));
						}
						
						rowValues[2] = "regional";
					} else if (record.getName().equals("LIGH")) {
						rowValues[1] = subRecord.getData().getValueAsString();
						rowValues[2] = "light";
					} else if (record.getName().equals("DOOR")) {
						rowValues = Arrays.copyOf(rowValues, rowValues.length);
						rowValues[1] = subRecord.getData().getValueAsString();
						rowValues[2] = "doorOpen";
					} else if (record.getName().equals("SNDG")) {
						rowValues[1] = subRecord.getData().getValueAsString();
					} else {
						continue;
					}
				} else if (subRecord.getName().equals("ANAM")) {
					if (record.getName().equals("DOOR")) {
						soundSubRecord = subRecord;
						
						rowValues = Arrays.copyOf(rowValues, rowValues.length);
						rowValues[1] = subRecord.getData().getValueAsString();
						rowValues[2] = "doorClose";
					} else {
						continue;
					}
				} else if (subRecord.getName().equals("DATA")) {
					if (record.getName().equals("SNDG")) {
						rowValues = Arrays.copyOf(rowValues, rowValues.length);
						int soundTypeConstant = Integer.parseInt(subRecord.getData().getValueAsString());
						switch (soundTypeConstant) {
						case 0:
							rowValues[2] = "creatureLeftFoot";
							break;
						case 1:
							rowValues[2] = "creatureRightFoot";
							break;
						case 2:
							rowValues[2] = "creatureLeftSwim";
							break;
						case 3:
							rowValues[2] = "creatureRightSwim";
							break;
						case 4:
							rowValues[2] = "creatureMoan";
							break;
						case 5:
							rowValues[2] = "creatureRoar";
							break;
						case 6:
							rowValues[2] = "creatureScream";
							break;
						case 7:
							rowValues[2] = "creatureLand";
							break;
						}
					} else {
						continue;
					}
				} else {
					continue;
				}
				
				if (rowValues[0] != null && rowValues[1] != null && rowValues[2] != null) {
					Row newRow = new Row(allColumns, rowValues);
					
					if (whereComparison.testConditionOnRow(newRow)) {
						
						if (updateValues != null && updateValues.containsKey("sound")) {
							if (record.getName().equals("REGN")) {
								
								//the entries in the region list need a special approach
								Map<String, String> variables = new HashMap<String, String>();
								variables.put("sound", rowValues[1]);
								String newValue = ModelFunctions.evaluateStringExpression(updateValues.get("sound"), variables);
								rowValues[1] = newValue;
								
								byte[] stringBytes = ModelFunctions.getBytesFromString(newValue+'\0');
								byte[] rawDataOld = soundSubRecord.getData().getRawData();
								byte[] rawDataNew = Arrays.copyOf(rawDataOld, rawDataOld.length);
								System.arraycopy(stringBytes, 0, rawDataNew, 0, stringBytes.length);
								
								try {
									SubRecord replaceSubrecod = new SubRecord("SNAM", rawDataNew, record);
									int soundSubRecordIndex = record.getSubRecords().indexOf(soundSubRecord);
									record.getSubRecords().remove(soundSubRecordIndex);
									record.getSubRecords().add(soundSubRecordIndex, replaceSubrecod);
									
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								
							} else {
								soundSubRecord.getData().setValueByString(updateValues.get("sound"));
								rowValues[1] = soundSubRecord.getData().getValueAsString();
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
		return "soundReferences";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}
}
