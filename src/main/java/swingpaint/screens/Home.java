package swingpaint.screens;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import swingpaint.helpers.Screen;


// The Home screen is the screen the user begins on.
public class Home extends JPanel implements ActionListener {
    // UI Variables.
    private JLabel titleLabel;
    private JButton newProjectButton;
    private JButton loadSaveButton;
    private JButton instructionsButton;
    private JButton settingsButton;
    private Consumer<Screen> changeScreen;

    // Constructor.
    public Home(Consumer<Screen> changeScreen) {
        // Initialize variables.
        this.changeScreen = changeScreen;

        // Configure JPanel.
        setPreferredSize(new Dimension(800, 600));
        setLayout(new GridBagLayout());
        GridBagConstraints c;

        titleLabel = new JLabel("Swing Paint");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 60));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(30, 30, 30, 30);
        add(titleLabel, c);

        Font buttonFont = new Font(titleLabel.getFont().getName(), Font.PLAIN, 40);

        newProjectButton = new JButton("New Project");
        newProjectButton.setAlignmentX(CENTER_ALIGNMENT);
        newProjectButton.setFont(buttonFont);
        newProjectButton.setSize(getPreferredSize());
        newProjectButton.setActionCommand("newProject");
        newProjectButton.addActionListener(this);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(5, 5, 5, 5);
        add(newProjectButton, c);

        loadSaveButton = new JButton("Load Project");
        loadSaveButton.setAlignmentX(CENTER_ALIGNMENT);
        loadSaveButton.setFont(buttonFont);
        loadSaveButton.setActionCommand("loadSave");
        loadSaveButton.addActionListener(this);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(5, 5, 5, 5);
        add(loadSaveButton, c);

        instructionsButton = new JButton("Instructions");
        instructionsButton.setAlignmentX(CENTER_ALIGNMENT);
        instructionsButton.setFont(buttonFont);
        instructionsButton.setActionCommand("openInstructions");
        instructionsButton.addActionListener(this);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(5, 5, 5, 5);
        add(instructionsButton, c);

        settingsButton = new JButton("Settings");
        settingsButton.setAlignmentX(CENTER_ALIGNMENT);
        settingsButton.setFont(buttonFont);
        settingsButton.setActionCommand("openSettings");
        settingsButton.addActionListener(this);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.insets = new Insets(5, 5, 5, 5);
        add(settingsButton, c);
    }


    // Handle actions.
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch(command) {
            case "newProject":
                changeScreen.accept(Screen.PROGRAM_EDITOR);
                break;
            case "loadSave":
                changeScreen.accept(Screen.PROJECT_SELECT);
                break;
            case "openInstructions": {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URI("https://docs.google.com/document/d/1M5Q5zqVk4eiarDhmxFQwS8-e92UszVV2/edit"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            }
            case "openSettings": {
                changeScreen.accept(Screen.SETTINGS);
            }
        }
    }
}
