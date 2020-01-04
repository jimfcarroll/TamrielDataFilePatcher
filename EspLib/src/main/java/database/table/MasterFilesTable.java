package database.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.Columns;
import database.Row;
import database.where.Comparison;
import model.ModelFunctions;
import model.Record;
import model.SubRecord;
import model.subrecorddata.SubRecordDataLong;
import model.subrecorddata.SubRecordDataString;

public class MasterFilesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"name", "size"},
			new Columns.Type[] {Columns.Type.text, Columns.Type.integer},
			new boolean[] {true, true}
	);

	public List<Row> insert(List<Record> records, List<Row> rows) {
		List<Row> returnRows = new ArrayList<Row>();
		Record startRecord = records.get(0);
		
		if (!startRecord.getName().equals("TES3")) {
			throw new IllegalArgumentException("The first record in the passed record list must be a TES3 record.");
		}
		
		for (Row row: rows) {
			SubRecord subRecord1 = new SubRecord("MAST", new SubRecordDataString(ModelFunctions.getDeEscapedString(row.getValueOf("name")),"name", 0, 999), startRecord);
			SubRecord subRecord2 = new SubRecord("DATA", new SubRecordDataLong(Integer.parseInt(row.getValueOf("size")),"size"), startRecord);
			
			int insertionIndex = findInsertionIndex(startRecord.getSubRecords());
			startRecord.getSubRecords().add(insertionIndex, subRecord2);
			startRecord.getSubRecords().add(insertionIndex, subRecord1);
			
			returnRows.add(new Row(allColumns, new String[]{ModelFunctions.getDeEscapedString(row.getValueOf("name")), row.getValueOf("size")}));
		}
		
		return returnRows;
	}
	
	private int findInsertionIndex(List<SubRecord> subrecords) {
		int index;
		for (index = 0; index < subrecords.size(); index++) {
			SubRecord subRecord = subrecords.get(index);
			if (subRecord.getName().equals("GMDT") || subRecord.getName().equals("SCRD") || subRecord.getName().equals("SCRS")) {
				break;
			}
		}
		return index;
	}
	
	public List<Row> select(List<Record> records, Columns columns, Comparison whereComparison) {
		return this.executeRUD(records, columns, whereComparison, null, false);
	}
	
	public List<Row> update(List<Record> records, Comparison whereComparison, Map<String,String> updateValues) {
		return this.executeRUD(records, this.getAllColumns(), whereComparison, updateValues, false);
	}
	
	public List<Row> delete(List<Record> records, Comparison whereComparison) {
		return this.executeRUD(records, this.getAllColumns(), whereComparison, null, true);
	}

	@Override
	public String getName() {
		return "masterFiles";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}
	
	private List<Row> executeRUD(List<Record> records, Columns selectedColumns, Comparison whereComparison, Map<String, String> updateValues, boolean delete) {
		List<Row> returnRows = new ArrayList<Row>();
		Record startRecord = records.get(0);
		
		if (!startRecord.getName().equals("TES3")) {
			throw new IllegalArgumentException("The first record in the passed record list must be a TES3 record.");
		}
		
		String currentMasterFile = null;
		for (int i = 0; i < startRecord.getSubRecords().size(); i++) {
			SubRecord subRecord = startRecord.getSubRecords().get(i);
			
			if (subRecord.getName().equals("MAST")) {
				currentMasterFile = subRecord.getData().getValueAsString();
				
			} else if (subRecord.getName().equals("DATA")) {
				String fileSize = subRecord.getData().getValueAsString();
				String[] rowValues = new String[]{currentMasterFile, fileSize};
				Row newRow = new Row(allColumns, rowValues);
				
				if (whereComparison.testConditionOnRow(newRow)) {
					
					if (updateValues != null) {
						if (updateValues.containsKey("name")) {
							startRecord.getSubRecords().get(i-1).getData().setValueByString(updateValues.get("name"));
							rowValues[0] = startRecord.getSubRecords().get(i-1).getData().getValueAsString();
						}
						if (updateValues.containsKey("size")) {
							subRecord.getData().setValueByString(updateValues.get("size"));
							rowValues[1] = subRecord.getData().getValueAsString();
						}
					}
					
					if (delete) {
						startRecord.getSubRecords().remove(i);
						startRecord.getSubRecords().remove(i-1);
						i -= 2;
					}
					
					if (selectedColumns != allColumns) {
						newRow = newRow.createFractionRow(selectedColumns);
					}
					returnRows.add(newRow);
				}
			}
		}
		
		return returnRows;
	}

}
