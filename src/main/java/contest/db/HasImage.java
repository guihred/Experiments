package contest.db;

public interface HasImage {
    String getImage();

    void setImage(String image);

    boolean matches(String s0);
}
