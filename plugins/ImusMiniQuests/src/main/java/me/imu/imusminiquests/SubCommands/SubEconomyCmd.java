package me.imu.imusminiquests.SubCommands;

import imu.iAPI.Utilities.TextUtils;
import imu.iAPI.Interfaces.CommandInterface;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * /imq economy [refresh]: the numbers percent money rewards are based on.
 */
public class SubEconomyCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        ManagerEconomy economy = ImusMiniQuests.getInstance().getEconomy();
        if (!ManagerEconomy.isAvailable())
        {
            sender.sendMessage(TextUtils.Color("&cNo economy found, money rewards need Vault and an economy plugin"));
            return true;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("refresh"))
        {
            economy.survey();
            sender.sendMessage(TextUtils.Color("&9Counting balances again, check back in a moment"));
            return true;
        }

        if (economy.getSurveyedAt() == 0)
        {
            sender.sendMessage(TextUtils.Color("&7Balances haven't been counted yet, try again in a moment"));
            return true;
        }

        long minutesAgo = (System.currentTimeMillis() - economy.getSurveyedAt()) / 60_000;
        sender.sendMessage(TextUtils.Color("&6MiniQuests economy &8(counted " + minutesAgo + " min ago)"));
        sender.sendMessage(TextUtils.Color("&7Players counted: &e" + economy.getCounted()));
        sender.sendMessage(TextUtils.Color("&7Total: &e" + ManagerEconomy.format(economy.getTotal())));
        sender.sendMessage(TextUtils.Color("&7Average: &e" + ManagerEconomy.format(economy.getAverage())));
        sender.sendMessage(TextUtils.Color("&7Median: &e" + ManagerEconomy.format(economy.getMedian())));
        sender.sendMessage(TextUtils.Color("&7Percent rewards use: &e" + economy.getBasis()
                + " &8(10% = " + ManagerEconomy.format(economy.clamp(economy.getBasisAmount() * 0.1)) + ")"));
        sender.sendMessage(TextUtils.Color("&8/" + commandLabel + " economy refresh to count again now"));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
