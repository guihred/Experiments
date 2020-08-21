
package ml.data;

import java.util.Collection;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import utils.PredicateEx;

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

    public boolean answer(Object test) {
        switch (type) {
            case EQ:
                return Objects.equals(test, ob) || Objects.equals(test, Objects.toString(ob));
            case NE:
                return !Objects.equals(test, ob) && !Objects.equals(test, Objects.toString(ob));
            case CONTAINS:
                return ob instanceof Collection ? ((Collection<?>) ob).contains(test)
                        : StringUtils.containsIgnoreCase(Objects.toString(test), Objects.toString(ob));
            case LIKE:
                return PredicateEx.test(s -> s.matches(Objects.toString(ob)), Objects.toString(test));
            default:
                break;
        }
        if (!(test instanceof Number) || !(ob instanceof Number)) {
            return false;
        }
        double number0 = ((Number) test).doubleValue();
        double number1 = ((Number) ob).doubleValue();
        switch (type) {
            case GE:
                return number0 >= number1;
            case GT:
                return number0 > number1;
            case LE:
                return number0 <= number1;
            case LT:
                return number0 < number1;
            default:
                return false;
        }
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