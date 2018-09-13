package contest.db;

public class QuestionPosition {
    private float x;
    private float y;
    private int page;
    private String line;
    private HasImage entity;

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public String getLine() {
        return line;
    }
    public void setLine(String line) {
        this.line = line;
    }
    public HasImage getEntity() {
        return entity;
    }
    public void setEntity(HasImage entity) {
        this.entity = entity;
    }
}