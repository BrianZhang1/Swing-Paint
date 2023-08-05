package swingpaint.screens;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


// The Home screen is the screen the user begins on.
public class Home extends JPanel implements ActionListener {
    // UI Variables.
    private JPanel centerPanel;
    private JLabel titleLabel;
    private JButton newProjectButton;
    private JButton loadSaveButton;
    private JButton instructionsButton;
    private Consumer<String> changeScreen;

    // Constructor.
    public Home(Consumer<String> changeScreen) {
        // Initialize variables.
        this.changeScreen = changeScreen;

        // Configure JPanel.
        setPreferredSize(new Dimension(800, 600));
        setLayout(new GridBagLayout());

        // Create UI elements.
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel("Swing Paint");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 60));
        titleLabel.setAlignmentX(JButton.CENTER_ALIGNMENT);

        Font buttonFont = new Font(titleLabel.getFont().getName(), Font.PLAIN, 40);

        newProjectButton = new JButton("New Project");
        newProjectButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        newProjectButton.setFont(buttonFont);
        newProjectButton.setSize(getPreferredSize());
        newProjectButton.setActionCommand("newProject");
        newProjectButton.addActionListener(this);

        loadSaveButton = new JButton("Load Save");
        loadSaveButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        loadSaveButton.setFont(buttonFont);
        loadSaveButton.setActionCommand("loadSave");
        loadSaveButton.addActionListener(this);

        instructionsButton = new JButton("Instructions");
        instructionsButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        instructionsButton .setFont(buttonFont);
        instructionsButton.setActionCommand("openInstructions");
        instructionsButton.addActionListener(this);


        centerPanel.add(titleLabel);
        centerPanel.add(new Spacer(30, 30));
        centerPanel.add(newProjectButton);
        centerPanel.add(new Spacer(30, 30));
        centerPanel.add(loadSaveButton);
        centerPanel.add(new Spacer(30, 30));
        centerPanel.add(instructionsButton);
        add(centerPanel);
    }


    // Handle actions.
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch(command) {
            case "newProject":
                changeScreen.accept("ProgramEditorNew");
                break;
            case "loadSave":
                changeScreen.accept("ProjectSelect");
                break;
            case "openInstructions": {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URL("https://docs.google.com/document/d/1M5Q5zqVk4eiarDhmxFQwS8-e92UszVV2/edit").toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }


    private class Spacer extends JPanel {
        public Spacer(int width, int height) {
            setSize(width, height);
        }
    }
}
