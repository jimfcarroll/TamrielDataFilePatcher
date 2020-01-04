package model.subrecorddata;

import java.nio.ByteBuffer;
import java.util.Arrays;

import model.ModelFunctions;

public class SubrecordDataVFXMEntry implements SubRecordData {
	byte[] leadingBytes, trailingBytes;
	String targetNPCId;

	public SubrecordDataVFXMEntry(byte[] bytes) {
		leadingBytes = Arrays.copyOfRange(bytes, 0, 84);
		targetNPCId = ModelFunctions.getStringFromBytes(Arrays.copyOfRange(bytes, 84, 116), true);
		trailingBytes = Arrays.copyOfRange(bytes, 116, 152);
	}

	@Override
	public byte[] getRawData() {
		ByteBuffer bb = ModelFunctions.getByteBufferToFill(152);
		bb.put(leadingBytes);
		bb.put(ModelFunctions.getBytesFromString(targetNPCId, 32));
		bb.put(trailingBytes);
		
		return bb.array();
	}

	public String getTargetNPCId() {
		return targetNPCId;
	}

	public void setTargetNPCId(String targetNPCId) {
		this.targetNPCId = targetNPCId;
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
