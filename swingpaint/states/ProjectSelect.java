package swingpaint.states;

import java.util.ArrayList;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import swingpaint.helpers.VoidCallback;


// A screen where the user can select one of the saved projects to edit.
public class ProjectSelect extends JPanel implements ActionListener {
    private ArrayList<ArrayList<String>> projects;  // Each element is an array of Strings containing the
                                                    // data of one project.
    private ArrayList<String> projectTitles;        // A list related (parallel indices) to projects which
                                                    // contains the title of each project.
    private ArrayList<ProjectRow> projectRows;      // Contains a ProjectRow object for each project.

    private JButton homeButton;

    private Consumer<ArrayList<String>> loadProject;           // Callback method to load a project in the ProgramEditor.
    private VoidCallback returnHome;                // Callback method to return to home page.
    private VoidCallback reloadProjectSelect;       // Callback method to reload project select page.
    private Consumer<Integer> deleteProjectCallback;       // Callback method to reload project select page.

    public ProjectSelect(ArrayList<String> data, Consumer<ArrayList<String>> loadProject, VoidCallback returnHome, VoidCallback reloadProjectSelect, Consumer<Integer> deleteProjectCallback) {
        // Initialize variables.
        projects = new ArrayList<>();
        projectTitles = new ArrayList<>();
        projectRows = new ArrayList<>();
        this.loadProject = loadProject;
        this.returnHome = returnHome;
        this.reloadProjectSelect = reloadProjectSelect;
        this.deleteProjectCallback = deleteProjectCallback;


        // Set layout and add core buttons.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        homeButton = new JButton("Back");
        homeButton.setActionCommand("returnHome");
        homeButton.addActionListener(this);
        add(homeButton);


        // Process data into separate projects.
        if(data != null) {
            ArrayList<String> projectData;
            projectData = new ArrayList<>();
            for(String line : data) {
                try {
                    if("ProjectStart".equals(line.substring(0, "ProjectStart".length()))) {
                        // Extract the title from the project header string.
                        projectTitles.add(line.split(";")[1].split("=")[1]);
                        projectData.add(line);
                        continue;   // skip the rest of the if statements.
                    }
                }
                catch(StringIndexOutOfBoundsException e) {
                    // do nothing.
                }
                if("ProjectEnd".equals(line)) {
                    projects.add(projectData);
                    projectData = new ArrayList<>();
                }
                else {
                    projectData.add(line);
                }
            }

            // Create a button for each project.
            for(int i = 0; i < projectTitles.size(); i++) {
                String title = projectTitles.get(i);
                ProjectRow pr = new ProjectRow(title, Integer.toString(i));
                projectRows.add(pr);
                add(pr);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String[] bits;

        bits = e.getActionCommand().split(" ");

        if("load".equals(bits[0])) {
            int selectedProjectIndex = Integer.parseInt(bits[1]);
            loadProject.accept(projects.get(selectedProjectIndex));
        }
        else if("delete".equals(bits[0])) {
            int selectedProjectIndex = Integer.parseInt(bits[1]);
            deleteProject(selectedProjectIndex);
        }
        else if("returnHome".equals(bits[0])) {
            returnHome.accept();
        }
    }


    // Deletes project from save.
    private void deleteProject(int index) {
        deleteProjectCallback.accept(index);
        reloadProjectSelect.accept();
    }


    // Represents a single row which contains the details of one project.
    private class ProjectRow extends JPanel {
        private JLabel titleLabel;
        private JButton loadButton;
        private JButton deleteButton;

        public ProjectRow(String title, String actionCommand) {
            titleLabel = new JLabel(title);
            loadButton = new JButton("Load");
            loadButton.addActionListener(ProjectSelect.this);
            loadButton.setActionCommand("load " + actionCommand);
            deleteButton = new JButton("X");
            deleteButton.addActionListener(ProjectSelect.this);
            deleteButton.setActionCommand("delete " + actionCommand);

            add(titleLabel);
            add(loadButton);
            add(deleteButton);
        }
    }

}
