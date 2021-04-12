
package ml.data;

import java.io.File;
import java.util.*;
import utils.ex.PredicateEx;

public class Question implements PredicateEx<Object> {
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

    public boolean isNot() {
        return not;
    }

    public void setInfoGain(double infoGain) {
        this.infoGain = infoGain;
    }

    @Override
    public boolean test(Object input) {
        return answer(input);
    }

    public boolean toggleNot() {
        not = !not;
        return not;
    }

    @Override
    public String toString() {
        String string = not ? "%s not %s %s" : "%s %s %s";
        return String.format(string, getColName(), getType().getSign(),
                ob instanceof String ? "\"" + ob + "\"" : Objects.toString(ob, ""));
    }

    public static DataframeBuilder builderWithQuestions(File file, Iterable<Question> questions) {
        DataframeBuilder builder = DataframeBuilder.builder(file);
        for (Question question : questions) {
            if (question.getType() == QuestionType.DISTINCT) {
                ((Set<?>) question.getOb()).clear();
            }
            builder.filterOut(question.getColName(), question);
        }
        return builder;
    }

    public static Object getQueryObject(DataframeML dataframe2, QuestionType type, String colName, String text2) {
        if (type == QuestionType.DISTINCT) {
            return new LinkedHashSet<>();
        }
        if (type == QuestionType.IN) {
            List<Object> inParameter = new ArrayList<>();
            String[] split = Objects.toString(text2, "").split("[,;\t\n]+");
            for (String string : split) {
                Object tryNumber = DataframeUtils.tryNumber(dataframe2, colName, string);
                inParameter.add(tryNumber);
            }
            return inParameter;
        }
        return DataframeUtils.tryNumber(dataframe2, colName, text2);
    }

    public static Question parseQuestion(DataframeML build, String question) {
        String[] tokens = question.replaceAll("[\",]", "").split(" +");
        if (tokens.length < 2) {
            return null;
        }
        String string = tokens[1];
        boolean not = "not".equals(string);

        QuestionType type = QuestionType.getBySign(not ? tokens[2] : tokens[1]);
        String string2 = type == QuestionType.EMPTY ? null : tokens[tokens.length - 1];
        String colName2 = tokens[0];
        Object queryObject = Question.getQueryObject(build, type, colName2, string2);
        return new Question(colName2, queryObject, type, not);
    }

}