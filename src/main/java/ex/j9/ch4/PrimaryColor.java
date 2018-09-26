package ex.j9.ch4;

/**
 * 6. Suppose that in Section 4.2.2, “The equals Method” (page 148), the
 * Item.equals method uses an instanceof test. Implement DiscountedItem.equals
 * so that it compares only the superclass if otherObject is an Item, but also
 * includes the discount if it is a DiscountedItem. Show that this method
 * preserves symmetry but fails to be transitive—that is, find a combination of
 * items and discounted items so that x.equals(y) and y.equals(z), but not
 * x.equals(z).
 *//** 
 * 7. Define an enumeration type for the eight combinations of primary colors
 * BLACK, RED, BLUE, GREEN, CYAN, MAGENTA, YELLOW, WHITE with methods getRed,
 * getGreen, and getBlue.
 */
public enum PrimaryColor {
    BLACK(0, 0, 0),
    RED(255, 0, 0),
    BLUE(0, 0, 255),
    GREEN(0, 255, 0),
    CYAN(0, 255, 255),
    MAGENTA(255, 0, 255),
    YELLOW(255, 255, 0),
    WHITE(255, 255, 255);
    private int red;
    private int green;
    private int blue;

    PrimaryColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getBlue() {
        return blue;
    }

    public int getGreen() {
        return green;
    }

    public int getRed() {
        return red;
    }
}