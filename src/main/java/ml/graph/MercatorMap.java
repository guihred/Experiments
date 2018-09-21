package ml.graph;

public class MercatorMap {

	public static final double DEFAULT_TOP_LATITUDE = 80;
	public static final double DEFAULT_BOTTOM_LATITUDE = -80;
	public static final double DEFAULT_LEFT_LONGITUDE = -180;
	public static final double DEFAULT_RIGHT_LONGITUDE = 180;

	/** Horizontal dimension of this map, in pixels. */
	protected double mapScreenWidth;
	/** Vertical dimension of this map, in pixels. */
	protected double mapScreenHeight;

	/** Northern border of this map, in degrees. */
	protected double topLatitude;
	/** Southern border of this map, in degrees. */
	protected double bottomLatitude;
	/** Western border of this map, in degrees. */
	protected double leftLongitude;
	/** Eastern border of this map, in degrees. */
	protected double rightLongitude;

	private double topLatitudeRelative;
	private double bottomLatitudeRelative;
	private double leftLongitudeRadians;
	private double rightLongitudeRadians;

	public MercatorMap(double mapScreenWidth, double mapScreenHeight) {
		this(mapScreenWidth, mapScreenHeight, DEFAULT_TOP_LATITUDE, DEFAULT_BOTTOM_LATITUDE, DEFAULT_LEFT_LONGITUDE,
				DEFAULT_RIGHT_LONGITUDE);
	}

	/**
	 * Creates a new MercatorMap with dimensions and bounding box to convert between
	 * geo-locations and screen coordinates.
	 *
	 * @param mapScreenWidth
	 *            Horizontal dimension of this map, in pixels.
	 * @param mapScreenHeight
	 *            Vertical dimension of this map, in pixels.
	 * @param topLatitude
	 *            Northern border of this map, in degrees.
	 * @param bottomLatitude
	 *            Southern border of this map, in degrees.
	 * @param leftLongitude
	 *            Western border of this map, in degrees.
	 * @param rightLongitude
	 *            Eastern border of this map, in degrees.
	 */
	public MercatorMap(double mapScreenWidth, double mapScreenHeight, double topLatitude, double bottomLatitude,
			double leftLongitude, double rightLongitude) {
		this.mapScreenWidth = mapScreenWidth;
		this.mapScreenHeight = mapScreenHeight;
		this.topLatitude = topLatitude;
		this.bottomLatitude = bottomLatitude;
		this.leftLongitude = leftLongitude;
		this.rightLongitude = rightLongitude;

		topLatitudeRelative = getScreenYRelative(topLatitude);
		bottomLatitudeRelative = getScreenYRelative(bottomLatitude);
		leftLongitudeRadians = getRadians(leftLongitude);
		rightLongitudeRadians = getRadians(rightLongitude);
	}

	/**
	 * Projects the geo location to Cartesian coordinates, using the Mercator
	 * projection.
	 *
	 * @param geoLocation
	 *            Geo location with (latitude, longitude) in degrees.
	 * @returns The screen coordinates with (x, y).
	 */
	public double[] getScreenLocation(double latitudeInDegrees, double longitudeInDegrees) {

		return new double[] { getScreenX(latitudeInDegrees), getScreenY(longitudeInDegrees) };
	}

    private static double getScreenYRelative(double latitudeInDegrees) {
		return Math.log(Math.tan(latitudeInDegrees / 360f * Math.PI + Math.PI / 4));
	}

	protected double getScreenY(double latitudeInDegrees) {
		return mapScreenHeight * (getScreenYRelative(latitudeInDegrees) - topLatitudeRelative)
				/ (bottomLatitudeRelative - topLatitudeRelative);
	}

    private static double getRadians(double deg) {
		return deg * Math.PI / 180;
	}

	protected double getScreenX(double longitudeInDegrees) {
		double longitudeInRadians = getRadians(longitudeInDegrees);
		return mapScreenWidth * (longitudeInRadians - leftLongitudeRadians)
				/ (rightLongitudeRadians - leftLongitudeRadians);
	}
}
