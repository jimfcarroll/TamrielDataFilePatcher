package database;

import database.table.BodypartReferencesTable;
import database.table.CellObjectsTable;
import database.table.ClassReferencesTable;
import database.table.CreatureReferencesTable;
import database.table.DialogueResponsesTable;
import database.table.EnchantmentReferencesTable;
import database.table.ExteriorCellsTable;
import database.table.FactionReferencesTable;
import database.table.FileReferencesTable;
import database.table.IDObjectsTable;
import database.table.InteriorCellsTable;
import database.table.ItemReferencesTable;
import database.table.MasterFilesTable;
import database.table.ScriptReferencesTable;
import database.table.ScriptsTable;
import database.table.SoundReferencesTable;
import database.table.SpellReferencesTable;
import database.table.Table;

public class Tables {
	public static Table getTable(String tableName) {
		
		if (tableName == null) {
			return null;
		}
	
		if (tableName.equals("cellObjects")) {
			return new CellObjectsTable();
		}
		
		if (tableName.equals("idObjects")) {
			return new IDObjectsTable();
		}
		
		if (tableName.equals("dialogueResponses")) {
			return new DialogueResponsesTable();
		}
		
		if (tableName.equals("scripts")) {
			return new ScriptsTable();
		}
		
		if (tableName.equals("exteriorCells")) {
			return new ExteriorCellsTable();
		}
		
		if (tableName.equals("interiorCells")) {
			return new InteriorCellsTable();
		}
		
		if (tableName.equals("masterFiles")) {
			return new MasterFilesTable();
		}
		
		if (tableName.equals("scriptReferences")) {
			return new ScriptReferencesTable();
		}
		
		if (tableName.equals("soundReferences")) {
			return new SoundReferencesTable();
		}
		
		if (tableName.equals("bodypartReferences")) {
			return new BodypartReferencesTable();
		}
		
		if (tableName.equals("itemReferences")) {
			return new ItemReferencesTable();
		}
		
		if (tableName.equals("creatureReferences")) {
			return new CreatureReferencesTable();
		}
		
		if (tableName.equals("fileReferences")) {
			return new FileReferencesTable();
		}
		
		if (tableName.equals("enchantmentReferences")) {
			return new EnchantmentReferencesTable();
		}
		
		if (tableName.equals("spellReferences")) {
			return new SpellReferencesTable();
		}
		
		if (tableName.equals("factionReferences")) {
			return new FactionReferencesTable();
		}
		
		if (tableName.equals("classReferences")) {
			return new ClassReferencesTable();
		}
		
		return null;
	}
}
