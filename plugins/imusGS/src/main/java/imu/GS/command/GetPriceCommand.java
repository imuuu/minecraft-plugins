package imu.GS.command;

import imu.GS.GsService;
import imu.GS.engine.pricing.Overflow;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * /g getprice [hand|hotbar|inventory]: what the items are worth before any shop's multipliers.
 */
public class GetPriceCommand implements CommandInterface
{
    private final GsService _service;

    public GetPriceCommand(GsService service)
    {
        _service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        Player player = (Player) sender;
        ItemSelection selection = args.length > 1 ? ItemSelection.parse(args[1]) : null;
        if (selection == null)
            selection = ItemSelection.HAND;

        for (ItemStack stack : selection.items(player))
        {
            String line = "&b" + stack.getType() + " &eprice is: &2" + Metods.Round(_service.getRules().value(stack)) + "&2$";
            Overflow overflow = _service.getMaterials().get(stack.getType()).getOverflow();
            if (overflow != null)
                line += " &b&k# &4Price drops after: &2" + overflow.softCap() + " &9items";
            player.sendMessage(Metods.msgC(line));
        }
        return true;
    }

    @Override
    public void FailedMsg(CommandSender sender, String msg)
    {
    }
}
