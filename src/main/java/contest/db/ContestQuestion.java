package contest.db;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table
public class ContestQuestion extends BaseEntity {
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
    private QuestionType type;

    public void appendExercise(String english) {
        exercise = Objects.toString(exercise, "") + english;
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

}