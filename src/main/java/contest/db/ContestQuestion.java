package contest.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table
public class ContestQuestion extends BaseEntity implements HasImage {
    private static int KEY;
    @ManyToOne
    @JoinColumn
    private Contest contest;

    @Column(length = 5000)
    private String exercise;

    @Column(length = 5000)
    private String subject;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer key = KEY++;

    @Column
    private Integer number;

    @Enumerated(EnumType.STRING)
    private QuestionType type = QuestionType.OPTIONS;

    @OneToMany(mappedBy = "exercise")
    private List<ContestQuestionAnswer> options;

    private String image;

    public void appendExercise(String english) {
        exercise = Objects.toString(exercise, "") + english;
    }

    public String getFormattedOptions() {
        if(options==null) {
            return "";
        }
        
        return options.stream().map(ContestQuestionAnswer::getAnswer).collect(Collectors.joining("\n\n"));
        
    }

    public void addOption(ContestQuestionAnswer e) {
        if(options==null) {
            options= new ArrayList<>();
        }
        options.add(e);
        
    }

    public Contest getContest() {
        return contest;
    }

    public String getExercise() {
        return exercise;
    }

    @Override
    public Integer getKey() {
        return key;
    }

    public Integer getNumber() {
        return number;
    }

    public QuestionType getType() {
        return type;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<ContestQuestionAnswer> getOptions() {
        return options;
    }

    public void setOptions(List<ContestQuestionAnswer> options) {
        this.options = options;
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public void appendImage(String image) {
        if (this.image == null) {
            this.image = image;
        } else {
            this.image += ";" + image;
        }

    }
    @Override
    public boolean matches(String s0) {
        boolean matches = s0.matches(ContestReader.QUESTION_PATTERN);
        if (!matches) {
            return false;
        }
        String split = s0.replaceAll(ContestReader.QUESTION_PATTERN, "$1");
        return split.equals(number + "");
    }

}