package contest.db;

import java.util.Objects;
import javax.persistence.*;
import org.apache.commons.lang3.StringUtils;
import utils.BaseEntity;
import utils.HasImage;

@Entity
@Table
public class ContestText extends BaseEntity implements HasImage {
    public static final String TEXTS_PATTERN = ".+ para \\w* *[aà l]*s quest[õion]+es [de ]*(\\d+) [ae] (\\d+)\\.*\\s*";

    @ManyToOne
    @JoinColumn
    private Contest contest;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer key;

    @Column
    private Integer max;
    @Column
    private Integer min;
    @Column(length = 10000)
    private String text;

    @Transient
    private String image;

    public ContestText() {
    }

    public ContestText(Contest contest) {
        this.contest = contest;
    }

    @Override
    public void appendImage(String english) {
        image = Objects.toString(image, "") + english;
    }

    public void appendText(String english) {
        text = Objects.toString(text, "") + english;
    }

    public Contest getContest() {
        return contest;
    }

    @Override
    public String getImage() {
        return image;
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

    @Override
    public boolean matches(String s0) {
        if (StringUtils.isNotBlank(s0) && StringUtils.isNotBlank(text) && text.contains(s0)) {
            return true;
        }

        return hasTexto(s0);
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    @Override
    public void setImage(String image) {
        this.image = image;
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
    public String toString() {
        return String.format("ContestText [contest=%s, key=%d, max=%d, min=%d]", contest, key, max, min);
    }

    public static boolean hasTexto(String s) {
        return s.matches(TEXTS_PATTERN) || s.startsWith("Texto");
    }

}