package database.table;

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
import model.subrecorddata.SubRecordData;
import model.subrecorddata.SubRecordDataUnknown;

public class ScriptsTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"name", "byteCode", "text"},
			new Columns.Type[] {Columns.Type.text, Columns.Type.text, Columns.Type.text},
			new boolean[] {true, true, true}
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
			
			if (!record.getName().equals("SCPT")) {
				continue;
			}
			
			String[] rowValues = new String[allColumns.getLength()];
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);				
				
				if (subRecord.getName().equals("SCHD")) {
					String value = ModelFunctions.getStringFromBytes(Arrays.copyOfRange(subRecord.getData().getRawData(), 0, 32));
					if (value.indexOf('\0') != -1) {
						rowValues[allColumns.getPositionOf("name")] = value.substring(0, value.indexOf('\0'));
					} else {
						rowValues[allColumns.getPositionOf("name")] = value;
					}
					
				} else if (subRecord.getName().equals("SCDT")) {
					rowValues[allColumns.getPositionOf("byteCode")] = subRecord.getData().getValueAsString();
				} else if (subRecord.getName().equals("SCTX")) {
					rowValues[allColumns.getPositionOf("text")] = subRecord.getData().getValueAsString();
				}
			}
			
			Row newRow = new Row(allColumns, rowValues);
			if (whereComparison.testConditionOnRow(newRow) == true) {
				
				if (updateValues != null) {
					for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
						SubRecord subRecord = record.getSubRecords().get(i2);
						
						if (subRecord.getName().equals("SCHD") && updateValues.containsKey("name")) {
							Map<String,String> variables = new HashMap<String, String>();
							variables.put("name", rowValues[allColumns.getPositionOf("name")]);
							String newValue = ModelFunctions.evaluateStringExpression(updateValues.get("name"), variables);
							
							byte[] nameBytes1 = ModelFunctions.getBytesFromString(newValue);
							byte[] nameBytes2 = new byte[32];
							System.arraycopy(nameBytes1, 0, nameBytes2, 0, nameBytes1.length);
							byte[] rawData = subRecord.getData().getRawData();
							System.arraycopy(nameBytes2, 0, rawData, 0, nameBytes2.length);
							SubRecordData subRecordData = new SubRecordDataUnknown(rawData);
							
							record.getSubRecords().remove(i2);
							record.getSubRecords().add(i2, new SubRecord("SCHD", subRecordData, record));
							rowValues[allColumns.getPositionOf("name")] = newValue;
							
						} else if (subRecord.getName().equals("SCDT") && updateValues.containsKey("byteCode")) {
							Map<String,String> variables = new HashMap<String, String>();
							variables.put("byteCode", rowValues[allColumns.getPositionOf("byteCode")]);
							String newValue = ModelFunctions.evaluateStringExpression(updateValues.get("byteCode"), variables);
							
							subRecord.getData().setValueByString(ModelFunctions.getEscapedString(newValue));
							rowValues[allColumns.getPositionOf("byteCode")] = newValue;
							
						} else if (subRecord.getName().equals("SCTX") && updateValues.containsKey("text")) {
							Map<String,String> variables = new HashMap<String, String>();
							variables.put("text", rowValues[allColumns.getPositionOf("text")]);
							String newValue = ModelFunctions.evaluateStringExpression(updateValues.get("text"), variables);
							
							subRecord.getData().setValueByString(ModelFunctions.getEscapedString(newValue));
							rowValues[allColumns.getPositionOf("text")] = newValue;
						}
					}
				}
				
				if (delete) {
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
		return "scripts";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}

}
