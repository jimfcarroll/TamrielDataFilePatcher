package processor;

import java.util.LinkedList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

public class FileProcessingOrder extends StatementVisitorAdapter{
	private List<database.Statement> statements;

	private FileProcessingOrder() {
		statements = new LinkedList<database.Statement>();
	}
	
	public static FileProcessingOrder fromSql(String sql) throws JSQLParserException {
		FileProcessingOrder order = new FileProcessingOrder();
		Statements stmt = CCJSqlParserUtil.parseStatements(sql);
		stmt.accept(order);
		
		return order;
	}
	
	public void visit(Statements arg0) {
		for (Statement statement : arg0.getStatements()) {
			statement.accept(this);
		}
	}
	
	public void visit(Insert arg0) {
		database.Insert newInsert = new database.Insert(arg0);
		statements.add(newInsert);
	}
	
	public void visit(Select arg0) {
		database.Select newSelect = new database.Select(arg0);
		statements.add(newSelect);
	}
	
	public void visit(Update arg0) {
		database.Update newUpdate = new database.Update(arg0);
		statements.add(newUpdate);
	}
	
	public void visit(Delete arg0) {
		database.Delete newDelete = new database.Delete(arg0);
		statements.add(newDelete);
	}

	public List<database.Statement> getStatements() {
		return statements;
	}
}
