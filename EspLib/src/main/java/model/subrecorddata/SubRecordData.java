package model.subrecorddata;


public interface SubRecordData {
	public byte[] getRawData();
	public void setValueByString(String stringValue);
	public String getValueAsString();
}
