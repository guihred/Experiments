package ex.j9;

import ex.j9.Ch3.Measurable;
import java.util.Arrays;
import java.util.List;

public class Employee implements Measurable {
    private static int i;
    private static final List<String> NAMES = Arrays.asList("Michael", "Charlie", "Jonas", "Margareth", "Juliet",
        "Frank", "Harry");

    private String name;
    private double salary;

    public Employee(double salary) {
        setSalary(salary);
        setName(NAMES.get(i++ % NAMES.size()));
    }


    @Override
    public double getMeasure() {
        return getSalary();
    }

    public String getName() {
        return name;
    }

    public double getSalary() {
        return salary;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return name + " " + salary + "  ";
    }
}