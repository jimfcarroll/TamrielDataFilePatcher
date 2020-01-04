package gui;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import processor.FileProcessor;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField fileNameField;

	public MainFrame() throws HeadlessException {
		setUp();
	}

	public MainFrame(GraphicsConfiguration gc) {
		super(gc);
		setUp();
	}

	public MainFrame(String title) throws HeadlessException {
		super(title);
		setUp();
	}

	public MainFrame(String title, GraphicsConfiguration gc) {
		super(title, gc);
		setUp();
	}
	
	private void setUp() {
		setDefaultCloseOperation(MainFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(2, 1));
		
		JTextArea sqlCommandTextArea = new JTextArea("SELECT * FROM cellObjects");
		JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.getViewport().add(sqlCommandTextArea, null);
		centerPanel.add(scrollPane1);
		
		JTextArea resultTextArea = new JTextArea("");
		JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.getViewport().add(resultTextArea, null);
		centerPanel.add(scrollPane2);
		
		add(centerPanel, "Center");
		
		JPanel fileNameFieldPanel = new JPanel();
		JLabel fileNameFieldLabel = new JLabel("file:");
		fileNameField = new JTextField(30);
		fileNameField.setText(System.getProperty("user.dir") + File.separatorChar);
		JButton startProcessingButton = new JButton("Process File");
		
		fileNameFieldPanel.add(fileNameFieldLabel);
		fileNameFieldPanel.add(fileNameField);
		fileNameFieldPanel.add(startProcessingButton);
		add(fileNameFieldPanel, "North");
		
		FileProcessor fileProcessor = new FileProcessor();
		ProcessFileButtonListener listener = new ProcessFileButtonListener(
				fileProcessor,
				sqlCommandTextArea,
				resultTextArea,
				startProcessingButton,
				fileNameField
		);
		
		startProcessingButton.addActionListener(listener);
		pack();
		setSize(600,400);
	}
}
