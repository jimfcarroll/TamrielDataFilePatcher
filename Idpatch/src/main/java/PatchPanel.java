import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PatchPanel extends JPanel implements ActionListener {
	private JCheckBox backupCheckBox;
	private JButton patchButton;
	private IdPatchMainFrame mainFrame;
	private JLabel label, label2;
	
	PatchPanel(IdPatchMainFrame mainFrame) {
		this.mainFrame = mainFrame;
		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(boxLayout);
		
		backupCheckBox = new JCheckBox("Create backup of patched files");
		backupCheckBox.setSelected(true);
		add(backupCheckBox);
		
		patchButton = new JButton("Patch Selected Files");
		Font font = new Font("Arial", Font.PLAIN, 20);
		
		patchButton.setFont(font);
		patchButton.addActionListener(this);
		add(patchButton);
		
		label = new JLabel();
		label.setForeground(Color.blue);
		add(label);
		
		label2 = new JLabel();
		add(label2);
	}
	
	void setButtonEnabled(boolean enabled) {
		this.patchButton.setEnabled(enabled);
	}
	
	void setInfo(String info) {
		label.setText(info);
	}
	
	void setLastId(String id) {
		label2.setText(id);
	}
	
	boolean backupsEnabled() {
		return backupCheckBox.isSelected();
	}
	
	public void actionPerformed(ActionEvent e) {
		mainFrame.startPatch();
	}
}
