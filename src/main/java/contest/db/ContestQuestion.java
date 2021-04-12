package contest.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.*;
import org.apache.commons.lang3.StringUtils;
import utils.BaseEntity;
import utils.HasImage;
import utils.StringSigaUtils;

@Entity
@Table
public class ContestQuestion extends BaseEntity implements HasImage {
    public static final String QUESTION_PATTERN = " *QUESTÃO +(\\d+)\\s*___+\\s+";

    public static final String QUESTAO = "QUESTÃO";

    @ManyToOne
    @JoinColumn
    private Contest contest;
    @Column(length = 5000)
    private String exercise;

    @Column(length = 5000)
    private String subject;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer key;

    @Column
    private Integer number;

    @Enumerated(EnumType.STRING)
    private ContestQuestionType type = ContestQuestionType.OPTIONS;

    @OneToMany(mappedBy = "exercise")
    private List<ContestQuestionAnswer> options;

    @Column(length = 5000)
    private String image;

    public void addOption(ContestQuestionAnswer e) {
        if (options == null) {
            options = new ArrayList<>();
        }
        options.add(e);
        if (options.size() > 5) {
            getLogger().error("__________________ERROR HERE _____________________________________");
        }
    }

    public void appendExercise(String english) {
        exercise = Objects.toString(exercise, "") + english;
    }

    @Override
    public void appendImage(String image1) {
        if (image == null) {
            image = image1;
        } else if (!image.endsWith(image1)) {
            image += ";" + image1;
        }

    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && super.equals(obj) && ((ContestQuestion) obj).key == key;
    }

    public Contest getContest() {
        return contest;
    }

    public String getExercise() {
        return exercise;
    }

    public String getFormattedOptions() {
        if (options == null) {
            return "";
        }

        return options.stream().map(ContestQuestionAnswer::getAnswer).collect(Collectors.joining("\n\n"));

    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public Integer getKey() {
        return key;
    }

    public Integer getNumber() {
        return number;
    }

    public List<ContestQuestionAnswer> getOptions() {
        return options;
    }

    public String getSubject() {
        return subject;
    }

    public ContestQuestionType getType() {
        return type;
    }

    public boolean hasAnswer() {
        return options != null && options.stream().anyMatch(ContestQuestionAnswer::getCorrect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean matches(String s0) {
        if (StringUtils.isNotBlank(s0) && StringUtils.isNotBlank(exercise) && exercise.contains(s0)) {
            return true;
        }

        boolean matches = isQuestionPattern(s0);
        if (!matches) {
            return false;
        }
        Integer numbers = StringSigaUtils.getApenasNumerosInt(s0);
        return Objects.equals(numbers, number);
    }

    public void setAnswer(char charAt) {
        int index = charAt - 'A';
        if (options != null && index >= 0 && index < options.size()) {
            options.get(index).setCorrect(true);
        } else if (type == ContestQuestionType.TRUE_FALSE) {
            options = new ArrayList<>();
            options.add(new ContestQuestionAnswer("CERTO", charAt == 'C', this, 1));
            options.add(new ContestQuestionAnswer("ERRADO", charAt == 'E', this, 2));
        } else {
            getLogger().error("ANSWER not SET {} {}", this, charAt);
        }
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    @Override
    public void setImage(String image) {
        this.image = image;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setOptions(List<ContestQuestionAnswer> options) {
        this.options = options;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setType(ContestQuestionType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("Question(subject=%s, key=%s, number=%s, type=%s)", subject, key, number, type);
    }

    public static boolean isQuestionPattern(String s) {
        return s != null && (s.matches(QUESTION_PATTERN) || s.startsWith(QUESTAO));
    }

}