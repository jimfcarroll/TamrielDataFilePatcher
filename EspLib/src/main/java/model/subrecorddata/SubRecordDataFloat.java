package model.subrecorddata;

import java.util.HashMap;
import java.util.Map;
import model.ModelFunctions;

public class SubRecordDataFloat implements SubRecordData {
	private float value;
	private String key;
	private int refractionDigits;

	public SubRecordDataFloat(byte[] bytes, String key, int refractionDigits) {
		this.value = ModelFunctions.getFloatFromBytes(bytes);
		this.key = key;
		this.refractionDigits = refractionDigits;
	}
	
	public SubRecordDataFloat(float value, String key, int refractionDigits) {
		this.value = value;
		this.key = key;
		this.refractionDigits = refractionDigits;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	@Override
	public byte[] getRawData() {
		return ModelFunctions.getBytesFromFloat(value);
	}

	@Override
	public void setValueByString(String stringValue) {
		Map<String,Float> variables = new HashMap<String, Float>();
		variables.put(key, value);
		
		value = ModelFunctions.evaluateMathematicalExpressionFloat(stringValue, variables);
	}

	@Override
	public String getValueAsString() {
		return ModelFunctions.formatFloatNumber(value, refractionDigits);
	}

}
