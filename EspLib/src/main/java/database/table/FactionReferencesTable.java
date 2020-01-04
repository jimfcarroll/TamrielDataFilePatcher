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
import model.subrecorddata.SubrecordDataFactionMembership;

public class FactionReferencesTable extends Table {
	protected Columns allColumns = new Columns(
			new String[] {"owningObject", "faction", "type"},
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
			
			String type = null;
			String owningObject = null;
			
			if (record.getName().equals("NPC_")) {
				type = "membership";
			} else if (record.getName().equals("STLN")) {
				type = "originalOwner";
			} else if (record.getName().equals("CELL")) {
				type = "owner";
			} else if (record.getName().equals("FACT")) {
				type = "factionRelation";
			} else if (record.getName().equals("PCDT")) {
				type = "playermembership";
			} else {
				continue;
			}
			
			for (int i2 = 0; i2 < record.getSubRecords().size(); i2++) {
				SubRecord subRecord = record.getSubRecords().get(i2);
				
				if (subRecord.getName().equals("NAME") && !record.getName().equals("PCDT")) {
					owningObject = subRecord.getData().getValueAsString();
				} else if (
						(subRecord.getName().equals("ANAM") && record.getName().equals("NPC_") && !subRecord.getData().getValueAsString().equals("")) ||
						(subRecord.getName().equals("CNAM") && record.getName().equals("CELL")) ||
						(subRecord.getName().equals("FNAM") && record.getName().equals("STLN")) ||
						(subRecord.getName().equals("ANAM") && record.getName().equals("FACT"))
				) {
					
					String[] rowValues = new String[3];
					rowValues[0] = owningObject; 
					rowValues[1] = subRecord.getData().getValueAsString();
					rowValues[2] = type;
					Row newRow = new Row(allColumns, rowValues);
					
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("faction")) {
								subRecord.getData().setValueByString(updateValues.get("faction"));
								rowValues[1] = subRecord.getData().getValueAsString();
							}
						}
						
						if (selectedColumns != allColumns) {
							newRow = newRow.createFractionRow(selectedColumns);
						}
						returnRows.add(newRow);
					}
				} else if (subRecord.getName().equals("FNAM") && record.getName().equals("PCDT")) {
					
					SubrecordDataFactionMembership dataMembership = (SubrecordDataFactionMembership)subRecord.getData();
					String[] rowValues = new String[3];
					rowValues[0] = "player"; 
					rowValues[1] = dataMembership.getFaction();
					rowValues[2] = type;
					Row newRow = new Row(allColumns, rowValues);
					
					if (whereComparison.testConditionOnRow(newRow)) {
						if (updateValues != null) {
							if (updateValues.containsKey("faction")) {
								Map<String,String> variables = new HashMap<String, String>();
								variables.put("faction", dataMembership.getFaction());
								String value = ModelFunctions.evaluateStringExpression(updateValues.get("faction"), variables);
								
								dataMembership.setFaction(value);
								rowValues[1] = dataMembership.getFaction();
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
		return "factionReferences";
	}

	@Override
	public Columns getAllColumns() {
		return allColumns;
	}

}
