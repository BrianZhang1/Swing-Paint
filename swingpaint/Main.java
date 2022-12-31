package swingpaint;

import javax.swing.JFrame;

import swingpaint.states.ProgramEditor;

class Main extends JFrame {
    private ProgramEditor programEditor;
    
    public Main() {
        programEditor = new ProgramEditor();
        
        init();
    }

    private void init() {
        add(programEditor);
        pack();

        setTitle("Swing Paint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public static void main(String[] args) {
        Main root = new Main();
        root.setVisible(true);
    }
}
