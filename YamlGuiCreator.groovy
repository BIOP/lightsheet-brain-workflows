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

def config = readYamlConfig( baseYaml )

// generate the dialog box
def dialog = new Dialog(config)

return


public class Dialog extends JFrame {

	
	public Dialog(yamlContent){
		
		myDialog(yamlContent)
	}
	
	// getters
	public boolean getEnterPressed(){return this.enterPressed}
	public boolean getValidated(){return this.validated}
	public String getUserId(){return this.userId}
	private File currentDir = new File("")

	public List<Map<String, String>> getSelectedList(){return this.selectionList}
	
	// generate the dialog box
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
	    this.setLocation((int)((width - 400)/2), (int)((height - 250)/2));
		
		JTabbedPane generalPanel = new JTabbedPane();
		
		yamlContent.each{
			JPanel tabPanel = new JPanel();
			def layout = new GroupLayout(tabPanel);
			layout.setAutoCreateGaps(true);
        	layout.setAutoCreateContainerGaps(true);
			
			ParallelGroup initialHGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			List<ParallelGroup> horizontalGroups = new ArrayList<>()
			List<ParallelGroup> verticalGroups = new ArrayList<>()
			horizontalGroups.add(initialHGroup)
			
			tabGUICreation(it.getValue(), layout, horizontalGroups, verticalGroups, initialHGroup)
			
			SequentialGroup horizontalGroup = layout.createSequentialGroup()
			horizontalGroups.each{gr -> horizontalGroup.addGroup(gr)}
			layout.setHorizontalGroup(horizontalGroup)
			
			SequentialGroup verticalGroup = layout.createSequentialGroup()
			verticalGroups.each{gr -> verticalGroup.addGroup(gr)}
			layout.setVerticalGroup(verticalGroup)
			
			tabPanel.setLayout(layout);
			generalPanel.addTab(it.getKey(), null, tabPanel, "Does nothing");
		}
		
        this.setContentPane(generalPanel);
        this.pack();
    }
       
    private void tabGUICreation(tabContentMap, layout, horizontalGroups, verticalGroups, labelHGroup){
    	
		if(tabContentMap != null){
			// create a new column for the values or getting the next one
			ParallelGroup valueHGroup
			ParallelGroup folderHGroup = null
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
			        (valueItem, folderItem) = selectRightField(key, value)
			        
			        if(folderItem != null){
						index = horizontalGroups.findIndexOf{it == valueHGroup}
						if(index == horizontalGroups.size() - 1){
							folderHGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							horizontalGroups.add(folderHGroup)
						}else{
							folderHGroup = horizontalGroups.get(index + 1)
						}
						folderHGroup.addComponent(folderItem)
			        }
			        
					JLabel label = new JLabel(key)
					labelHGroup.addComponent(label)
					valueHGroup.addComponent(valueItem)
					
					ParallelGroup VGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					VGroup.addComponent(label).addComponent(valueItem)
					if(folderItem != null) VGroup.addComponent(folderItem)
					verticalGroups.add(VGroup)
				}
			}
		}
	}
	
	private selectRightField(label, value){
		
		if(value instanceof Boolean){
			JCheckBox chk = new JCheckBox();
			chk.setSelected(value);
			return [chk, null]
		}
		
		if(label.endsWith("file") || label.endsWith("path") || label.endsWith("dir")){
			JTextField tfFolder = new JTextField(""+value);
			tfFolder.setColumns(5);
			
	        def fileOption
	        def title
	        if(label.endsWith("dir")){
	        	fileOption = JFileChooser.DIRECTORIES_ONLY
	        	title = "Choose folder"
	        }else{
	        	fileOption = JFileChooser.FILES_ONLY
	        	title = "Choose file"
	        }
	        JButton browseFolderButton = new JButton(title);
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