package me.imu.imuschallenges.SubCommands;

import imu.iAPI.Utilities.TextUtils;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;
import me.imu.imuschallenges.Shop.ChallengeShopNpcAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ic create npc: spawns an ImusAPI NPC that opens the challenge shop, and opens its settings.
 */
public class SubCreateNpcCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        Player player = (Player) sender;
        ImusAPI._instance.GetNpcManager().createAndEdit(player, ChallengeShopNpcAction.ID, "", "&6Challenge Shop");
        player.sendMessage(TextUtils.Color("&aChallenge shop NPC created. &7Sneak + click it later to change it."));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
