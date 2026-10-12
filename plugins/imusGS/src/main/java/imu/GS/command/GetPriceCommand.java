package imu.GS.command;

import imu.GS.text.Messages;
import imu.GS.GsService;
import imu.GS.engine.pricing.Overflow;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Methods;
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
            String line = Messages.get("getprice.line", "item", stack.getType(), "price", Methods.Round(_service.getRules().value(stack)));
            Overflow overflow = _service.getMaterials().get(stack.getType()).getOverflow();
            if (overflow != null)
                line += Messages.get("getprice.drops-after", "amount", overflow.softCap());
            player.sendMessage(Methods.msgC(line));
        }
        return true;
    }

    @Override
    public void FailedMsg(CommandSender sender, String msg)
    {
    }
}
