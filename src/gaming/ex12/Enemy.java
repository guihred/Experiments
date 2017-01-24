package gaming.ex12;

public interface Enemy {

	void attack(Player player);

	boolean isClose(Player player);

}
