package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SelectFileButtonListener implements ActionListener {
	private JFrame parentFrame;
	private JTextField fileTextField;
	private JButton button;

	public SelectFileButtonListener(JFrame parentFrame, JTextField fileTextField, JButton button) {
		this.parentFrame = parentFrame;
		this.fileTextField = fileTextField;
		this.button = button;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		button.setEnabled(false);
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Morrowind Plugin & Savegame Files", "ess", "esp", "esm");
		fileChooser.setFileFilter(filter);
		fileChooser.setMultiSelectionEnabled(false);
		int returnValue = fileChooser.showOpenDialog(parentFrame);
		button.setEnabled(true);
		
		if (returnValue != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File file = fileChooser.getSelectedFile();
		
		try {
			fileTextField.setText(file.getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
