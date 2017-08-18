package japstudy;

import java.net.URL;
import java.util.stream.Stream;

import simplebuilder.ResourceFXUtils;

public enum JapaneseAudio {
	AUDIO_1(1, "jaf01lesson122216.mp3"),
	AUDIO_2(2, "jaf02lesson122216.mp3"),
	AUDIO_3(3, "jaf03lesson122216"),
	AUDIO_4(4, "jaf04lesson122216"),
	AUDIO_5(5, "jaf05lesson032814"),
	AUDIO_6(6, "jaf06lesson122216"),
	AUDIO_7(7, "jaf07lesson122216"),
	AUDIO_8(8, "jaf08lesson122216"),
	AUDIO_9(9, "jaf09lesson122216"),
	AUDIO_10(10, "jaf10lesson122216"),
	AUDIO_11(11, "jaf11lesson122216"),
	AUDIO_12(12, "jaf12lesson122216"),
	AUDIO_13(13, "jaf13lesson122216"),
	AUDIO_14(14, "jaf14lesson122216"),
	AUDIO_15(15, "jaf15lesson122216"),
	AUDIO_16(16, "jaf16lesson122216"),
	AUDIO_17(17, "jaf17lesson122216");
	private final String file;
	private final int lesson;

	private JapaneseAudio(int lesson, String file) {
		this.lesson = lesson;
		this.file = file;
		
	}

	public String getFile() {
		return file;
	}

	public static JapaneseAudio getAudio(int lesson) {
		return Stream.of(values()).filter(e -> e.lesson == lesson).findFirst().orElse(null);
	}

	public URL getURL() {
		return ResourceFXUtils.toURL(file);
	}
	public int getLesson() {
		return lesson;
	}
}
