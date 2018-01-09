package japstudy.db;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
public class JapaneseLesson implements Serializable {
	@Column(length = 2000)
	private String english;
	@Column(length = 2000)
	private String japanese;

	private LocalTime start;
	private LocalTime end;

	@EmbeddedId
	private LessonPK pk = new LessonPK();

	@Column(length = 2000)
	private String romaji;

	public void addEnglish(String english) {
		this.english = Objects.toString(this.english, "") + english;
	}
	public void addJapanese(String japanese) {
		this.japanese = Objects.toString(this.japanese, "") + japanese.trim();
	}

	public void addRomaji(String romaji) {
		this.romaji = Objects.toString(this.romaji, "") + romaji.trim();
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
		JapaneseLesson other = (JapaneseLesson) obj;

		return Objects.equals(other.pk, pk);
	}

	public String getEnglish() {
		return english;
	}

	public Integer getExercise() {
		return pk.getExercise();
	}

	public String getJapanese() {
		return japanese;
	}

	public Integer getLesson() {
		return pk.getLesson();
	}

	public LessonPK getPk() {
		return pk;
	}

	public String getRomaji() {
		return romaji;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pk);
	}

	public void setEnglish(String english) {
		this.english = english;
	}

	public void setExercise(Integer exercise) {
		pk.setExercise(exercise);
	}

	public void setJapanese(String japanese) {
		this.japanese = japanese;
	}

	public void setLesson(Integer lesson) {
		pk.setLesson(lesson);
	}

	public void setPk(LessonPK pk) {
		this.pk = pk;
	}

	public void setRomaji(String romaji) {
		this.romaji = romaji;
	}

	@Override
	public String toString() {
		return String.format("JapaneseLesson [english=%s, exercise=%d, japanese=%s, romaji=%s]", english,
				pk.getExercise(), japanese, romaji);
	}

	public LocalTime getStart() {
		return start;
	}

	public void setStart(LocalTime start) {
		this.start = start;
	}

	public LocalTime getEnd() {
		return end;
	}

	public void setEnd(LocalTime end) {
		this.end = end;
	}
	
}