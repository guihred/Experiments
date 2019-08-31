package contest.db;

import japstudy.db.BaseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.*;
import utils.HasImage;

@Entity
@Table
public class ContestQuestion extends BaseEntity implements HasImage {
    private static int keyCounter;
    @ManyToOne
    @JoinColumn
    private Contest contest;

    @Column(length = 5000)
    private String exercise;

    @Column(length = 5000)
    private String subject;
    @Id
    private Integer key = keyCounter++;

    @Column
    private Integer number;

    @Enumerated(EnumType.STRING)
    private QuestionType type = QuestionType.OPTIONS;

    @OneToMany(mappedBy = "exercise")
    private List<ContestQuestionAnswer> options;

	@Column(length = 5000)
    private String image;

    public void addOption(ContestQuestionAnswer e) {
        if(options==null) {
            options= new ArrayList<>();
        }
        options.add(e);
        
    }

    public void appendExercise(String english) {
        exercise = Objects.toString(exercise, "") + english;
    }

    @Override
	public void appendImage(String image1) {
        if (image == null) {
			image = image1;
        } else {
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
        if(options==null) {
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

    public QuestionType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
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
    public void setType(QuestionType type) {
        this.type = type;
    }

}