package me.imu.imusunstablefearsystem.Inventories;

import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Viewer;
import me.imu.imusunstablefearsystem.Enums.DIFFICULT;
import org.bukkit.Material;

/**
 * Shown at the portal: the player picks fear or no fear for the next days. Closing it gives the portal back.
 */
public class Inv_SelectDifficulty extends Menu
{
	static final int NO_FEAR = 9 + 2;
	static final int FEAR = 9 + 6;

	/**
	 * What picking does on the server.
	 */
	public interface Choice
	{
		void choose(Viewer viewer, DIFFICULT difficult);

		void closed(Viewer viewer);
	}

	private final Choice _choice;
	private boolean _chosen;

	public Inv_SelectDifficulty(Choice choice)
	{
		super("&9Select your difficulty, last 4 days", 3);
		_choice = choice;
	}

	@Override
	public boolean locksOwnInventory()
	{
		return true;
	}

	@Override
	protected void render(Canvas canvas)
	{
		canvas.fill(Icon.filler(Material.BLACK_STAINED_GLASS_PANE));
		canvas.set(NO_FEAR, Icon.of(Material.IRON_HORSE_ARMOR, "&cNO FEAR SYSTEM", DIFFICULT.getLore(DIFFICULT.NO_FEAR)),
				click -> Choose(click, DIFFICULT.NO_FEAR));
		canvas.set(FEAR, Icon.of(Material.DIAMOND_HORSE_ARMOR, "&4FEAR SYSTEM ENABLED", DIFFICULT.getLore(DIFFICULT.FEAR)),
				click -> Choose(click, DIFFICULT.FEAR));
	}

	private void Choose(Click click, DIFFICULT difficult)
	{
		// Once only, also when clicked again before the menu is gone
		if (_chosen)
			return;
		_chosen = true;
		_choice.choose(click.viewer(), difficult);
		click.close();
	}

	@Override
	protected void onClose(Viewer viewer)
	{
		_choice.closed(viewer);
	}
}
