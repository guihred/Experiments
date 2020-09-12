
package ml.data;

public class Question {
    private final String colName;
    private final Object ob;
    private QuestionType type = QuestionType.EQ;
    private double infoGain;

    public Question(String colName, Object ob, QuestionType type) {
        this.colName = colName;
        this.ob = ob;
        this.type = type;
    }

    public boolean answer(Object ob1) {
        return type.execute(ob1, ob);
    }

    public String getColName() {
        return colName;
    }

    public double getInfoGain() {
        return infoGain;
    }

    public void setInfoGain(double infoGain) {
        this.infoGain = infoGain;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", getColName(), type.getSign(), ob instanceof String ? "\"" + ob + "\"" : ob);
    }

}