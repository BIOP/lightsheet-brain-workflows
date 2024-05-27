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
import javax.swing.GroupLayout.ParallelGroup
import javax.swing.GroupLayout.SequentialGroup
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.FlowLayout;
import javax.swing.BoxLayout
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FlowLayout
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JOptionPane; 
import javax.swing.JScrollPane
import java.nio.file.Paths
import java.nio.file.InvalidPathException
import ij.IJ

#@ File baseYaml

// reads the yaml file
def config = readYamlConfig( baseYaml )

// generate the dialog box
def dialog = new Dialog(config)

return


public class Dialog extends JFrame {
	
	public Dialog(yamlContent){
		myDialog(yamlContent)
	}
	
	String DEFAULT_PATH_KEY = "scriptDefaultDir"
	int COLUMN = 0
	int ROW = 1
	int WIDTH = 2
	int rowCounter = 0
	private File currentDir = IJ.getProperty(DEFAULT_PATH_KEY) == null ? new File("") : ((File)IJ.getProperty(DEFAULT_PATH_KEY))
	def yamlConfig
	
	/**
	 * Generates the GUI
	 **/
	public void myDialog(yamlContent) {
		yamlConfig = yamlContent
		
		// set general frame
		this.setTitle("Select your custom configuration")
	    this.setVisible(true);
	    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    this.setPreferredSize(new Dimension(630, 700));
	    
	    // get the screen size
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        
        // set location in the middle of the screen
	    this.setLocation((int)((width - 630)/2), (int)((height - 700)/2));
		JTabbedPane generalPanel = new JTabbedPane();
        
		yamlContent.each{
			// create the tab
			JPanel tabPanel = new JPanel();
			tabPanel.setLayout(new GridBagLayout());

			// initialize the columns/lines
			List<Integer> columnList = new ArrayList()
			columnList.add(0)
			boolean separtorToAdd = true
			
			// create the content of the current tab
			Map<JComponent, Integer[]> components = tabGUICreation(it.getValue(), layout, columnList, columnList.get(0), separtorToAdd)
			
			// define the GUI tab
			GridBagConstraints constraints = new GridBagConstraints();
		    constraints.fill = GridBagConstraints.BOTH;
		    constraints.insets = new Insets(5,5,5,5);

			components.each{componentItem ->
				JComponent component = componentItem.getKey()
				Integer[] constraintsArray = componentItem.getValue()
				constraints.gridwidth = constraintsArray[WIDTH]; 
		        constraints.gridx = constraintsArray[COLUMN];
		        constraints.gridy = constraintsArray[ROW];
		        tabPanel.add(component, constraints);  	
			}
			 constraints.gridwidth = 1;
			
			// format correctly the panel
			JPanel nicePanel = new JPanel();
			nicePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			nicePanel.add(tabPanel)
			
			// add a scroll bar if necessary
			JScrollPane sp = new JScrollPane(nicePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			// add the tab to the main GUI
			generalPanel.addTab(it.getKey().split("_").collect{ it.capitalize() }.join(" "), null, sp, ""+it.getKey());
		}
		
		JPanel nicePanel = new JPanel();
		nicePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		nicePanel.add(runPanelCreation())
			
		// add a scroll bar if necessary
		JScrollPane sp = new JScrollPane(nicePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		// add the last tab to save the new yaml
		generalPanel.addTab("Run", null, sp, "Run")
		
        this.setContentPane(generalPanel);
        this.pack();
    }
    
	/**
	 * Create the last tab of the GUI, to save the new parameters
	 */
    private JPanel runPanelCreation(){
    	JPanel tabPanel = new JPanel();
		def layout = new GroupLayout(tabPanel);
		
		JTextArea textArea = new JTextArea();
		textArea.setPreferredSize(new Dimension(300, 20));

		JButton bnAddFolders = new JButton("Add folders");
		bnAddFolders.addActionListener(e->{
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooser.setCurrentDirectory(currentDir);
            directoryChooser.setMultiSelectionEnabled(true)
            directoryChooser.setDialogTitle("Choose the CZI folders");
            directoryChooser.showDialog(new JDialog(),"Select");

            if (directoryChooser.getSelectedFiles() != null){
            	def previousText = textArea.getText()
                textArea.setText(previousText + directoryChooser.getSelectedFiles().join("\n") + "\n");
                currentDir = directoryChooser.getSelectedFile()
                IJ.setProperty(DEFAULT_PATH_KEY, currentDir)
            }
        });
        
		JButton bnClearFolders = new JButton("Clear folders");
		bnClearFolders.addActionListener(e->{
    		textArea.setText("");
		})
		
		JLabel finalMessage = new JLabel("")
		JButton bnSave = new JButton("Save");
		// add listener on Ok and Cancel button
		bnSave.addActionListener(e->{
			def text = textArea.getText()
			def message = ""
			if(text.isEmpty() ){
				message += "You have to select at least one CZI folder and the analysis folder !"
			}else{
				def cziFolders = text.split("\n")
				message = saveYamlParameters(yamlConfig, cziFolders)
			}
			finalMessage.setText("<html>"+message+"</html>")
		})
		
		
		GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5,5,5,5);

        int settingsRow = 0;
        tabPanel.setLayout(new GridBagLayout());
		         
		constraints.gridwidth = 5; 
		constraints.gridheight = 5;
        constraints.gridx = 0;
        constraints.gridy = settingsRow;
        tabPanel.add(textArea, constraints);
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        
        constraints.gridx = 5;
        constraints.gridy = settingsRow++;
        tabPanel.add(bnAddFolders, constraints);
        
        constraints.gridx = 5;
        constraints.gridy = settingsRow++;
        tabPanel.add(bnClearFolders, constraints);
        
        constraints.gridx = 5;
        constraints.gridy = settingsRow++;
        tabPanel.add(bnSave, constraints);
        
        settingsRow++
        constraints.gridx = 0;
        constraints.gridy = ++settingsRow;
        tabPanel.add(finalMessage, constraints);
        
    	return tabPanel
    }
    
    /**
     * Create the tab content (item and field creation, listeners, columns & line assignment)
     */
    private Map<JComponent, Integer[]> tabGUICreation(tabContentMap, layout, columnList, labelCIndex, separtorToAdd){
    	
    	Map<JComponent, Integer[]> components = new LinkedHashMap<>()
		
		if(tabContentMap != null){
			// create a new column for the values or getting the next one
			int valueCIndex
			int folderCIndex = -1 // this group is for a potential second column which contain the buttons to choose files
			def index = columnList.findIndexOf{it == labelCIndex}
			if(index == columnList.size() - 1){
				valueCIndex = labelCIndex + 1
				columnList.add(valueCIndex)
			}else{
				valueCIndex = columnList.get(index + 1)
			}
			
			tabContentMap.each{
				def value = it.getValue()
				def key = it.getKey().split("_").collect{ it.capitalize() }.join(" ")
				
				// if the curent value is actually another Map of parameters, recusively call the method
				if(value instanceof Map<?,?>){
					
					// add a separator to each main category
					if(separtorToAdd){
						JSeparator separator = new JSeparator()
						Integer[] constraintsSeparator = new Integer[4]
						constraintsSeparator[COLUMN] = labelCIndex
						constraintsSeparator[ROW] = rowCounter++
						constraintsSeparator[WIDTH] = 10
						components.put(separator, constraintsSeparator)
					}
					
					// add the label to the current column
					JLabel label  = new JLabel(key);
					Integer[] constraints = new Integer[4]
					constraints[COLUMN] = labelCIndex
					constraints[ROW] = rowCounter
					constraints[WIDTH] = 1
					components.put(label, constraints)
							
					// add the new indented lines for the next group
					components.putAll(tabGUICreation(value, layout, columnList, valueCIndex, false))
				}else{
					def valueItem
					def folderItem
					// returns the GUI field corresponding to file/string/boolean
					(valueItem, folderItem) = selectRightField(it, key, value)
			        
			        // special case for folders because they need two fields (text field + button)
			        if(folderItem != null){
			        	// get the next column if exists. Otherwise, create it.
						index = columnList.findIndexOf{it == valueCIndex}
						if(index == columnList.size() - 1){
							folderCIndex = valueCIndex + 1
							columnList.add(folderCIndex)
						}else{
							folderCIndex = columnList.get(index + 1)
						}
			        }
			        
			        // add fields to the columns
					JLabel label = new JLabel(key)
					label.setHorizontalAlignment(SwingConstants.RIGHT);
					Integer[] constraints = new Integer[4]
					constraints[COLUMN] = labelCIndex
					constraints[ROW] = rowCounter
					constraints[WIDTH] = 1
					components.put(label, constraints)
					
					Integer[] constraintsValue = new Integer[4]
					constraintsValue[COLUMN] = valueCIndex
					constraintsValue[ROW] = rowCounter
					constraintsValue[WIDTH] = 1
					components.put(valueItem, constraintsValue)
					
					if(folderItem != null){
						Integer[] constraintsFolder = new Integer[4]
						constraintsFolder[COLUMN] = folderCIndex
						constraintsFolder[ROW] = rowCounter++
						constraintsFolder[WIDTH] = 1
						components.put(folderItem, constraintsFolder)
					}else{
						rowCounter++
					}
				}
			}
		}
		
		return components
	}
	
	
	/**
	 * Choose the right category of field (boolean, String, File) according by guesing it from the value
	 */
	private selectRightField(entry, label, value){
		if(value == null)
			value = ""
		
		// create a checkbox
		if(value instanceof Boolean){
			JCheckBox chk = new JCheckBox();
			chk.setSelected(value);
			chk.addActionListener(e -> {
				entry.setValue(chk.isSelected())
	        });
			return [chk, null]
		}
		
		// create a text field and a button to search for a file
		if(label.toLowerCase().endsWith("file") || label.toLowerCase().endsWith("path") || label.toLowerCase().endsWith("dir")){
			JTextField tfFolder = new JTextField(""+value);
			tfFolder.setColumns(5);
			
	        def fileOption
	        def title
	        
	        // select the option to restrict to files/folders only
	        if(label.toLowerCase().endsWith("dir")){
	        	fileOption = JFileChooser.DIRECTORIES_ONLY
	        	title = "Choose folder"
	        }else{
	        	fileOption = JFileChooser.FILES_ONLY
	        	title = "Choose file"
	        }
	        
	        // create the button
	        JButton browseFolderButton = new JButton(title);
	        
	        // add a listener on a button that opens a search GUI
	        browseFolderButton.addActionListener(e->{
	            JFileChooser directoryChooser = new JFileChooser();
	            directoryChooser.setFileSelectionMode(fileOption);
	            directoryChooser.setCurrentDirectory(currentDir);
	            directoryChooser.setDialogTitle(title);
	            def result = directoryChooser.showDialog(new JDialog(),"Select");

	            if (result == JFileChooser.APPROVE_OPTION && directoryChooser.getSelectedFile() != null){
	                tfFolder.setText(directoryChooser.getSelectedFile().getAbsolutePath());
	                currentDir = directoryChooser.getSelectedFile()
	                IJ.setProperty(DEFAULT_PATH_KEY, currentDir)
	                entry.setValue(currentDir.getAbsolutePath())
	            }
	        });
	        return [tfFolder, browseFolderButton]
		}
		
		// create a simple text field
		def tfString = new JTextField(""+value)
		tfString.getDocument().addDocumentListener(new DocumentListener() {
		  public void changedUpdate(DocumentEvent e) {
		  	setValue()
		  }
		  public void removeUpdate(DocumentEvent e) {
		    setValue()
		  }
		  public void insertUpdate(DocumentEvent e) {
		    setValue()		  
		  }
		  
		  public void setValue(){
		  	if(value instanceof Integer && tfString.getText() != null && !tfString.getText().isEmpty())
		    	entry.setValue(Integer.parseInt(tfString.getText()))
		    else if (value instanceof Double && tfString.getText() != null && !tfString.getText().isEmpty())
		    	entry.setValue(Double.parseDouble(tfString.getText()))
		    else
		    	entry.setValue(tfString.getText())
		  }
		});
		
		return [tfString, null]
	}
	
	/**
	 * Prepare the yaml files for the selected czi files
	 *
	 */
	def saveYamlParameters(yamlConfig, cziFolders){
		def filesToAnalyse = []
		def message = ""
		
		def user = yamlConfig.general.user
		def analysisFolder = yamlConfig.general.save_dir
		
		if(!(new File(analysisFolder)).exists()){
			return "The analysis folder doesn't exists ! Please enter a valid one.<br>"
		}
		
		// Create folder in analysis folder
		def userAnalysisFolder = new File(analysisFolder, user)

		// Simply prepare all the data
		cziFolders.each{ path ->
			def folder = new File(path)
			if(folder.exists()){
				// Copy the base YAML file to each subfolder and give it the name imageName_params.yml
				folder.eachFileRecurse(FILES) {
			    	if(it.name.endsWith('.czi')) {
			    		filesToAnalyse.add(it)
			    	}
			    }
			}else{
				message += "The folder '"+folder.getName()+"' doesn't exists ! Cannot save the YAML file.<br>"
			}
		}
		
		filesToAnalyse.each{cziFile ->
			
			// Get the required information. Folder with the name of the dataset
			def imageName = FilenameUtils.getBaseName(cziFile.getAbsolutePath())
			def outputDirectory = new File(userAnalysisFolder, imageName)
			outputDirectory.mkdirs()
			
			def localYamlConfigFile = new File(outputDirectory, "${imageName}_configuration.yml")
			
			// Prepare the configuration 
			yamlConfig.general.input_file = cziFile.toString()
			yamlConfig.general.output_dir = outputDirectory.toString()
		
			// Define the xml already here
			def bigStitcherXMLFile = new File(outputDirectory, "${imageName}.xml")
			yamlConfig.bigstitcher.xml_file = bigStitcherXMLFile.toString()
		
			saveYamlToFile(yamlConfig, localYamlConfigFile)
			message += "YAML file saved for '"+cziFile.getName()+"'<br>"
		}
		return message
	}
	
	/**
	 * saves a new yaml file
	 */
	def saveYamlToFile(def yamlConfig, def yamlPath) {
	    final DumperOptions options = new DumperOptions()
	    options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK )
	    options.setPrettyFlow( true )
	    def yaml = new Yaml( options )
	    
	
	    def writer = new FileWriter( yamlPath )
	    println "saving to ${yamlPath.toString()}"
	    yaml.dump( yamlConfig, writer )
	    writer.close()
	}

} 

/**
 * Reads the yaml file
 */
def readYamlConfig(File yamlFile) {
	Yaml parser = new Yaml()
	def data = parser.load( yamlFile.text )
	return data
}

