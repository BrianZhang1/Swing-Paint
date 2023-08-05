package swingpaint.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import swingpaint.helpers.Project;


// A screen where the user can select one of the saved projects to edit.
public class ProjectSelect extends JPanel implements ActionListener {
    // Data structures
    private ArrayList<Project> projects;  // contains all projects.

    // UI
    private JLabel screenTitleLabel;    // Title.
    private JPanel contentPanel;        // Contains all components in the center.
    private JButton sortByNameButton;   // Sorts projects by name, alphabetically.
    private boolean sortAscending;      // Whether to sort by name ascending or descending.
    private JPanel searchPanel;         // Panel for search components.
    private JLabel searchLabel;
    private JTextField searchTextField; // Searches projects by name.
    private JPanel projectsPanelContainer;  // Contains project panel.
    private JPanel projectsPanel;           // Contains project rows.
    private JPanel projectsPanelSpacer;     // Takes up remaining space in projects panel container for layout manager purposes.
    private JScrollPane projectsPanelScrollPane;    // Scrollbar for projectsPanel
    private JButton homeButton;         

    // Callbacks
    private Consumer<Project> loadProject;           // Callback method to load a project in the ProgramEditor.
    private Runnable returnHome;                // Callback method to return to home page.
    private Runnable reloadProjectSelect;       // Callback method to reload project select page.
    private Consumer<Integer> deleteProjectCallback;       // Callback method to reload project select page.


    // Main constructor.
    public ProjectSelect(ArrayList<Project> projects, Consumer<Project> loadProject, Runnable returnHome, Runnable reloadProjectSelect, Consumer<Integer> deleteProjectCallback) {
        // Initialize variables.
        this.projects = projects;
        this.loadProject = loadProject;
        this.returnHome = returnHome;
        this.reloadProjectSelect = reloadProjectSelect;
        this.deleteProjectCallback = deleteProjectCallback;
        sortAscending = true;


        // Create UI
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints c;

        screenTitleLabel = new JLabel("Project Select");
        screenTitleLabel.setFont(new Font("Dialog", Font.BOLD, 40));
        add(screenTitleLabel, BorderLayout.PAGE_START);

        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();

        sortByNameButton = new JButton("Sort by Name");
        sortByNameButton.setActionCommand("sortByName");
        sortByNameButton.addActionListener(this);
        c.anchor = GridBagConstraints.LINE_START;
        contentPanel.add(sortByNameButton, c);

        searchPanel = new JPanel();
        searchLabel = new JLabel("Search");
        searchTextField = new JTextField(12);
        searchTextField.setActionCommand("search");
        searchTextField.addActionListener(this);
        searchTextField.setAlignmentX(JTextField.LEFT_ALIGNMENT);
        searchPanel.add(searchLabel);
        searchPanel.add(searchTextField);
        c.gridy = 1;
        contentPanel.add(searchPanel, c);

        projectsPanelContainer = new JPanel();
        projectsPanelContainer.setLayout(new GridBagLayout());
        c = new GridBagConstraints();

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

        c = new GridBagConstraints();
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        contentPanel.add(projectsPanelScrollPane, c);

        add(contentPanel, BorderLayout.CENTER);

        homeButton = new JButton("Back to Home");
        homeButton.setActionCommand("returnHome");
        homeButton.addActionListener(this);
        homeButton.setPreferredSize(new Dimension(20, homeButton.getPreferredSize().height));
        add(homeButton, BorderLayout.PAGE_END);


        // Process data into separate projects.
        if(projects != null) {
            // Display all projects.
            displayProjects(projects);
        }
    }

    // Displays a row for each project in projects.
    private void displayProjects(ArrayList<Project> projects) {
        // First, clear all current visible projects.
        projectsPanel.removeAll();

        // Add all projects given.
        for(int i = 0; i < projects.size(); i++) {
            ProjectRow pr = new ProjectRow(projects.get(i).getTitle(), Integer.toString(i));
            projectsPanel.add(pr);
        }

        projectsPanel.revalidate();
    }

    
    // Handle actions.
    @Override
    public void actionPerformed(ActionEvent e) {
        String[] bits;

        bits = e.getActionCommand().split(" ");

        // Different behaviour depending on action.
        if("load".equals(bits[0])) {
            int selectedProjectIndex = Integer.parseInt(bits[1]);
            loadProject.accept(projects.get(selectedProjectIndex));
        }
        else if("delete".equals(bits[0])) {
            int selectedProjectIndex = Integer.parseInt(bits[1]);
            deleteProject(selectedProjectIndex);
        }
        else if("returnHome".equals(bits[0])) {
            returnHome.run();
        }
        else if("sortByName".equals(bits[0])) {
            sortByName(sortAscending);
        }
        else if("search".equals(bits[0])) {
            search(searchTextField.getText());
        }
    }


    // Deletes project from save.
    private void deleteProject(int index) {
        deleteProjectCallback.accept(index);
        reloadProjectSelect.run();
    }


    // Sorts and displays projects in alphabetical order. Uses bubble sort.
    private void sortByName(boolean ascending) {
        if(projects.size() < 2) {
            // do nothing.
            return;
        }

        // Implement optimized bubble sort.
        boolean swapped = true;    // tracks whether a swap was made in each loop.
        while(swapped) {
            swapped = false;
            for(int i = 1; i < projects.size(); i++) {
                // compare lexographically.
                if(ascending) {
                    if(projects.get(i).getTitle().compareTo(projects.get(i-1).getTitle()) < 0) {
                        // swap the two projects.
                        Project temp = projects.get(i);
                        projects.remove(i);
                        projects.add(i, projects.get(i-1));
                        projects.remove(i-1);
                        projects.add(i-1, temp);
                        swapped = true;
                    }
                }
                else {
                    if(projects.get(i).getTitle().compareTo(projects.get(i-1).getTitle()) > 0) {
                        // swap the two projects.
                        Project temp = projects.get(i);
                        projects.remove(i);
                        projects.add(i, projects.get(i-1));
                        projects.remove(i-1);
                        projects.add(i-1, temp);
                        swapped = true;
                    }
                }
            }
        }

        // Toggle ascending.
        if(ascending) {
            sortAscending = false;
        }
        else {
            sortAscending = true;
        }

        // Redisplay rows.
        displayProjects(projects);
    }


    // Displays projects that meet search criteria.
    private void search(String criteria) {
        ArrayList<Project> passedProjects = new ArrayList<>();  // projects that pass the search criteria.
        for(Project p : projects) {
            if(p.getTitle().contains(criteria)) {
                passedProjects.add(p);
            }
        }

        displayProjects(passedProjects);
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
