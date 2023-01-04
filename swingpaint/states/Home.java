package swingpaint.states;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.function.Consumer;

public class Home extends JPanel implements ActionListener {
    private JButton newProjectButton;
    private JButton loadSaveButton;
    private Consumer<String> changeState;

    public Home(Consumer<String> changeState) {
        this.changeState = changeState;

        newProjectButton = new JButton("New Project");
        newProjectButton.setActionCommand("newProject");
        newProjectButton.addActionListener(this);

        loadSaveButton = new JButton("Load Save");
        loadSaveButton.setActionCommand("loadSave");
        loadSaveButton.addActionListener(this);

        add(newProjectButton);
        add(loadSaveButton);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch(command) {
            case "newProject":
                changeState.accept("ProgramEditorNew");
                break;
            case "loadSave":
                changeState.accept("ProgramEditorLoad");
                break;
        }
    }
}
