package imu.GS.Npc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.profile.PlayerProfile;

import imu.GS.Main.Main;
import imu.GS.ShopUtl.Shop;
import imu.iAPI.Other.Metods;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Registry;

/**
 * Shop NPCs without an NPC plugin: a villager or a mannequin (looks like a player, can wear any player's skin).
 * The shop it opens is stored on the entity itself, so the NPC lives and dies with its chunk like any mob.
 */
public class ShopNpcManager implements Listener
{
	public static final String PERM_EDIT = "gs.npc.edit";

	public enum NpcType
	{
		VILLAGER,
		PLAYER
	}

	private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

	private final Main _main;
	private final NamespacedKey _keyShop;
	private final NamespacedKey _keySkin;
	// Set while the editor changes a profession, so OnCareerChange lets it through
	private boolean _changingProfession = false;

	public ShopNpcManager(Main main)
	{
		_main = main;
		_keyShop = new NamespacedKey(main, "npc_shop");
		_keySkin = new NamespacedKey(main, "npc_skin");
	}

	// ==================== NPC DATA ====================

	public boolean IsShopNpc(Entity entity)
	{
		return entity instanceof LivingEntity && entity.getPersistentDataContainer().has(_keyShop, PersistentDataType.STRING);
	}

	public NpcType GetType(LivingEntity npc)
	{
		return npc instanceof Mannequin ? NpcType.PLAYER : NpcType.VILLAGER;
	}

