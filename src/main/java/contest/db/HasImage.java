package contest.db;

public interface HasImage {
    void appendImage(String image);

    String getImage();

    boolean matches(String s0);

    void setImage(String image);
}
