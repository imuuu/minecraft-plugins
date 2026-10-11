package imu.GS.Npc;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import imu.GS.Main.Main;
import imu.iAPI.Npc.NpcManager;

/**
 * Shop NPCs made before NPCs moved to ImusAPI kept their data under imusGS keys. They are turned into
 * ImusAPI NPCs when their chunk loads.
 */
public class LegacyShopNpcs implements Listener
{
	private final NpcManager _npcs;
	private final NamespacedKey _keyShop;
	private final NamespacedKey _keySkin;
	private final NamespacedKey _keyHomeYaw;
	private final NamespacedKey _keyLook;

	public LegacyShopNpcs(Main main, NpcManager npcs)
	{
		_npcs = npcs;
		_keyShop = new NamespacedKey(main, "npc_shop");
		_keySkin = new NamespacedKey(main, "npc_skin");
		_keyHomeYaw = new NamespacedKey(main, "npc_home_yaw");
		_keyLook = new NamespacedKey(main, "npc_look");
	}

	/** Converts the NPCs in chunks that are already loaded. */
	public void ConvertLoaded()
	{
		for(World world : Bukkit.getWorlds())
		{
			for(LivingEntity entity : world.getLivingEntities()) Convert(entity);
		}
	}

	@EventHandler
	public void OnEntitiesLoad(EntitiesLoadEvent e)
	{
		for(Entity entity : e.getEntities()) Convert(entity);
	}

	void Convert(Entity entity)
	{
		if(!(entity instanceof LivingEntity npc)) return;
		PersistentDataContainer pdc = npc.getPersistentDataContainer();
		String shopUuid = pdc.get(_keyShop, PersistentDataType.STRING);
		if(shopUuid == null) return;

		_npcs.adopt(npc, ShopNpcAction.ID, shopUuid);
		Float homeYaw = pdc.get(_keyHomeYaw, PersistentDataType.FLOAT);
		_npcs.setHomeYaw(npc, homeYaw != null ? homeYaw : npc.getYaw());
		Byte look = pdc.get(_keyLook, PersistentDataType.BYTE);
		if(look != null) _npcs.setLooksAtPlayers(npc, look != 0);

		pdc.remove(_keyShop);
		pdc.remove(_keySkin);
		pdc.remove(_keyHomeYaw);
		pdc.remove(_keyLook);
	}
}
