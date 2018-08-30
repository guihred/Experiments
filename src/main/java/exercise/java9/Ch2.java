package exercise.java9;

public class Ch2 {
	/*
	 * 
	 * 1. Change the calendar printing program so it starts the week on a Sunday.
	 * Also make it print a newline at the end (but only one).
	 * 
	 * 2. Consider the nextInt method of the Scanner class. Is it an accessor or
	 * mutator? Why? What about the nextInt method of the Random class?
	 * 
	 * 3. Can you ever have a mutator method return something other than void ? Can
	 * you ever have an accessor method return void ? Give examples when possible.
	 * 
	 * 4. Why can't you implement a Java method that swaps the contents of two int
	 * variables? Instead, write a method that swaps the contents of two IntHolder
	 * objects. (Look up this rather obscure class in the API documentation.) Can
	 * you swap the contents of two Integer objects?
	 * 
	 * 5. Implement an immutable class Point that describes a point in the plane.
	 * Provide a constructor to set it to a specific point, a no-arg constructor to
	 * set it to the origin, and methods getX , getY , translate , and scale . The
	 * translate method moves the point by a given amount in x - and y -direction.
	 * The scale method scales both coordinates by a given factor. Implement these
	 * methods so that they return new points with the results. For example, Click
	 * here to view code image Point p = new Point(3, 4).translate(1, 3).scale(0.5);
	 * should set p to a point with coordinates (2,3.5).
	 * 
	 * 6. Repeat the preceding exercise, but now make translate and scale into
	 * mutators.
	 * 
	 * 7. Add javadoc comments to both versions of the Point class from the
	 * preceding exercises.
	 * 
	 * 8. In the preceding exercises, providing the constructors and getter methods
	 * of the Point class was rather repetitive. Most IDEs have shortcuts for
	 * writing the boilerplate code. What does your IDE offer?
	 * 
	 * 9. Implement a class Car that models a car traveling along the x -axis,
	 * consuming gas as it moves. Provide methods to drive by a given number of
	 * miles, to add a given number of gallons to the gas tank, and to get the
	 * current distance from the origin and fuel level. Specify the fuel efficiency
	 * (in miles/gallons) in the constructor. Should this be an immutable class? Why
	 * or why not?
	 * 
	 * 10. In the RandomNumbers class, provide two static methods randomElement that
	 * get a random element from an array or array list of integers. (Return zero if
	 * the array or array list is empty.) Why couldn't you make these methods into
	 * instance methods of int[] or ArrayList<Integer> ?
	 * 
	 * 11. Rewrite the Cal class to use static imports for the System and LocalDate
	 * classes.
	 * 
	 * 12. Make a file HelloWorld.java that declares a class HelloWorld in a package
	 * ch01.sec01 . Put it into some directory, but not in a ch01/sec01
	 * subdirectory. From that directory, run javac HelloWorld.java . Do you get a
	 * class file? Where? Then run java HelloWorld . What happens? Why? (Hint: Run
	 * javap HelloWorld and study the warning message.) Finally, try javac -d .
	 * HelloWorld.java . Why is that better?
	 * 
	 * 13. Download the JAR file for OpenCSV from http://opencsv.sourceforge.net .
	 * Write a class with a main method that reads a CSV file of your choice and
	 * prints some of the content. There is sample code on the OpenCSV website. You
	 * haven't yet learned to deal with exceptions. Just use the following header
	 * for the main method: Click here to view code image public static void
	 * main(String[] args) throws Exception The point of this exercise is not to do
	 * anything useful with CSV files, but to practice using a library that is
	 * delivered as a JAR file.
	 * 
	 * 14. Compile the Network class. Note that the inner class file is named
	 * Network$Member.class . Use the javap program to spy on the generated code.
	 * The command javap -private Classname displays the methods and instance
	 * variables. Where do you see the reference to the enclosing class? (In
	 * Linux/Mac OS, you need to put a \ before the $ symbol when running javap .)
	 * 
	 * 15. Fully implement the Invoice class in Section 2.6.1 , “Static Nested
	 * Classes ” (page 85). Provide a method that prints the invoice and a demo
	 * program that constructs and prints a sample invoice.
	 * 
	 * 16. Implement a class Queue , an unbounded queue of strings. Provide methods
	 * add , adding at the tail, and remove , removing at the head of the queue.
	 * Store elements as a linked list of nodes. Make Node a nested class. Should it
	 * be static or not?
	 * 
	 * 17. Provide an iterator —an object that yields the elements of the queue in
	 * turn—for the queue of the preceding class. Make Iterator a nested class with
	 * methods next and hasNext . Provide a method iterator() of the Queue class
	 * that yields a Queue.Iterator . Should Iterator be static or not?
	 */
}
