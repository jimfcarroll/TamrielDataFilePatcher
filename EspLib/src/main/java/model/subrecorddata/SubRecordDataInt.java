package model.subrecorddata;

import java.util.HashMap;
import java.util.Map;
import model.ModelFunctions;

public class SubRecordDataInt implements SubRecordData {
	private int value;
	private String key;
	
	public SubRecordDataInt(byte[] bytes, String key) {
		super();
		this.value = ModelFunctions.getIntFromBytes(bytes);
		this.key = key;
	}

	public SubRecordDataInt(int value, String key) {
		super();
		this.value = value;
		this.key = key;
	}

	@Override
	public byte[] getRawData() {
		return ModelFunctions.getBytesFromInt(value);
	}

	@Override
	public void setValueByString(String stringValue) {
		Map<String,Float> variables = new HashMap<String, Float>();
		variables.put(key, new Float(value));
		
		value = Math.round(ModelFunctions.evaluateMathematicalExpressionFloat(stringValue, variables));
	}

	@Override
	public String getValueAsString() {
		return String.valueOf(value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
