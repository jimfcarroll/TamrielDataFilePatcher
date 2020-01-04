package database.where;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;

public class WhereEvaluator extends ExpressionVisitorAdapter {
	private Comparison comparison = null;
	
	public Comparison evaluateWhere(Expression WhereExpression) {
		WhereExpression.accept(this);
		return comparison;
	}

	@Override
	public void visit(AndExpression arg0) {
		Comparison leftsideComparison = evaluateWhere(arg0.getLeftExpression());
		Comparison rightsideComparison = evaluateWhere(arg0.getRightExpression());
		comparison = new ComposedComparison(leftsideComparison, rightsideComparison, ComposedComparison.Conjunction.and);
	}
	
	@Override
	public void visit(OrExpression arg0) {
		Comparison leftsideComparison = evaluateWhere(arg0.getLeftExpression());
		Comparison rightsideComparison = evaluateWhere(arg0.getRightExpression());
		comparison = new ComposedComparison(leftsideComparison, rightsideComparison, ComposedComparison.Conjunction.or);
	}

	@Override
	public void visit(EqualsTo arg0) {
		comparison = new SingleComparison(
				arg0.getLeftExpression().toString(),
				arg0.getRightExpression().toString(),
				SingleComparison.Operation.equals
		);
	}

	@Override
	public void visit(GreaterThan arg0) {
		comparison = new SingleComparison(
				arg0.getLeftExpression().toString(),
				arg0.getRightExpression().toString(),
				SingleComparison.Operation.greaterthan
		);
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		comparison = new SingleComparison(
				arg0.getLeftExpression().toString(),
				arg0.getRightExpression().toString(),
				SingleComparison.Operation.greaterthanorequals
		);
	}

	@Override
	public void visit(LikeExpression arg0) {
		comparison = new SingleComparison(
				arg0.getLeftExpression().toString(),
				arg0.getRightExpression().toString(),
				SingleComparison.Operation.equals
		);
	}

	@Override
	public void visit(MinorThan arg0) {
		comparison = new SingleComparison(
				arg0.getLeftExpression().toString(),
				arg0.getRightExpression().toString(),
				SingleComparison.Operation.lessthan
		);
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		comparison = new SingleComparison(
				arg0.getLeftExpression().toString(),
				arg0.getRightExpression().toString(),
				SingleComparison.Operation.lessthanorequals
		);
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		comparison = new SingleComparison(
				arg0.getLeftExpression().toString(),
				arg0.getRightExpression().toString(),
				SingleComparison.Operation.unequals
		);
	}
	
	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void visit(IsNullExpression arg0) {
		// TODO Auto-generated method stub
		
	}
}
