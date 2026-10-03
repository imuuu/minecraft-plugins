package imu.iAPI.Events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

// A BlockBreakEvent fired only to ask protection plugins (claims, regions) whether the player may
// break the block. The player isn't mining it, so the tool in their hand has nothing to do with it:
// listeners that act on the held tool (mining enchants, tool abilities) should skip this event.
// Has no handler list of its own, so it reaches every BlockBreakEvent listener.
public class FakeBlockBreakEvent extends BlockBreakEvent
{
	public FakeBlockBreakEvent(Block block, Player player)
	{
		super(block, player);
	}
}
