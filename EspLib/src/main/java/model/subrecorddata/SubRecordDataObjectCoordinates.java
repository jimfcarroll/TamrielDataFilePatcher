package model.subrecorddata;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import model.ModelFunctions;

public class SubRecordDataObjectCoordinates implements SubRecordData {
	private float xPos;
	private float yPos;
	private float zPos;
	private float xRotate;
	private float yRotate;
	private float zRotate;
	
	public SubRecordDataObjectCoordinates(byte[] bytes) {
		ByteBuffer bb = ModelFunctions.getByteBufferFromBytes(bytes);
		xPos = bb.getFloat();
		yPos = bb.getFloat();
		zPos = bb.getFloat();
		xRotate = bb.getFloat();
		yRotate = bb.getFloat();
		zRotate = bb.getFloat();
	}

	@Override
	public byte[] getRawData() {
		ByteBuffer bb = ModelFunctions.getByteBufferToFill(24);
		bb.putFloat(xPos);
		bb.putFloat(yPos);
		bb.putFloat(zPos);
		bb.putFloat(xRotate);
		bb.putFloat(yRotate);
		bb.putFloat(zRotate);
		
		return bb.array();
	}

	public void setValueByString(String value) {
		String[] subExpressions = value.split("#");
		
		if (subExpressions.length != 6) {
			throw new IllegalArgumentException("The passed position data must contain 6 positions separated by #");
		}
		
		Map<String,Float> variables = new HashMap<String, Float>();
		variables.put("xPos", xPos);
		variables.put("yPos", yPos);
		variables.put("zPos", zPos);
		variables.put("xRotate", xRotate);
		variables.put("yRotate", yRotate);
		variables.put("zRotate", zRotate);
		
		xPos = ModelFunctions.evaluateMathematicalExpressionFloat(subExpressions[0], variables);
		yPos = ModelFunctions.evaluateMathematicalExpressionFloat(subExpressions[1], variables);
		zPos = ModelFunctions.evaluateMathematicalExpressionFloat(subExpressions[2], variables);
		xRotate = ModelFunctions.evaluateMathematicalExpressionFloat(subExpressions[3], variables);
		yRotate = ModelFunctions.evaluateMathematicalExpressionFloat(subExpressions[4], variables);
		zRotate = ModelFunctions.evaluateMathematicalExpressionFloat(subExpressions[5], variables);
	}

	public String getValueAsString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ModelFunctions.formatFloatNumber(xPos, 3));
		sb.append(';');
		sb.append(ModelFunctions.formatFloatNumber(yPos, 3));
		sb.append(';');
		sb.append(ModelFunctions.formatFloatNumber(zPos, 3));
		sb.append(';');
		sb.append(ModelFunctions.formatFloatNumber(xRotate, 3));
		sb.append(';');
		sb.append(ModelFunctions.formatFloatNumber(yRotate, 3));
		sb.append(';');
		sb.append(ModelFunctions.formatFloatNumber(zRotate, 3));
		
		return sb.toString();
	}

	public float getxPos() {
		return xPos;
	}

	public void setxPos(float xPos) {
		this.xPos = xPos;
	}

	public float getyPos() {
		return yPos;
	}

	public void setyPos(float yPos) {
		this.yPos = yPos;
	}

	public float getzPos() {
		return zPos;
	}

	public void setzPos(float zPos) {
		this.zPos = zPos;
	}

	public float getxRotate() {
		return xRotate;
	}

	public void setxRotate(float xRotate) {
		this.xRotate = xRotate;
	}

	public float getyRotate() {
		return yRotate;
	}

	public void setyRotate(float yRotate) {
		this.yRotate = yRotate;
	}

	public float getzRotate() {
		return zRotate;
	}

	public void setzRotate(float zRotate) {
		this.zRotate = zRotate;
	}
	
}
