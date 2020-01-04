import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileSelectorPanel extends JPanel implements ActionListener {
	private List<String> selectedFiles;
	private JTextField selectedFilesTextField;
	private JFrame parentFrame;
	private JButton openFileDialogueButton;

	public FileSelectorPanel(JFrame parentFrame) {
		this.parentFrame = parentFrame;
		selectedFiles = new LinkedList<String>();
		selectedFilesTextField = new JTextField(40);
		selectedFilesTextField.setEditable(false);
		openFileDialogueButton = new JButton("Open Files");
		openFileDialogueButton.addActionListener(this);
		
		add(selectedFilesTextField);
		add(openFileDialogueButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		openFileDialogueButton.setEnabled(false);
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Morrowind Plugin & Savegame Files", "ess", "esp", "esm");
		fileChooser.setFileFilter(filter);
		fileChooser.setMultiSelectionEnabled(true);
		int returnValue = fileChooser.showOpenDialog(parentFrame);
		openFileDialogueButton.setEnabled(true);
		
		if (returnValue != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File[] selectedFiles = fileChooser.getSelectedFiles();
		if (selectedFiles.length == 0) {
			return;
		}
		
		this.selectedFiles.clear();
		StringBuilder sB = new StringBuilder();
		for (File file: selectedFiles) {
			try {
				this.selectedFiles.add(file.getCanonicalPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			sB.append('"');
			sB.append(file.getName());
			sB.append("\" ");
		}
		
		selectedFilesTextField.setText(sB.toString());
	}
	
	void setLoadButtonEnabled(boolean enabled) {
		this.openFileDialogueButton.setEnabled(enabled);
	}

	public List<String> getSelectedFiles() {
		return selectedFiles;
	}
}
