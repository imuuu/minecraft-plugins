package imu.GS.SubCmds;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import imu.GS.Main.Main;
import imu.GS.Npc.ShopNpcAction;
import imu.GS.Other.CmdData;
import imu.GS.ShopUtl.Shop;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

/** /gs create npc &lt;shop name&gt;: spawns an ImusAPI NPC that opens the shop, and opens its settings. */
public class SubCreateNpcCMD implements CommandInterface
{
	private final Main _main;
	private final CmdData _data;

	public SubCreateNpcCMD(Main main, CmdData data)
	{
		_main = main;
		_data = data;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		Player player = (Player) sender;
		if(args.length < 3)
		{
			player.sendMessage(_data.get_syntaxText());
			return false;
		}

		String shopName = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).replace("\"", "");
		Shop shop = _main.get_shopManager().GetShop(shopName);
		if(shop == null)
		{
			player.sendMessage(Metods.msgC("&cShop not found: &9"+shopName));
			return false;
		}

		ImusAPI._instance.GetNpcManager().createAndEdit(player, ShopNpcAction.ID, shop.GetUUID().toString(), shop.GetDisplayName());
		player.sendMessage(Metods.msgC("&aShop NPC for &9"+shop.GetName()+" &acreated. &7Sneak + click it later to change it."));
		return true;
	}

	@Override
	public void FailedMsg(CommandSender sender, String msg)
	{

	}
}
