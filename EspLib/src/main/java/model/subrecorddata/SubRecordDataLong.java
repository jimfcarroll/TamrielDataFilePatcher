package model.subrecorddata;

import java.util.HashMap;
import java.util.Map;

import model.ModelFunctions;

public class SubRecordDataLong implements SubRecordData {
	private long value;
	private String key;
	
	public SubRecordDataLong(byte[] bytes, String key) {
		super();
		this.value = ModelFunctions.getLongFromBytes(bytes);
		this.key = key;
	}

	public SubRecordDataLong(int value, String key) {
		super();
		this.value = value;
		this.key = key;
	}

	@Override
	public byte[] getRawData() {
		return ModelFunctions.getBytesFromLong(value);
	}

	@Override
	public void setValueByString(String stringValue) {
		Map<String,Double> variables = new HashMap<String, Double>();
		variables.put(key, new Double(value));
		
		value = Math.round(ModelFunctions.evaluateMathematicalExpressionDouble(stringValue, variables));
	}

	@Override
	public String getValueAsString() {
		return String.valueOf(value);
	}

	public long getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
