package gaming.ex21;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
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
        return CatanResource.newImage(CatanResource.USER_PNG, getProposer(), Port.SIZE / 4.);
	}

    public static boolean isDealUnfeasible(Deal deal, ObjectProperty<PlayerColor> currentPlayer2,
        Map<PlayerColor, List<CatanCard>> cards2) {
        PlayerColor proposer = deal.getProposer();
        return currentPlayer2.get() == proposer
            || cards2.get(currentPlayer2.get()).stream().noneMatch(e -> e.getResource() == deal.getWantedType())
            || !CatanCard.containsEnough(cards2.get(proposer), deal.getDealTypes());
    }

    private static ImageView newResource(final ResourceType type) {
		return CatanResource.newImage(type.getPure(), Port.SIZE / 4.);
	}
}
