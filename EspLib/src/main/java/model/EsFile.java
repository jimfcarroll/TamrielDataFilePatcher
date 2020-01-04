package model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class EsFile {
	private List<Record> records;
	private String fileName;
	
	public EsFile(String fileName) throws IOException {
		records = new ArrayList<Record>(100);
		this.fileName = fileName;
		readRecordsFromFile();
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setRecords(List<Record> records) {
		this.records = records;
	}
	
	public List<Record> getRecords() {
		return records;
	}
	
	public void writeToFileSystem() {
		try {
			FileOutputStream fOS = new FileOutputStream(fileName);
			for (Record record : records) {
				fOS.write(record.getRawData());
			}
			fOS.flush();
			fOS.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readRecordsFromFile() throws IOException {
		FileInputStream fIS = new FileInputStream(fileName);
		byte[] bytes = new byte[(int)(fIS.getChannel().size())];
		fIS.read(bytes);
		fIS.close();

		ByteBuffer byteBuffer = ModelFunctions.getByteBufferFromBytes(bytes);
		while (byteBuffer.hasRemaining()) {
			readNextRecord(byteBuffer);
		}
	}
	
	private void readNextRecord(ByteBuffer byteBuffer) throws UnsupportedEncodingException {
		String recordName = ModelFunctions.getNextRecordNameFromByteBuffer(byteBuffer);
		int recordSize = byteBuffer.getInt();
		int header = byteBuffer.getInt();
		int flags = byteBuffer.getInt();
		
		byte[] subrecordBytes = new byte[recordSize];
		byteBuffer.get(subrecordBytes);
		
		if (recordName.equals("CELL")) {
			records.add(new RecordCell(recordName, header, flags, subrecordBytes));
		} else {
			records.add(new Record(recordName, header, flags, subrecordBytes));
		}
	}
}
