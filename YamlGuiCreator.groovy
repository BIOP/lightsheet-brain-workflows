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

// reads the yaml file
def config = readYamlConfig( baseYaml )

// generate the dialog box
def dialog = new Dialog(config)

return


public class Dialog extends JFrame {
	
	public Dialog(yamlContent){
		myDialog(yamlContent)
	}
	
	private File currentDir = new File("")
	
	/**
	 * Generates the GUI
	 **/
	public void myDialog(yamlContent) {
		// set general frame
		this.setTitle("Select your custom configuration")
	    this.setVisible(true);
	    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    //this.setPreferredSize(new Dimension(400, 250));
	    
	    // get the screen size
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        
        // set location in the middle of the screen
	    this.setLocation((int)((width - 400)/2), (int)((height - 700)/2));
		
		JTabbedPane generalPanel = new JTabbedPane();
		
		yamlContent.each{
			// create the tab
			JPanel tabPanel = new JPanel();
			def layout = new GroupLayout(tabPanel);
			layout.setAutoCreateGaps(true);
        	layout.setAutoCreateContainerGaps(true);
			
			// initialize the columns/lines
			ParallelGroup initialHGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			List<ParallelGroup> horizontalGroups = new ArrayList<>()
			List<ParallelGroup> verticalGroups = new ArrayList<>()
			horizontalGroups.add(initialHGroup)
			
			// create the content of the current tab
			tabGUICreation(it.getValue(), layout, horizontalGroups, verticalGroups, initialHGroup)
			
			// set the columns
			SequentialGroup horizontalGroup = layout.createSequentialGroup()
			horizontalGroups.each{gr -> horizontalGroup.addGroup(gr)}
			layout.setHorizontalGroup(horizontalGroup)
			
			// set the lines
			SequentialGroup verticalGroup = layout.createSequentialGroup()
			verticalGroups.each{gr -> verticalGroup.addGroup(gr)}
			layout.setVerticalGroup(verticalGroup)
			
			// add the tab to the main GUI
			tabPanel.setLayout(layout);
			generalPanel.addTab(it.getKey(), null, tabPanel, ""+it.getKey());
		}
		// add the last tab to save the new yaml
		generalPanel.addTab("Run", null, runPanelCreation(), "Run")
		
        this.setContentPane(generalPanel);
        this.pack();
    }
    
	/**
	 * Create the last tab of the GUI, to save the new parameters
	 */
    private JPanel runPanelCreation(){
    	JPanel tabPanel = new JPanel();
		def layout = new GroupLayout(tabPanel);
		
		// create the buttons
		JButton bnSave = new JButton("Save");
    	JButton bnCancel = new JButton("Cancel");

		// set the columns
		SequentialGroup horizontalGroup = layout.createSequentialGroup()
											.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
											.addComponent(bnSave)
											.addComponent(bnCancel))
		// set the line
		SequentialGroup verticalGroup = layout.createSequentialGroup()
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(bnSave))
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(bnCancel))
		// add to the tab content
    	layout.setHorizontalGroup(horizontalGroup)
    	layout.setVerticalGroup(verticalGroup)
    	tabPanel.setLayout(layout);
    	
    	return tabPanel
    }
    
    /**
     * Create the tab content (item and field creation, listeners, columns & line assignment)
     */
    private void tabGUICreation(tabContentMap, layout, horizontalGroups, verticalGroups, labelHGroup){
    	
		if(tabContentMap != null){
			// create a new column for the values or getting the next one
			ParallelGroup valueHGroup
			ParallelGroup folderHGroup = null // this group is for a potential second column which contain the buttons to choose files
			def index = horizontalGroups.findIndexOf{it == labelHGroup}
			if(index == horizontalGroups.size() - 1){
				valueHGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				horizontalGroups.add(valueHGroup)
			}else{
				valueHGroup = horizontalGroups.get(index + 1)
			}
			
			tabContentMap.each{
				def value = it.getValue()
				def key = it.getKey()
				
				// if the cuurent value is actually another Map of parameters, recusively call the method
				if(value instanceof Map<?,?>){
					// add the label to the current column
					JLabel label  = new JLabel(key);
					labelHGroup.addComponent(label)
					
					// create a new line with the label only
					ParallelGroup VGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					VGroup.addComponent(label)
					verticalGroups.add(VGroup)
					
					// add the new indented lines for the next group
					tabGUICreation(value, layout, horizontalGroups, verticalGroups, valueHGroup)
				}else{
					def valueItem
					def folderItem
					// returns the GUI field corresponding to file/string/boolean
			        (valueItem, folderItem) = selectRightField(key, value)
			        
			        // special case for folders because they need two fields (text field + button)
			        if(folderItem != null){
			        	// get the next column if exists. Otherwise, create it.
						index = horizontalGroups.findIndexOf{it == valueHGroup}
						if(index == horizontalGroups.size() - 1){
							folderHGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							horizontalGroups.add(folderHGroup)
						}else{
							folderHGroup = horizontalGroups.get(index + 1)
						}
						
						// add the button to the next column
						folderHGroup.addComponent(folderItem)
			        }
			        
			        // add fields to the columns
					JLabel label = new JLabel(key)
					labelHGroup.addComponent(label)
					valueHGroup.addComponent(valueItem)
					
					// create a new line in the GUI
					ParallelGroup VGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					VGroup.addComponent(label).addComponent(valueItem)
					if(folderItem != null) VGroup.addComponent(folderItem)
					verticalGroups.add(VGroup)
				}
			}
		}
	}
	
	
	/**
	 * Choose the right category of field (boolean, String, File) according by guesing it from the value
	 */
	private selectRightField(label, value){
		// create a checkbox
		if(value instanceof Boolean){
			JCheckBox chk = new JCheckBox();
			chk.setSelected(value);
			return [chk, null]
		}
		
		// create a text field and a button to search for a file
		if(label.endsWith("file") || label.endsWith("path") || label.endsWith("dir")){
			JTextField tfFolder = new JTextField(""+value);
			tfFolder.setColumns(5);
			
	        def fileOption
	        def title
	        
	        // select the option to restrict to files/folders only
	        if(label.endsWith("dir")){
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
	            if(currentDir.exists()) directoryChooser.setCurrentDirectory(currentDir);
	            directoryChooser.setDialogTitle(title);
	            def result = directoryChooser.showDialog(new JDialog(),"Select");

	            if (result == JFileChooser.APPROVE_OPTION && directoryChooser.getSelectedFile() != null){
	                tfFolder.setText(directoryChooser.getSelectedFile().getAbsolutePath());
	                currentDir = directoryChooser.getSelectedFile()
	            }
	        });
	        return [tfFolder, browseFolderButton]
		}
		
		// create a simple text field
		return [new JTextField(""+value), null]
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