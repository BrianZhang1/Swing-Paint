package swingpaint;

import javax.swing.JFrame;
import javax.swing.JPanel;

import swingpaint.states.ProjectEditor;
import swingpaint.states.Home;
import swingpaint.states.ProjectSelect;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;

class Main extends JFrame {
    private JPanel currentState;
    ArrayList<String> data;                 // All saved data.
    ArrayList<String> selectedProjectData;  // The data for the selected project (selected in ProjectSelect).
    
    public Main() {
        setTitle("Swing Paint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Initialize variables.
        selectedProjectData = null;

        loadData();

        // Initial State
        changeState("Home");
    }

    // Loads data file.
    private void loadData() {
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

    }

    private void changeState(String newState) {
        // Clear current state.
        if(currentState != null) {
            remove(currentState);
        }

        // Add new state.
        if("ProgramEditorNew".equals(newState)) {
            currentState = new ProjectEditor(s -> changeState(s), s -> setTitle(s), () -> pack());
        }
        else if("ProgramEditorLoad".equals(newState)) {
            currentState = new ProjectEditor(s -> changeState(s), s -> setTitle(s), () -> pack(), selectedProjectData);
        }
        else if("Home".equals(newState)) {
            loadData();
            currentState = new Home(s -> changeState(s));
        }
        else if("ProjectSelect".equals(newState)) {
            currentState = new ProjectSelect(data, s -> loadProject(s));
        }
        add(currentState);
        currentState.requestFocusInWindow();    // set focus on the new state
        pack();
    }

    // Called by the ProjectSelect state. Loads the given project.
    private void loadProject(ArrayList<String> projectData) {
        selectedProjectData = projectData;
        changeState("ProgramEditorLoad");
    }
    
    public static void main(String[] args) {
        Main root = new Main();
        root.setVisible(true);
    }
}
