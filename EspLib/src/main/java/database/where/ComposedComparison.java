package database.where;

import database.Row;

public class ComposedComparison implements Comparison{
	public enum Conjunction {and, or};
	
	private Comparison leftExpression, rightExpression;
	private Conjunction conjunction;
	
	public ComposedComparison(Comparison leftExpression, Comparison rightExpression, Conjunction conjunction) {
		super();
		this.leftExpression = leftExpression;
		this.rightExpression = rightExpression;
		this.conjunction = conjunction;
	}

	@Override
	public boolean testConditionOnRow(Row row) {
		if (conjunction == Conjunction.and) {
			return leftExpression.testConditionOnRow(row) && rightExpression.testConditionOnRow(row);
		}
		
		return leftExpression.testConditionOnRow(row) || rightExpression.testConditionOnRow(row);
	}
}
