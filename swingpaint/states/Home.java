package swingpaint.states;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


// The Home state is the screen the user begins on.
public class Home extends JPanel implements ActionListener {
    // UI Variables.
    private JPanel centerPanel;
    private JLabel titleLabel;
    private JButton newProjectButton;
    private JButton loadSaveButton;
    private Consumer<String> changeState;

    public Home(Consumer<String> changeState) {
        // Initialize variables.
        this.changeState = changeState;

        // Configure JPanel.
        setPreferredSize(new Dimension(800, 600));
        setLayout(new GridBagLayout());

        // Create UI elements.
        JPanel spacer;
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

        centerPanel.add(titleLabel);
        spacer = new JPanel();
        spacer.setSize(30, 30);
        centerPanel.add(spacer);
        centerPanel.add(newProjectButton);
        spacer = new JPanel();
        spacer.setSize(10, 10);
        centerPanel.add(spacer);
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
