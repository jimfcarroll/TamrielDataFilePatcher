package model;

import java.io.UnsupportedEncodingException;

import model.subrecorddata.SubRecordDataFloat;
import model.subrecorddata.SubRecordDataInt;
import model.subrecorddata.SubRecordDataLong;
import model.subrecorddata.SubRecordData;
import model.subrecorddata.SubRecordDataByte;
import model.subrecorddata.SubRecordDataComposed;
import model.subrecorddata.SubRecordDataObjectCoordinates;
import model.subrecorddata.SubRecordDataSCVR;
import model.subrecorddata.SubRecordDataShort;
import model.subrecorddata.SubRecordDataString;
import model.subrecorddata.SubRecordDataUnknown;
import model.subrecorddata.SubrecordDataFactionMembership;
import model.subrecorddata.SubrecordDataNPCO;
import model.subrecorddata.SubrecordDataNPDT;
import model.subrecorddata.SubrecordDataSPDT;
import model.subrecorddata.SubrecordDataVFXMEntry;

public class SubRecord{
	protected Record parentRecord;
	protected String name;
	protected SubRecordData data;
	
	public SubRecord(String name, byte[] subrecordDataBytes, Record parentRecord) throws UnsupportedEncodingException {
		super();
		this.name = name;
		this.parentRecord = parentRecord;
		readSubRecordData(subrecordDataBytes);
	}
	
	public SubRecord(String name, SubRecordData data, Record parentRecord) {
		super();
		this.name = name;
		this.data = data;
		this.parentRecord = parentRecord;
	}

	public String getName() {
		return name;
	}

	public SubRecordData getData() {
		return data;
	}
	
	public byte[] getRawData() {
		byte[] subRawData = data.getRawData();
		byte[] rawData = new byte[subRawData.length + 8];
		
		System.arraycopy(ModelFunctions.getBytesFromString(name), 0, rawData, 0, 4);
		System.arraycopy(ModelFunctions.getBytesFromInt(subRawData.length), 0, rawData, 4, 4);
		System.arraycopy(subRawData, 0, rawData, 8, subRawData.length);
		
		return rawData;
	}
	
