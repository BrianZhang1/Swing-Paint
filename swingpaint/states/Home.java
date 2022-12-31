package swingpaint.states;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.function.Consumer;

public class Home extends JPanel implements ActionListener {
    private JButton toProgramEditor;
    private Consumer<String> changeState;

    public Home(Consumer<String> changeState) {
        this.changeState = changeState;

        toProgramEditor = new JButton("Program Editor");
        toProgramEditor.setActionCommand("to program editor");
        toProgramEditor.addActionListener(this);

        add(toProgramEditor);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch(command) {
            case "to program editor":
                changeState.accept("ProgramEditor");
                break;
        }
    }
}
