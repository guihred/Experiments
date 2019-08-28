package gaming.ex21;

public enum CatanAction {
    EXCHANGE,
    MAKE_DEAL,
    THROW_DICE,
    SKIP_TURN,
    ACCEPT_DEAL,

    PLACE_THIEF(),
    PLACE_VILLAGE,
    PLACE_ROAD,
    PLACE_CITY,

    BUY_VILLAGE,
    BUY_ROAD,
    BUY_CITY,
    BUY_DELEVOPMENT,

    SELECT_KNIGHT,
    SELECT_MONOPOLY,
    SELECT_ROAD_BUILDING,
    SELECT_UNIVERSITY,
    SELECT_YEAR_OF_PLENTY,

    SELECT_BRICK,
    SELECT_ROCK,
    SELECT_SHEEP,
    SELECT_WHEAT,
    SELECT_WOOD,

    RESOURCE_BRICK,
    RESOURCE_ROCK,
    RESOURCE_SHEEP,
    RESOURCE_WHEAT,
    RESOURCE_WOOD;

    private CatanAction() {
    }


    public static CatanAction getAction(String prefix, Enum<?> type) {
        CatanAction[] values = values();
        for (CatanAction action : values) {
            if (action.name().contains(prefix) && action.name().contains(type.name())) {
                return action;
            }
        }
        return null;
    }

}