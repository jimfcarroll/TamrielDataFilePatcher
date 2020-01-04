package processor;


import java.util.LinkedList;
import java.util.List;
import database.Delete;
import database.Insert;
import database.Row;
import database.Select;
import database.Statement;
import database.Update;
import database.table.Table;
import model.EsFile;
import model.ModelFunctions;


public class FileProcessor {
	
	public String processOrderAndFormatResults(EsFile file, FileProcessingOrder order) {
		List<StatementProcessingResult> results = processOrder(file, order);
		StringBuilder sB = new StringBuilder();
		
		for (StatementProcessingResult result : results) {
			Statement statement = result.getStatement();
			Table table = statement.getTable();
			String[] columnNames = table.getAllColumns().getColumnNames();
			List<Row> rows = result.getRows();
			
			if (statement instanceof Insert) {
				sB.append("Created rows:\n");
			}
			else if (statement instanceof Select) {
				Select select = (Select)statement;
				columnNames = select.getColumns().getColumnNames();
				sB.append("Found rows:\n");
			}
			else if (statement instanceof Update) {
				sB.append("Affected rows:\n");
			}
			else {
				sB.append("Deleted rows:\n");
			}
			
			sB.append(formatColumns(columnNames));
			sB.append('\n');
			
			for (int i = 0; i < rows.size(); i++) {
				sB.append(formatRow(rows.get(i)));
				if (i < rows.size()-1) {
					sB.append(',');
				}
				sB.append('\n');
			}
			sB.append('\n');
			
		}
		
		return sB.toString();
	}
	
	public List<StatementProcessingResult> processOrder(EsFile file, FileProcessingOrder order) {
		List<StatementProcessingResult> result = new LinkedList<StatementProcessingResult>();
		
		for (Statement statement : order.getStatements()) {
			List<Row> rows = processStatement(file, statement);
			result.add(new StatementProcessingResult(statement, rows));
		}
		
		return result;
	}
	
	public List<Row> processStatement(EsFile file, Statement statement) {
		Table table = statement.getTable();
		List<Row> rows = null;
		
		if (statement instanceof Insert) {
			Insert insert = (Insert)statement;
			rows = table.insert(file.getRecords(), insert.getRows());
		}
		
		else if (statement instanceof Select) {
			Select select = (Select)statement;
			rows = table.select(file.getRecords(), select.getColumns(), select.getWhereComparison());
		}
		
		else if (statement instanceof Update) {
			Update update = (Update)statement;
			rows = table.update(file.getRecords(), update.getWhereComparison(), update.getUpdateValues());
		}
		
		else {
			Delete delete = (Delete)statement;
			rows = table.delete(file.getRecords(), delete.getWhereComparison());
		}
		
		return rows;
	}
	
	private String formatColumns(String[] columns) {
		StringBuilder sB = new StringBuilder();
		sB.append('(');
		for (int i = 0; i < columns.length; i++) {
			sB.append(columns[i]);
			if (i < columns.length-1) {
				sB.append(", ");
			}
		}
		sB.append(')');
		return sB.toString();
	}
	
	
	private String formatRow(Row row) {
		StringBuilder sB = new StringBuilder();
		sB.append('(');
		
		for (int i = 0; i < row.getLength(); i++) {
			String column = row.getColumns().getColumnNames()[i];
			if (row.isNumeric(column)) {
				sB.append(row.getValueOf(column));
			} else {
				sB.append(ModelFunctions.getEscapedString(row.getValueOf(column)));
			}
			
			if (i < row.getLength()-1) {
				sB.append(", ");
			}
		}
		
		sB.append(')');
		return sB.toString();
	}
}
