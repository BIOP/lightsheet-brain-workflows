import javax.swing.*
import java.awt.*
import java.text.NumberFormat

def centerFrameOnScreen(frame) {
    def screenSize = frame.toolkit.screenSize
    frame.location = new Point(
        (screenSize.width - frame.width) / 2,
        (screenSize.height - frame.height) / 2
    )
}

def onButtonClick(event) {
    def selectedVariable = dropdown.selectedItem
    def outputFolderPath = outputFolderField.text
    def inputFilePath = inputFileField.text
    def stitchingChecked = stitchingCheckbox.isSelected()
    def registrationChecked = registrationCheckbox.isSelected()
    def integerValue = integerField.value as int
    def floatValue = floatField.value as float

    def message = """
        Selected variable: $selectedVariable
        Output Folder: $outputFolderPath
        Input File: $inputFilePath
        Stitching Checked: $stitchingChecked
        Registration Checked: $registrationChecked
        Integer Value: $integerValue
        Float Value: $floatValue
    """.stripIndent()

    JOptionPane.showMessageDialog(frame, message)
}

def browseFoldersOrFiles(event) {
    def chooser = new JFileChooser()
    chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
    def result = chooser.showOpenDialog(frame)
    if (result == JFileChooser.APPROVE_OPTION) {
        outputFolderField.text = chooser.selectedFile.absolutePath
    }
}

def browseFiles(event) {
    def chooser = new JFileChooser()
    chooser.fileSelectionMode = JFileChooser.FILES_ONLY
    def result = chooser.showOpenDialog(frame)
    if (result == JFileChooser.APPROVE_OPTION) {
        outputFolderField.text = chooser.selectedFile.absolutePath
    }
}

def browseFolders(event) {
    def chooser = new JFileChooser()
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    def result = chooser.showOpenDialog(frame)
    if (result == JFileChooser.APPROVE_OPTION) {
        outputFolderField.text = chooser.selectedFile.absolutePath
    }
}

def onAddUserClick(event) {
    def newUser = JOptionPane.showInputDialog(frame, "Enter new user:")
    if (newUser && userlist.contains(newUser)) {
        userList << newUser
        dropdown.addItem(newUser)
    }
}

def onEditParametersClick(event) {
    JOptionPane.showMessageDialog(frame, "You wish you could!")
}

def onStartClick(event) {
    JOptionPane.showMessageDialog(frame, "Macro started!")
}

// Create the main frame
def frame = new JFrame("Image pre-processing parameters selection")
frame.layout = new GridBagLayout()
frame.size = new Dimension(600, 300)
frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

// Create GridBagConstraints for layout control
constraints = new GridBagConstraints()

// -----------------------------------------------------------------------

// Create a label for the dropdown list
selectUserLabel = new JLabel("Select User:")
constraints.gridx = 0
constraints.gridy = 0
constraints.insets.bottom = 5  // Increase bottom inset to add space
frame.add(selectUserLabel, constraints)

// Create a dropdown list
userList = ["Axel", "Lana", "Carl", "Lorenzo"]
dropdown = new JComboBox(userList.toArray())
constraints.gridx = 1
frame.add(dropdown, constraints)

// Create a button to add a new user
def addUserButton = new JButton("Add User", actionPerformed: onAddUserClick)
constraints.gridx = 2
frame.add(addUserButton, constraints)

// -----------------------------------------------------------------------

// Create input file or folder components
def inputFileLabel = new JLabel("Select input file or folder:")
constraints.gridx = 0
constraints.gridy = 1
frame.add(inputFileLabel, constraints)

def inputFileField = new JTextField(20)
constraints.gridx = 1
constraints.weightx = 1.0  // Increase weight for the second column
frame.add(inputFileField, constraints)

def inputFileButton = new JButton("Browse", actionPerformed: browseFoldersOrFiles)
constraints.gridx = 2
frame.add(inputFileButton, constraints)

// -----------------------------------------------------------------------

// Create output folder components
def outputFolderLabel = new JLabel("Select output Folder:")
constraints.gridx = 0
constraints.gridy = 2
constraints.weightx = 0.5  // Increase weight for the first column
frame.add(outputFolderLabel, constraints)

def outputFolderField = new JTextField(20)
constraints.gridx = 1
constraints.weightx = 1.0  // Increase weight for the second column
frame.add(outputFolderField, constraints)

def outputFolderButton = new JButton("Browse", actionPerformed: browseFolders)
constraints.gridx = 2
constraints.weightx = 0.1  // Increase weight for the third column
frame.add(outputFolderButton, constraints)

// -----------------------------------------------------------------------

// Create checkboxes for Stitching and Registration
def processingLabel = new JLabel("Select processing:")
constraints.gridx = 0
constraints.gridy = 3
constraints.weightx = 0.5  // Increase weight for the first column
frame.add(processingLabel, constraints)

def stitchingCheckbox = new JCheckBox("Stitching")
constraints.gridx = 1
constraints.weightx = 1.0  // Increase weight for the second column
frame.add(stitchingCheckbox, constraints)

def registrationCheckbox = new JCheckBox("Registration")
constraints.gridx = 2
constraints.weightx = 0.1  // Increase weight for the third column
frame.add(registrationCheckbox, constraints)

// -----------------------------------------------------------------------

// Create checkboxes for saving in 2D or 3D
def savingLabel = new JLabel("Select output format:")
constraints.gridx = 0
constraints.gridy = 4
constraints.weightx = 0.5  // Increase weight for the first column
frame.add(savingLabel, constraints)

def save2DCheckbox = new JCheckBox("2D tiffs")
constraints.gridx = 1
constraints.weightx = 1.0  // Increase weight for the second column
frame.add(save2DCheckbox, constraints)

def save3DCheckbox = new JCheckBox("3D tiffs")
constraints.gridx = 2
constraints.weightx = 0.1  // Increase weight for the third column
frame.add(save3DCheckbox, constraints)

// -----------------------------------------------------------------------

// Create yml parameters file import components
def parameterFileLabel = new JLabel("Select 'parameters.yml':")
constraints.gridx = 0
constraints.gridy = 5
constraints.weightx = 0.5  // Increase weight for the first column
frame.add(parameterFileLabel, constraints)

def parameterFileField = new JTextField(20)
constraints.gridx = 1
constraints.weightx = 1.0  // Increase weight for the second column
frame.add(parameterFileField, constraints)

def parameterFileButton = new JButton("Browse", actionPerformed: browseFiles)
constraints.gridx = 2
constraints.weightx = 0.1  // Increase weight for the third column
frame.add(parameterFileButton, constraints)

// -----------------------------------------------------------------------

// Create a button to display the selected variables
def button1 = new JButton("Edit parameters", actionPerformed: onEditParametersClick)
constraints.gridx = 1
constraints.gridy = 6
constraints.weightx = 1.0  // Increase weight for the second column
frame.add(button1, constraints)

// -----------------------------------------------------------------------

// Create a button to display the selected variables
def button2 = new JButton("Start", actionPerformed: onStartClick)
constraints.gridx = 2
constraints.gridy = 7
constraints.gridwidth = 3
constraints.weightx = 0.1  // Increase weight for the third column
frame.add(button2, constraints)

// Center the frame on the screen
centerFrameOnScreen(frame)

// Set the frame visibility
frame.visible = true
