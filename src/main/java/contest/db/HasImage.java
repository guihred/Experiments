package contest.db;

public interface HasImage {
    String getImage();

    void setImage(String image);

    void appendImage(String image);

    boolean matches(String s0);
}
