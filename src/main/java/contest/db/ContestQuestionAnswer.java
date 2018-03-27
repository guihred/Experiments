package contest.db;

import java.io.Serializable;
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
public class ContestQuestionAnswer implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Integer key;

	@Column(length = 5000)
	private String answer;
	@Column
	private Integer number;
	@Column
	private Boolean correct = Boolean.FALSE;
	@ManyToOne
	@JoinColumn
	private ContestQuestion exercise;

	public void appendAnswer(String english) {
		answer = Objects.toString(answer, "") + english;
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
		ContestQuestionAnswer other = (ContestQuestionAnswer) obj;
		return Objects.equals(other.key, key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

}