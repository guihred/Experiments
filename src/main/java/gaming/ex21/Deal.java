package gaming.ex21;

import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import utils.ResourceFXUtils;

public class Deal extends HBox {
	private final PlayerColor proposer;
	private final ResourceType wantedType;
	private final List<ResourceType> dealTypes;

	public Deal(final PlayerColor proposer, final ResourceType type, final List<ResourceType> dealTypes) {
		this.proposer = proposer;
		wantedType = type;
		this.dealTypes = dealTypes;
		getChildren().addAll(newUserImage(), newResource(wantedType), new Text("->"));
		getChildren().addAll(this.dealTypes.stream().map(this::newResource).collect(Collectors.toList()));
	}

	public List<ResourceType> getDealTypes() {
		return dealTypes;
	}

	public PlayerColor getProposer() {
		return proposer;
	}

	public ResourceType getWantedType() {
		return wantedType;
	}

	private ImageView newResource(final ResourceType type) {
		String pure = type.getPure();
		ImageView e = new ImageView(ResourceFXUtils.toExternalForm("catan/" + pure));
		e.setFitWidth(Port.SIZE / 4.);
		e.setPreserveRatio(true);
		return e;
	}

	private ImageView newUserImage() {
		ImageView userImage = new ImageView(CatanResource
				.convertImage(new Image(ResourceFXUtils.toExternalForm("catan/user.png")), getProposer().getColor()));
		userImage.setFitWidth(Port.SIZE / 4);
		userImage.setPreserveRatio(true);
		return userImage;
	}


}
