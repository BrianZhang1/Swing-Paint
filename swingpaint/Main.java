package swingpaint;

import javax.swing.JFrame;
import javax.swing.JPanel;

import swingpaint.states.ProgramEditor;
import swingpaint.states.Home;

class Main extends JFrame {
    private JPanel currentState;
    
    public Main() {
        // Declare initial state.
        changeState("ProgramEditor");

        setTitle("Swing Paint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void changeState(String newState) {
        // Clear current state.
        if(currentState != null) {
            remove(currentState);
        }

        // Add new state.
        if("ProgramEditor".equals(newState)) {
            currentState = new ProgramEditor(s -> changeState(s));
        } else if("Home".equals(newState)) {
            currentState = new Home(s -> changeState(s));
        }
        add(currentState);
        currentState.requestFocusInWindow();    // set focus on the new state
        pack();
    }
    
    public static void main(String[] args) {
        Main root = new Main();
        root.setVisible(true);
    }
}
