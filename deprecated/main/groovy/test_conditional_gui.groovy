import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class DynamicOptionsExample {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Dynamic Options Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JCheckBox checkBox = new JCheckBox("Show More Options");
        JLabel additionalLabel = new JLabel("Additional Option:");
        JTextField additionalTextField = new JTextField();

        additionalLabel.setVisible(false);
        additionalTextField.setVisible(false);

        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    additionalLabel.setVisible(true);
                    additionalTextField.setVisible(true);
                } else {
                    additionalLabel.setVisible(false);
                    additionalTextField.setVisible(false);
                }
            }
        });

        panel.add(new JLabel("Select User:"));
        panel.add(new JComboBox<>(new String[]{"User 1", "User 2", "User 3"}));
        panel.add(new JLabel("Float Value:"));
        panel.add(new JTextField());
        panel.add(new JLabel("Int Value:"));
        panel.add(new JTextField());
        panel.add(new JLabel("Use Method:"));
        panel.add(new JCheckBox("Use Method"));
        panel.add(new JLabel("Select File/Folder:"));
        panel.add(new JFileChooser());

        panel.add(checkBox);
        panel.add(new JPanel()); // Placeholder

        panel.add(additionalLabel);
        panel.add(additionalTextField);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            // Handle OK button click
            System.out.println("Additional Option: " + additionalTextField.getText());
            frame.dispose();
        });

        panel.add(okButton);

        frame.add(panel);
        frame.setVisible(true);
    }
}

