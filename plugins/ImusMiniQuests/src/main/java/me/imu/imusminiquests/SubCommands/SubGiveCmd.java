package me.imu.imusminiquests.SubCommands;

import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerRarities;
import me.imu.imusminiquests.Quests.PanelRarity;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * /imq give &lt;player&gt; &lt;quest&gt; [amount] [rarity]
 */
public class SubGiveCmd implements CommandInterface
{
    private final CmdData _data;

    public SubGiveCmd(CmdData data)
    {
        _data = data;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length < 3)
        {
            sender.sendMessage(ChatColor.RED + "Usage: " + _data.get_syntaxText());
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null)
        {
            sender.sendMessage(ChatColor.RED + "Player " + args[1] + " is not online");
            return true;
        }

        Quest quest = ImusMiniQuests.getInstance().getQuests().getQuest(args[2]);
        if (quest == null)
        {
            sender.sendMessage(ChatColor.RED + "No quest called " + args[2] + ", see /" + label + " list");
            return true;
        }

        int amount = 1;
        if (args.length > 3)
        {
            try
            {
                amount = Math.max(1, Math.min(64, Integer.parseInt(args[3])));
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(ChatColor.RED + args[3] + " is not a number");
                return true;
            }
        }

        // Without a rarity every panel rolls its own, like found ones
        ManagerRarities rarities = ImusMiniQuests.getInstance().getRarities();
        PanelRarity rarity = null;
        if (args.length > 4)
        {
            if (!rarities.getNames().contains(args[4].toLowerCase(Locale.ROOT)))
            {
                sender.sendMessage(ChatColor.RED + "No rarity called " + args[4] + ", use one of " + rarities.getNames());
                return true;
            }
            rarity = rarities.get(args[4]);
        }

        // Quest items never stack, each one is its own item
        for (int i = 0; i < amount; i++)
        {
            InvUtil.AddItemToInventoryOrDrop(target, rarity == null ? QuestItem.create(quest) : QuestItem.create(quest, rarity));
        }
        sender.sendMessage(Metods.msgC("&9Gave &e" + amount + "&9 x " + quest.getName() + "&9 to &e" + target.getName()));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender sender, String s)
    {
        sender.sendMessage(ChatColor.RED + "Usage: " + _data.get_syntaxText());
    }
}
