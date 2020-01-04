package model.subrecorddata;

import java.nio.ByteBuffer;
import java.util.Arrays;
import model.ModelFunctions;

public class SubrecordDataSPDT implements SubRecordData {
	private int type;
	private String spellId;
	private long l1, l2;
	private String casterId;
	private String itemId;
	private long l3;
	private byte[] restData;

	public SubrecordDataSPDT(byte[] bytes) {
		type = ModelFunctions.getIntFromBytes(Arrays.copyOfRange(bytes, 0, 4));
		spellId = ModelFunctions.getStringFromBytes(Arrays.copyOfRange(bytes, 4, 36), true);
		l1 = ModelFunctions.getLongFromBytes(Arrays.copyOfRange(bytes, 36, 44));
		l2 = ModelFunctions.getLongFromBytes(Arrays.copyOfRange(bytes, 44, 52));
		casterId = ModelFunctions.getStringFromBytes(Arrays.copyOfRange(bytes, 52, 84), true);
		itemId = ModelFunctions.getStringFromBytes(Arrays.copyOfRange(bytes, 84, 116), true);
		l3 = ModelFunctions.getLongFromBytes(Arrays.copyOfRange(bytes, 116, 124));
		restData = Arrays.copyOfRange(bytes, 124, 160);
	}

	@Override
	public byte[] getRawData() {
		ByteBuffer bb = ModelFunctions.getByteBufferToFill(160);
		bb.putInt(type);
		bb.put(ModelFunctions.getBytesFromString(spellId, 32));
		bb.putLong(l1);
		bb.putLong(l2);
		bb.put(ModelFunctions.getBytesFromString(casterId, 32));
		bb.put(ModelFunctions.getBytesFromString(itemId, 32));
		bb.putLong(l3);
		bb.put(restData);
		
		return bb.array();
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getSpellId() {
		return spellId;
	}

	public void setSpellId(String spellId) {
		this.spellId = spellId;
	}

	public long getL1() {
		return l1;
	}

	public void setL1(long l1) {
		this.l1 = l1;
	}

	public long getL2() {
		return l2;
	}

	public void setL2(long l2) {
		this.l2 = l2;
	}

	public String getCasterId() {
		return casterId;
	}

	public void setCasterId(String casterId) {
		this.casterId = casterId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public long getL3() {
		return l3;
	}

	public void setL3(long l3) {
		this.l3 = l3;
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
