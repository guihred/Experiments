package fxproexercises.ch08;

import java.io.InputStream;
import java.net.URL;

public enum Chapter8Resource {
	TEEN_TITANS("TeenTitans.mp3"),
	MEDIA("media.css");
	private String file;

	private Chapter8Resource(String file) {
		this.file = file;
	}

	public InputStream getInputStream() {
		return getClass().getResourceAsStream(file);
	}

	public URL getURL() {
		return getClass().getResource(file);
	}

}
