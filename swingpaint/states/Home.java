package swingpaint.states;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.util.function.Consumer;

public class Home extends JPanel implements ActionListener {
    private JPanel centerPanel;
    private JButton newProjectButton;
    private JButton loadSaveButton;
    private Consumer<String> changeState;

    public Home(Consumer<String> changeState) {
        this.changeState = changeState;

        setPreferredSize(new Dimension(1280, 800));
        setLayout(new GridBagLayout());

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
