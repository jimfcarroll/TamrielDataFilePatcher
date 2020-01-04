package processor;

import java.util.List;
import database.Row;
import database.Statement;

public class StatementProcessingResult {
	private Statement statement;
	private List<Row> rows;
	
	public StatementProcessingResult(Statement statement, List<Row> rows) {
		super();
		this.statement = statement;
		this.rows = rows;
	}

	public Statement getStatement() {
		return statement;
	}

	public List<Row> getRows() {
		return rows;
	}
}
