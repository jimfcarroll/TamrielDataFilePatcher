package model.subrecorddata;

import java.util.HashMap;
import java.util.Map;
import model.ModelFunctions;

public class SubRecordDataShort implements SubRecordData {
	private short value;
	private String key;

	public SubRecordDataShort(byte[] bytes, String key) {
		if (bytes.length != 2) {
			throw new IllegalArgumentException("The passed bytes array must consist of two bytes.");
		}
		
		this.key = key;
		this.value = ModelFunctions.getShortFromBytes(bytes);
	}

	@Override
	public byte[] getRawData() {
		return ModelFunctions.getBytesFromShort(value);
	}

	@Override
	public void setValueByString(String stringValue) {
		Map<String,Float> variables = new HashMap<String, Float>();
		variables.put(key, new Float(value));
		
		value = (short)Math.round(ModelFunctions.evaluateMathematicalExpression(stringValue, variables));
	}

	@Override
	public String getValueAsString() {
		return String.valueOf(value);
	}

}
