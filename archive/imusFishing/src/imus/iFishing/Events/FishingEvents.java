package imus.iFishing.Events;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
public class FishingEvents implements Listener
{
	private ImusFishing _main;
	private 
	public FishingEvents()
	{
		Bukkit.getPluginManager().registerEvents(this, ImusFishing.Instance);
		_main = ImusFishing.Instance;
	}

	@EventHandler
	public void onFishing(PlayerFishEvent e)
	{
		if (e.isCancelled())
			return;

		Player player = e.getPlayer();

		if (e.getState() == State.BITE || e.getState() == State.CAUGHT_FISH)
		{
			new BukkitRunnable() {
				@Override
				public void run()
				{
					Interaction hand = getHand(player);
					if (hand == null)
						return;
					doRightClick(player, hand);
				}
			}.runTaskLater(_main, e.getState() == State.BITE ? 5
					: 20);
			return;
		}
	}

	private void doRightClick(Player player, Interaction hand)
	{
		ServerPlayer serverPlayer = null;
		try
		{
			serverPlayer = (ServerPlayer) player.getClass().getMethod("getHandle").invoke(player);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			e.printStackTrace();
		}

		if (main.getConfig().getBoolean("Only_Specific_Rod.Enable"))
		{
			ItemStack item = CraftItemStack.asBukkitCopy(serverPlayer.getItemInHand(hand));
			if (!item.hasItemMeta()
					|| !item.getItemMeta().getPersistentDataContainer().has(main.key, PersistentDataType.BOOLEAN))
				return;
		}

		serverPlayer.gameMode.useItem(serverPlayer, serverPlayer.level(), serverPlayer.getItemInHand(hand), hand);
		serverPlayer.swing(hand, true);
	}

	private Interaction getHand(Player player)
	{
		return player.getInventory().getItemInMainHand().getType().equals(Material.FISHING_ROD)
				? 
				: player.getInventory().getItemInOffHand().getType().equals(Material.FISHING_ROD)
						? InteractionHand.OFF_HAND
						: null;
	}
//	@EventHandler
//	public void OnFishing(PlayerFishEvent e)
//	{
//
//		if(e.getState() != State.BITE) return;;
//		
//		CraftFishHook  hook = (CraftFishHook)e.getHook();
//		
//		EntityFishingHook eHook =  hook.getHandle();
//		Location loc = e.getPlayer().getLocation();
//		ItemStack fishingRod = e.getPlayer().getInventory().getItemInMainHand();
//		int unbLvl = Metods._ins.GetItemStackEnchantCount(fishingRod, Enchantment.DURABILITY);
//
//		double random = ThreadLocalRandom.current().nextDouble(0, 100+1);
//		//System.out.println("random: "+random + " level: "+unbLvl + "chance: "+(unbLvl != 0 ? (100.0/unbLvl) : 0.0));
//		if( random > (unbLvl != 0 ? (100/unbLvl) : 0))
//		{
//			Metods._ins.giveDamage(fishingRod, 1, true);
//		}
//		
//		new BukkitRunnable() {
//			
//			@Override
//			public void run() {
//				
//				eHook.a(CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR)));
//			}
//		}.runTaskLater(ImusFishing._instance, 3);
//		
//		new BukkitRunnable() {
//			
//			@Override
//			public void run() {
//				
//				
//				if(e.getPlayer().getInventory().getItemInMainHand().getType() != Material.FISHING_ROD) return;
//				
//				CraftWorld nmsWorld = (CraftWorld)e.getPlayer().getWorld();
//				
//				EntityHuman eHuman = ((CraftPlayer)e.getPlayer()).getHandle();
//				EntityFishingHook h =  new EntityFishingHook(eHuman,nmsWorld.getHandle(), 0, 0);
//
//				nmsWorld.addEntity(h, null);
//				h.a(loc.getX(), loc.getY(), loc.getZ());
//				
//			}
//		}.runTaskLater(ImusFishing._instance, 30);
//
//	}
}
