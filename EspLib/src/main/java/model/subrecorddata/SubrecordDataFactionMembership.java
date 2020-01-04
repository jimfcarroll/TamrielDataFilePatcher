package model.subrecorddata;

import java.nio.ByteBuffer;

import model.ModelFunctions;

public class SubrecordDataFactionMembership implements SubRecordData {
	private int int1, int2, int3;
	private String factionName;
	private byte[] factionNameBytes; //to retain the restbytes

	public SubrecordDataFactionMembership(byte[] rawData) {
		ByteBuffer bb = ModelFunctions.getByteBufferFromBytes(rawData);
		
		int1 = bb.getInt();
		int2 = bb.getInt();
		int3 = bb.getInt();
		
		factionNameBytes = new byte[32];
		bb.get(factionNameBytes);
		factionName = ModelFunctions.getStringFromBytes(factionNameBytes, true);
		
		if (factionName.contains("\0")) {
			factionName = factionName.substring(0, factionName.indexOf('\0'));
		}
	}

	@Override
	public byte[] getRawData() {
		ByteBuffer bb = ModelFunctions.getByteBufferToFill(44);
		bb.putInt(int1);
		bb.putInt(int2);
		bb.putInt(int3);
		
		byte[] stringBytes1 = ModelFunctions.getBytesFromString(factionName+'\0');
		byte[] stringBytes2 = factionNameBytes.clone();
		System.arraycopy(stringBytes1, 0, stringBytes2, 0, stringBytes1.length);
		bb.put(stringBytes2);
		
		return bb.array();
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

	public String getFaction() {
		return factionName;
	}

	public void setFaction(String factionName) {
		this.factionName = factionName;
	}
}
