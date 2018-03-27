package contest.db;

import java.io.Serializable;
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
public class ContestQuestion implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Integer key;

	@Column(length = 5000)
	private String exercise;
	@Column
	private Integer number;
	@ManyToOne
	@JoinColumn
	private Contest contest;

	@Enumerated(EnumType.STRING)
	private QuestionType type;

	enum QuestionType {
		OPTIONS,
		TRUE_FALSE;
	}

	public void appendExercise(String english) {
		exercise = Objects.toString(exercise, "") + english;
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
		ContestQuestion other = (ContestQuestion) obj;
		return Objects.equals(other.key, key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

}