package model.subrecorddata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import model.ModelFunctions;

public class SubrecordDataNPCO implements SubRecordData {
	int number;
	String item;

	public SubrecordDataNPCO(byte[] bytes) {
		number = ModelFunctions.getIntFromBytes(Arrays.copyOfRange(bytes, 0, 4));
		item = ModelFunctions.getStringFromBytes(Arrays.copyOfRange(bytes, 4, bytes.length)).trim()+ '\0';
	}

	@Override
	public byte[] getRawData() {
		byte[] bytes = ModelFunctions.getBytesFromString(item);
		byte[] idBytes = new byte[32];
		System.arraycopy(bytes, 0, idBytes, 0, bytes.length);
		
		byte[] countBytes = ModelFunctions.getBytesFromInt(number);
		byte[] completeBytes = new byte[36];
		System.arraycopy(countBytes, 0, completeBytes, 0, 4);
		System.arraycopy(idBytes, 0, completeBytes, 4, 32);
		
		return completeBytes;
	}
	
	public void setNumberByString(String stringValue) {
		Map<String,Float> variables = new HashMap<String, Float>();
		variables.put("number", new Float(number));
		number = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(stringValue, variables));
	}
	
	public void setItemByString(String stringValue) {
		Map<String,String> variables = new HashMap<String,String>();
		variables.put("item", item);
		item = ModelFunctions.evaluateStringExpression(stringValue, variables);
		
		if (!item.endsWith("\0")) {
			item = item + '\0';
		}
	}
	
	public String getNumberAsString() {
		return String.valueOf(number);
	}
	
	public String getItemAsString() {
		return item.substring(0, item.length()-1);
	}

	@Override
	public void setValueByString(String stringValue) {
		String[] splitValues = stringValue.split(";");
		if (splitValues.length != 2) {
			throw new IllegalArgumentException("");
		}
		
		setNumberByString(splitValues[0]);
		setItemByString(splitValues[1]);
	}

	@Override
	public String getValueAsString() {
		return getNumberAsString()+";"+getItemAsString();
	}

}
