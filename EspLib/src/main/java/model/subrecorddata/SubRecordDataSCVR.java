package model.subrecorddata;


import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import model.ModelFunctions;

public class SubRecordDataSCVR implements SubRecordData {
	private static final Map<Character, String> typeToTypeName = new HashMap<Character, String>();
	private static final Map<String, Character> typeNameToType = new HashMap<String, Character>();
	static {
		typeToTypeName.put('\0', "");
		typeToTypeName.put('0', "?");
		typeToTypeName.put('1', "Function");
		typeToTypeName.put('2', "Global");
		typeToTypeName.put('3', "Local");
		typeToTypeName.put('4', "Journal");
		typeToTypeName.put('5', "Item");
		typeToTypeName.put('6', "Dead");
		typeToTypeName.put('7', "Not ID");
		typeToTypeName.put('8', "Not Faction");
		typeToTypeName.put('9', "Not Class");
		typeToTypeName.put('A', "Not Race");
		typeToTypeName.put('B', "Not Cell");
		typeToTypeName.put('C', "Not Local");
		
		for (Character key : typeToTypeName.keySet()) {
			typeNameToType.put(typeToTypeName.get(key), key);
		}
	}
	
	private static final Map<Character, String> compareOpToCompareOpName = new HashMap<Character, String>();
	private static final Map<String, Character> compareOpNameToCompareOp = new HashMap<String, Character>();
	static {
		compareOpToCompareOpName.put('\0', "");
		compareOpToCompareOpName.put('0', "=");
		compareOpToCompareOpName.put('1', "!=");
		compareOpToCompareOpName.put('2', ">");
		compareOpToCompareOpName.put('3', ">=");
		compareOpToCompareOpName.put('4', "<");
		compareOpToCompareOpName.put('5', "<=");
		
		for (Character key : compareOpToCompareOpName.keySet()) {
			compareOpNameToCompareOp.put(compareOpToCompareOpName.get(key), key);
		}
	}
	
	private byte index;
	private byte type;
	private String function;
	private byte compareOp;
	private String name;

	public SubRecordDataSCVR(byte[] bytes) {
		index = bytes[0];
		type = bytes[1];
		function = ModelFunctions.getStringFromBytes(new byte[] {bytes[2], bytes[3]});
		compareOp = bytes[4];
		
		if (bytes.length > 5) {
			name = ModelFunctions.getStringFromBytes(Arrays.copyOfRange(bytes, 5, bytes.length));
		} else {
			name = null;
		}
	}
	
	public SubRecordDataSCVR(int numberIndex) {
		this.index = ModelFunctions.getByteFromChar(String.valueOf(numberIndex).charAt(0));
		type = 0;
		function = "";
		compareOp = 0;
		name = "";
	}

	@Override
	public byte[] getRawData() {
		ByteBuffer bb;
		byte[] valueBytes = null;
		
		if (name == null) {
			bb = ModelFunctions.getByteBufferToFill(5);
		} else {
			valueBytes = ModelFunctions.getBytesFromString(name);
			bb = ModelFunctions.getByteBufferToFill(5+valueBytes.length);
		}
		
		bb.put(index);
		bb.put(type);
		bb.put(ModelFunctions.getBytesFromString(function));
		bb.put(compareOp);
		
		if (name != null) {
			bb.put(valueBytes);
		}
		
		return bb.array();
	}
	
	public void setValueByStringFor(String stringValue, String variable) {
		Map<String, String> variables = new HashMap<String, String>();
		char indexChar = ModelFunctions.getCharFromByte(index);
		int index = Integer.parseInt(indexChar+"")+1;
		
		if (variable.equals("type")) {
			
			variables.put("func"+(index)+"Type", typeToTypeName.get(ModelFunctions.getCharFromByte(type)));
			stringValue = ModelFunctions.evaluateStringExpression(stringValue);
			type = ModelFunctions.getByteFromChar(typeNameToType.get(stringValue));
			
		} else if (variable.equals("function")) {
			
			variables.put("func"+(index)+"Func", function);
			stringValue = ModelFunctions.evaluateStringExpression(stringValue);
			function = stringValue;
			
		} else if (variable.equals("compareOp")) {
			
			variables.put("func"+(index)+"CompareOp", compareOpToCompareOpName.get(ModelFunctions.getCharFromByte(compareOp)));
			stringValue = ModelFunctions.evaluateStringExpression(stringValue);
			compareOp = ModelFunctions.getByteFromChar(compareOpNameToCompareOp.get(stringValue));
			
		} else if (variable.equals("value")) {
			
			variables.put("func"+(index)+"Value", name);
			stringValue = ModelFunctions.evaluateStringExpression(stringValue, variables);
			if (stringValue.length() > 0) {
				name = stringValue;
			} else {
				name = null;
			}
			
		}  else {
			throw new IllegalArgumentException(variable+" can not be retrieved.");
		}
	}
	
	public String getValueAsStringOf(String variable) {
		
		if (variable.equals("type")) {
			return typeToTypeName.get(ModelFunctions.getCharFromByte(type));
		} else if (variable.equals("function")) {
			return function;
		} else if (variable.equals("compareOp")) {
			return compareOpToCompareOpName.get(ModelFunctions.getCharFromByte(compareOp));
		} else if (variable.equals("value")) {
			if (name == null) {
				return "";
			}
			return name;
		}  else {
			throw new IllegalArgumentException(variable+" can not be retrieved.");
		}
	}
	

	@Override
	public void setValueByString(String stringValue) {
	}

	@Override
	public String getValueAsString() {
		return null;
	}
	
	public int getNumberIndex() {
		return Integer.parseInt(ModelFunctions.getCharFromByte(index)+"");
	}

	public void setName(String name) {
		this.name = name;
	}
}
