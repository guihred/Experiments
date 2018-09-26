package contest.db;

public class QuestionPosition {
    private float x;
    private float y;
    private int page;
    private String line;
    private HasImage entity;

    public HasImage getEntity() {
        return entity;
    }
    public String getLine() {
        return line;
    }
    public int getPage() {
        return page;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public void setEntity(HasImage entity) {
        this.entity = entity;
    }
    public void setLine(String line) {
        this.line = line;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
}