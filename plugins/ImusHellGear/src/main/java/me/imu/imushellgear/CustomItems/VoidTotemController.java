package me.imu.imushellgear.CustomItems;

import java.awt.Color;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import me.imu.imushellgear.Events.VoidTotemEvents;
import me.imu.imushellgear.ImusHellGear;
import imu.iAPI.Other.Metods;
import net.md_5.bungee.api.ChatColor;

public class VoidTotemController implements Listener 
{
    private static VoidTotemController instance;

    private static final int SEARCH_RADIUS = 50;
    private static final int SEARCH_STEP = 2;

    public VoidTotemController()
    {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, ImusHellGear.Instance);
    }

    public void findSafeBlock(Player player)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Location safe = findNearestGround(player.getLocation(), SEARCH_RADIUS, SEARCH_STEP);

                if(safe == null)
                {
                    safe = player.getRespawnLocation() != null ? player.getRespawnLocation() : Bukkit.getWorlds().get(0).getSpawnLocation();
                }

                player.teleport(safe);

                //remove player from active players
                VoidTotemEvents.instance().setSaved(player);
            }
        }.runTask(ImusHellGear.Instance);
    }

    // Walks square rings outward from the player, closest ring first. Each column is one
    // heightmap lookup and unloaded chunks are skipped, so this never scans or loads blocks
    // and is cheap enough for the main thread (at most ~2600 lookups with radius 50, step 2).
    private Location findNearestGround(Location center, int radius, int step)
    {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cz = center.getBlockZ();

        Location ground = getGround(world, cx, cz, center);
        if(ground != null) return ground;

        for(int r = step; r <= radius; r += step)
        {
            for(int i = -r; i <= r; i += step)
            {
                if((ground = getGround(world, cx + i, cz - r, center)) != null) return ground;
                if((ground = getGround(world, cx + i, cz + r, center)) != null) return ground;
            }
            for(int i = -r + step; i <= r - step; i += step)
            {
                if((ground = getGround(world, cx - r, cz + i, center)) != null) return ground;
                if((ground = getGround(world, cx + r, cz + i, center)) != null) return ground;
            }
        }
        return null;
    }

    // Standing spot on top of the column's highest solid block, or null for empty/unloaded columns
    private Location getGround(World world, int x, int z, Location facing)
    {
        if(!world.isChunkLoaded(x >> 4, z >> 4)) return null;

        Block top = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        if(top.getY() <= world.getMinHeight() || !top.getType().isSolid()) return null;

        Location loc = top.getLocation().add(0.5, 1, 0.5);
        loc.setYaw(facing.getYaw());
        loc.setPitch(facing.getPitch());
        return loc;
    }
    
    @EventHandler
    public void onVoidTotemAnvil(PrepareAnvilEvent event) 
    {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);
        ItemStack result;

        if(first == null || second == null) return;

        if(first.getType() == Material.TOTEM_OF_UNDYING && second.getType() == Material.ELYTRA) {
            result = GetVoidtotemItem();
            event.setResult(result);
        }
    }

    @EventHandler
    public void onVoidTotemAnvilCraft(InventoryClickEvent event) 
    {
        if(event.isCancelled()) return;
        if(event.getClickedInventory() == null) return;
        if(event.getClickedInventory().getType() != org.bukkit.event.inventory.InventoryType.ANVIL) return;
        if(event.getSlotType() != org.bukkit.event.inventory.InventoryType.SlotType.RESULT) return;

        AnvilInventory inv = (AnvilInventory) event.getClickedInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);
        ItemStack result = inv.getItem(2);

        if(first == null || second == null || result == null) return;
        if(!IsVoidTotem(result)) return;

        Metods._ins.InventoryAddItemOrDrop(event.getCurrentItem(), (Player)event.getWhoClicked());
        event.setCurrentItem(null);
        inv.setItem(0, null);
        inv.setItem(1, null);
    }

    public static boolean IsVoidTotem(ItemStack item)
    {
        return "void".equals(Metods._ins.getPersistenData(item, "totemtype", PersistentDataType.STRING));
    }

    public static ItemStack GetVoidtotemItem()
    {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = item.getItemMeta();
        assert meta != null : "ItemMeta for voidtotem is null!";

        meta.setDisplayName(ChatColor.of(new Color(122, 55, 173)) + "Void Totem");
        meta.setLore(List.of("Why do we fall?", "So we can learn to pick ourselves up."));
        //meta.getPersistentDataContainer().set(new NamespacedKey(ImusHellGear.Instance, "totemtype"), PersistentDataType.STRING, "void");
        item.setItemMeta(meta);
        Metods._ins.setPersistenData(item, "totemtype", PersistentDataType.STRING, "void");
        Metods._ins.AddGlow(item);
        return item;
    }

    public VoidTotemController instance() {
        return instance;
    }
}
