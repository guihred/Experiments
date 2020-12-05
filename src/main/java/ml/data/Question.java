
package ml.data;

import java.util.Objects;

public class Question {
    private final String colName;
    private final Object ob;
    private final QuestionType type;
    private double infoGain;
    private boolean not;

    public Question(String colName, Object ob, QuestionType type) {
        this.colName = colName;
        this.ob = ob;
        this.type = type;
    }

    public Question(String colName, Object ob, QuestionType type, boolean not) {
        this.colName = colName;
        this.ob = ob;
        this.type = type;
        this.not = not;
    }

    public boolean answer(Object ob1) {
        boolean execute = getType().execute(ob1, ob);
        return not ? !execute : execute;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Question other = (Question) obj;
        return Objects.equals(colName, other.colName) && Objects.equals(not, other.not) && Objects.equals(ob, other.ob)
                && Objects.equals(getType(), other.getType());

    }

    public String getColName() {
        return colName;
    }

    public double getInfoGain() {
        return infoGain;
    }

    public Object getOb() {
        return ob;
    }

    public QuestionType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(colName, ob, getType(), not);
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
        return String.format(string, getColName(), getType().getSign(), ob instanceof String ? "\"" + ob + "\"" : Objects.toString(ob, ""));
    }

}