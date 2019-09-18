package japstudy;

import java.time.LocalTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import utils.BaseEntity;

@Entity
@Table
public class JapaneseLesson extends BaseEntity {
	@Column(length = 2000)
	private String english;
	@Column(length = 2000)
	private String japanese;

	private LocalTime start;
	private LocalTime end;
    @Column(length = 2000)
    private String audio;

	@EmbeddedId
	private LessonPK pk = new LessonPK();

	@Column(length = 2000)
	private String romaji;

	public void addEnglish(String english1) {
		english = Objects.toString(english, "") + english1;
	}

	public void addJapanese(String japanese1) {
		japanese = Objects.toString(japanese, "") + japanese1.trim();
	}

	public void addRomaji(String romaji1) {
		romaji = Objects.toString(romaji, "") + romaji1.trim();
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

    public String getAudio() {
        return audio;
    }
	public LocalTime getEnd() {
		return end;
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

	@Override
    public LessonPK getKey() {
        return pk;
    }

	public Integer getLesson() {
		return pk.getLesson();
	}

	public String getRomaji() {
		return romaji;
	}

	public LocalTime getStart() {
		return start;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pk);
	}

	public void setAudio(String audio) {
        this.audio = audio;
    }

	public void setEnd(LocalTime end) {
		this.end = end;
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

    public void setStart(LocalTime start) {
		this.start = start;
	}

    @Override
	public String toString() {
		return String.format("JapaneseLesson [english=%s, exercise=%d, japanese=%s, romaji=%s]", english,
				pk.getExercise(), japanese, romaji);
	}
	
}