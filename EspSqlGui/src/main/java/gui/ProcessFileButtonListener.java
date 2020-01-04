package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import processor.FileProcessingOrder;
import processor.FileProcessor;
import model.EsFile;

public class ProcessFileButtonListener implements ActionListener, Runnable {
	private FileProcessor fileProcessor;
	private JTextField fileTextField;
	private JTextArea sqlTextField;
	private JTextArea resultTextField;
	private JButton sourceButton;
	
	public ProcessFileButtonListener(FileProcessor fileProcessor, JTextArea sqlTextField, JTextArea resultTextField, JButton sourceButton, JTextField fileTextField) {
		this.fileProcessor = fileProcessor;
		this.sqlTextField = sqlTextField;
		this.resultTextField = resultTextField;
		this.sourceButton = sourceButton;
		this.fileTextField = fileTextField;
	}

	public void actionPerformed(ActionEvent e) {
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void run() {
		sourceButton.setEnabled(false);
		
		try {
			EsFile esFile = new EsFile(fileTextField.getText());
			FileProcessingOrder fileProcessingOrder = FileProcessingOrder.fromSql(sqlTextField.getText());
			
			
			String res = fileProcessor.processOrderAndFormatResults(esFile, fileProcessingOrder);
			resultTextField.setText(res);
			
			esFile.writeToFileSystem();
		} catch (Exception e) {
			e.printStackTrace();
			sourceButton.setEnabled(true);
			return;
		}
		
		sourceButton.setEnabled(true);
	}

}
