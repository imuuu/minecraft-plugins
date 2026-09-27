package imu.DontLoseItems.CustomItems;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

import imu.DontLoseItems.Events.VoidTotemEvents;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ImusUtilities;
import net.md_5.bungee.api.ChatColor;

public class VoidTotemController implements Listener 
{
    private static VoidTotemController instance;

    private final String defaultWorld = "World";

    public VoidTotemController() 
    {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, DontLoseItems.Instance);
    }

    public void findSafeBlock(Player player)
    {
        Location mid = player.getLocation();
        World end = mid.getWorld();
        assert end != null;

        new BukkitRunnable()
        {

            @Override
            public void run()
            {
                mid.setY(30.0);
                LinkedList<Location> list = ImusUtilities.CreateSphere(mid, 50, ImusAPI.AirHashSet, null);

                new BukkitRunnable()
                {

                    @Override
                    public void run()
                    {
                        if(list.size() == 0)
                        {
                            Location locc = player.getBedSpawnLocation() != null ? player.getBedSpawnLocation() : Objects.requireNonNull(Bukkit.getServer().getWorld(defaultWorld)).getSpawnLocation();
                            player.teleport(locc);
                            //remove player from active players
                            VoidTotemEvents.instance().setSaved(player);
                            return;
                        }

                        //System.out.println("found block"+ list.get(0).toVector()+" type: "+list.get(0).getBlock());

                        player.teleport(findTop(list.get(0)));

                        //remove player from active players
                        VoidTotemEvents.instance().setSaved(player);

                    }
                }.runTask(DontLoseItems.Instance);
            }
        }.runTaskAsynchronously(DontLoseItems.Instance);

    }

    private Location findTop(Location bottom)
    {
        Location safe = bottom.clone();
        bottom.setY(255.0);

        while(!bottom.getBlock().getType().isSolid())
        {
            bottom.subtract(0,1,0);

            if( bottom.getY() < 0) return safe;
        }
        return bottom;
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

        Metods._ins.InventoryAddItemOrDrop(event.getCurrentItem(), (Player)event.getWhoClicked());
        event.setCurrentItem(null);
        inv.setItem(0, null);
        inv.setItem(1, null);
    }

    public static ItemStack GetVoidtotemItem() 
    {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = item.getItemMeta();
        assert meta != null : "ItemMeta for voidtotem is null!";

        meta.setDisplayName(ChatColor.of(new Color(122, 55, 173)) + "Void Totem");
        meta.setLore(List.of("Why do we fall?", "So we can learn to pick ourselves up."));
        //meta.getPersistentDataContainer().set(new NamespacedKey(DontLoseItems.Instance, "totemtype"), PersistentDataType.STRING, "void");
        item.setItemMeta(meta);
        Metods._ins.setPersistenData(item, "totemtype", PersistentDataType.STRING, "void");
        Metods._ins.AddGlow(item);
        return item;
    }

    public VoidTotemController instance() {
        return instance;
    }
}
