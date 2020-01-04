package model.subrecorddata;

import java.nio.ByteBuffer;
import java.util.Arrays;
import model.ModelFunctions;

public class SubrecordDataNPDT implements SubRecordData {
	
	private String affectedNpcId;
	private byte[] restData;

	public SubrecordDataNPDT(byte[] bytes) {
		affectedNpcId = ModelFunctions.getStringFromBytes(Arrays.copyOfRange(bytes, 0, 32), true);
		restData = Arrays.copyOfRange(bytes, 32, bytes.length);
	}

	@Override
	public byte[] getRawData() {
		ByteBuffer bb = ModelFunctions.getByteBufferToFill(restData.length + 32);
		bb.put(ModelFunctions.getBytesFromString(affectedNpcId, 32));
		bb.put(restData);
		return bb.array();
	}

	public String getAffectedNpcId() {
		return affectedNpcId;
	}

	public void setAffectedNpcId(String affectedNpcId) {
		this.affectedNpcId = affectedNpcId;
	}

	@Override
	public void setValueByString(String stringValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getValueAsString() {
		// TODO Auto-generated method stub
		return null;
	}

}
