package model.subrecorddata;

import model.ModelFunctions;

public class SubRecordDataUnknown implements SubRecordData {

	private byte[] rawData;

	public SubRecordDataUnknown(byte[] rawData) {
		super();
		this.rawData = rawData;
	}

	public byte[] getRawData() {
		return rawData;
	}
	
	public void setValueByString(String value) {
		rawData = ModelFunctions.getBytesFromString(ModelFunctions.evaluateStringExpression(value));
	}

	public String getValueAsString() {
		return ModelFunctions.getStringFromBytes(rawData);
	}
}
