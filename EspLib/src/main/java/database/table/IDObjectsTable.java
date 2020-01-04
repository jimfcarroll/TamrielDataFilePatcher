package database.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import database.Columns;
import database.Row;
import database.where.Comparison;
import model.Record;
import model.SubRecord;

public class IDObjectsTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"id", "type"},
			new Columns.Type[] {Columns.Type.text, Columns.Type.text},
			new boolean[] {true, false}
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
		return executeRUD(records, allColumns, whereComparison, updateValues, false);
	}

	@Override
	public List<Row> delete(List<Record> records, Comparison whereComparison) {
		return executeRUD(records, allColumns, whereComparison, null, true);
	}
	
	@Override
	public String getName() {
		return "idObjects";
	}
	
	public Columns getAllColumns() {
		return allColumns;
	}
	
	private List<Row> executeRUD(List<Record> records, Columns selectedColumns, Comparison whereComparison, Map<String, String> updateValues, boolean delete) {
		List<Row> returnRows = new ArrayList<Row>(100);
		
		for (Iterator<Record> recordIterator = records.iterator(); recordIterator.hasNext();) {
			Record record = recordIterator.next();
			
			if (!isIDObject(record.getName())) {
				continue;
			}
			
			SubRecord idSubRecord = null;
			String[] values = new String[2];
			values[1] = recordNameToType.get(record.getName());
			
			if (values[1] == null) {
				values[1] = "unknown";
			}
			
			for (SubRecord subRecord : record.getSubRecords()) {
				if (subRecord.getName().equals("NAME")) {
					idSubRecord = subRecord;
					values[0] = subRecord.getData().getValueAsString();
					break;
				}
			}
			
			if (values[0] == null) {
				continue;
			}
			
			Row newRow = new Row(allColumns, values);
			if (whereComparison.testConditionOnRow(newRow) == true) {
				//an update
				if (updateValues != null) {
					if (updateValues.containsKey("id")) {
						idSubRecord.getData().setValueByString(updateValues.get("id"));
						values[0] = idSubRecord.getData().getValueAsString();
					} if (updateValues.containsKey("type")) {
						System.out.println("The type of an id object cannot be changed in this table.");
					}
				}
				
				//a deletion
				if (delete == true) {
					recordIterator.remove();
				}
				
				if (selectedColumns != allColumns) {
					newRow = newRow.createFractionRow(selectedColumns);
				}
				
				returnRows.add(newRow);
			}
		}
		
		return returnRows;
	}
	
	private boolean isIDObject(String name) {
		boolean noIDObject = name.equals("TES3") ||
				name.equals("GMST") ||
				name.equals("SKIL") ||
				name.equals("MGEF") ||
				name.equals("SCPT") ||
				name.equals("LAND") ||
				name.equals("PGRD") ||
				name.equals("DIAL") ||
				name.equals("INFO") ||
				name.equals("CELL");
		
		return !noIDObject;
	}
}
