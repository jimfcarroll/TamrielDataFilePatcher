package database;

public class Row {
	private Columns columns;
	private String[] values;
	
	public Row(Columns columns, String[] values) {
		super();
		
		if (values.length != columns.getLength()) {
			throw new IllegalArgumentException("The amount of entries in the value array must match the amount of columns.");
		}
		
		this.columns = columns;
		this.values = values;
	}
	
	public Row createFractionRow(Columns selectedColumns) {
		String[] values = new String[selectedColumns.getLength()];
		String[] columnNames = selectedColumns.getColumnNames();
		
		for (int i = 0; i < values.length; i++) {
			values[i] = getValueOf(columnNames[i]);
		}
		
		return new Row(selectedColumns, values);
	}
	
	public String getValueOf(String column) {
		if (!columns.hasColumn(column)) {
			throw new IllegalArgumentException("Column "+column+" is not contained in this row.");
		}
		
		return values[columns.getPositionOf(column)];
	}
	
	public int getLength() {
		return columns.getLength();
	}
	
	public boolean hasColumn(String column) {
		return columns.hasColumn(column);
	}
	
	public Columns.Type getTypeOf(String column) {
		return columns.getTypeOf(column);
	}
	
	public boolean isNumeric(String column) {
		return columns.isNumeric(column);
	}

	public Columns getColumns() {
		return columns;
	}

	public String[] getValues() {
		return values;
	}
}
