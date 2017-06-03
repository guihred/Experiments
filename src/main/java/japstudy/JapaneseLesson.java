package japstudy;

import java.util.Objects;

public class JapaneseLesson {
	private String english;
	private Integer exercise;
	private String japanese;
	private Integer lesson;
	private String romaji;

	public String getEnglish() {
		return english;
	}

	public void addEnglish(String english) {
		this.english = Objects.toString(this.english, "") + english;
	}

	public void addJapanese(String japanese) {
		this.japanese = Objects.toString(this.japanese, "") + japanese.trim();
	}

	public void addRomaji(String romaji) {
		this.romaji = Objects.toString(this.romaji, "") + romaji.trim();
	}

	public Integer getExercise() {
		return exercise;
	}

	public String getJapanese() {
		return japanese;
	}

	public Integer getLesson() {
		return lesson;
	}

	public String getRomaji() {
		return romaji;
	}

	public void setEnglish(String english) {
		this.english = english;
	}

	public void setExercise(Integer exercise) {
		this.exercise = exercise;
	}

	public void setJapanese(String japanese) {
		this.japanese = japanese;
	}

	public void setLesson(Integer lesson) {
		this.lesson = lesson;
	}

	public void setRomaji(String romaji) {
		this.romaji = romaji;
	}

	@Override
	public String toString() {
		return "JapaneseLesson [english=" + english + ", exercise=" + exercise + ", japanese=" + japanese + ", romaji="
				+ romaji + "]";
	}
	
}