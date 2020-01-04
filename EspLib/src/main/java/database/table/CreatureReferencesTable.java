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

public class CreatureReferencesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"owningObject", "creature", "type"},
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
			
			String type;
			String owningObject = null;
			if (record.getName().equals("CREA")) {
				type = "soundgenParent";
			} else if (record.getName().equals("REGN")) {
				type = "sleep";
			} else if (record.getName().equals("LEVC")) {
				type = "levelled";
			} else if (record.getName().equals("SNDG")) {
				type = "soundgen";
			} else if (record.getName().equals("CELL")) {
				//can be various types.
				type = null;
			} else if (record.getName().equals("STLN")) {
				type = "originalOwner";
			} else if (record.getName().equals("KLST")) {
				type = "killed";
				owningObject = "player";
			} else if (record.getName().equals("CNTC")) {
				type = "soul";
			} else {
				type = null;
				continue;
			}
			
			String[] rowValues = null;
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				if (subRecord.getName().equals("NAME")) {
					owningObject = subRecord.getData().getValueAsString();
					
				} else if (subRecord.getName().equals("NPCO") && record.getName().equals("CNTC")) {
					SubrecordDataNPCO item = (SubrecordDataNPCO)subRecord.getData();
					owningObject = item.getItemAsString();
					
				} else if (	(subRecord.getName().equals("CNAM") && !record.getName().equals("REGN") && !record.getName().equals("KLST") && !record.getName().equals("CNTC") && !record.getName().equals("STLN") && !record.getName().equals("CELL")) ||
							(subRecord.getName().equals("BNAM") && record.getName().equals("REGN")) ||
							(subRecord.getName().equals("ANAM") && record.getName().equals("CELL")) ||
							(subRecord.getName().equals("XSOL") && record.getName().equals("CELL")) ||
							(subRecord.getName().equals("ONAM") && record.getName().equals("STLN")) ||
							(subRecord.getName().equals("XSOL") && record.getName().equals("CNTC")) ||
							(subRecord.getName().equals("KNAM") && record.getName().equals("KLST"))
				) {
					
					if (record.getName().equals("CELL")) {
						if (subRecord.getName().equals("XSOL")) {
							type = "soul";
						} else {
							type = "owner";
						}
					}
					
					rowValues = new String[3];
					rowValues[0] = owningObject; 
					rowValues[1] = subRecord.getData().getValueAsString();
					rowValues[2] = type;
					
					Row newRow = new Row(allColumns, rowValues);
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("creature")) {
								subRecord.getData().setValueByString(updateValues.get("creature"));
								rowValues[1] = subRecord.getData().getValueAsString();
							}
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
		return "creatureReferences";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}
}
