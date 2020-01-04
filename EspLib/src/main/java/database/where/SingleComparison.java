package database.where;


import java.util.HashMap;
import java.util.Map;

import database.Columns;
import database.Row;
import model.ModelFunctions;


public class SingleComparison implements Comparison {
	public enum Operation {equals, unequals, greaterthan, greaterthanorequals, lessthan, lessthanorequals,
		contains, startswith, endswith, notcontains, notstartswith, notendswith};
	
	private String leftExpression, rightExpression;
	private Operation operation;
	
	public SingleComparison(String leftExpression, String rightExpression, Operation operation) {
		super();
		this.leftExpression = leftExpression;
		this.rightExpression = rightExpression;
		this.operation = operation;
	}

	@Override
	public boolean testConditionOnRow(Row row) {
		Columns columns = row.getColumns();
		String[] columnNames = columns.getColumnNames();
		
		//Number comparison
		if (ModelFunctions.isNumberExpression(leftExpression, columns) && ModelFunctions.isNumberExpression(rightExpression, columns)) {
			
			Map<String,Float> variables = new HashMap<String, Float>();
			for (int i = 0; i < columnNames.length; i++) {
				if (row.isNumeric(columnNames[i])) {
					variables.put(columnNames[i], Float.parseFloat(row.getValueOf(columnNames[i])));
				}
			}
			
			float leftExpressionResult = ModelFunctions.evaluateMathematicalExpressionFloat(leftExpression, variables);
			float rightExpressionResult = ModelFunctions.evaluateMathematicalExpressionFloat(rightExpression, variables);
			return comparisonResult(leftExpressionResult, rightExpressionResult);

			
		//String comparison
		} else if (ModelFunctions.isStringExpression(leftExpression, columns) && ModelFunctions.isStringExpression(rightExpression, columns)) {
			
			Map<String,String> variables = new HashMap<String, String>();
			for (int i = 0; i < columnNames.length; i++) {
				if (!row.isNumeric(columnNames[i])) {
					variables.put(columnNames[i], row.getValueOf(columnNames[i]));
				}
			}
			
			String leftExpressionResult = ModelFunctions.evaluateStringExpression(leftExpression, variables);
			String rightExpressionResult = ModelFunctions.evaluateStringExpression(rightExpression, variables);
			
			//falls auf der rechten seite wildcards vorhanden sind:
			if (operation == Operation.equals) {
				if (rightExpressionResult.startsWith("%")) {
					if (rightExpressionResult.endsWith("%")) {
						operation = Operation.contains;
						rightExpressionResult = rightExpressionResult.substring(1, rightExpressionResult.length()-1);
					} else {
						operation = Operation.endswith;
						rightExpressionResult = rightExpressionResult.substring(1);
					}
				} else if (rightExpressionResult.endsWith("%")) {
					operation = Operation.startswith;
					rightExpressionResult = rightExpressionResult.substring(0, rightExpressionResult.length()-1);
				}
				
				boolean comparisonResult = comparisonResult(leftExpressionResult, rightExpressionResult);
				//reset the operation
				operation = Operation.equals;
				return comparisonResult;
				
			} else if (operation == Operation.unequals) {
				if (rightExpressionResult.startsWith("%")) {
					if (rightExpressionResult.endsWith("%")) {
						operation = Operation.notcontains;
						rightExpressionResult = rightExpressionResult.substring(1, rightExpressionResult.length()-1);
					} else {
						operation = Operation.notendswith;
						rightExpressionResult = rightExpressionResult.substring(1);
					}
				} else if (rightExpressionResult.endsWith("%")) {
					operation = Operation.notstartswith;
					rightExpressionResult = rightExpressionResult.substring(0, rightExpressionResult.length()-1);
				}
				
				boolean comparisonResult = comparisonResult(leftExpressionResult, rightExpressionResult);
				//reset the operation
				operation = Operation.unequals;
				return comparisonResult;
			}
			
			return comparisonResult(leftExpressionResult, rightExpressionResult);
		}
		
		//This happens when the two expressions left and right of the operation don't work together.
		System.out.println("The expressions " + leftExpression + " and " + rightExpression +
				" need to be both string or number expressions. No comparison possible.");
		return false;
	}
	
	
	public boolean comparisonResult(double leftExpressionResult, double rightExpressionResult) {
		switch (operation) {
			case equals:
				return leftExpressionResult == rightExpressionResult;
			case unequals:
				return leftExpressionResult != rightExpressionResult;
			case greaterthan:
				return leftExpressionResult > rightExpressionResult;
			case greaterthanorequals:
				return leftExpressionResult >= rightExpressionResult;
			case lessthan:
				return leftExpressionResult < rightExpressionResult;
			case lessthanorequals:
				return leftExpressionResult <= rightExpressionResult;
			default:
				return false;
		}
	}
	
	public boolean comparisonResult(String leftExpressionResult, String rightExpressionResult) {
		switch (operation) {
			case equals:
				return leftExpressionResult.trim().equalsIgnoreCase(rightExpressionResult.trim());
			case unequals:
				return !leftExpressionResult.trim().equalsIgnoreCase(rightExpressionResult.trim());
			case greaterthan:
				return leftExpressionResult.trim().compareToIgnoreCase(rightExpressionResult.trim()) > 0;
			case greaterthanorequals:
				return leftExpressionResult.trim().compareToIgnoreCase(rightExpressionResult.trim()) >= 0;
			case lessthan:
				return leftExpressionResult.trim().compareToIgnoreCase(rightExpressionResult.trim()) < 0;
			case lessthanorequals:
				return leftExpressionResult.trim().compareToIgnoreCase(rightExpressionResult.trim()) <= 0;
			case contains:
				return leftExpressionResult.trim().toLowerCase().contains(rightExpressionResult.trim().toLowerCase());
			case startswith:
				return leftExpressionResult.trim().toLowerCase().startsWith(rightExpressionResult.trim().toLowerCase());
			case endswith:
				return leftExpressionResult.trim().toLowerCase().endsWith(rightExpressionResult.trim().toLowerCase());
			case notcontains:
				return !leftExpressionResult.trim().toLowerCase().contains(rightExpressionResult.trim().toLowerCase());
			case notstartswith:
				return !leftExpressionResult.trim().toLowerCase().startsWith(rightExpressionResult.trim().toLowerCase());
			case notendswith:
				return !leftExpressionResult.trim().toLowerCase().endsWith(rightExpressionResult.trim().toLowerCase());
				
			default:
				return false;
		}
	}

	public String getLeftExpression() {
		return leftExpression;
	}

	public String getRightExpression() {
		return rightExpression;
	}

	public Operation getOperation() {
		return operation;
	}
}
