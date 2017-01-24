package exercism;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Random;
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
        assertThat(robot.getName(), not(equalTo(new Robot().getName())));
    }

    @Test
    public void resetName() {
        final String name = robot.getName();
        robot.reset();
        final String name2 = robot.getName();
        assertThat(name, not(equalTo(name2)));
        assertIsValidName(name2);
    }

    private static void assertIsValidName(String name) {
        assertThat(name.matches(EXPECTED_ROBOT_NAME_PATTERN), is(true));
    }
}

class Robot{
	String name;

	public Robot() {
		reset();
	}
	String getName() {
		return name;
	}
	
	Random random = new Random();
	void reset() {
		name = newLetter() + newLetter() + newNumber() + newNumber() + newNumber();
	}

	private String newLetter() {
		String[] a = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");

		String string = a[random.nextInt(a.length)];
		return string;
	}

	private String newNumber() {
		String[] a = "0123456789".split("");

		String string = a[random.nextInt(a.length)];
		return string;
	}
	
}