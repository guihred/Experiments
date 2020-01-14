package contest.db;

import java.util.Objects;
import javax.persistence.*;
import org.junit.Ignore;
import utils.BaseEntity;

@Entity
@Table
public class ContestQuestionAnswer extends BaseEntity {
	@Column(length = 5000)
	private String answer;

	@Column
	private Boolean correct = Boolean.FALSE;
	@ManyToOne
	@JoinColumn
	private ContestQuestion exercise;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer key;
    @Column
    private Integer number;


    public ContestQuestionAnswer() {
    }

    public ContestQuestionAnswer(String answer, Boolean correct, ContestQuestion exercise, 
        Integer number) {
        this.answer = answer;
        this.correct = correct;
        this.exercise = exercise;
        this.number = number;
    }

	public void appendAnswer(String english) {
		answer = Objects.toString(answer, "") + english;
	}

    @Override
    public boolean equals(Object obj) {
        return obj != null && super.equals(obj) && Objects.equals(((ContestQuestionAnswer) obj).number, number);
    }

    public String getAnswer() {
        return answer;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public ContestQuestion getExercise() {
        return exercise;
    }

    @Override
    @Ignore
    public Integer getKey() {
        return key;
    }

    public Integer getNumber() {
        return number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public void setExercise(ContestQuestion exercise) {
        this.exercise = exercise;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

}