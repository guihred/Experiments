package gaming.ex21;

import javafx.beans.NamedArg;

public class ExtraPoint extends CatanResource {

    private long record;
    private String url;

    public ExtraPoint(@NamedArg("url") String url) {
        super(url);
        this.url = url;
        view.setFitWidth(CatanResource.RADIUS);
        managedProperty().bind(visibleProperty());
    }

    public long getRecord() {
        return record;
    }

    public String getUrl() {
        return url;
    }

    public void setRecord(final long record) {
        this.record = record;
    }

}
