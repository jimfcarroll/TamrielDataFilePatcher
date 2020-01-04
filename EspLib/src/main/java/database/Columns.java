package database;

import java.util.Arrays;
import java.util.Map;

public class Columns {
	public enum Type {text, number, integer, other};

	private String[] columns;
	private Type[] types;
	private boolean[] updatable;
	
	public Columns(String[] columns, Type[] types) {
		super();
		
		if (columns.length != types.length) {
			throw new IllegalArgumentException("The amount of entries in the passed arrays must match");
		}
		
		this.columns = columns;
		this.types = types;
		updatable = new boolean[types.length];
		Arrays.fill(updatable, true);
	}
	
	public Columns(String[] columns, Type[] types, boolean[] updatable) {
		super();
		
		if (columns.length != types.length || columns.length != updatable.length) {
			throw new IllegalArgumentException("The amount of entries in the passed arrays must match");
		}
		
		this.columns = columns;
		this.types = types;
		this.updatable = updatable;
	}
	
	public String[] createEmptyRowValues() {
		String[] values = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			if (types[i] == Type.text || types[i] == Type.other) {
				values[i] = "";
			} else {
				values[i] = "0";
			}
		}
		
		return values;
	}
	
	public Row createEmptyRow() {
		return new Row(this, createEmptyRowValues());
	}
	
	public boolean hasColumn(String column) {
		return getPositionOf(column) != -1;
	}

	public int getPositionOf(String column) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equalsIgnoreCase(column)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public Type getTypeOf(String column) {
		if (!hasColumn(column)) {
			throw new IllegalArgumentException("the passed column "+column+" is not contained in this table.");
		}
		
		return types[getPositionOf(column)];
	}
	
	public boolean isNumeric(String column) {
		Columns.Type type = getTypeOf(column);
		return type == Columns.Type.number || type == Columns.Type.integer;
	}
	
	public boolean isUpdatable(String column) {
		return updatable[getPositionOf(column)];
	}
	
	public boolean containsNonUpdatableFields(Map<String,String> updateValues) {
		for (String key:updateValues.keySet()) {
			if (!isUpdatable(key)) {
				return true;
			}
		}
		return false;
	}
	
	public int getLength() {
		return columns.length;
	}
	
	public String[] getColumnNames() {
		return columns.clone();
	}
}
