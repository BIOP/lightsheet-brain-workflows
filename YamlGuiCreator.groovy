@Grab('org.yaml:snakeyaml:2.2')

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import org.apache.commons.io.FilenameUtils
import static groovy.io.FileType.FILES
import java.awt.AWTEvent;
import java.util.stream.Collectors
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.*;
import java.awt.FlowLayout;
import javax.swing.BoxLayout
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JOptionPane; 
import java.nio.file.Paths
import java.nio.file.InvalidPathException

#@ File baseYaml

def config = readYamlConfig( baseYaml )

// generate the dialog box
def dialog = new Dialog(config)

return


public class Dialog extends JFrame {
	
	private JComboBox<String> cmbProject;
    private JComboBox<String> cmbDataset;
    private JButton bnOk = new JButton("Finish");
    private JButton bnCancel = new JButton("Cancel");
    private JButton bnNext = new JButton("Next");
    private DefaultComboBoxModel<String> modelCmbProject;
    private DefaultComboBoxModel<String> modelCmbDataset;
    	
	//Client client;
	def userId;
	def project_list;
	boolean enterPressed;
	boolean validated;
	File currentDir = null;
	List<Map<String, String>> selectionList = new ArrayList<>()
	Map<String, List<String>> projectNewDatasets = new HashMap<>()
	String FOL_PATH = "path";
    String OMR_PRJ = "project";
    String OMR_DST = "dataset";
    String IS_NEW_DST = "isNewDataset";
    String RAW_DATA = "rawData";
    String DST_NAME = "datasetName";
	
	public Dialog(yamlContent){
		
		myDialog(yamlContent)
	}
	
	// getters
	public boolean getEnterPressed(){return this.enterPressed}
	public boolean getValidated(){return this.validated}
	public List<Map<String, String>> getSelectedList(){return this.selectionList}
	
	// generate the dialog box
	public void myDialog(yamlContent) {
		// set general frame
		this.setTitle("Select your custom configuration")
	    this.setVisible(true);
	    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    this.setPreferredSize(new Dimension(400, 250));
	    
	    // get the screen size
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        
        // set location in the middle of the screen
	    this.setLocation((int)((width - 400)/2), (int)((height - 250)/2));
		
		JTabbedPane generalPanel = new JTabbedPane();
		
		yamlContent.each{
			JPanel tabGUI = tabGUICreation(it.getValue())
			println tabGUI
			generalPanel.addTab(it.getKey(), null, tabGUI, "Does nothing");
		}
		
        this.setContentPane(generalPanel);
        this.pack();
    }
    
    private JPanel tabGUICreation(tabContentList){
		JPanel tabPanel = new JPanel();
		tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
		
		if(tabContentList != null){
			tabContentList.each{
		        JPanel window = new JPanel();
		        window.setLayout(new BoxLayout(window, BoxLayout.X_AXIS));
		        
		        JLabel label  = new JLabel(it.getKey());
		        window.add(label);
		        
		        def value = it.getValue()

        		if(value instanceof Boolean){
        			JCheckBox chk = new JCheckBox();
    				chk.setSelected(value);
    				window.add(chk);
        		}else{
        			if(value instanceof List<?>){
        				JTextField tf = new JTextField(""+it.getValue());
	        			tf.setColumns(15);
	        			window.add(tf);
        			}else{
					    //Paths.get(value.toString())
					    if(value.toString().contains("/") || value.toString().contains("\\")){
						   	// Root folder for project
					        JTextField tfFolder = new JTextField();
					        tfFolder.setColumns(15);
					
					        // button to choose root folder
					        JButton browseFolderButton = new JButton("Choose folder");
					        browseFolderButton.addActionListener(e->{
					            JFileChooser directoryChooser = new JFileChooser();
					            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					            directoryChooser.setCurrentDirectory(currentDir);
					            directoryChooser.setDialogTitle("Choose the project folder");
					            directoryChooser.showDialog(new JDialog(),"Select");
					
					            if (directoryChooser.getSelectedFile() != null){
					                tfFolder.setText(directoryChooser.getSelectedFile().getAbsolutePath());
					                currentDir = directoryChooser.getSelectedFile()
					            }
					        });
	        				
					        window.add(tfFolder);
					        window.add(browseFolderButton);
					    }else{
	        				JTextField tf = new JTextField(""+it.getValue());
		        			tf.setColumns(15);
		        			window.add(tf);
					    }
        			}
        		}
		        tabPanel.add(window)
			}
		}
		return tabPanel
	}
} 

def readYamlConfig(File yamlFile) {
	Yaml parser = new Yaml()
	def data = parser.load( yamlFile.text )
	return data
}

def saveYamlToFile( def yamlConfig, def yamlPath ) {
    final DumperOptions options = new DumperOptions()
    options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK )
    options.setPrettyFlow( true )
    def yaml = new Yaml( options )
    

    def writer = new FileWriter( yamlPath )
    println "saving to ${yamlPath.toString()}"
    yaml.dump( yamlConfig, writer )
    writer.close()
}
