package model;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Record {
	private String name;
	private int header;
	private int flags;
	protected List<SubRecord> subRecords;
	
	public Record(String name, int header, int flags, byte[] subRecordBytes) throws UnsupportedEncodingException {
		super();
		this.name = name;
		this.header = header;
		this.flags = flags;
		this.subRecords = new ArrayList<SubRecord>();
		readSubrecordsFromBytes(subRecordBytes);
	}
	
	public String getName() {
		return name;
	}
	
	public int getHeader() {
		return header;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public List<SubRecord> getSubRecords() {
		return subRecords;
	}
	
	public byte[] getRawData() {
		List<byte[]> rawDataChunks = new LinkedList<byte[]>();
		int size = 0;
		
		for (SubRecord subRecord : subRecords) {
			byte[] rawData = subRecord.getRawData();
			size += rawData.length;
			rawDataChunks.add(rawData);
		}
		
		byte[] returnRawData = new byte[size + 16];
		System.arraycopy(ModelFunctions.getBytesFromString(name), 0, returnRawData, 0, 4);
		System.arraycopy(ModelFunctions.getBytesFromInt(size), 0, returnRawData, 4, 4);
		System.arraycopy(ModelFunctions.getBytesFromInt(header), 0, returnRawData, 8, 4);
		System.arraycopy(ModelFunctions.getBytesFromInt(flags), 0, returnRawData, 12, 4);
		
		int startPosition = 16;
		for (byte[] rawDataChunk : rawDataChunks) {
			System.arraycopy(rawDataChunk, 0, returnRawData, startPosition, rawDataChunk.length);
			startPosition += rawDataChunk.length;
		}
		
		return returnRawData;
	}
	
	protected void readSubrecordsFromBytes(byte[] subRecordBytes) throws UnsupportedEncodingException {
		ByteBuffer byteBuffer = ModelFunctions.getByteBufferFromBytes(subRecordBytes);

		while (byteBuffer.hasRemaining()) {
			readNextSubRecord(byteBuffer);
		}
	}
	
	protected void readNextSubRecord(ByteBuffer byteBuffer) throws UnsupportedEncodingException {
		String subRecordName = ModelFunctions.getNextRecordNameFromByteBuffer(byteBuffer);
		int recordSize = byteBuffer.getInt();
		byte[] subRecordDataBytes = new byte[recordSize];
		byteBuffer.get(subRecordDataBytes);
		
		subRecords.add(new SubRecord(subRecordName, subRecordDataBytes, this));
	}
}

class RecordCell extends Record {
	private boolean referencesStarted;

	public RecordCell(String name, int header, int flags, byte[] subRecordBytes) throws UnsupportedEncodingException {
		super(name, header, flags, subRecordBytes);
		referencesStarted = false;
	}

	protected void readNextSubRecord(ByteBuffer byteBuffer) throws UnsupportedEncodingException {
		String subRecordName = ModelFunctions.getNextRecordNameFromByteBuffer(byteBuffer);
		int recordSize = byteBuffer.getInt();
		byte[] subRecordDataBytes = new byte[recordSize];
		byteBuffer.get(subRecordDataBytes);
		
		if (!referencesStarted && subRecordName.equals("FRMR")) {
			referencesStarted = true;
		}
		
		if (!referencesStarted) {
			subRecords.add(new SubRecordCellDef(subRecordName, subRecordDataBytes, this));
		} else {
			subRecords.add(new SubRecord(subRecordName, subRecordDataBytes, this));
		}
	}
}
