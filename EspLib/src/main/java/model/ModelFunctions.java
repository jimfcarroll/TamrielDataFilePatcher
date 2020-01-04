package model;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import database.Columns;

public class ModelFunctions {
	public static final String ASCII_CHARSET_STRING = "US-ASCII";
	public static final String ISO_CHARSET_STRING = "ISO-8859-15";
	
	public static ByteBuffer getByteBufferFromBytes(byte[] bytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		return byteBuffer;
	}
	
	public static ByteBuffer getByteBufferToFill(int len) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(len);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		return byteBuffer;
	}
	
	public static byte[] getBytesFromFloat(float f) {
		ByteBuffer b = getByteBufferToFill(4);
		b.putFloat(f);
		return b.array();
	}
	
	public static float getFloatFromBytes(byte[] bytes) {
		ByteBuffer b = getByteBufferFromBytes(bytes);
		return b.getFloat();
	}
	
	public static String formatFloatNumber(float number, int fractionDigits) {
		NumberFormat nF = NumberFormat.getNumberInstance(Locale.US);
		nF.setRoundingMode(RoundingMode.HALF_UP);
		nF.setMinimumFractionDigits(fractionDigits);
		nF.setMaximumFractionDigits(fractionDigits);
		nF.setGroupingUsed(false);
		
		return nF.format(number);
	}
	
	public static byte[] getBytesFromInt(int i) {
		ByteBuffer b = getByteBufferToFill(4);
		b.putInt(i);
		return b.array();
	}
	
	public static int getIntFromBytes(byte[] bytes) {
		ByteBuffer b = getByteBufferFromBytes(bytes);
		return b.getInt();
	}
	
	public static byte[] getBytesFromLong(long i) {
		ByteBuffer b = getByteBufferToFill(8);
		b.putLong(i);
		return b.array();
	}
	
	public static long getLongFromBytes(byte[] bytes) {
		ByteBuffer b = getByteBufferFromBytes(bytes);
		return b.getLong();
	}
	
	public static byte[] getBytesFromShort(short i) {
		ByteBuffer b = getByteBufferToFill(2);
		b.putShort(i);
		return b.array();
	}
	
	public static short getShortFromBytes(byte[] bytes) {
		ByteBuffer b = getByteBufferFromBytes(bytes);
		return b.getShort();
	}
	
	public static char getCharFromByte(byte b) {
		try {
			String s = new String(new byte[] {b}, ASCII_CHARSET_STRING);
			return s.charAt(0);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return '\0';
		}
	}
	
	public static byte getByteFromChar(char c) {
		try {
			String s = new String(new char[] {c});
			return s.getBytes(ASCII_CHARSET_STRING)[0];
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return '\0';
		}
	}
	
	public static byte[] getBytesFromString(String s) {
		try {
			return s.getBytes(ISO_CHARSET_STRING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] getBytesFromString(String s, int padTo) {
		byte[] bytes = getBytesFromString(s);
		
		if (bytes.length < padTo) {
			bytes = Arrays.copyOf(bytes, padTo);
		}
		
		return bytes;
	}
	
	public static String getStringFromBytes(byte[] bytes) {
		try {
			return new String(bytes, ISO_CHARSET_STRING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getStringFromBytes(byte[] bytes, boolean removePadding) {
		String res = getStringFromBytes(bytes);
		
		if (removePadding) {
			res = res.trim();
		}
		
		return res;
	}
	
	public static String getEscapedString(String s) {
		char[] chars = s.toCharArray();
		StringBuilder sB = new StringBuilder();
		sB.append('\'');
		for (char c : chars) {
			if (c == '\\') {
				sB.append('\\');
			} else if (c == '\'') {
				sB.append('\'');
			}
			sB.append(c);
		}
		sB.append('\'');
		
		return sB.toString();
	}
	
	public static String getDeEscapedString(String s) {
		return evaluateStringExpression(s);
	}
	
	public static String getNextRecordNameFromByteBuffer(ByteBuffer b) {
		byte[] recordNameBytes = new byte[] {
				b.get(),
				b.get(),
				b.get(),
				b.get()
		};
		
		try {
			String recordName = new String(recordNameBytes, ASCII_CHARSET_STRING);
			return recordName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static float evaluateMathematicalExpression(String expression) {
		return evaluateMathematicalExpression(expression, null);
	}
	
	public static float evaluateMathematicalExpression(String expression, Map<String,Float> variables) {
		org.nfunk.jep.JEP myParser = new org.nfunk.jep.JEP();
		
		if (variables != null) {
			Set<String> keySet = variables.keySet();
			for (String key: keySet) {
				myParser.addVariable(key, variables.get(key));
			}
		}
		
		myParser.parseExpression(expression);
		return (float)myParser.getValue();
	}
	
	public static double evaluateMathematicalExpressionDouble(String expression, Map<String,Double> variables) {
		org.nfunk.jep.JEP myParser = new org.nfunk.jep.JEP();
		
		if (variables != null) {
			Set<String> keySet = variables.keySet();
			for (String key: keySet) {
				myParser.addVariable(key, variables.get(key));
			}
		}
		
		myParser.parseExpression(expression);
		return myParser.getValue();
	}
	
	public static String evaluateStringExpression(String expression) {
		return evaluateStringExpression(expression, null);
	}
	
	public static String evaluateStringExpression(String expression, Map<String,String> variables){
		StringBuilder literalBuilder = new StringBuilder();
		StringBuilder variableBuilder = new StringBuilder();
		Reader reader = new StringReader(expression);
		boolean literal = false;
		boolean escape = false;
		char previousChar = '\0';
		int charNum;
		
		try {
		
			while ((charNum = reader.read()) != -1 ) {
				char c = (char)charNum;
				
				switch (c) {
				case '\'':
					if (literal && escape) {
						literalBuilder.append(c);
						escape = false;
					} else {
						literal = !literal;
						if (literal == true && previousChar == '\'') {
							literalBuilder.append('\'');
						}
					}
					break;
				case '\\':
					if (literal && escape) {
						literalBuilder.append(c);
						escape = false;
					} else if (literal) {
						escape = true;
					}
					break;
				case '+':
					if (!literal && variableBuilder.length() > 0 && variables != null) {
						//since the variable ended, append its value to the stringBuilder
						literalBuilder.append(variables.get(variableBuilder.toString()));
						variableBuilder = new StringBuilder();
					} else if (literal) {
						literalBuilder.append(c);
					}
					break;
				case ' ':
				case '\n':
				case '\r':
					if (literal) {
						literalBuilder.append(c);
					}
					break;
				default:
					if (literal) {
						literalBuilder.append(c);
					} else {
						variableBuilder.append(c);
					}
				}
				
				previousChar = c;
			}
		
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		//if the expression ended and there is an inevaluated variable
		if (variableBuilder.length() > 0 && variables != null) {
			literalBuilder.append(variables.get(variableBuilder.toString()));
		}
		
		return literalBuilder.toString();
	}
	
	public static boolean isNumberExpression(String expression, Columns columns) {

		if (expression.contains("'")) {
			return false;
		}
		
		expression = expression.trim();
		if (Character.isDigit(expression.charAt(0))) {
			return true;
		}
		
		if (expression.charAt(0) == '-' && expression.length() > 1 && Character.isDigit(expression.charAt(1))) {
			return true;
		}
		
		String firstVariable = "";
		for(int i = 0; i < expression.length() && Character.isLetter(expression.charAt(i)); i++) {
			firstVariable += expression.charAt(i);
		}
		
		switch (columns.getTypeOf(firstVariable)) {
			case number:
			case integer:
				return true;
			default:
				break;
		}
		
		return false;
	}
	
	public static boolean isStringExpression(String expression, Columns columns) {
		return !isNumberExpression(expression, columns);
	}
}
