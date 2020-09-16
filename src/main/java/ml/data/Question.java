
package ml.data;

public class Question {
    private final String colName;
    private final Object ob;
    private QuestionType type = QuestionType.EQ;
    private double infoGain;
    private boolean not = false;

    public Question(String colName, Object ob, QuestionType type) {
        this.colName = colName;
        this.ob = ob;
        this.type = type;
    }

    public boolean answer(Object ob1) {
        boolean execute = type.execute(ob1, ob);
        return not ? !execute : execute;
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

    public boolean toggleNot() {
        not = !not;
        return not;
    }

    @Override
    public String toString() {
        String string = not ? "%s not %s %s" : "%s %s %s";
        return String.format(string, getColName(), type.getSign(), ob instanceof String ? "\"" + ob + "\"" : ob);
    }

}