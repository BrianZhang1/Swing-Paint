package swingpaint.screens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Settings extends JPanel {
    // All configurable settings.
    public static enum Setting {
        DRAG_POINT_LENGTH ("Length of Corner-Resize rectangles.", 5),
        DEFAULT_PROJECT_WIDTH ("Default Project Width", 800),
        DEFAULT_PROJECT_LENGTH ("Default Project Length", 600);

        // Each setting has a display name which is shown to the user in the settings screen.
        private final String displayName;
        private final int defaultValue;

        private Setting(String displayName, int defaultValue) {
            this.displayName = displayName;
            this.defaultValue = defaultValue;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getDefaultValue() {
            return defaultValue;
        }
    }

    
    // UI
    private JLabel titleLabel;
    

    public Settings(
        Runnable returnHome,     // boolean determines whether settings save or not.
        LinkedHashMap<Setting, Integer> settings
    )
    {
        setPreferredSize(new Dimension(800, 600));
        setLayout(new GridBagLayout());
        GridBagConstraints c;

        // screen title
        titleLabel = new JLabel("Settings");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 30));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(20, 20, 20, 20);
        add(titleLabel, c);

        JLabel wipLabel = new JLabel("Note: this page is in development and altering the settings has no effect on the program.");
        wipLabel.setFont(wipLabel.getFont().deriveFont(Font.PLAIN, 12));
        wipLabel.setAlignmentX(LEFT_ALIGNMENT);
        wipLabel.setForeground(Color.RED);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, 20, 10, 20);
        add(wipLabel, c);

        // create a label, text field, and button for each setting
        settings.forEach((setting, value) -> {
            JLabel settingLabel = new JLabel(setting.getDisplayName());
            JTextField textField = new JTextField(Integer.toString(value), 5);
            textField.setMaximumSize(textField.getPreferredSize());
            JButton setButton = new JButton("Set");
            setButton.addActionListener(e -> {
                try {
                    settings.put(setting, Integer.parseInt(textField.getText()));
                    JOptionPane.showMessageDialog(
                        this, 
                        "Success!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE
                        );
                }
                catch(NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                        this, 
                        textField.getText()+" is not a valid integer.", 
                        "Invalid value.", 
                        JOptionPane.ERROR_MESSAGE
                        );
                }
            });

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(settingLabel);
            p.add(textField);
            p.add(setButton);
            p.setAlignmentX(LEFT_ALIGNMENT);
            p.setMaximumSize(getPreferredSize());
            GridBagConstraints c1 = new GridBagConstraints();
            c1.gridx = 0;
            c1.anchor = GridBagConstraints.LINE_START;
            c1.insets = new Insets(5, 20, 5, 0);
            add(p, c1);
        });
        
        // take up vertical space
        c = new GridBagConstraints();
        c.gridx = 0;
        c.weighty = 1;
        add(Box.createGlue(), c);

        JButton returnHomeButton = new JButton("Return Home");
        returnHomeButton.setFont(returnHomeButton.getFont().deriveFont(Font.PLAIN, 24));
        returnHomeButton.addActionListener(e -> returnHome.run());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(0, 10, 10, 0);
        c.anchor = GridBagConstraints.LINE_START;
        add(returnHomeButton, c);

        // take up horizontal space
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        add(Box.createGlue(), c);
    }
}


