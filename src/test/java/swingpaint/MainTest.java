package swingpaint;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import swingpaint.helpers.Screen;
import swingpaint.screens.Home;
import swingpaint.screens.Settings;

public class MainTest {
    private static Main root;

    @BeforeClass
    public static void setUp() {
        root = new Main();
        root.setVisible(true);
    }

    @Test
    public void testNavigationFromHome() {
        root.changeScreen(Screen.SETTINGS);
        assertTrue("User should be able to navigate to settings screen.", root.currentScreen instanceof Settings);
    }

    @Test
    public void testReturnHome() {
        root.changeScreen(Screen.HOME);
        assertTrue("User should be able to return home from settings screen.", root.currentScreen instanceof Home);
    }
}
