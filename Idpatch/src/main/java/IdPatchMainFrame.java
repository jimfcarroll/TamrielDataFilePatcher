
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import model.EsFile;
import net.sf.jsqlparser.JSQLParserException;


public class IdPatchMainFrame extends JFrame {
	private FileSelectorPanel fileSelectorPanel;
	private PatchPanel patchPanel;
	private ProgressDisplayPanel progressDisplayPanel;
	private List<Translation> translations;
	private FilePatcher filePatcher;

	public IdPatchMainFrame() throws HeadlessException, IOException {
		super("Tamriel-Data Filepatcher");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(650, 380);
		setLayout(new BorderLayout());
		
		URL applicationRootPathURL = getClass().getProtectionDomain().getCodeSource().getLocation();
		File applicationRootPath = new File(applicationRootPathURL.getPath());
		File file;
		if(applicationRootPath.isDirectory()){
			file = new File(applicationRootPath, "Translation.txt");
		}
		else{
			file = new File(applicationRootPath.getParentFile(), "Translation.txt");
		}
		
		if (!file.exists()) {
			file = new File(System.getProperty("user.dir") + File.separatorChar + "Translation.txt");
			
			if (!file.exists()) {
				addErrorNotification("<html>The Translation.txt is not available. Please add it to the current folder and restart the program.</html>");
				return;
			}
		}

		try {
			translations = TranslationFileReader.readTranslationsFromFile(file.getCanonicalPath());
		} catch (IOException e) {
			addErrorNotification("<html>The Translation.txt could not be read. Please make sure that there are no syntax errors in the file and restart the program.</html>");
			e.printStackTrace();
			return;
		}
		filePatcher = new FilePatcher(this);

		fileSelectorPanel = new FileSelectorPanel(this);
		fileSelectorPanel.setLayout(new FlowLayout());
		add(fileSelectorPanel, "North");

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(1, 2));
		patchPanel = new PatchPanel(this);
		centerPanel.add(patchPanel);
		progressDisplayPanel = new ProgressDisplayPanel();
		centerPanel.add(progressDisplayPanel);
		add(centerPanel, "Center");
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		IdPatchMainFrame mainFrame;
		try {
			mainFrame = new IdPatchMainFrame();
			mainFrame.setVisible(true);
		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void startPatch() {
		fileSelectorPanel.setLoadButtonEnabled(false);
		patchPanel.setButtonEnabled(false);
		
		final List<String> fileNames = fileSelectorPanel.getSelectedFiles();
		final List<EsFile> esFiles = new LinkedList<EsFile>();
		
		for (String fileName: fileNames) {
			try {
				esFiles.add(new EsFile(fileName));
			} catch (IOException e) {
				addErrorNotification("An error occured while trying to read the files to be patched.");
				e.printStackTrace();
				return;
			}
		}
		
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					filePatcher.patchFiles(esFiles, translations, "Tamriel_Data.esm", 7501911, patchPanel.backupsEnabled());
				} catch (JSQLParserException e) {
					addErrorNotification("An error occured while trying to patch the files.");
					e.printStackTrace();
				}
			}
		};
		
		Thread thread = new Thread(runnable);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
	
	synchronized void notifyAboutCurrentFile(String currentFile) {
		patchPanel.setInfo("Patching "+currentFile+"...");
	}
	
	synchronized void notifyAboutProgress(int percent, boolean finished) {
		progressDisplayPanel.updateProgress(percent);
		
		if (finished) {
			fileSelectorPanel.setLoadButtonEnabled(true);
			patchPanel.setButtonEnabled(true);
			patchPanel.setInfo("Finished");
		}
	}
	
	synchronized void notifyAboutLastObject(String object) {
		patchPanel.setLastId(object);
	}
	
	void addErrorNotification(String text) {
		JLabel notificationLabel = new JLabel(text);
		notificationLabel.setForeground(Color.red);
		notificationLabel.setFont(new Font("Arial", Font.BOLD, 20));
		add(notificationLabel, "Center");
	}
}
