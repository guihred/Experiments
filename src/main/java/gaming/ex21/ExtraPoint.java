package gaming.ex21;

public class ExtraPoint extends CatanResource {

	private long record;

	public ExtraPoint(final String url) {
		super("catan/" + url);
		view.setFitWidth(Terrain.RADIUS);
		managedProperty().bind(visibleProperty());
	}

	public long getRecord() {
		return record;
	}

	public void setRecord(final long record) {
		this.record = record;
	}

}
