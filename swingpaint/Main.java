/*
 * TODO: Error handling.
 * TODO: Comments and prettify! (constructor/method/class/file comments, as well as overall explanation)
 * TODO: Add more options to ProjectSelect (delete, back to home, sort) and make list format
 * TODO: Add layer reordering (move to front/back)
 * TODO: Add duplicate sprite button.
 * TODO: Add tutorial and demo, including where examples of where all key concepts are used.
 * TODO: Create project objects.
 * TODO: Implement search/sort.
 */
package swingpaint;

import javax.swing.JFrame;
import javax.swing.JPanel;

import swingpaint.states.ProjectEditor;
import swingpaint.states.Home;
import swingpaint.states.ProjectSelect;
import swingpaint.helpers.Project;
import swingpaint.sprites.JOval;
import swingpaint.sprites.JPolygon;
import swingpaint.sprites.JImage;
import swingpaint.sprites.JRectangle;
import swingpaint.sprites.JSprite;

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
    ArrayList<Project> projects; // All projects.
    Project selectedProject;  // The data for the selected project (selected in ProjectSelect).
    
    public Main() {
        setTitle("Swing Paint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Initialize variables.
        selectedProject = null;
        projects = new ArrayList<>();

        // Load data.
        loadData();

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

        Project project = new Project();    // stores current project while reading data.
        String bits[];                      // for splitting.
        try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            String line = br.readLine();
            while(line != null) {
                // Check if starts new project.
                try {
                    if("ProjectStart".equals(line.substring(0, "ProjectStart".length()))) {
                        // Load metadata.
                        bits = line.split(";");
                        project.setTitle(bits[1].split("=")[1]);
                        String projectSizeBits = bits[2].split("=")[1];
                        project.setWidth(Integer.parseInt(projectSizeBits.split(",")[0]));
                        project.setHeight(Integer.parseInt(projectSizeBits.split(",")[1]));
                        line = br.readLine();
                        continue;
                    }
                }
                catch(StringIndexOutOfBoundsException e) {
                    // do nothing. Raised by String.substring().
                }

                try {
                    if("ProjectEnd".equals(line.substring(0, "ProjectEnd".length()))) {
                        // End of project data.
                        projects.add(project);
                        project = new Project();
                        line = br.readLine();
                        continue;
                    }
                }
                catch(StringIndexOutOfBoundsException e) {
                    // do nothing. Raised by String.substring().
                }

                // Read sprite data.
                bits = line.split(";");
                String type = bits[0].split("=")[1];
                JSprite sprite = null;
                switch(type) {
                    case "rectangle": {
                        // Create JRectangle and set color
                        JRectangle jr = new JRectangle(JRectangle.rectangleFromString(bits));
                        jr.setRGBString(bits[5].split("=")[1]);
                        sprite = jr;
                        break;
                    }
                    case "oval": {
                        // Create JOval and set color
                        JOval jo = new JOval(JRectangle.rectangleFromString(bits));
                        jo.setRGBString(bits[5].split("=")[1]);
                        sprite = jo;
                        break;
                    }
                    case "polygon": {
                        // Create JPolygon and set color
                        JPolygon jp = new JPolygon(JPolygon.polygonFromString(bits));
                        jp.setRGBString(bits[3].split("=")[1]);
                        sprite = jp;
                        break;
                    }
                    case "image": {
                        // Extract attributes.
                        int x = Integer.parseInt(bits[1].split("=")[1]);
                        int y = Integer.parseInt(bits[2].split("=")[1]);
                        int width = Integer.parseInt(bits[3].split("=")[1]);
                        int height = Integer.parseInt(bits[4].split("=")[1]);
                        String imageName = bits[5].split("=")[1];

                        // Create JImage
                        JImage ji = new JImage(JImage.imageFromName(imageName), imageName, x, y, width, height);
                        sprite = ji;
                        break;
                    }
                }

                // add sprite to project sprites.
                if(sprite != null) {
                    project.addSprite(sprite);
                }
                else {
                    // Error.
                    System.out.println("An error has occured while loading data (null sprite).");
                }
                
                // Read next line.
                line = br.readLine();
            }
        }
        catch(FileNotFoundException e) {
            // do nothing. Leave projects empty.
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    // Writes projects to data file.
    private void writeData() {
        try(PrintWriter pw = new PrintWriter(new FileWriter("data.txt"))) {
            for(Project project : projects) {
                // Write header metadata.
                pw.println(String.format("ProjectStart;title=%s;size=%d,%d", project.getTitle(), project.getWidth(), project.getHeight()));     

                // Write sprite data.
                for(JSprite sprite : project.getSprites()) {
                    pw.println(sprite.toString());
                }

                // Mark end of this project.
                pw.println("ProjectEnd");
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
            currentState = new ProjectEditor(s -> changeState(s), s -> setTitle(s), () -> pack(), s -> saveProject(s), new Project(selectedProject));
        }
        else if("Home".equals(newState)) {
            currentState = new Home(s -> changeState(s));
        }
        else if("ProjectSelect".equals(newState)) {
            currentState = new ProjectSelect(projects, s -> loadProject(s), () -> changeState("Home"), () -> changeState("ProjectSelect"), i -> deleteProject(i));
        }
        add(currentState);
        currentState.requestFocusInWindow();    // set focus on the new state
        pack();
    }

    // Called by the ProjectSelect state. Loads the given project.
    private void loadProject(Project project) {
        selectedProject = project;
        changeState("ProgramEditorLoad");
    }

    // Saves a project by appending it to the end of the data file.
    private void saveProject(Project project) {
        projects.add(project);
        writeData();
    }

    // Deletes a project by copying data through iteration, but excluding the selected project.
    private void deleteProject(int targetIndex) {
        projects.remove(targetIndex);
        writeData();
    }
    
    public static void main(String[] args) {
        Main root = new Main();
        root.setVisible(true);
    }
}
