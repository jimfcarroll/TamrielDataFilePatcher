import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import database.Row;
import database.Statement;
import model.EsFile;
import model.ModelFunctions;
import model.Record;
import model.SubRecord;
import model.subrecorddata.SubRecordDataString;
import model.subrecorddata.SubrecordDataNPDT;
import model.subrecorddata.SubrecordDataSPDT;
import model.subrecorddata.SubrecordDataVFXMEntry;
import net.sf.jsqlparser.JSQLParserException;
import processor.FileProcessingOrder;
import processor.FileProcessor;
import processor.StatementProcessingResult;

public class FilePatcher {
	private IdPatchMainFrame mainFrame;
	
	public FilePatcher(IdPatchMainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
	}

	public void patchFiles(List<EsFile> esFiles, List<Translation> translations, String newMasterFileName, int newMasterFileSize, boolean createBackups) throws JSQLParserException {
		FileProcessor fileProcessor = new FileProcessor();
		int currentFile = 0;
		
		for (EsFile esFile: esFiles) {
			mainFrame.notifyAboutCurrentFile(esFile.getFileName().substring(esFile.getFileName().lastIndexOf(File.separatorChar)+1));
			
			if (createBackups) {
				String currentFileName = esFile.getFileName();
				esFile.setFileName(currentFileName+".bak");
				esFile.writeToFileSystem();
				esFile.setFileName(currentFileName);
			}
			
			boolean saveGame = esFile.getFileName().toLowerCase().endsWith("ess");
		
			//Remove old master files and add the new one.
			FileProcessingOrder order = FileProcessingOrder.fromSql("DELETE FROM masterFiles WHERE name='TR_Data.esm' OR name='PC_Data.esm' OR name='Skyrim_Data.esm';");
			fileProcessor.processStatement(esFile, order.getStatements().get(0));

			order = FileProcessingOrder.fromSql("DELETE FROM masterFiles WHERE name='"+newMasterFileName+"';");
			fileProcessor.processStatement(esFile, order.getStatements().get(0));

			order = FileProcessingOrder.fromSql("INSERT INTO masterFiles (name, size) VALUES ('"+newMasterFileName+"',"+newMasterFileSize+");");
			fileProcessor.processStatement(esFile, order.getStatements().get(0));
			
			//
			List<String> duplicateIds = new ArrayList<String>();
			HashMap<String, String> translationMap = new HashMap<String, String>(70000);
			
			Statement scriptLoadStatement = FileProcessingOrder.fromSql("SELECT * FROM scripts;").getStatements().get(0);
			Statement dialogueLoadStatement = FileProcessingOrder.fromSql("SELECT * FROM dialogueResponses;").getStatements().get(0);
			order = FileProcessingOrder.fromSql("SELECT * FROM cellObjects; SELECT * FROM itemReferences; SELECT * FROM spellReferences; SELECT * FROM creatureReferences; SELECT * FROM classReferences; SELECT * FROM factionReferences; SELECT * FROM enchantmentReferences; SELECT * FROM bodypartReferences; SELECT * FROM scriptReferences; SELECT * FROM soundReferences; SELECT * FROM dialogueResponses; SELECT * FROM exteriorCells;");
			List<StatementProcessingResult> results = fileProcessor.processOrder(esFile, order);
			
			long handledEntries = 0;
			long totalEntries = (saveGame) ? translations.size() + esFile.getRecords().size() : translations.size();
			for (StatementProcessingResult result : results) {
				totalEntries += result.getRows().size();
			}
			
			//idObjects
			for (Translation translation : translations) {
				translationMap.put(translation.getPreviousId().toLowerCase(), translation.getNewId());
				mainFrame.notifyAboutLastObject("idObjects: "+translation.getNewId());
				boolean duplicate = duplicateIds.contains(translation.getNewId());
				
				//if the current object has a new id that was already given to a different object before
				if (
					(duplicate && translation.getNewId().endsWith("Region"))/* dirty workaround for region entries in savegames */ ||
					(!saveGame && (duplicate || !translation.getNewId().startsWith("T_")))
				) {
					order = FileProcessingOrder.fromSql("DELETE FROM idObjects WHERE id="+ModelFunctions.getEscapedString(translation.getPreviousId())+";");
					fileProcessor.processStatement(esFile, order.getStatements().get(0));
				}
				//otherwise change the id according to the translation.
				else {
					order = FileProcessingOrder.fromSql("UPDATE idObjects SET id="+ModelFunctions.getEscapedString(translation.getNewId())+" WHERE id="+ModelFunctions.getEscapedString(translation.getPreviousId())+";");
					fileProcessor.processStatement(esFile, order.getStatements().get(0));
					duplicateIds.add(translation.getNewId());
				}
				
				List<Row> scriptRows = fileProcessor.processStatement(esFile, scriptLoadStatement);
				for (Row row : scriptRows) {
					
					//update the name of the script
					String name = row.getValueOf("name");
					if (name.equalsIgnoreCase(translation.getPreviousId())) {
						
						//repurposing the duplicateIds - list to ensure that this script only exists once in the result file.
						if (!saveGame && duplicate) {
							order = FileProcessingOrder.fromSql("DELETE FROM scripts WHERE name ='"+name+"';");
							fileProcessor.processStatement(esFile, order.getStatements().get(0));
							continue;
						} else {
							order = FileProcessingOrder.fromSql("UPDATE scripts SET name ='"+translation.getNewId()+"' WHERE name='"+name+"';");
							fileProcessor.processStatement(esFile, order.getStatements().get(0));
							name = translation.getNewId();
						}
					}
					
					String text = row.getValueOf("text");
					if (text != null) {
						text = text.trim();
						
						//remove unwanted characters from the beginning of the script.
						while (!text.startsWith("b") && !text.startsWith("B")) {
							text = text.substring(1);
						}
						
						String textChanged = text.replaceAll("(?i)"+translation.getPreviousId(), translation.getNewId());
						
						//sometimes the name of the script at the top gets accidentally overwritten
						if (!textChanged.toLowerCase().startsWith("begin "+name.toLowerCase())) {
							String messedUpHeader = textChanged.substring(0, textChanged.indexOf('\r'));
							textChanged = textChanged.replaceAll(messedUpHeader, "Begin "+name);
						}
						
						if (!textChanged.equals(text)) {
							order = FileProcessingOrder.fromSql("UPDATE scripts SET text ="+ModelFunctions.getEscapedString(textChanged)+" WHERE name='"+name+"';");
							fileProcessor.processStatement(esFile, order.getStatements().get(0));
						}
					}
					
//					List<Row> dialogueRows = fileProcessor.processStatement(esFile, dialogueLoadStatement);
//					for (Row dialogueRow : dialogueRows) {
//						String resultText = dialogueRow.getValueOf("resultText");
//						String resultTextChanged = resultText.replaceAll("(?i)"+translation.getPreviousId(), translation.getNewId());
//						
//						if (!resultTextChanged.equals(resultText)) {
//							order = FileProcessingOrder.fromSql("UPDATE dialogueResponses SET resultText ="+ModelFunctions.getEscapedString(resultTextChanged)+" WHERE id='"+dialogueRow.getValueOf("id")+"';");
//							fileProcessor.processStatement(esFile, order.getStatements().get(0));
//						}
//					}
					
//					String byteCode = row.getValueOf("byteCode");
//					if (byteCode != null) {
//						String byteCodeChanged = byteCode.replaceAll("(?i)"+translation.getPreviousId(), translation.getNewId());
//						if (!byteCodeChanged.equals(byteCode)) {
//							order = FileProcessingOrder.fromSql("UPDATE scripts SET byteCode ="+ModelFunctions.getEscapedString(byteCodeChanged)+" WHERE name='"+name+"';");
//							fileProcessor.processStatement(esFile, order.getStatements().get(0));
//						}
//					}
				}
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
			}
			
			//cellObjects
			List<String> completedTranslations = new ArrayList<String>(70000);
			
			//iterating the file contents manually due to performance-issues
			for (Record record : esFile.getRecords()) {
				if (!record.getName().equals("CELL")) {
					continue;
				}
				boolean cellObjectsStarted = false;
				for (SubRecord subRecord : record.getSubRecords()) {
					if (cellObjectsStarted) {
						if (subRecord.getName().equals("NAME")) {
							handledEntries++;
							float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
							progress += currentFile*(100f/esFiles.size());
							mainFrame.notifyAboutProgress((int)progress, false);
							
							String id = subRecord.getData().getValueAsString().toLowerCase().replaceAll("\n", "").replaceAll("\r", "");
							if (translationMap.containsKey(id)) {
								SubRecordDataString data = (SubRecordDataString)subRecord.getData();
								data.setValue(translationMap.get(id));
							}
						}
					} else if (subRecord.getName().equals("FRMR")) {
						cellObjectsStarted = true;
					} else if (subRecord.getName().equals("NAME")) {
						mainFrame.notifyAboutLastObject("cellObjects in: \""+subRecord.getData().getValueAsString()+"\"");
					}
				}
			}
			
			
			//itemReferences
			for (Row row : results.get(1).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("item").toLowerCase();
				if (!translationMap.containsKey(id)) {
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("itemReferences: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE itemReferences SET item='"+newId+"' WHERE item="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			//spellReferences
			for (Row row : results.get(2).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("spell").toLowerCase();
				if (!translationMap.containsKey(id)) {
					if (id.startsWith("tr_") || id.startsWith("pc_")) {
					}
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("spellReferences: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE spellReferences SET spell='"+newId+"' WHERE spell="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			//creatureReferences
			for (Row row : results.get(3).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("creature").toLowerCase();
				if (!translationMap.containsKey(id)) {
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("creatureReferences: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE creatureReferences SET creature='"+newId+"' WHERE creature="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			//classReferences
			for (Row row : results.get(4).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("class").toLowerCase();
				if (!translationMap.containsKey(id)) {
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("classReferences: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE classReferences SET class='"+newId+"' WHERE class="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			//factionReferences
			for (Row row : results.get(5).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("faction").toLowerCase();
				if (!translationMap.containsKey(id)) {
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("factionReferences: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE factionReferences SET faction='"+newId+"' WHERE faction="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			//enchantmentReferences
			for (Row row : results.get(6).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("enchantment").toLowerCase();
				if (!translationMap.containsKey(id)) {
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("enchantmentReferences: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE enchantmentReferences SET enchantment='"+newId+"' WHERE enchantment="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			//bodypartReferences
			for (Row row : results.get(7).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("bodypart").toLowerCase();
				if (!translationMap.containsKey(id)) {
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("bodypartReferences: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE bodypartReferences SET bodypart='"+newId+"' WHERE bodypart="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			//scriptReferences
			for (Row row : results.get(8).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("script").toLowerCase();
				if (!translationMap.containsKey(id)) {
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("scriptReferences: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE scriptReferences SET script='"+newId+"' WHERE script="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			//soundReferences
			for (Row row : results.get(9).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("sound").toLowerCase();
				if (!translationMap.containsKey(id)) {
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("soundReferences: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE soundReferences SET sound='"+newId+"' WHERE sound="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			//dialogueResponses
			for (Row row : results.get(10).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String responseId = row.getValueOf("id");
				mainFrame.notifyAboutLastObject("dialogueResponses: "+responseId);

				//speaker id
				String npc = row.getValueOf("npc").toLowerCase();
				if (translationMap.containsKey(npc)) {
					order = FileProcessingOrder.fromSql("UPDATE dialogueResponses SET npc ='"+translationMap.get(npc)+"' WHERE id='"+responseId+"';");
					fileProcessor.processStatement(esFile, order.getStatements().get(0));
				}
				//speaker class
				String npcClass = row.getValueOf("class").toLowerCase();
				if (translationMap.containsKey(npcClass)) {
					order = FileProcessingOrder.fromSql("UPDATE dialogueResponses SET class ='"+translationMap.get(npcClass)+"' WHERE id='"+responseId+"';");
					fileProcessor.processStatement(esFile, order.getStatements().get(0));
				}
				//speakers and player faction
				String faction = row.getValueOf("faction").toLowerCase();
				if (translationMap.containsKey(faction)) {
					order = FileProcessingOrder.fromSql("UPDATE dialogueResponses SET faction ='"+translationMap.get(faction)+"' WHERE id='"+responseId+"';");
					fileProcessor.processStatement(esFile, order.getStatements().get(0));
				}
				String playersFaction = row.getValueOf("playersFaction").toLowerCase();
				if (translationMap.containsKey(playersFaction)) {
					order = FileProcessingOrder.fromSql("UPDATE dialogueResponses SET playersFaction ='"+translationMap.get(playersFaction)+"' WHERE id='"+responseId+"';");
					fileProcessor.processStatement(esFile, order.getStatements().get(0));
				}
				
				//if the id shows up in any of the function values
				for (int i =1; i <= 6; i++) {
					String currentValue = row.getValueOf("func"+i+"Value").toLowerCase();
					if (translationMap.containsKey(currentValue)) {
						order = FileProcessingOrder.fromSql("UPDATE dialogueResponses SET func"+i+"Value ='"+translationMap.get(currentValue)+"' WHERE id='"+responseId+"';");
						fileProcessor.processStatement(esFile, order.getStatements().get(0));
					}
				}

				//exchanging text occurence of the old id for the new id in the resultText
				String resultText = row.getValueOf("resultText");
				String resultTextChanged =  resultText;
				List<String> resultTextElements = new LinkedList<String>();
				
				String[] resultTextSplit = resultText.split("\"");
				for (int i = 0; i < resultTextSplit.length; i++) {
					String element = resultTextSplit[i];
					
					//every second element is a literal
					if (i % 2 == 1) {
						resultTextElements.add(element);
						continue;
					}
					
					String[] elementSplit1 = element.split("\n");
					for (String elementSplitLine1 : elementSplit1) {
						String[] elementSplit2 = elementSplitLine1.split("\r");
						for (String elementSplitLine2 : elementSplit2) {
							String[] elementSplit3 = elementSplitLine2.split(" ");
							for (String elementSplitLine3 : elementSplit3) {
								String[] elementSplit4 = elementSplitLine3.split(",");
								for (String elementSplitLine4 : elementSplit4) {
									resultTextElements.add(elementSplitLine4);
								}
							
							}
						}
					}
				}
				
				for (String element : resultTextElements) {
					if (translationMap.containsKey(element.toLowerCase())) {
						resultTextChanged = resultTextChanged.replaceAll(element, translationMap.get(element.toLowerCase()));
					}
				}
				if (!resultTextChanged.equals(resultText)) {
					order = FileProcessingOrder.fromSql(
							"UPDATE dialogueResponses SET resultText ="+ModelFunctions.getEscapedString(resultTextChanged)+" WHERE id='"+responseId+"';"
							);
					fileProcessor.processStatement(esFile, order.getStatements().get(0));
				}
			}
			
			//exteriorCells
			for (Row row : results.get(11).getRows()) {
				
				handledEntries++;
				float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
				progress += currentFile*(100f/esFiles.size());
				mainFrame.notifyAboutProgress((int)progress, false);
				
				String id = row.getValueOf("region").toLowerCase();
				if (!translationMap.containsKey(id)) {
					continue;
				}
				
				if (Collections.binarySearch(completedTranslations, id) >= 0) {
					continue;
				}
				
				String newId = translationMap.get(id);
				mainFrame.notifyAboutLastObject("exteriorCells: "+newId);
				order = FileProcessingOrder.fromSql("UPDATE exteriorCells SET region="+ModelFunctions.getEscapedString(newId)+" WHERE region="+ModelFunctions.getEscapedString(id)+";");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				completedTranslations.add(id);
				Collections.sort(completedTranslations);
			}
			completedTranslations.clear();
			
			
			
			//creatureInstances and containers in savegames
			if (saveGame) {
				mainFrame.notifyAboutLastObject("savegame - clearing region entries");
				order = FileProcessingOrder.fromSql("DELETE FROM idObjects WHERE type='region';");
				fileProcessor.processStatement(esFile, order.getStatements().get(0));
				
				mainFrame.notifyAboutLastObject("savegame - creatures & containers & spelleffects");
				//fixing these manually to improve performance
				for (Record record : esFile.getRecords()) {
					handledEntries++;
					float progress = ((handledEntries*100f) / totalEntries) / esFiles.size();
					progress += currentFile*(100f/esFiles.size());
					mainFrame.notifyAboutProgress((int)progress, false);
					
					if (record.getName().equals("CELL")) {
						boolean referencesStarted = false;
						
						for (SubRecord subrecord : record.getSubRecords()) {
							if (subrecord.getName().equals("FRMR") && !referencesStarted) {
								referencesStarted = true;
							} else if ((subrecord.getName().equals("NAME") || subrecord.getName().equals("XSOL") || subrecord.getName().equals("ANAM")) && referencesStarted) {
								SubRecordDataString data = (SubRecordDataString)subrecord.getData();
								String referenceIdWithDigits = data.getValueAsString();
								
								String newId = retrieveNewIdWithDigits(translationMap, referenceIdWithDigits);
								if (newId != null) {
									data.setValue(newId);
								}
								
							}
						}
						
					} else if (record.getName().equals("REFR")) {
						for (SubRecord subrecord : record.getSubRecords()) {
							if (subrecord.getName().equals("TGTN")) {
								SubRecordDataString data = (SubRecordDataString)subrecord.getData();
								String referenceIdWithDigits = data.getValueAsString();
								
								String newId = retrieveNewIdWithDigits(translationMap, referenceIdWithDigits);
								if (newId != null) {
									data.setValue(newId);
								}
								
							}
						}
						
					} else if (record.getName().equals("SPLM")) {
						
						for (SubRecord subrecord : record.getSubRecords()) {
							if (subrecord.getName().equals("SPDT")) {
								SubrecordDataSPDT data = (SubrecordDataSPDT)subrecord.getData();
								
								if (translationMap.containsKey(data.getItemId().toLowerCase())) {
									data.setItemId(translationMap.get(data.getItemId().toLowerCase()));
								}
								
								if (translationMap.containsKey(data.getSpellId().toLowerCase())) {
									data.setSpellId(translationMap.get(data.getSpellId().toLowerCase()));
								}
								
								String casterId = data.getCasterId().toLowerCase();
								if (translationMap.containsKey(casterId)) {
									data.setCasterId(translationMap.get(casterId));
								} else {
									
									String newCasterId = retrieveNewIdWithDigits(translationMap, casterId);
									if (newCasterId != null) {
										data.setCasterId(newCasterId);
									}
								}
								
							} else if (subrecord.getName().equals("NPDT")) {
								SubrecordDataNPDT data = (SubrecordDataNPDT)subrecord.getData();
								
								String affectedNpcId = data.getAffectedNpcId().toLowerCase();
								if (translationMap.containsKey(affectedNpcId)) {
									data.setAffectedNpcId(translationMap.get(affectedNpcId));
								} else {
									
									String newAffectedNpcId = retrieveNewIdWithDigits(translationMap, affectedNpcId);
									if (newAffectedNpcId != null) {
										data.setAffectedNpcId(newAffectedNpcId);
									}
								}
							}
						}
						
					} else if (record.getName().equals("VFXM")) {
						
						for (SubRecord subrecord : record.getSubRecords()) {
							if (subrecord.getName().equals("VNAM")) {
								SubrecordDataVFXMEntry data = (SubrecordDataVFXMEntry)subrecord.getData();
								
								String affectedNpcId = data.getTargetNPCId().toLowerCase();
								if (translationMap.containsKey(affectedNpcId)) {
									data.setTargetNPCId(translationMap.get(affectedNpcId));
								} else {
									
									String newAffectedNpcId = retrieveNewIdWithDigits(translationMap, affectedNpcId);
									if (newAffectedNpcId != null) {
										data.setTargetNPCId(newAffectedNpcId);
									}
								}
							}
						}
					}
				}
				
			}
			
		
			esFile.writeToFileSystem();
			currentFile++;
		}
		
		mainFrame.notifyAboutLastObject("");
		mainFrame.notifyAboutProgress(100, true);
	}
	
	private String retrieveNewIdWithDigits(Map<String, String> translationMap, String referenceIdWithDigits) {
		if (referenceIdWithDigits.length() <= 8) {
			return null;
		}
		
		String referenceIdWithoutDigits = referenceIdWithDigits.substring(0, referenceIdWithDigits.length()-8).toLowerCase();
		
		if (translationMap.containsKey(referenceIdWithoutDigits)) {
			String translation = translationMap.get(referenceIdWithoutDigits);
			
			if (translation.length() > 23) {
				translation = translation.substring(0, 23);
			}
			
			return translation + referenceIdWithDigits.substring(referenceIdWithDigits.length()-8);
		}
		
		return null;
	}
	
	private boolean typeIsSpell(String type) {
		return type.equals("spell");
	}
	
	private boolean typeIsSound(String type) {
		return type.equals("sound");
	}
	
	private boolean typeIsScript(String type) {
		return type.equals("script");
	}
	
	private boolean typeIsItem(String type) {
		return	type.equals("misc item") ||
				type.equals("weapon") ||
				type.equals("light") ||
				type.equals("armor") ||
				type.equals("clothing") ||
				type.equals("repair item") ||
				type.equals("apparatus") ||
				type.equals("lockpick") ||
				type.equals("probe") ||
				type.equals("ingredient") ||
				type.equals("book") ||
				type.equals("alchemy") ||
				type.equals("levelled item")
				;
	}
	
	private boolean typeIsFaction(String type) {
		return type.equals("faction");
	}
	
	private boolean typeIsEnchantment(String type) {
		return type.equals("enchantment");
	}
	
	private boolean typeIsNpcOrCreature(String type) {
		return type.equals("npc") || type.equals("creature") || type.equals("levelled creature");
	}
	
	private boolean typeIsClass(String type) {
		return type.equals("class");
	}
	
	private boolean typeIsBodypart(String type) {
		return type.equals("bodypart");
	}
	
	private boolean typeIsCellObject(String type) {
		return 	type.equals("static") ||
				type.equals("door") ||
				type.equals("misc item") ||
				type.equals("weapon") ||
				type.equals("container") ||
				type.equals("creature") ||
				type.equals("bodypart") ||
				type.equals("light") ||
				type.equals("npc") ||
				type.equals("armor") ||
				type.equals("clothing") ||
				type.equals("repair item") ||
				type.equals("activator") ||
				type.equals("apparatus") ||
				type.equals("lockpick") ||
				type.equals("probe") ||
				type.equals("ingredient") ||
				type.equals("book") ||
				type.equals("alchemy") ||
				type.equals("levelled creature")
				;
	}
	
}
