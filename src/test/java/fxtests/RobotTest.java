package fxtests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import exercism.Robot;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import org.junit.Test;

public class RobotTest {

    private static final String EXPECTED_ROBOT_NAME_PATTERN = "[A-Z]{2}\\d{3}";
    private final Robot robot = new Robot();


    @Test
    public void hasName() {
		EnumSet.allOf(ChronoUnit.class);
        assertIsValidName(robot.getName());
    }


    @Test
    public void differentRobotsHaveDifferentNames() {
        assertThat("Name should be different", robot.getName(), not(equalTo(new Robot().getName())));
    }

    @Test
    public void resetName() {
        final String name = robot.getName();
        robot.reset();
        final String name2 = robot.getName();
        assertThat("Names should not be equal", name, not(equalTo(name2)));
        assertIsValidName(name2);
    }

    private static void assertIsValidName(String name) {
        assertThat("Name should be valid", name.matches(EXPECTED_ROBOT_NAME_PATTERN), is(true));
    }
}