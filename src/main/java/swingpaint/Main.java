package swingpaint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import swingpaint.helpers.Project;
import swingpaint.screens.Home;
import swingpaint.screens.ProjectEditor;
import swingpaint.screens.ProjectSelect;
import swingpaint.sprites.JImage;
import swingpaint.sprites.JOval;
import swingpaint.sprites.JPolygon;
import swingpaint.sprites.JRectangle;
import swingpaint.sprites.JSprite;



// Class that handles higher-level functions (switching between screens, file handling).
class Main extends JFrame {
    private JPanel currentScreen;    // the current screen
    ArrayList<Project> projects;    // All projects.
    Project selectedProject;        // The data for the selected project (selected in ProjectSelect).
    ArrayList<String> userImages;     // The names of all the images in the userImage directory.
    

    // Initialize frame.
    public Main() {
        // Configure frame.
        setTitle("Swing Paint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Initialize variables.
        selectedProject = null;
        projects = new ArrayList<>();
        userImages = new ArrayList<>();

        // Load data.
        loadData();

        // Initial Screen
        changeScreen("Home");
    }


    // Loads data file.
    private void loadData() {
        // Create userImages folder if it does not exist.
        File userImagesDirectory = new File("userImages");
        if(!userImagesDirectory.exists()) {
            userImagesDirectory.mkdir();
        }
        // Read user images.
        else {
            File[] files = userImagesDirectory.listFiles();
            if(files != null) {
                for(File file : files) {
                    userImages.add(file.getName());
                }
            }
        }


        // Read project data.
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
                    JOptionPane.showMessageDialog(this, "An error has occured while loading data (null sprite).",
                        "Null Sprite Error", JOptionPane.ERROR_MESSAGE);
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


    // Changes the screen to newScreen.
    private void changeScreen(String newScreen) {
        // Clear current screen.
        if(currentScreen != null) {
            remove(currentScreen);
        }

        // Reset frame title.
        setTitle("Swing Paint");

        // Add new screen.
        if("ProgramEditorNew".equals(newScreen)) {
            currentScreen = new ProjectEditor(s -> changeScreen(s), s -> setTitle(s), () -> pack(), s -> saveProject(s), userImages);
        }
        else if("ProgramEditorLoad".equals(newScreen)) {
            currentScreen = new ProjectEditor(s -> changeScreen(s), s -> setTitle(s), () -> pack(), s -> saveProject(s), new Project(selectedProject), userImages);
        }
        else if("Home".equals(newScreen)) {
            currentScreen = new Home(s -> changeScreen(s));
        }
        else if("ProjectSelect".equals(newScreen)) {
            currentScreen = new ProjectSelect(projects, s -> loadProject(s), () -> changeScreen("Home"), () -> changeScreen("ProjectSelect"), i -> deleteProject(i));
        }
        add(currentScreen);
        currentScreen.requestFocusInWindow();    // set focus on the new screen
        pack();
    }


    // Called by the ProjectSelect screen. Loads the given project.
    private void loadProject(Project project) {
        selectedProject = project;
        changeScreen("ProgramEditorLoad");
    }


    // Saves a project.
    private void saveProject(Project project) {
        projects.add(project);
        writeData();
    }

    // Deletes a project.
    private void deleteProject(int targetIndex) {
        projects.remove(targetIndex);
        writeData();
    }
    

    // Program starts here.
    public static void main(String[] args) {
        Main root = new Main();
        root.setVisible(true);
    }
}
