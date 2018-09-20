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

	public InputStream getInputStream() {
        return ResourceFXUtils.toStream(file);
	}

	public URL getURL() {
        return ResourceFXUtils.toURL(file);
	}

}

//Failed tests:
//  FXTest.test:54
//Class HeatGraphExample threw an exception
//Class HistogramExample threw an exception
//Class Chart3dGraph threw an exception
//
//  FXTest.test2:68
//Class MultilineExample threw an exception
//Class RegressionChartExample threw an exception
//Class PopulacionalPyramidExample threw an exception
//Class PointsExample threw an exception
//
//  FXTest.test3:81
//Class TimelineExample threw an exception
//Class WorldMapExample2 threw an exception
//Class WorldMapExample3 threw an exception
//Class WorldMapExample threw an exception
