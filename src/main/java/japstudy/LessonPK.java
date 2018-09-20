package japstudy;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LessonPK implements Serializable {
	@Column
	private Integer exercise;
	@Column
	private Integer lesson;

	@Override
	public int hashCode() {
		return Objects.hash(lesson, exercise);
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
		LessonPK other = (LessonPK) obj;
		return Objects.equals(exercise, other.exercise) && Objects.equals(lesson, other.lesson);
	}
	public Integer getExercise() {
		return exercise;
	}
	public void setExercise(Integer exercise) {
		this.exercise = exercise;
	}
	public Integer getLesson() {
		return lesson;
	}
	public void setLesson(Integer lesson) {
		this.lesson = lesson;
	}

}