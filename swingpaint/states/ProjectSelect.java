package swingpaint.states;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import swingpaint.helpers.VoidCallback;


// A screen where the user can select one of the saved projects to edit.
public class ProjectSelect extends JPanel implements ActionListener {
    // Data structures
    private ArrayList<ArrayList<String>> projects;  // Each element is an array of Strings containing the
                                                    // data of one project.
    private ArrayList<String> projectTitles;        // A list related (parallel indices) to projects which
                                                    // contains the title of each project.

    // UI
    private JLabel screenTitleLabel;    // Title.
    private JPanel projectsPanelContainer;  // Contains project panel.
    private JPanel projectsPanel;           // Contains project rows.
    private JPanel projectsPanelSpacer;     // Takes up remaining space in projects panel container for layout manager purposes.
    private JScrollPane projectsPanelScrollPane;    // Scrollbar for projectsPanel
    private JButton homeButton;         

    // Callbacks
    private Consumer<ArrayList<String>> loadProject;           // Callback method to load a project in the ProgramEditor.
    private VoidCallback returnHome;                // Callback method to return to home page.
    private VoidCallback reloadProjectSelect;       // Callback method to reload project select page.
    private Consumer<Integer> deleteProjectCallback;       // Callback method to reload project select page.

    public ProjectSelect(ArrayList<String> data, Consumer<ArrayList<String>> loadProject, VoidCallback returnHome, VoidCallback reloadProjectSelect, Consumer<Integer> deleteProjectCallback) {
        // Initialize variables.
        projects = new ArrayList<>();
        projectTitles = new ArrayList<>();
        this.loadProject = loadProject;
        this.returnHome = returnHome;
        this.reloadProjectSelect = reloadProjectSelect;
        this.deleteProjectCallback = deleteProjectCallback;


        // Create UI
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 800));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        screenTitleLabel = new JLabel("Project Select");
        screenTitleLabel.setFont(new Font("Dialog", Font.BOLD, 40));
        add(screenTitleLabel, BorderLayout.PAGE_START);

        projectsPanelContainer = new JPanel();
        projectsPanelContainer.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        projectsPanel = new JPanel();
        projectsPanel.setLayout(new BoxLayout(projectsPanel, BoxLayout.Y_AXIS));
        projectsPanel.setBackground(Color.black);
        projectsPanelContainer.add(projectsPanel);

        projectsPanelSpacer = new JPanel();
        c.weighty = 1;
        c.gridy = 1;
        projectsPanelContainer.add(projectsPanelSpacer, c);

        projectsPanelScrollPane = new JScrollPane(projectsPanelContainer);
        projectsPanelScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(projectsPanelScrollPane, BorderLayout.CENTER);
        

        homeButton = new JButton("Back to Home");
        homeButton.setActionCommand("returnHome");
        homeButton.addActionListener(this);
        homeButton.setPreferredSize(new Dimension(20, homeButton.getPreferredSize().height));
        add(homeButton, BorderLayout.PAGE_END);


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
                projectsPanel.add(pr);
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
            setLayout(new FlowLayout(FlowLayout.LEFT));
            titleLabel = new JLabel(title);
            titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.PLAIN, 24));
            titleLabel.setBorder(new EmptyBorder(0, 0, 0, 50));

            loadButton = new JButton("Load");
            loadButton.addActionListener(ProjectSelect.this);
            loadButton.setActionCommand("load " + actionCommand);
            deleteButton = new JButton("Delete");
            deleteButton.addActionListener(ProjectSelect.this);
            deleteButton.setActionCommand("delete " + actionCommand);

            add(titleLabel);
            add(loadButton);
            add(deleteButton);
        }
    }

}
