package model.subrecorddata;

import java.util.HashMap;
import java.util.Map;
import model.ModelFunctions;

public class SubRecordDataString implements SubRecordData {

	private String value, key;
	private int minimumLength, maximumLength;
	private boolean nullEnd;
	
	public SubRecordDataString(byte[] bytes, String key, int minimumLength, int maximumLength) {
		this.key = key;
		this.minimumLength = minimumLength;
		this.maximumLength = maximumLength;
		nullEnd = true;
		this.value = ModelFunctions.getStringFromBytes(bytes);
	}
	
	public SubRecordDataString(byte[] bytes, String key, int minimumLength, int maximumLength, boolean nullEnd) {
		this.key = key;
		this.minimumLength = minimumLength;
		this.maximumLength = maximumLength;
		this.nullEnd = nullEnd;
		this.value = ModelFunctions.getStringFromBytes(bytes);
	}
	
	public SubRecordDataString(byte[] bytes, String key, int minimumLength, int maximumLength, boolean nullEnd, boolean removeLinewraps) {
		this.key = key;
		this.minimumLength = minimumLength;
		this.maximumLength = maximumLength;
		this.nullEnd = nullEnd;
		this.value = ModelFunctions.getStringFromBytes(bytes);
		if (removeLinewraps) {
			this.value = this.value.replaceAll("\n", "");
			this.value = this.value.replaceAll("\r", "");
		}
	}
	
	public SubRecordDataString(String value, String key, int minimumLength, int maximumLength) {
		this.key = key;
		this.minimumLength = minimumLength;
		this.maximumLength = maximumLength;
		nullEnd = true;
		setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (value.length() < minimumLength || value.length() > maximumLength) {
			throw new IllegalArgumentException("The passed string must have a minimum length of " + minimumLength + " and a maximum length of " + maximumLength + "." + " Length of passed string:" + value.length());
		}
		
		if (!value.endsWith("\0") && nullEnd) {
			value = value + '\0';
		}
		
		this.value = value;
	}
	
	public void setValueByString(String stringValue) {
		Map<String,String> variables = new HashMap<String, String>();
		variables.put(key, value);
		String value = ModelFunctions.evaluateStringExpression(stringValue, variables);
		setValue(value);
	}
	
	public String getValueAsString() {
		if (nullEnd && value.endsWith("\0"))
			return value.substring(0, value.length()-1);
		
		return value;
	}

	public byte[] getRawData() {
		return ModelFunctions.getBytesFromString(value);
	}

}