	/** The linked shop, null when it was deleted. */
	public Shop GetShop(LivingEntity npc)
	{
		String uuid = npc.getPersistentDataContainer().get(_keyShop, PersistentDataType.STRING);
		if(uuid == null) return null;
		try
		{
			return _main.get_shopManager().GetShop(UUID.fromString(uuid));
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	public void SetShop(LivingEntity npc, Shop shop)
	{
		npc.getPersistentDataContainer().set(_keyShop, PersistentDataType.STRING, shop.GetUUID().toString());
	}

	/** Name with &amp; colour codes. */
	public String GetName(LivingEntity npc)
	{
		Component name = npc.customName();
		return name == null ? "" : LEGACY.serialize(name);
	}

	public void SetName(LivingEntity npc, String name)
	{
		npc.customName(LEGACY.deserialize(name));
		npc.setCustomNameVisible(true);
	}

	/** The text under a player NPC's name, with &amp; colour codes. */
	public String GetSubtitle(Mannequin npc)
	{
		Component description = npc.getDescription();
		return description == null ? "" : LEGACY.serialize(description);
	}

	public void SetSubtitle(Mannequin npc, String subtitle)
	{
		npc.setDescription(LEGACY.deserialize(subtitle));
	}

	/** Name of the player whose skin a player NPC wears, null for the default skin. */
	public String GetSkinName(LivingEntity npc)
	{
		return npc.getPersistentDataContainer().get(_keySkin, PersistentDataType.STRING);
	}

	public static Registry<Villager.Profession> Professions()
	{
		return RegistryAccess.registryAccess().getRegistry(RegistryKey.VILLAGER_PROFESSION);
	}

	public static Registry<Villager.Type> VillagerTypes()
	{
		return RegistryAccess.registryAccess().getRegistry(RegistryKey.VILLAGER_TYPE);
	}

	// ==================== SPAWNING ====================

	public LivingEntity Spawn(NpcType type, Location loc, Shop shop, String name)
	{
		LivingEntity npc = SpawnEntity(type, loc);
		SetShop(npc, shop);
		SetName(npc, name);
		return npc;
	}

	LivingEntity SpawnEntity(NpcType type, Location loc)
	{
		if(type == NpcType.PLAYER)
		{
			return loc.getWorld().spawn(loc, Mannequin.class, mannequin ->
			{
				Setup(mannequin);
				mannequin.setImmovable(true);
				mannequin.setDescription(Component.text("Shop"));
			});
		}

		return loc.getWorld().spawn(loc, Villager.class, villager ->
		{
			Setup(villager);
			villager.setProfession(Villager.Profession.NONE);
		});
	}

	void Setup(LivingEntity npc)
	{
		npc.setAI(false);
		npc.setInvulnerable(true);
		npc.setSilent(true);
		npc.setCollidable(false);
		npc.setPersistent(true);
		npc.setRemoveWhenFarAway(false);
		npc.setCustomNameVisible(true);
	}

	/** Replaces the NPC with one of the other type. Returns the new entity. */
	public LivingEntity ChangeType(LivingEntity npc, NpcType type, Player editor)
	{
		if(GetType(npc) == type) return npc;

		Location loc = npc.getLocation();
		String name = GetName(npc);
		String shopUuid = npc.getPersistentDataContainer().get(_keyShop, PersistentDataType.STRING);
		String skin = GetSkinName(npc);

		LivingEntity replacement = SpawnEntity(type, loc);
		PersistentDataContainer pdc = replacement.getPersistentDataContainer();
		pdc.set(_keyShop, PersistentDataType.STRING, shopUuid);
		SetName(replacement, name);
		npc.remove();

		if(replacement instanceof Mannequin mannequin && skin != null)
		{
			SetSkin(mannequin, skin, editor, null);
		}
		return replacement;
	}

	/**
	 * Looks up the skin of a player name from Mojang in the background and puts it on the NPC.
	 * The skin is stored on the entity, so it isn't fetched again.
	 * @param onDone runs on the main thread when the skin was applied
	 */
	public void SetSkin(Mannequin npc, String playerName, Player editor, Runnable onDone)
	{
		final UUID npcId = npc.getUniqueId();
		PlayerProfile profile = Bukkit.createProfile(playerName);
		profile.update().whenComplete((result, error) ->
		{
			if(!_main.isEnabled()) return;
			Bukkit.getScheduler().runTask(_main, () ->
			{
				if(error != null || result == null || !result.hasTextures())
				{
					editor.sendMessage(Metods.msgC("&cNo skin found for player &e"+playerName));
					return;
				}
				if(!(Bukkit.getEntity(npcId) instanceof Mannequin mannequin)) return;

				mannequin.setProfile(ResolvableProfile.resolvableProfile(result));
				mannequin.getPersistentDataContainer().set(_keySkin, PersistentDataType.STRING, result.getName() != null ? result.getName() : playerName);
				editor.sendMessage(Metods.msgC("&aThe NPC now has the skin of &e"+playerName));
				if(onDone != null) onDone.run();
			});
		});
	}

	public void ResetSkin(Mannequin npc)
	{
		npc.setProfile(Mannequin.defaultProfile());
		npc.getPersistentDataContainer().remove(_keySkin);
	}

	public static boolean IsValidPlayerName(String name)
	{
		return PLAYER_NAME.matcher(name).matches();
	}

	/** Turns the NPC to look at the location. */
	public void Face(LivingEntity npc, Location target)
	{
		Location loc = npc.getLocation();
		double dx = target.getX() - loc.getX();
		double dz = target.getZ() - loc.getZ();
		float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
		npc.setRotation(yaw, 0);
	}

	public List<String> ShopNames()
	{
		List<String> names = new ArrayList<>();
		for(Shop shop : _main.get_shopManager().GetShops()) names.add(shop.GetName());
		return names;
	}

	// ==================== EVENTS ====================

	void Use(Player player, LivingEntity npc)
	{
		if(player.isSneaking() && player.hasPermission(PERM_EDIT))
		{
			// Inventories must not be opened inside the click event itself
			Bukkit.getScheduler().runTask(_main, () -> new ShopNpcEditorInv(_main, this, npc).open(player));
			return;
		}

		Shop shop = GetShop(npc);
		if(shop == null)
		{
			player.sendMessage(Metods.msgC("&9This shop is closed."));
			if(player.hasPermission(PERM_EDIT)) player.sendMessage(Metods.msgC("&7Its shop was deleted. Sneak + click the NPC to link another shop."));
			return;
		}
		Bukkit.getScheduler().runTask(_main, () -> shop.Open(player));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void OnInteract(PlayerInteractEntityEvent e)
	{
		if(!IsShopNpc(e.getRightClicked())) return;

		// Also stops villager trading, name tags and leads
		e.setCancelled(true);
		if(e.getHand() != EquipmentSlot.HAND) return;

		Use(e.getPlayer(), (LivingEntity)e.getRightClicked());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void OnAttack(PrePlayerAttackEntityEvent e)
	{
		if(!IsShopNpc(e.getAttacked())) return;

		e.setCancelled(true);
		Use(e.getPlayer(), (LivingEntity)e.getAttacked());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void OnDamage(EntityDamageEvent e)
	{
		// Invulnerable already, but /kill-style damage and plugins can still get here
		if(IsShopNpc(e.getEntity()) && e.getCause() != EntityDamageEvent.DamageCause.KILL) e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void OnCombust(EntityCombustEvent e)
	{
		if(IsShopNpc(e.getEntity())) e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void OnCareerChange(VillagerCareerChangeEvent e)
	{
		if(IsShopNpc(e.getEntity()) && !_changingProfession) e.setCancelled(true);
	}

	public void SetProfession(Villager npc, Villager.Profession profession)
	{
		_changingProfession = true;
		try
		{
			npc.setProfession(profession);
			// A villager without a level drops its profession
			if(npc.getVillagerLevel() < 1) npc.setVillagerLevel(1);
		}
		finally
		{
			_changingProfession = false;
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void OnTransform(EntityTransformEvent e)
	{
		// Lightning (witch) and zombies (zombie villager) would replace the NPC
		if(IsShopNpc(e.getEntity())) e.setCancelled(true);
	}
}
