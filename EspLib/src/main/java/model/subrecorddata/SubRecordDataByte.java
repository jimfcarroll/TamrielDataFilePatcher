package model.subrecorddata;

import java.util.HashMap;
import java.util.Map;
import model.ModelFunctions;

public class SubRecordDataByte implements SubRecordData {
	private byte value;
	private String key;

	public SubRecordDataByte(byte[] bytes, String key) {
		if (bytes.length != 1) {
			throw new IllegalArgumentException("The passed bytes array must only contain a single byte.");
		}
		
		this.key = key;
		this.value = bytes[0];
	}
	
	public SubRecordDataByte(byte value, String key) {
		this.key = key;
		this.value = value;
	}

	@Override
	public byte[] getRawData() {
		return new byte[] {value};
	}

	@Override
	public void setValueByString(String stringValue) {
		Map<String, Float> variables = new HashMap<String, Float>();
		variables.put(key, new Float(value));
		value = (byte)ModelFunctions.evaluateMathematicalExpression(stringValue, variables);
	}

	@Override
	public String getValueAsString() {
		return String.valueOf(value);
	}

}
