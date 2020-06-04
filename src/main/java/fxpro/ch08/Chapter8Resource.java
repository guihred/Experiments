package fxpro.ch08;

import java.io.InputStream;
import java.net.URL;
import utils.ResourceFXUtils;

public enum Chapter8Resource {
	TEEN_TITANS("TeenTitans.mp3"),
	MEDIA("media.css");
	private String file;

    Chapter8Resource(String file) {
		this.file = file;
	}

	public String getFile() {
        return file;
    }

	public InputStream getInputStream() {
        return ResourceFXUtils.toStream(file);
	}

    public URL getURL() {
        return ResourceFXUtils.toURL(file);
	}

}
