package ch.epfl.biop.lbw;

import ij.IJ;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlGUI extends JFrame {

    String DEFAULT_PATH_KEY = "scriptDefaultDir";
    int COLUMN = 0;
    int ROW = 1;
    int WIDTH = 2;
    int rowCounter = 0;
    private File currentDir = IJ.getProperty(DEFAULT_PATH_KEY) == null ? new File("") : ((File)IJ.getProperty(DEFAULT_PATH_KEY));
    Config yamlConfig;

    /**
     * Generates the GUI
     **/
    public void myDialog(Config yamlContent) {
        yamlConfig = yamlContent;

        // set general frame
        this.setTitle("Select your custom configuration");
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

        List<Field> fields = Arrays.asList(Config.class.getDeclaredFields());

        fields.forEach( f -> {
            String k = f.getName();

            // create the tab
            JPanel tabPanel = new JPanel();
            tabPanel.setLayout(new GridBagLayout());

            // initialize columns/lines
            List<Integer> columnList = new ArrayList();
            columnList.add(0);
            boolean separatorToAdd = true;

            // create the content of the current tab
            Map<JComponent, Integer[]> components;
            try {
                components = tabGUICreation(f.get(yamlConfig), f, columnList, columnList.get(0), separatorToAdd);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // define the GUI tab
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.insets = new Insets(5,5,5,5);

            components.forEach((component, constraintsArray) -> {
                constraints.gridwidth = constraintsArray[WIDTH];
                constraints.gridx = constraintsArray[COLUMN];
                constraints.gridy = constraintsArray[ROW];
                tabPanel.add(component, constraints);
            });

            constraints.gridwidth = 1;

            // format correctly the panel
            JPanel nicePanel = new JPanel();
            nicePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            nicePanel.add(tabPanel);

            // add a scroll bar if necessary
            JScrollPane sp = new JScrollPane(nicePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            // add the tab to the main GUI
            generalPanel.addTab(String.join(" ", k.toUpperCase().split("_")), null, sp, k);
        });

        JPanel nicePanel = new JPanel();
        nicePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        nicePanel.add(runPanelCreation());

        // add a scroll bar if necessary
        JScrollPane sp = new JScrollPane(nicePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // add the last tab to save the new yaml
        generalPanel.addTab("Run", null, sp, "Run");

        this.setContentPane(generalPanel);
        this.pack();
    }

    /**
     * Create the last tab of the GUI, to save the new parameters
     */
    private JPanel runPanelCreation(){
        JPanel tabPanel = new JPanel();

        JTextArea textArea = new JTextArea();
        JScrollPane scrollTextArea = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTextArea.setBounds(3, 3, 350, 250);
        scrollTextArea.setPreferredSize(new Dimension(400, 300));

        JButton bnAddFolders = new JButton("Add folders");
        bnAddFolders.addActionListener(e->{
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooser.setCurrentDirectory(currentDir);
            directoryChooser.setMultiSelectionEnabled(true);
            directoryChooser.setDialogTitle("Choose the CZI folders");
            directoryChooser.showDialog(new JDialog(),"Select");

            File[] selectedFiles = directoryChooser.getSelectedFiles();
            if (selectedFiles != null){
                String previousText = textArea.getText();
                List<String> textList = Arrays.stream(selectedFiles).map(File::getAbsolutePath).collect(Collectors.toList());
                if(!previousText.isEmpty()) {
                    textList.add(0, previousText);
                }
                textArea.setText(String.join("\n", textList));
                currentDir = directoryChooser.getSelectedFile();
                IJ.setProperty(DEFAULT_PATH_KEY, currentDir);
            }
        });

        JButton bnClearFolders = new JButton("Clear folders");
        bnClearFolders.addActionListener(e->{
            textArea.setText("");
        });

        JLabel finalMessage = new JLabel("<html>--- Logger ---</html>");
        JButton bnSave = new JButton("Save");
        // add listener on Ok and Cancel button
        bnSave.addActionListener(e->{
            String text = textArea.getText();
            List<String> messages = new ArrayList<>();
            messages.add(finalMessage.getText().replace("<html>","").replace("</html>",""));

            if(text.isEmpty() ){
                messages.add("<p style='color:orange;'>You have to select at least one CZI folder and the analysis folder !</p>");
            } else {
                String[] cziFolders = text.split("\n");
                messages.addAll(saveYamlParameters(yamlConfig, Arrays.asList(cziFolders)));
            }
            finalMessage.setText("<html>"+String.join(" ",messages)+"</html>");
        });

        enableDragAndDrop(textArea, finalMessage);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5,5,5,5);

        int settingsRow = 0;
        tabPanel.setLayout(new GridBagLayout());

        constraints.gridwidth = 5;
        constraints.gridheight = 5;
        constraints.gridx = 0;
        constraints.gridy = settingsRow;
        tabPanel.add(scrollTextArea, constraints);
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

        settingsRow++;
        constraints.gridx = 0;
        constraints.gridy = ++settingsRow;
        tabPanel.add(finalMessage, constraints);

        return tabPanel;
    }

    private void enableDragAndDrop(JTextArea textArea, JLabel finalMessage) {
        DropTarget target = new DropTarget(textArea, new DropTargetListener(){
            public void dragEnter(DropTargetDragEvent e) {}
            public void dragExit(DropTargetEvent e) {}
            public void dragOver(DropTargetDragEvent e) {}
            public void dropActionChanged(DropTargetDragEvent e) {}
            public void drop(DropTargetDropEvent e) {
                try {

                    // Accept the drop first, important!
                    e.acceptDrop(DnDConstants.ACTION_COPY);

                    // Get the files that are dropped as java.util.List
                    List<File> list = ((List<File>)e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));

                    ArrayList<File> files = new ArrayList<>();
                    ArrayList<String> errorMessages = new ArrayList<>();

                    list.forEach(file -> {
                            if(file.isDirectory()) {
                                files.add(file);
                            } else {
                                errorMessages.add("<p style='color:orange;'>'" + file.getName() + "' is a file, not a folder ! Cannot be added to the list</p>");
                            }
                        }
                    );

                    if(!errorMessages.isEmpty()){
                        String previousText = finalMessage.getText().replace("<html>","").replace("</html>","");
                        errorMessages.add(0, previousText);
                        finalMessage.setText("<html>"+String.join(" ", errorMessages)+"</html>");
                    }

                    // Now get the first file from the list,
                    String previousText = textArea.getText();

                    if(!previousText.isEmpty()) {
                        files.add(0, new File(previousText));
                    }

                    textArea.setText(files.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")));

                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        });
    }

    /**
     * Create the tab content (item and field creation, listeners, columns & line assignment)
     */
    private Map<JComponent, Integer[]> tabGUICreation(final Object o, Field f, List<Integer> columnList, int labelCIndex, boolean separtorToAdd) throws IllegalAccessException {

        Map<JComponent, Integer[]> components = new LinkedHashMap<>();

        // create a new column for the values or getting the next one
        int valueCIndex;
        int folderCIndex = -1; // this group is for a potential second column which contain the buttons to choose files
        int index = columnList.indexOf(labelCIndex); //columnList.findIndexOf{it == labelCIndex}
        if ( index == columnList.size() - 1 ){
            valueCIndex = labelCIndex + 1;
            columnList.add(valueCIndex);
        } else {
            valueCIndex = columnList.get(index + 1);
        }

        List<Field> fields = Arrays.asList(f.getType().getDeclaredFields());

        for (Field field : fields) {
            Object value;
            try {
                value = field.get(o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            String key = String.join(" ", field.getName().toUpperCase().split("_"));

            if (!Config.simpleClasses().contains(field.getType())) {

                // add a separator to each main category
                if(separtorToAdd){
                    JSeparator separator = new JSeparator();
                    Integer[] constraintsSeparator = new Integer[4];
                    constraintsSeparator[COLUMN] = labelCIndex;
                    constraintsSeparator[ROW] = rowCounter++;
                    constraintsSeparator[WIDTH] = 10;
                    components.put(separator, constraintsSeparator);
                }

                // add the label to the current column
                JLabel label  = new JLabel(key);
                Integer[] constraints = new Integer[4];
                constraints[COLUMN] = labelCIndex;
                constraints[ROW] = rowCounter;
                constraints[WIDTH] = 1;
                components.put(label, constraints);

                // add the new indented lines for the next group
                components.putAll(tabGUICreation(value, field, columnList, valueCIndex, false));
            } else {

                // returns the GUI field corresponding to file/string/boolean
                if (Config.parametersToSkip().contains(key.toLowerCase())) continue; // Skips these two fields

                AbstractMap.SimpleEntry<JComponent, Object> tempEntry = selectRightField(o, field, key);

                JComponent valueItem = tempEntry.getKey();
                Object folderItem = tempEntry.getValue();

                // special case for folders because they need two fields (text field + button)
                if (folderItem != null) {
                    // get the next column if exists. Otherwise, create it.
                    index = columnList.indexOf(valueCIndex);
                    if(index == columnList.size() - 1){
                        folderCIndex = valueCIndex + 1;
                        columnList.add(folderCIndex);
                    } else {
                        folderCIndex = columnList.get(index + 1);
                    }
                }

                // add fields to the columns
                JLabel label = new JLabel(key);
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                Integer[] constraints = new Integer[4];
                constraints[COLUMN] = labelCIndex;
                constraints[ROW] = rowCounter;
                constraints[WIDTH] = 1;
                components.put(label, constraints);

                Integer[] constraintsValue = new Integer[4];
                constraintsValue[COLUMN] = valueCIndex;
                constraintsValue[ROW] = rowCounter;
                constraintsValue[WIDTH] = 1;
                components.put(valueItem, constraintsValue);

                if(folderItem != null){
                    Integer[] constraintsFolder = new Integer[4];
                    constraintsFolder[COLUMN] = folderCIndex;
                    constraintsFolder[ROW] = rowCounter++;
                    constraintsFolder[WIDTH] = 1;
                    components.put((JComponent) folderItem, constraintsFolder);
                } else {
                    rowCounter++;
                }
            }
        }
        return components;
    }


    /**
     * Choose the right category of field (boolean, String, File) according by guesing it from the value
     */
    private AbstractMap.SimpleEntry<JComponent, Object> selectRightField(Object o, Field field, String label) throws IllegalAccessException {
        Class<?> clazz = field.getType();

        // create a checkbox
        if (Boolean.class.equals(clazz)) {
            JCheckBox chk = new JCheckBox();
            Boolean currentValue = (Boolean) field.get(o);
            chk.setSelected(currentValue != null && currentValue);
            chk.addActionListener(e -> {
                try {
                    field.set(o, chk.isSelected());
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            });
            return new AbstractMap.SimpleEntry<>(chk, null);
        }

        // Creates a text field and a button to search for a file
        if(label.toLowerCase().endsWith("file") || label.toLowerCase().endsWith("path") || label.toLowerCase().endsWith("dir")){
            String value = (String) field.get(o);
            JTextField tfFolder = new JTextField(value);
            tfFolder.setColumns(5);

            int fileOption;
            String title;

            // select the option to restrict to files/folders only
            if(label.toLowerCase().endsWith("dir")){
                fileOption = JFileChooser.DIRECTORIES_ONLY;
                title = "Choose folder";
            }else{
                fileOption = JFileChooser.FILES_ONLY;
                title = "Choose file";
            }

            // create the button
            JButton browseFolderButton = new JButton(title);

            // add a listener on a button that opens a search GUI
            browseFolderButton.addActionListener( e -> {
                JFileChooser directoryChooser = new JFileChooser();
                directoryChooser.setFileSelectionMode(fileOption);
                directoryChooser.setCurrentDirectory(currentDir);
                directoryChooser.setDialogTitle(title);
                int result = directoryChooser.showDialog(new JDialog(), "Select");

                if (result == JFileChooser.APPROVE_OPTION && directoryChooser.getSelectedFile() != null){
                    tfFolder.setText(directoryChooser.getSelectedFile().getAbsolutePath());
                    currentDir = directoryChooser.getSelectedFile();
                    IJ.setProperty(DEFAULT_PATH_KEY, currentDir);
                    try {
                        field.set(o, currentDir.getAbsolutePath());
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            return new AbstractMap.SimpleEntry<>(tfFolder, browseFolderButton);
        }

        // create a simple text field

        Object value = (field.get(o)==null)?"":field.get(o);

        JTextField tfString = new JTextField(value.toString());
        tfString.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                setValue();
            }
            public void removeUpdate(DocumentEvent e) {
                setValue();
            }
            public void insertUpdate(DocumentEvent e) {
                setValue();
            }

            public void setValue() {
                if (value instanceof Integer && tfString.getText() != null && !tfString.getText().isEmpty()) {
                    try {
                        field.set(o, Integer.parseInt(tfString.getText()));
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                    //entry.setValue(Integer.parseInt(tfString.getText()));
                } else if (value instanceof Double && tfString.getText() != null && !tfString.getText().isEmpty()) {
                    try {
                        field.set(o, Double.parseDouble(tfString.getText()));
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    try {
                        field.set(o, tfString.getText());
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        return new AbstractMap.SimpleEntry<>(tfString, null);
    }

    /**
     * Prepare the yaml files for the selected czi files
     *
     */
    List<String> saveYamlParameters(Config yamlConfig, List<String> cziFolders) {
        List<File> filesToAnalyse = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        String user = yamlConfig.general.user;
        String analysisFolder = yamlConfig.general.save_dir;

        if(!(new File(analysisFolder)).exists()){
            messages.add("<p style='color:red;'>The analysis folder doesn't exists ! Please enter a valid one.</p>");
            return messages;
        }

        // Simply prepare all the data
        cziFolders.forEach( path -> {
            File folder = new File(path);
            if(folder.exists()){
                filesToAnalyse.addAll(FileUtils.listFiles(folder, new String[]{"czi"}, true));
            } else {
                messages.add("<p style='color:red;'>The folder '"+folder.getName()+"' doesn't exist ! Cannot save the YAML file.</p>");
            }
        });

        filesToAnalyse.forEach(cziFile -> {

            // Get the required information. Folder with the name of the dataset
            String imageName = FilenameUtils.getBaseName(cziFile.getAbsolutePath());
            File outputDirectory = new File(analysisFolder, imageName);
            outputDirectory.mkdirs();

            File localYamlConfigFile = new File(outputDirectory, imageName+"_configuration.yml");

            // Prepare the configuration
            yamlConfig.general.input_file = cziFile.toString();
            yamlConfig.general.output_dir = outputDirectory.toString();

            // Define the xml already here
            File bigStitcherXMLFile = new File(outputDirectory, imageName+".xml");
            yamlConfig.bigstitcher.xml_file = bigStitcherXMLFile.toString();
            try {
                saveYamlToFile(yamlConfig, localYamlConfigFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            messages.add("<p style='color:green;'>YAML file saved for '"+cziFile.getName()+"'</p>");
        });

        return messages;
    }

    /**
     * Saves a YAML configuration to a file.
     *
     * @param yamlConfig the object representing the YAML configuration
     * @param yamlPath   the path to the YAML file to save
     * @throws IOException if an I/O error occurs
     */
    public static void saveYamlToFile(Config yamlConfig, String yamlPath) throws IOException {
        // Configure the DumperOptions
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        // Create the Yaml instance
        Yaml yaml = new Yaml(options);

        // Write the YAML to a file
        try (FileWriter writer = new FileWriter(yamlPath)) {
            System.out.println("Saving to " + yamlPath);
            yaml.dump(yamlConfig, writer);
        }
    }

}
