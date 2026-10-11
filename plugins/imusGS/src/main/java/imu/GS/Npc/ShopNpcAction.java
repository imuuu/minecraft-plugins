package imu.GS.Npc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import imu.GS.Main.Main;
import imu.GS.ShopUtl.Shop;
import imu.iAPI.Npc.NpcAction;
import imu.iAPI.Other.Metods;

/** ImusAPI NPC action: opens a general store. The data is the shop's uuid, so renaming the shop keeps the NPC working. */
public class ShopNpcAction implements NpcAction
{
	public static final String ID = "imusgs:shop";

	private final Main _main;

	public ShopNpcAction(Main main)
	{
		_main = main;
	}

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public String getName()
	{
		return "Opens a general store";
	}

	@Override
	public void use(Player player, LivingEntity npc, String data)
	{
		Shop shop = GetShop(data);
		if(shop == null)
		{
			player.sendMessage(Metods.msgC("&9This shop is closed."));
			return;
		}
		shop.Open(player);
	}

	@Override
	public String describe(String data)
	{
		Shop shop = GetShop(data);
		return shop == null ? "&cno shop, it was deleted" : "Shop: &a"+shop.GetName();
	}

	@Override
	public String getDataPrompt()
	{
		return "Shop";
	}

	@Override
	public List<String> getDataHelp()
	{
		List<String> lines = new ArrayList<>();
		lines.add("Name of the shop the NPC opens. Shops:");
		List<Shop> shops = _main.get_shopManager().GetShops();
		for(int i = 0; i < Math.min(10, shops.size()); i++) lines.add("&f"+shops.get(i).GetName());
		if(shops.size() > 10) lines.add("&8... and "+(shops.size()-10)+" more");
		return lines;
	}

	@Override
	public String toEditText(String data)
	{
		Shop shop = GetShop(data);
		return shop == null ? "" : shop.GetName();
	}

	@Override
	public String parseData(String text)
	{
		Shop shop = _main.get_shopManager().GetShop(text);
		if(shop == null) throw new IllegalArgumentException("No shop named "+text);
		return shop.GetUUID().toString();
	}

	@Override
	public String getPermission()
	{
		return "gs.create.npc";
	}

	Shop GetShop(String data)
	{
		if(data == null || data.isEmpty()) return null;
		try
		{
			return _main.get_shopManager().GetShop(UUID.fromString(data));
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
}
