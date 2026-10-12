package imu.GS.command;

import imu.GS.GsService;
import imu.GS.engine.shop.Shop;
import imu.GS.npc.ShopNpcAction;
import imu.GS.presets.ShopPreset;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Npc.NpcManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import static imu.GS.command.CommandText.rest;
import static imu.GS.command.CommandText.send;

/**
 * /gs presets, /gs create preset &lt;preset&gt; [name] and /gs village: shops made from presets, with their NPCs.
 */
public class VillageCommands
{
    private final GsService _service;

    public VillageCommands(GsService service)
    {
        _service = service;
    }

    /**
     * /gs presets
     */
    public void presets(Player player, String[] args)
    {
        send(player, "&9Shop presets &7(plugins/imusGS/presets):");
        for (ShopPreset preset : _service.getPresets().all())
        {
            Shop existing = _service.getShop(preset.name());
            send(player, "&7  " + preset.id() + " &r" + preset.displayName() + (preset.buyOnly() ? " &7(buy only)" : " &7(buys and sells)")
                    + (existing != null ? " &a- made" : ""));
        }
        send(player, "&7/gs create preset <preset> [name] makes one where you stand, /gs village all of them around you");
    }

    /**
     * /gs create preset &lt;preset&gt; [name]: the shop and its NPC where the player stands.
     */
    public void createPreset(Player player, String[] args)
    {
        ShopPreset preset = args.length >= 3 ? _service.getPresets().get(args[2]) : null;
        if (preset == null)
        {
            send(player, "&c/gs create preset <" + String.join("|", _service.getPresets().ids()) + "> [name]");
            return;
        }
        String name = rest(args, 3);
        if (name.isEmpty())
            name = freeName(preset.name());
        else if (_service.getShop(name) != null)
        {
            send(player, "&cA shop named " + name + " already exists");
            return;
        }
        Location here = player.getLocation();
        here.setPitch(0);
        Shop shop = make(preset, name, here, null);
        send(player, "&aMade &r" + shop.getDisplayName() + " &aand its NPC. &7Sneak + click the NPC to change it.");
    }

    /**
     * /gs village: every preset that has no shop yet, in an arc in front of the player, the NPCs facing them.
     */
    public void village(Player player, String[] args)
    {
        List<ShopPreset> missing = new ArrayList<>();
        for (ShopPreset preset : _service.getPresets().all())
        {
            if (_service.getShop(preset.name()) == null)
                missing.add(preset);
        }
        if (missing.isEmpty())
        {
            send(player, "&eEvery preset already has its shop. &7/gs create preset <preset> <name> makes another.");
            return;
        }

        Location center = player.getLocation();
        double radius = Math.max(6, missing.size() * 1.3);
        double spread = Math.min(200, 25.0 * (missing.size() - 1));
        List<String> made = new ArrayList<>();
        for (int i = 0; i < missing.size(); i++)
        {
            double angle = missing.size() == 1 ? 0 : -spread / 2 + spread * i / (missing.size() - 1);
            Vector direction = center.getDirection().setY(0).normalize().rotateAroundY(Math.toRadians(-angle)).multiply(radius);
            Location spot = ground(center.clone().add(direction));
            Shop shop = make(missing.get(i), missing.get(i).name(), spot, center);
            made.add(shop.getDisplayName());
        }
        send(player, "&aThe village has " + made.size() + " new shops: &r" + String.join("&7, &r", made));
        send(player, "&7Sneak + click an NPC to move it or change it.");
    }

    /**
     * @param faceTo where the NPC looks when nobody is near, null for the spot's own direction
     */
    /**
     * /gs remove shop &lt;name&gt; confirm: the shop, its stock and the NPCs that open it.
     */
    public void removeShop(CommandSender sender, String[] args)
    {
        boolean confirm = args.length > 3 && args[args.length - 1].equalsIgnoreCase("confirm");
        String name = rest(confirm ? Arrays.copyOf(args, args.length - 1) : args, 2);
        Shop shop = _service.getShop(name);
        if (shop == null)
        {
            send(sender, "&cShop not found: &9" + name);
            return;
        }
        if (!confirm)
        {
            send(sender, "&cThis removes " + shop.getName() + ", everything in it and its NPCs. &eAdd &aconfirm &eto the end.");
            return;
        }
        NpcManager npcs = ImusAPI._instance.GetNpcManager();
        String id = shop.getId().toString();
        int removed = 0;
        for (World world : Bukkit.getWorlds())
        {
            for (LivingEntity entity : world.getLivingEntities())
            {
                if (npcs.isNpc(entity) && ShopNpcAction.ID.equals(npcs.getActionId(entity)) && id.equals(npcs.getData(entity)))
                {
                    entity.remove();
                    removed++;
                }
            }
        }
        _service.deleteShop(shop);
        send(sender, "&eRemoved " + shop.getName() + " and " + removed + " NPC" + (removed == 1 ? "" : "s"));
    }

    private Shop make(ShopPreset preset, String name, Location spot, Location faceTo)
    {
        String displayName = name.equals(preset.name()) ? preset.displayName() : name;
        Shop shop = _service.createShop(preset, name, displayName);

        NpcManager npcs = ImusAPI._instance.GetNpcManager();
        LivingEntity npc = npcs.spawn(NpcManager.NpcType.VILLAGER, spot, ShopNpcAction.ID, shop.getId().toString(), npcName(preset));
        if (npc instanceof Villager villager)
        {
            Villager.Profession profession = NpcManager.professions().get(NamespacedKey.minecraft(preset.profession().toLowerCase(Locale.ROOT)));
            if (profession != null)
                npcs.setProfession(villager, profession);
        }
        if (faceTo != null)
            npcs.face(npc, faceTo);
        _service.getPlugin().updateTabCompletion();
        return shop;
    }

    /**
     * "&amp;6Bjorn &amp;7the Blacksmith": one of the preset's names and its title.
     */
    private static String npcName(ShopPreset preset)
    {
        if (preset.npcNames().isEmpty())
            return preset.displayName();
        String name = preset.npcNames().get(ThreadLocalRandom.current().nextInt(preset.npcNames().size()));
        return "&6" + name + (preset.npcTitle().isEmpty() ? "" : " " + preset.npcTitle());
    }

    private String freeName(String name)
    {
        if (_service.getShop(name) == null)
            return name;
        for (int i = 2; ; i++)
        {
            if (_service.getShop(name + " " + i) == null)
                return name + " " + i;
        }
    }

    /**
     * The spot on the ground nearest to the height of the location: up out of solid blocks, down to the floor.
     */
    private static Location ground(Location spot)
    {
        Location loc = spot.clone();
        loc.setX(loc.getBlockX() + 0.5);
        loc.setZ(loc.getBlockZ() + 0.5);
        loc.setY(loc.getBlockY());
        for (int i = 0; i < 6 && isSolid(loc.getBlock()); i++)
            loc.add(0, 1, 0);
        for (int i = 0; i < 8 && !isSolid(loc.getBlock().getRelative(0, -1, 0)); i++)
            loc.add(0, -1, 0);
        return loc;
    }

    private static boolean isSolid(Block block)
    {
        return block.getType().isSolid();
    }
}