	protected void readSubRecordData(byte[] subrecordDataBytes) throws UnsupportedEncodingException {
		
		//subrecords in a cell without the cell definitions (see SubRecordCellDef)
		if (parentRecord.getName().equals("CELL")) {
			if ((name.equals("XSCL"))) {
				this.data = new SubRecordDataFloat(subrecordDataBytes, "scale", 2);
			} else if (name.equals("NAME")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "id", 1, 50);
			} else if (name.equals("DATA") || name.equals("DODT")) {
				this.data = new SubRecordDataObjectCoordinates(subrecordDataBytes);
			} else if (name.equals("FRMR")) {
				this.data = new SubRecordDataInt(subrecordDataBytes, "index");
			} else if (name.equals("XSOL")) {
				//soul in a soulgem
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 1, 32);
			} else if (name.equals("ANAM")) {
				//owner of an object
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 1, 32);
			} else if (name.equals("TNAM")) {
				//a trap
				this.data = new SubRecordDataString(subrecordDataBytes, "spell", 1, 32);
			} else if (name.equals("KNAM")) {
				//a key
				this.data = new SubRecordDataString(subrecordDataBytes, "item", 1, 32);
			} else if (name.equals("CNAM")) {
				//owning faction
				this.data = new SubRecordDataString(subrecordDataBytes, "faction", 1, 50);
			} else {
				this.data = new SubRecordDataUnknown(subrecordDataBytes);
			}
			return;
		}
		
		if (parentRecord.getName().equals("REFR")) {
			 if (name.equals("TGTN")) {
				//target group member
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 1, 32);
				return;
			} else if (name.equals("WNAM")) {
				//a readied spell
				this.data = new SubRecordDataString(subrecordDataBytes, "spell", 1, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("STLN")) {
			if (name.equals("ONAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 1, 32);
			} else if (name.equals("FNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "faction", 1, 50);
			} else if (name.equals("NAME")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "item", 1, 32);
			} else {
				this.data = new SubRecordDataUnknown(subrecordDataBytes);
			}
			return;
		}
		
		if (parentRecord.getName().equals("SCPT")) {
			if (name.equals("SCTX")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "text", 1, 99999, false);
			} else {
				this.data = new SubRecordDataUnknown(subrecordDataBytes);
			}
			return;
		}
		
		if (parentRecord.getName().equals("LAND")) {
			if (name.equals("INTV")) {
				this.data = new SubRecordDataComposed(
						subrecordDataBytes,
						new SubRecordDataComposed.Type[] {SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Integer},
						new String[] {"gridX", "gridY"}
				);
			} else {
				this.data = new SubRecordDataUnknown(subrecordDataBytes);
			}
			return;
		}
		
		if (parentRecord.getName().equals("FACT")) {
			if (name.equals("ANAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "faction", 1, 50, false);
				return;
			}
		}
		
		if (parentRecord.getName().equals("TES3")) {
			if (name.equals("MAST")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "name", 1, 99999);
			} else if (name.equals("DATA")) {
				this.data = new SubRecordDataLong(subrecordDataBytes, "size");
			} else {
				this.data = new SubRecordDataUnknown(subrecordDataBytes);
			}
			return;
		}
		
		if (parentRecord.getName().equals("INFO")) {
			if ((name.equals("INAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "id", 0, 100);
			} else if ((name.equals("PNAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "previousResponseId", 0, 100);
			} else if ((name.equals("NNAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "followingResponseId", 0, 100);
			} else if ((name.equals("DATA"))) {
				this.data = new SubRecordDataComposed(
						subrecordDataBytes,
						new SubRecordDataComposed.Type[] {SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Byte, SubRecordDataComposed.Type.Byte, SubRecordDataComposed.Type.Byte, SubRecordDataComposed.Type.Byte},
						new String[] {"?1", "disposition", "rank", "gender", "playersRank", "?2"}
				);
			} else if ((name.equals("ONAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "npc", 0, 32);
			} else if ((name.equals("RNAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "race", 0, 100);
			} else if ((name.equals("CNAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "class", 0, 100);
			} else if ((name.equals("FNAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "faction", 0, 100);
			} else if ((name.equals("ANAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "cell", 0, 100);
			} else if ((name.equals("DNAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "playersFaction", 0, 100);
			} else if ((name.equals("NAME"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "text", 0, 512, false);
			} else if ((name.equals("SNAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "sound", 0, 100);
			} else if ((name.equals("QSTN"))) {
				this.data = new SubRecordDataByte(subrecordDataBytes, "journalName");
			} else if ((name.equals("QSTF"))) {
				this.data = new SubRecordDataByte(subrecordDataBytes, "journalFinish");
			} else if ((name.equals("QSTR"))) {
				this.data = new SubRecordDataByte(subrecordDataBytes, "journalRestart");
			} else if ((name.equals("FLTV"))) {
				this.data = new SubRecordDataFloat(subrecordDataBytes, "targetValue", 3);
			} else if ((name.equals("INTV"))) {
				this.data = new SubRecordDataInt(subrecordDataBytes, "targetValue");
			} else if ((name.equals("BNAM"))) {
				this.data = new SubRecordDataString(subrecordDataBytes, "resultText", 0, 9999, false);
			} else if ((name.equals("SCVR"))) {
				this.data = new SubRecordDataSCVR(subrecordDataBytes);
			} else {
				this.data = new SubRecordDataUnknown(subrecordDataBytes);
			}
			
			return;
		}
		
		if (parentRecord.getName().equals("DOOR")) {
			if (name.equals("SNAM") || name.equals("ANAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "sound", 0, 100);
				return;
			}
			//based on misinformation?
//			if (name.equals("SCIP")) {
//				this.data = new SubRecordDataString(subrecordDataBytes, "script", 0, 100);
//				return;
//			}
		}
		
		if (parentRecord.getName().equals("REGN")) {
			if (name.equals("SNAM")) {
				this.data = new SubRecordDataUnknown(subrecordDataBytes);
				return;
			} else if (name.equals("BNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 0, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("ARMO")) {
			if (name.equals("BNAM") || name.equals("CNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "bodypart", 0, 32, false);
				return;
			} else if (name.equals("ENAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "enchantment", 0, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("CLOT")) {
			if (name.equals("BNAM") || name.equals("CNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "bodypart", 0, 32, false);
				return;
			} else if (name.equals("ENAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "enchantment", 0, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("WEAP")) {
			if (name.equals("ENAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "enchantment", 0, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("BOOK")) {
			if (name.equals("ENAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "enchantment", 0, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("LEVI")) {
			if (name.equals("INAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "item", 0, 32);
				return;
			} else if (name.equals("INTV")) {
				this.data = new SubRecordDataShort(subrecordDataBytes, "number");
				return;
			}
		}
		
		if (parentRecord.getName().equals("LEVC")) {
			if (name.equals("CNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 0, 32);
				return;
			} else if (name.equals("INTV")) {
				this.data = new SubRecordDataShort(subrecordDataBytes, "level");
				return;
			}
		}
		
		if (parentRecord.getName().equals("SNDG")) {
			if (name.equals("CNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 0, 32);
				return;
			} else if (name.equals("DATA")) {
				this.data = new SubRecordDataInt(subrecordDataBytes, "soundType");
				return;
			}
		}
		
		if (parentRecord.getName().equals("CREA")) {
			if (name.equals("CNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 0, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("LTEX")) {
			if (name.equals("DATA")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "file", 0, 100);
				return;
			}
		}
		
		if (parentRecord.getName().equals("SOUN")) {
			if (name.equals("FNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "file", 0, 100);
				return;
			}
		}
		
		if (parentRecord.getName().equals("NPC_")) {
			if (name.equals("ANAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "faction", 0, 100);
				return;
			} else if (name.equals("CNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "class", 0, 100);
				return;
			} else if (name.equals("BNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "bodypart", 0, 32);
				return;
			} else if (name.equals("KNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "bodypart", 0, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("SPLM")) {
			if (name.equals("SPDT")) {
				this.data = new SubrecordDataSPDT(subrecordDataBytes);
				return;
			} else if (name.equals("NPDT")) {
				this.data = new SubrecordDataNPDT(subrecordDataBytes);
				return;
			} else {
				this.data = new SubRecordDataUnknown(subrecordDataBytes);
				return;
			}
		}
		
		if (parentRecord.getName().equals("VFXM")) {
			if (name.equals("VNAM")) {
				this.data = new SubrecordDataVFXMEntry(subrecordDataBytes);
				return;
			} else {
				this.data = new SubRecordDataUnknown(subrecordDataBytes);
				return;
			}
		}
		
		if (parentRecord.getName().equals("PCDT")){
			//faction membership
			if (name.equals("FNAM")) {
				this.data = new SubrecordDataFactionMembership(subrecordDataBytes);
				return;
			}
			//apparati within alchemy slots.
			else if (name.equals("NAM0") || name.equals("NAM1") || name.equals("NAM2") || name.equals("NAM3")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "item", 1, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("CNTC")){
			//a soul in a soulgem (container)
			if (name.equals("XSOL")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 1, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("KLST")){
			//A creature entry in the killing stats of a savegame
			if (name.equals("KNAM")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "creature", 1, 32);
				return;
			}
		}
		
		if (parentRecord.getName().equals("PGRD")){
			if (name.equals("DATA")) {
				this.data = new SubRecordDataComposed(
						subrecordDataBytes,
						new SubRecordDataComposed.Type[] {SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Short, SubRecordDataComposed.Type.Short},
						new String[] {"gridX", "gridY", "?", "numberOfPoints"}
				);
				return;
			} else if (name.equals("NAME")) {
				this.data = new SubRecordDataString(subrecordDataBytes, "cell", 0, 100);
				return;
			}
			
			this.data = new SubRecordDataUnknown(subrecordDataBytes);
			return;
		}
		
		//fallback for NAME.
		if (name.equals("NAME")) {
			this.data = new SubRecordDataString(subrecordDataBytes, "id", 0, 100, true, true);
			return;
		}
		
		//fallback for SCRI
		if (name.equals("SCRI")) {
			this.data = new SubRecordDataString(subrecordDataBytes, "script", 0, 100);
			return;
		}
		
		//fallback for SNAM
		if (name.equals("SNAM")) {
			this.data = new SubRecordDataString(subrecordDataBytes, "sound", 0, 100);
			return;
		}
		
		//fallback for NPCO
		if (name.equals("NPCO")) {
			this.data = new SubrecordDataNPCO(subrecordDataBytes);
			return;
		}
		
		//fallback for MODL
		if (name.equals("MODL")) {
			this.data = new SubRecordDataString(subrecordDataBytes, "file", 0, 100);
			return;
		}
		
		//fallback for ITEX.
		if (name.equals("ITEX")) {
			this.data = new SubRecordDataString(subrecordDataBytes, "file", 0, 100);
			return;
		}
		
		//fallback for NPCS.
		if (name.equals("NPCS")) {
			this.data = new SubRecordDataString(subrecordDataBytes, "spell", 0, 32);
			return;
		}
		
		this.data = new SubRecordDataUnknown(subrecordDataBytes);
	}
}


class SubRecordCellDef extends SubRecord {

	public SubRecordCellDef(String name, byte[] subrecordDataBytes, Record parentRecord) throws UnsupportedEncodingException {
		super(name, subrecordDataBytes, parentRecord);
	}

	protected void readSubRecordData(byte[] subrecordDataBytes) throws UnsupportedEncodingException {
		if (name.equals("NAME")) {
			this.data = new SubRecordDataString(subrecordDataBytes, "name", 0, 100);
		} else if (name.equals("RGNN")) {
			this.data = new SubRecordDataString(subrecordDataBytes, "region", 0, 100);
		} else if (name.equals("WHGT")) {
			this.data = new SubRecordDataFloat(subrecordDataBytes, "waterheight", 3);
		} else if (name.equals("NAM5")) {
			this.data = new SubRecordDataInt(subrecordDataBytes, "referenceCount");
		} else if (name.equals("DATA")) {
			this.data = new SubRecordDataComposed(
					subrecordDataBytes,
					new SubRecordDataComposed.Type[]{SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Integer},
					new String[]{"flags", "gridX", "gridY"}
			);
		} else if (name.equals("AMBI")) {
			this.data = new SubRecordDataComposed(
					subrecordDataBytes,
					new SubRecordDataComposed.Type[]{SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Integer, SubRecordDataComposed.Type.Float},
					new String[]{"ambientColor", "sunlightColor", "fogColor", "fogDensity"}
			);
		} else {
			this.data = new SubRecordDataUnknown(subrecordDataBytes);
		}
	}
}
