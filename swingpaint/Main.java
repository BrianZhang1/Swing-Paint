/*
 * TODO: Error handling.
 * TODO: Reorganize option combo boxes.
 * TODO: Comments and prettify! (constructor/method/class/file comments, as well as overall explanation)
 * TODO: Add more options to ProjectSelect (delete, back to home, sort) and make list format
 * TODO: Add layer reordering (move to front/back)
 */
package swingpaint;

import javax.swing.JFrame;
import javax.swing.JPanel;

import swingpaint.states.ProjectEditor;
import swingpaint.states.Home;
import swingpaint.states.ProjectSelect;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
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

        // Initial State
        changeState("Home");
    }

    // Loads data file.
    private void loadData() {
        // Create userImages folder if it does not exist.
        File userImagesDirectory = new File("userImages");
        if(!userImagesDirectory.exists()) {
            userImagesDirectory.mkdir();
        }

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

    // Writes data to data file.
    private void writeData() {
        try(PrintWriter pw = new PrintWriter(new FileWriter("data.txt"))) {
            for(String line : data) {
                pw.println(line);
            }
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
            currentState = new ProjectEditor(s -> changeState(s), s -> setTitle(s), () -> pack(), s -> saveProject(s));
        }
        else if("ProgramEditorLoad".equals(newState)) {
            currentState = new ProjectEditor(s -> changeState(s), s -> setTitle(s), () -> pack(), s -> saveProject(s), selectedProjectData);
        }
        else if("Home".equals(newState)) {
            loadData();
            currentState = new Home(s -> changeState(s));
        }
        else if("ProjectSelect".equals(newState)) {
            currentState = new ProjectSelect(data, s -> loadProject(s), () -> changeState("Home"), () -> changeState("ProjectSelect"), i -> deleteProject(i));
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

    // Saves a project by appending it to the end of the data file.
    private void saveProject(ArrayList<String> projectData) {
        try(PrintWriter pw = new PrintWriter(new FileWriter("data.txt", true))) {
            for(String line : projectData) {
                pw.println(line);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    // Deletes a project by copying data through iteration, but excluding the selected project.
    private void deleteProject(int targetIndex) {
        ArrayList<String> newData = new ArrayList<>();  // Stores the new data.
        int curIndex = -1;                              // represents the current project index.
        boolean deleting = false;                       // whether currently deleting lines.

        for(String line : data) {
            // Read if line starts signals beginning of new project.
            try{
                if("ProjectStart".equals(line.substring(0, "ProjectStart".length()))) {
                    curIndex++;
                }
            }
            catch(StringIndexOutOfBoundsException e) {
                // do nothing.
            }

            // Check conditions to begin/end deleting.
            if(targetIndex == curIndex) {
                deleting = true;
            } 
            else {
                deleting = false;
            }

            // Add the new line if not currently deleting.
            if(!deleting) {
                newData.add(line);
            }
        }

        // Replace data.
        data = newData;
        writeData();
    }
    
    public static void main(String[] args) {
        Main root = new Main();
        root.setVisible(true);
    }
}
