package me.imu.imuschallenges.SubCommands;

import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.Interfaces.CommandInterface;
import me.imu.imuschallenges.Datas.DataPlayerAdvancements;
import me.imu.imuschallenges.Managers.ManagerAdvancement;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubStatsAdvancements implements CommandInterface {
	private CmdData _data;

	public SubStatsAdvancements(CmdData data) {
		_data = data;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can use this command.");
			return true;
		}

		Player targetPlayer = null;
		boolean showPoints = false;

		if (args.length > 2) {
			// Check if 'points' is the last argument
			showPoints = args[args.length - 1].equalsIgnoreCase("points");

			// Determine the target player
			String playerName = showPoints ? args[2] : args[args.length - 1];
			targetPlayer = Bukkit.getPlayer(playerName);
		}

		if (targetPlayer == null) {
			targetPlayer = (Player) sender;
		}

		DataPlayerAdvancements data = ManagerAdvancement.getInstance().checkPlayerAchievements(targetPlayer);
		data.sendMessage(targetPlayer);

		// If 'points' argument is present, show points
		if (showPoints) {
			targetPlayer.sendMessage("Total Points: " + data.getPoints());
		}

		return true;
	}

	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		// Implementation for failed message
	}
}
