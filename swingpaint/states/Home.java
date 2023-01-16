package swingpaint.states;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


// The Home state is the screen the user begins on.
public class Home extends JPanel implements ActionListener {
    // UI Variables.
    private JPanel centerPanel;
    private JButton newProjectButton;
    private JButton loadSaveButton;
    private Consumer<String> changeState;

    public Home(Consumer<String> changeState) {
        // Initialize variables.
        this.changeState = changeState;

        // Configure JPanel.
        setPreferredSize(new Dimension(1280, 800));
        setLayout(new GridBagLayout());

        // Create UI elements.
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        newProjectButton = new JButton("New Project");
        newProjectButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        newProjectButton.setBorder(new EmptyBorder(10, 10, 10, 10));
        newProjectButton.setActionCommand("newProject");
        newProjectButton.addActionListener(this);

        loadSaveButton = new JButton("Load Save");
        loadSaveButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        loadSaveButton.setBorder(new EmptyBorder(10, 10, 10, 10));
        loadSaveButton.setActionCommand("loadSave");
        loadSaveButton.addActionListener(this);

        centerPanel.add(newProjectButton);
        centerPanel.add(loadSaveButton);
        add(centerPanel);
    }


    // Handle actions.
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch(command) {
            case "newProject":
                changeState.accept("ProgramEditorNew");
                break;
            case "loadSave":
                changeState.accept("ProjectSelect");
                break;
        }
    }
}
