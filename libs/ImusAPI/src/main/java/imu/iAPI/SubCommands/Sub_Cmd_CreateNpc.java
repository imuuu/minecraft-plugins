package imu.iAPI.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Npc.CommandNpcAction;
import imu.iAPI.Npc.NpcManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ia npc create: an NPC that runs commands, set in its settings menu that opens right away.
 */
public class Sub_Cmd_CreateNpc implements CommandInterface
{
	private final NpcManager _npcs;

	public Sub_Cmd_CreateNpc(NpcManager npcs)
	{
		_npcs = npcs;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		Player player = (Player) sender;
		_npcs.createAndEdit(player, CommandNpcAction.ID, "", "&eNPC");
		return true;
	}

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {

	}
}
