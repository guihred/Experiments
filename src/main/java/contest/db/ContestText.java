package contest.db;

import java.text.MessageFormat;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table
public class ContestText extends BaseEntity implements HasImage {
    private static int KEY;
    @ManyToOne
    @JoinColumn
    private Contest contest;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer key = KEY++;

    @Column
    private Integer max;

    @Column
    private Integer min;
    @Column(length = 5000)
    private String text;
    @Transient
    private String image;

    public ContestText() {
    }

    public ContestText(Contest contest) {
        this.contest = contest;
    }

    @Override
    public String toString() {
        return MessageFormat.format("ContestText [contest={0}, key={1}, max={2}, min={3}]", contest, key, max, min);
    }

    public void appendText(String english) {
        text = Objects.toString(text, "") + english;
    }

    @Override
    public void appendImage(String english) {
        image = Objects.toString(image, "") + english;
    }
    public Contest getContest() {
        return contest;
    }

    @Override
    public Integer getKey() {
        return key;
    }

    public Integer getMax() {
        return max;
    }

    public Integer getMin() {
        return min;
    }

    public String getText() {
        return text;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public void setText(String text) {
        this.text = text;
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
    public boolean matches(String s0) {
        return s0.matches(ContestReader.TEXTS_PATTERN);
    }

}