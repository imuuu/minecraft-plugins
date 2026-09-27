package imu.DontLoseItems.Events;

import java.awt.Color;
import java.util.HashSet;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import imu.DontLoseItems.CustomItems.VoidTotemController;
import imu.DontLoseItems.Managers.Manager_HellArmor;
import imu.iAPI.Other.Metods;
import net.md_5.bungee.api.ChatColor;

public class VoidTotemEvents implements Listener 
{
    private static VoidTotemEvents instance;
    private final VoidTotemController controller;

    private final HashSet<Player> players;
    
    public VoidTotemEvents() 
    {
        instance = this;
        controller = new VoidTotemController();
        players = new HashSet<>();
    }

    public static VoidTotemEvents instance() {
        return instance;
    }
    
    @EventHandler
    public void VoidDamageEvent(EntityDamageEvent e)
    {
        if(e.getEntity() instanceof Player)
        {
            Player player = (Player)e.getEntity();
            if(e.getCause() == EntityDamageEvent.DamageCause.VOID) 
            {
                if(players.contains(player)) {
                    e.setCancelled(true);
                    return;
                }

                if(ValidateTotem(player.getInventory())) {
                    player.playEffect(EntityEffect.TOTEM_RESURRECT);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 1200, 0));
                    players.add(player);
                    controller.findSafeBlock(player);
                }
            }
        }
    }

    private boolean ValidateTotem(PlayerInventory inv) {
        ItemStack offhand = inv.getItemInOffHand();
        ItemStack mainhand = inv.getItemInMainHand();
        
        if(Manager_HellArmor.Instance.IsVoidHelmet(inv.getHelmet())
          && Manager_HellArmor.Instance.IsVoidChestplate(inv.getChestplate())
          && Manager_HellArmor.Instance.IsVoidLeggins(inv.getLeggings())
          && Manager_HellArmor.Instance.IsVoidBoots(inv.getBoots()))
        {
        	return true;
        }
       
        if(GetPersistant(offhand)) 
        {
            inv.setItemInOffHand(new ItemStack(Material.AIR));
            return true;
        } else if(GetPersistant(mainhand)) 
        {
            inv.setItemInMainHand(new ItemStack(Material.AIR));
            return true;
        } else return false;
    }

    private boolean GetPersistant(ItemStack item) 
    {
    	String str = Metods._ins.getPersistenData(item, "totemtype", PersistentDataType.STRING);
    	if(str == null) return false;
    	
    	if(str.matches("void")) return true;
    	
    	return false;

    }

    //recipe event ?
    public void setSaved(Player player) {
        player.removePotionEffect(PotionEffectType.LEVITATION);
        Color clr = new Color(45, 250, 158);
        player.sendMessage(ChatColor.of(clr) + "You were saved by the Void Totem!");
        player.sendMessage(ChatColor.of(clr) + "Be more careful next time!");
        players.remove(player);
    }

   
}
