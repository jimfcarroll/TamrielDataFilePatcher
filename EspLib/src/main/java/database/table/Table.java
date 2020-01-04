package database.table;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import database.Columns;
import database.Row;
import database.where.Comparison;
import model.Record;

public abstract class Table {
	public static final Map<String, String> recordNameToType;
	public static final Map<String, String> recordTypeToName;
	static {
		Map<String, String> recordNameToTypeInternal = new HashMap<String, String>();
		Map<String, String> recordTypeToNameInternal = new HashMap<String, String>();
		
		recordNameToTypeInternal.put("TES3", "fileheader");
		recordNameToTypeInternal.put("GMST", "gamesetting");
		recordNameToTypeInternal.put("GLOB", "global variable");
		recordNameToTypeInternal.put("CLAS", "class");
		recordNameToTypeInternal.put("FACT", "faction");
		recordNameToTypeInternal.put("RACE", "race");
		recordNameToTypeInternal.put("SOUN", "sound");
		recordNameToTypeInternal.put("SNDG", "sound generater");
		recordNameToTypeInternal.put("SKIL", "skill");
		recordNameToTypeInternal.put("MGEF", "magic effect");
		recordNameToTypeInternal.put("SCPT", "script");
		recordNameToTypeInternal.put("REGN", "region");
		recordNameToTypeInternal.put("BSGN", "birthsign");
		recordNameToTypeInternal.put("LTEX", "landtexture");
		recordNameToTypeInternal.put("STAT", "static");
		recordNameToTypeInternal.put("DOOR", "door");
		recordNameToTypeInternal.put("MISC", "misc item");
		recordNameToTypeInternal.put("WEAP", "weapon");
		recordNameToTypeInternal.put("CONT", "container");
		recordNameToTypeInternal.put("SPEL", "spell");
		recordNameToTypeInternal.put("CREA", "creature");
		recordNameToTypeInternal.put("BODY", "bodypart");
		recordNameToTypeInternal.put("LIGH", "light");
		recordNameToTypeInternal.put("ENCH", "enchantment");
		recordNameToTypeInternal.put("NPC_", "npc");
		recordNameToTypeInternal.put("ARMO", "armor");
		recordNameToTypeInternal.put("CLOT", "clothing");
		recordNameToTypeInternal.put("REPA", "repair item");
		recordNameToTypeInternal.put("ACTI", "activator");
		recordNameToTypeInternal.put("APPA", "apparatus");
		recordNameToTypeInternal.put("LOCK", "lockpick");
		recordNameToTypeInternal.put("PROB", "probe");
		recordNameToTypeInternal.put("INGR", "ingredient");
		recordNameToTypeInternal.put("BOOK", "book");
		recordNameToTypeInternal.put("ALCH", "alchemy");
		recordNameToTypeInternal.put("LEVI", "levelled item");
		recordNameToTypeInternal.put("LEVC", "levelled creature");
		recordNameToTypeInternal.put("CELL", "cell");
		recordNameToTypeInternal.put("LAND", "landscape");
		recordNameToTypeInternal.put("PGRD", "pathgrid");
		recordNameToTypeInternal.put("DIAL", "dialogue topic");
		recordNameToTypeInternal.put("INFO", "dialogue response");
		recordNameToTypeInternal.put("CNTC", "savegame container");
		
		Set<String> keySet = recordNameToTypeInternal.keySet();
		for (String key: keySet) {
			recordTypeToNameInternal.put(recordNameToTypeInternal.get(key), key);
		}
		
		recordNameToType = Collections.unmodifiableMap(recordNameToTypeInternal);
		recordTypeToName = Collections.unmodifiableMap(recordTypeToNameInternal);
	};
	
	public abstract List<Row> insert(List<Record> records, List<Row> rows);
	
	public abstract List<Row> select(List<Record> records, Columns selectedColumns, Comparison whereComparison);
	
	public abstract List<Row> update(List<Record> records, Comparison whereComparison, Map<String,String> updateValues);
	
	public abstract List<Row> delete(List<Record> records, Comparison whereComparison);
	
	public abstract String getName();
	
	public abstract Columns getAllColumns();
	
	public Columns getColumns(String[] names) {
		Columns allColumns = getAllColumns();
		
		if (names.length == 1 && names[0].equals("*")) {
			return allColumns;
		}
		
		Columns.Type[] types = new Columns.Type[names.length];
		for (int i = 0; i < names.length; i++) {
			types[i] = allColumns.getTypeOf(names[i]);
		}
		
		return new Columns(names, types);
	}
	
}
