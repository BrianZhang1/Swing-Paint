package swingpaint.states;

import java.util.ArrayList;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


// A screen where the user can select one of the saved projects to edit.
public class ProjectSelect extends JPanel implements ActionListener {
    private ArrayList<ArrayList<String>> projects;  // Each element is an array of Strings containing the
                                                    // data of one project.
    private ArrayList<String> projectTitles;        // A list related (parallel indices) to projects which
                                                    // contains the title of each project.
    private ArrayList<JButton> projectButtons;      // Contains a load button for each project.
    private Consumer<ArrayList<String>> loadProject;           // Callback method to load a project in the ProgramEditor.

    public ProjectSelect(ArrayList<String> data, Consumer<ArrayList<String>> loadProject) {
        // Initialize variables.
        projects = new ArrayList<>();
        projectTitles = new ArrayList<>();
        projectButtons = new ArrayList<>();
        this.loadProject = loadProject;


        if(data != null) {
            // Process data into separate projects.
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
                JButton button = new JButton(title);
                button.setActionCommand(Integer.toString(i));
                button.addActionListener(this);
                projectButtons.add(new JButton(title));
                add(button);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedProjectIndex = Integer.parseInt(e.getActionCommand());
        loadProject.accept(projects.get(selectedProjectIndex));
    }

}
