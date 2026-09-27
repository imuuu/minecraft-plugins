package imu.DontLoseItems.Events;

import static java.util.Arrays.stream;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Other.Metods;

public class ElytraGenerationEvents implements Listener
{

	private int elytraChance = 25;
	private ItemStack replacement = new ItemStack(Material.DIAMOND_BLOCK, 1);

	public ElytraGenerationEvents()
	{
		// TODO get elytrachance from config
	}

	@EventHandler
	public void ElytraGenerationEvent(ChunkPopulateEvent e)
	{
		if (e.getWorld().getEnvironment() != World.Environment.THE_END) return;
		
		
		List<ItemFrame> frames = stream(e.getChunk()
				.getEntities())
				.sequential()
				.filter(x -> x.getType() == EntityType.ITEM_FRAME)
				.map(x -> (ItemFrame) x)
				.collect(Collectors.toList());
		for (ItemFrame f : frames)
		{
			if (ThreadLocalRandom.current().nextInt(100) > elytraChance)
			{
				f.setItem(replacement);
			}
		}
	}
	@EventHandler
	public void initPersistence(PlayerJoinEvent e) {
		Integer data = Metods._ins.getPersistenData(e.getPlayer(), "hasTotemInfo", PersistentDataType.INTEGER);
		if(data == null) {
			Metods._ins.setPersistenData(e.getPlayer(), "hasTotemInfo", PersistentDataType.INTEGER, 0);
		}
	}
	@EventHandler
	public void ElytraCheckEvent(EntityPickupItemEvent e) {
		if(e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			if(e.getItem().getItemStack().getType() == Material.ELYTRA) {
				int first = Metods._ins.getPersistenData(player, "hasTotemInfo", PersistentDataType.INTEGER);
				if(first == 0) {
					World world = player.getWorld();
					if(world.getEnvironment() != World.Environment.THE_END) return;
					if(!player.getInventory().addItem(getInfoBook()).isEmpty()) {
						world.dropItemNaturally(player.getLocation(), getInfoBook());
					}
					Metods._ins.setPersistenData(player, "hasTotemInfo", PersistentDataType.INTEGER, 1);
					// ? player.sendMessage("");
				}
			}

		}
	}

	private ItemStack getInfoBook() {
		ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
		ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
		if(!(meta instanceof BookMeta)) {
			System.out.println("ItemStack of written_book has no book meta!");
			return new ItemStack(Material.BOOK);
		}
		BookMeta bookMeta = (BookMeta) meta;
		//bookMeta.setTitle(ChatColor.of(new Color(129, 21, 153)) + "Secrets of the Void");
		bookMeta.setTitle(ChatColor.DARK_PURPLE + "Mysteries of the Void");
		bookMeta.setAuthor(ChatColor.DARK_PURPLE + "A wise old man");
		bookMeta.setPages(List.of(
				"""
				The seventh night of the sixth moon of year 384
				
				My studies in the mysteries of the void has finally led to a remarkable discovery.
				
				Upon finding an Elytra in the haunting hallways of the End City, I had an epiphany.""",

				"""
				Could the powers of the Elytra be harvested and bound to the wills of men?
				I had to try.
				
				My research led me to believe that the vast powers contained in the Totems of Undying,
				the ones that the magicians of the pillagers are so fond of,""",

				"""
				could just maybe be able to contain the might of an Elytra.
				In the midst of my research I was able to do it, the incredible creation of a Void Totem.
				By combining a Totem of Undying and an Elytra in a smithing table, I was able
				to bind their powers together.""",

				"""
				With careful testing my predictions were proven true.
				The Void Totem was able to save me from falling into the Void, and thus I could
				keep exploring the End further and further."""
		));
		/*
				Could the powers of the Elytra be harvested and bound to the wills of men?
				I had to try.

				My research led me to believe that the vast powers contained in the Totems of Undying,
				the ones that the magicians of the pillagers are so fond of,
				could just maybe be able to contain the might of an Elytra.

				In the midst of my research I was able to do it, the incredible creation of
				a Void Totem.

				By combining a Totem of Undying and an Elytra in a smithing table, I was able
				to bind their powers together. With careful testing my predictions were proven true.
				The Void Totem was able to save me from falling into the Void, and thus I could
				keep exploring the End further and further.
		 */

		bookMeta.setGeneration(BookMeta.Generation.ORIGINAL);
		item.setItemMeta(bookMeta);
		return item;
	}
}
