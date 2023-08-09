package swingpaint;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.fasterxml.jackson.databind.ObjectMapper;

import swingpaint.helpers.Project;
import swingpaint.helpers.Screen;
import swingpaint.screens.Home;
import swingpaint.screens.ProjectEditor;
import swingpaint.screens.ProjectSelect;
import swingpaint.screens.Settings;
import swingpaint.screens.Settings.Setting;
import swingpaint.sprites.JImage;
import swingpaint.sprites.JOval;
import swingpaint.sprites.JPolygon;
import swingpaint.sprites.JRectangle;
import swingpaint.sprites.JSprite;



// Class that handles higher-level functions (switching between screens, file handling).
class Main extends JFrame {
    JPanel currentScreen;    // the current screen
    ArrayList<Project> projects;    // All projects.
    Project selectedProject;        // The data for the selected project (selected in ProjectSelect).
    ArrayList<String> userImages;     // The names of all the images in the userImage directory.
    LinkedHashMap<Setting, Integer> settings;   // user settings.
    

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
        settings = new LinkedHashMap<>();
        // TODO load settings from file
        Arrays.asList(Settings.Setting.values()).forEach(setting -> {
            settings.put(setting, setting.getDefaultValue());
        });

        // Load data.
        loadData();

        // Initial Screen
        changeScreen(Screen.HOME);
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
        ObjectMapper mapper = new ObjectMapper();
        JsonData jsonData = null;
        try {
            jsonData = mapper.readValue(new File("data.json"), JsonData.class);
        }
        catch(FileNotFoundException e) {
            // do nothing. File will be created on next writeData() call.
            return;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        Project project = new Project();
        String bits[];  // for storing the result of String.split()
        ArrayList<JsonProject> jsonProjects = jsonData.getProjects();
        for(JsonProject jsonProject : jsonProjects) {
            project = new Project();
            project.setTitle(jsonProject.getTitle());
            project.setWidth(jsonProject.getWidth());
            project.setHeight(jsonProject.getHeight());
            project.setDateCreated(LocalDateTime.parse(jsonProject.getDateCreated()));
            project.setDateModified(LocalDateTime.parse(jsonProject.getDateModified()));
            ArrayList<String> spritesData = jsonProject.getSprites();
            if(spritesData == null) continue;
            for(String spriteData : jsonProject.getSprites()) {
                bits = spriteData.split(";");
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
            }

            projects.add(project);
        }
        
    }


    // Writes projects to data file.
    private void writeData() {
        JsonData jsonData = new JsonData(projects);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File("data.json"), jsonData);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    private static class JsonData {
        private ArrayList<JsonProject> projects;

        private JsonData(ArrayList<Project> projects) {
            this.projects = new ArrayList<>();
            for(Project project : projects) {
                this.projects.add(new JsonProject(project));
            }
        }

        private JsonData() {
        }

        public ArrayList<JsonProject> getProjects() {
            return projects;
        }

        public void setProjects(ArrayList<JsonProject> projects) {
            this.projects = projects;
        }
    }


    private static class JsonProject {
        private String title;
        private int width, height;
        private String dateModified, dateCreated;
        private ArrayList<String> sprites;

        private JsonProject(Project project) {
            title = project.getTitle();
            width = project.getWidth();
            height = project.getHeight();
            dateModified = project.getDateCreated().toString();
            dateCreated = project.getDateModified().toString();
            sprites = new ArrayList<>();
            for(JSprite sprite : project.getSprites()) {
                sprites.add(sprite.toString());
            }
        }

        private JsonProject() {

        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getDateModified() {
            return dateModified;
        }

        public void setDateModified(String dateModified) {
            this.dateModified = dateModified;
        }

        public String getDateCreated() {
            return dateCreated;
        }

        public void setDateCreated(String dateCreated) {
            this.dateCreated = dateCreated;
        }

        public ArrayList<String> getSprites() {
            return sprites;
        }

        public void setSprites(ArrayList<String> sprites) {
            this.sprites = sprites;
        }

    }


    // Changes the screen to newScreen.
    void changeScreen(Screen newScreen) {
        // Clear current screen.
        if(currentScreen != null) {
            remove(currentScreen);
        }

        // Reset frame title.
        setTitle("Swing Paint");

        // Add new screen.
        switch(newScreen) {
            case PROGRAM_EDITOR: {
                currentScreen = new ProjectEditor(s -> changeScreen(s), 
                    s -> setTitle(s), 
                    () -> pack(), 
                    s -> saveProject(s), 
                    userImages, 
                    (selectedProject == null) ? null : new Project(selectedProject),
                    projects.stream()
                        .map(p -> p.getTitle())
                        .collect(Collectors.toList()),
                    settings
                );
                break;
            }

            case HOME: {
                currentScreen = new Home(s -> changeScreen(s));
                break;
            }

            case PROJECT_SELECT: {
                currentScreen = new ProjectSelect(projects, 
                    project -> loadProject(project), 
                    () -> changeScreen(Screen.HOME), 
                    () -> changeScreen(Screen.PROJECT_SELECT), 
                    i -> deleteProject(i)
                );
                break;
            }

            case SETTINGS: {
                currentScreen = new Settings(() -> changeScreen(Screen.HOME), settings);
                break;
            }
        }
        add(currentScreen);
        currentScreen.requestFocusInWindow();    // set focus on the new screen
        pack();
    }


    // Called by the ProjectSelect screen. Loads the given project.
    void loadProject(Project project) {
        selectedProject = project;
        changeScreen(Screen.PROGRAM_EDITOR);
        selectedProject = null;
    }


    // Saves a project.
    void saveProject(Project project) {
        projects.add(project);
        writeData();
    }

    // Deletes a project.
    void deleteProject(int targetIndex) {
        projects.remove(targetIndex);
        writeData();
    }
    

    // Program starts here.
    public static void main(String[] args) {
        Main root = new Main();
        root.setVisible(true);
    }
}
