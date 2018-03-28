package contest.db;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

	public void appendAnswer(String english) {
		answer = Objects.toString(answer, "") + english;
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
    public Integer getKey() {
        return key;
    }

    public Integer getNumber() {
        return number;
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