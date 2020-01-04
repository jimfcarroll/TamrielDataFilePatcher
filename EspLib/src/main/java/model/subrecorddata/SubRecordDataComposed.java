package model.subrecorddata;


import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import model.ModelFunctions;

public class SubRecordDataComposed implements SubRecordData {
	public enum Type { Byte, Short, Integer, Float }
	private Type[] types;
	private String[] variables;
	private Object[] values;
	private int length;

	public SubRecordDataComposed(byte[] bytes, Type[] types, String[] variables) {
		super();
		this.types = types;
		this.variables = variables;
		this.values = new Object[types.length];
		this.length = bytes.length;
		
		ByteBuffer bb = ModelFunctions.getByteBufferFromBytes(bytes);
		for (int i = 0; i < types.length; i++) {
			switch (types[i]) {
			case Byte:
				values[i] = new Byte(bb.get());
				break;
			case Short:
				values[i] = new Short(bb.getShort());
				break;
			case Integer:
				values[i] = new Integer(bb.getInt());
				break;
			default:
				values[i] = new Float(bb.getFloat());
			}
		}
	}

	@Override
	public byte[] getRawData() {
		ByteBuffer bb = ModelFunctions.getByteBufferToFill(length);
		for (int i = 0; i < values.length; i++) {
			switch (types[i]) {
			case Byte:
				bb.put(((Byte)values[i]).byteValue());
				break;
			case Short:
				bb.putShort(((Short)values[i]).shortValue());
				break;
			case Integer:
				bb.putInt(((Integer)values[i]).intValue());
				break;
			default:
				bb.putFloat(((Float)values[i]).floatValue());
			}
		}
		
		return bb.array();
	}
	
	public void setValueByStringFor(String stringValue, String variable) {
		int index = getIndexOf(variable);
		if (index == -1) {
			throw new IllegalArgumentException(variable+" is not a variable in this subrecord.");
		}
		
		//prepare the variable for the mathematical evaluation
		float currentValue = getNumberValueOf(values[index]);
		Map<String, Float> variables = new HashMap<String, Float>();
		variables.put(variable, currentValue);
		
		float newValue = ModelFunctions.evaluateMathematicalExpressionFloat(stringValue, variables);
		setValueAt(index, newValue);
	}
	
	public String getValueAsStringOf(String variable) {
		int index = getIndexOf(variable);
		if (index == -1) {
			throw new IllegalArgumentException(variable+" is not a variable in this subrecord.");
		}
		
		float currentValue = getNumberValueOf(values[index]);
		switch (types[index]) {
		case Byte:
		case Short:
		case Integer:
			return String.valueOf(Math.round(currentValue));
		default:
			return ModelFunctions.formatFloatNumber(currentValue, 3);
		}
	}
	
	public int getValueAsIntOf(String variable) {
		int index = getIndexOf(variable);
		if (index == -1) {
			throw new IllegalArgumentException(variable+" is not a variable in this subrecord.");
		}
		
		return Math.round(getNumberValueOf(values[index]));
	}
	
	public float getValueAsFloatOf(String variable) {
		int index = getIndexOf(variable);
		if (index == -1) {
			throw new IllegalArgumentException(variable+" is not a variable in this subrecord.");
		}
		
		return getNumberValueOf(values[index]);
	}

	@Override
	public void setValueByString(String stringValue) {
		String[] splitValues = stringValue.split(";");
		if (splitValues.length != variables.length) {
			throw new IllegalArgumentException("The passed value cannot be split into the correct amount of subvalues.");
		}
		
		for (int i = 0; i < variables.length; i++) {
			setValueByStringFor(splitValues[i], variables[i]);
		}
	}

	@Override
	public String getValueAsString() {
		StringBuilder sB = new StringBuilder();
		for (int i = 0; i < variables.length; i++) {
			sB.append(getValueAsStringOf(variables[i]));
			if (i < variables.length-1) {
				sB.append(';');
			}
		}
		
		return sB.toString();
	}
	
	public int getIndexOf(String variable) {
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].equals(variable)) {
				return i;
			}
		}
		return -1;
	}
	
	private void setValueAt(int i, float newValue) {
		switch (types[i]) {
		case Byte:
			values[i] = new Byte((byte)Math.round(newValue));
			break;
		case Short:
			values[i] = new Short((short)Math.round(newValue));
			break;
		case Integer:
			values[i] = new Integer(Math.round(newValue));
			break;
		default:
			values[i] = new Float(newValue);
		}
	}
	
	private float getNumberValueOf(Object o) {
		if (o instanceof Byte) {
			return (((Byte)o).byteValue());
		} else if (o instanceof Short) {
			return (((Short)o).shortValue());
		} else if (o instanceof Integer) {
			return (((Integer)o).intValue());
		} else {
			return (((Float)o).floatValue());
		}
	}
}
