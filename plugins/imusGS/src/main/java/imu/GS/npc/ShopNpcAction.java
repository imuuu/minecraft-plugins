package imu.GS.npc;

import imu.GS.text.Messages;
import imu.GS.GsService;
import imu.GS.engine.shop.Shop;
import imu.iAPI.Npc.NpcAction;
import imu.iAPI.Other.Methods;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ImusAPI NPC action: opens a general store. The data is the shop's id, so renaming the shop keeps the NPC working.
 */
public class ShopNpcAction implements NpcAction
{
    public static final String ID = "imusgs:shop";

    private final GsService _service;

    public ShopNpcAction(GsService service)
    {
        _service = service;
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
        Shop shop = shop(data);
        if (shop == null)
        {
            Messages.send(player, "shop.npc-closed");
            return;
        }
        _service.openShop(player, shop);
    }

    @Override
    public String describe(String data)
    {
        Shop shop = shop(data);
        return shop == null ? "&cno shop, it was deleted" : "Shop: &a" + shop.getName();
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
        List<String> names = _service.getShopNames();
        for (int i = 0; i < Math.min(10, names.size()); i++)
            lines.add("&f" + names.get(i));
        if (names.size() > 10)
            lines.add("&8... and " + (names.size() - 10) + " more");
        return lines;
    }

    @Override
    public String toEditText(String data)
    {
        Shop shop = shop(data);
        return shop == null ? "" : shop.getName();
    }

    @Override
    public String parseData(String text)
    {
        Shop shop = _service.getShop(text);
        if (shop == null)
            throw new IllegalArgumentException("No shop named " + text);
        return shop.getId().toString();
    }

    @Override
    public String getPermission()
    {
        return "gs.create.npc";
    }

    private Shop shop(String data)
    {
        if (data == null || data.isEmpty())
            return null;
        try
        {
            return _service.getShop(UUID.fromString(data));
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
