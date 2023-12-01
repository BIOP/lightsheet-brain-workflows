import javax.swing.*
import java.awt.*

class JComboBoxExample {
    static void main(String[] args) {
        // Your String[] array
        def stringArray = ["Option 1", "Option 2", "Option 3"]

        // Create a JComboBox
        def comboBox = new JComboBox<String>()

        // Populate the JComboBox with the elements from the String[]
        comboBox.model = new DefaultComboBoxModel<String>(stringArray.toArray(new String[0]))

        // Create a JFrame and add the JComboBox
        def frame = new JFrame("JComboBox Example")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = new FlowLayout()
        frame.add(comboBox)
        frame.size = new Dimension(300, 150)
        frame.visible = true
    }
}

