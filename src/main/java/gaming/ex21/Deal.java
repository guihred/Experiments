package gaming.ex21;

import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class Deal extends HBox {
	private final PlayerColor proposer;
	private final ResourceType wantedType;
	private final List<ResourceType> dealTypes;

	public Deal(final PlayerColor proposer, final ResourceType type, final List<ResourceType> dealTypes) {
		this.proposer = proposer;
		wantedType = type;
		this.dealTypes = dealTypes;
		getChildren().addAll(newResource(wantedType), new Text("<->"), newUserImage());
		getChildren().addAll(this.dealTypes.stream().map(Deal::newResource).collect(Collectors.toList()));
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

	private ImageView newUserImage() {
		return CatanResource.newImage(CatanResource.newImage("user.png", getProposer().getColor()),
				Port.SIZE / 4.);
	}

	private static ImageView newResource(final ResourceType type) {
		String pure = type.getPure();
		return CatanResource.newImage(pure, Port.SIZE / 4.);
	}


}
