package swingpaint;

import javax.swing.JFrame;
import javax.swing.JPanel;

import swingpaint.states.ProgramEditor;
import swingpaint.states.Home;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

class Main extends JFrame {
    private JPanel currentState;
    ArrayList<String> data;
    
    public Main() {
        setTitle("Swing Paint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load data.txt file
        this.data = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            String line = br.readLine();
            while(line != null) {
                data.add(line);
                line = br.readLine();
            }
        }
        catch(FileNotFoundException e) {
            data = null;
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        // Initial State
        changeState("ProgramEditor");
    }

    private void changeState(String newState) {
        // Clear current state.
        if(currentState != null) {
            remove(currentState);
        }

        // Add new state.
        if("ProgramEditor".equals(newState)) {
            if(data == null) {
                currentState = new ProgramEditor(s -> changeState(s));
            }
            else {
                currentState = new ProgramEditor(s -> changeState(s), data);
            }
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
