package imu.DontLoseItems.CustomItems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Fire;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import imu.DontLoseItems.CustomItems.RarityItems.Hell_ReflectShield;
import imu.DontLoseItems.Enums.CATEGORY;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.Managers.Manager_HellArmor;
import imu.DontLoseItems.main.DontLoseItems;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ImusUtilities;

public final class Hell_ReflectShieldController implements Listener
{
	public static Hell_ReflectShieldController Instance;

	public class ArrowReflect
	{
		public Arrow Arrow;
		public UUID Uuid;
		public String Str_uuid;
		public Location Loc_start;
		public ITEM_RARITY Rarity;

		public ArrowReflect(Arrow arrow, ITEM_RARITY rarity)
		{
			Arrow = arrow;
			Uuid = arrow.getUniqueId();
			Str_uuid = Uuid.toString();
			Loc_start = arrow.getLocation();
			Rarity = rarity;
		}

		public double GetDistanceFromStart()
		{
			if (Arrow == null)
				return 999999;
			if (Arrow.getLocation() == null)
				return 999999;

			return Arrow.getLocation().distance(Loc_start);
		}
	}

	public double ArrowStartSpeed = 0.3f;
	public double ArrowSpeedIncrease = 1.18f;
	// public double ArrowDamageMulti = 2f;
	public double ArrowLifeTimeSecons = 2;
	public boolean ArrowGravity = false;
	public final double DistanceFireStarts = 1;
	public final int _fireTime = 2; // 14 => is max and seems to be full fire 1-14

	private HashSet<ArrowReflect> _reflected_arrows;
	private final String _PD_REFLECTED_ARROW = "REFLECTED_ARROW";
	private final String _PD_REFLECTED_ARROW_FIRE = "REFLECTED_ARROW_FIRE";
	private final String _PD_FIRE_TICKS = "FIRE_TICKS";
	private final double MAX_VELOCITY = 5;
	private final int FIRE_CIRCLE_RADIUS = 3;
	private Cooldowns _cds;

	private HashSet<Material> _ignoreSet;
	private int _mythicFireChance = 25;

	private final String _PD_HELL_REFLECT_SHIELD = "HELL_REFLECT_SHIELD";
	private final String _PD_HELL_TIER = "HELL_TIER";

	public boolean IsEnabled = true;
	private Hell_ReflectShield[] _hellReflectShields = { 
//			new Hell_ReflectShield(ITEM_RARITY.Common, new double[] { 0 }), // not																														// used
//			new Hell_ReflectShield(ITEM_RARITY.Uncommon, new double[] { 0 }), // not used
//			new Hell_ReflectShield(ITEM_RARITY.Rare, new double[] { 0 }), // not used
			new Hell_ReflectShield(ITEM_RARITY.Epic, new double[] { 0 }),
			new Hell_ReflectShield(ITEM_RARITY.Mythic, new double[] { 0 }),
			new Hell_ReflectShield(ITEM_RARITY.Legendary, new double[] { 0 }),
			new Hell_ReflectShield(ITEM_RARITY.Void, new double[] { 0 }), };

	public ItemStack CreateItem(ITEM_RARITY rarity)
	{
		Hell_ReflectShield rarityItem = _hellReflectShields[0];

		for (Hell_ReflectShield shield : _hellReflectShields)
		{
			if (shield.Rarity == rarity)
			{
				rarityItem = shield;
			}
		}

		ItemStack stack = rarityItem.GetItemStack();

		ArrayList<String> lores = new ArrayList<>();
		lores.add(" ");
		lores.add("&9Blocks arrows &3any &9direction");
		lores.add(" ");
		if (rarity != ITEM_RARITY.Epic)
			lores.add("&eReflects &7arrow &9back to");
		if (rarity == ITEM_RARITY.Epic)
			lores.add("&eReflects &7arrow &9back to attacker");

		if (rarity == ITEM_RARITY.Mythic)
			lores.add("&9attacker and &5doubles");
		if (rarity == ITEM_RARITY.Legendary || rarity == ITEM_RARITY.Void)
			lores.add("&9attacker and &5triples");
		if (rarity != ITEM_RARITY.Epic)
			lores.add("&9it damage");

		if (rarity != ITEM_RARITY.Epic)
			lores.add(" ");
//		if(rarity == ITEM_RARITY.Epic) lores.add("&9Arrow leaves a small trail");
//		if(rarity == ITEM_RARITY.Epic) lores.add("&9of flames behind it");

		if (rarity == ITEM_RARITY.Mythic)
			lores.add("&9Arrow leaves a substantial");
		if (rarity == ITEM_RARITY.Mythic)
			lores.add("&9trail of &4flames &9behind it");

		if (rarity == ITEM_RARITY.Legendary || rarity == ITEM_RARITY.Void)
			lores.add("&9Arrow leaves a massive");
		if (rarity == ITEM_RARITY.Legendary || rarity == ITEM_RARITY.Void)
			lores.add("&9trail of &4flames &9behind it");
		lores.add(" ");
		if (rarity == ITEM_RARITY.Legendary || rarity == ITEM_RARITY.Void)
			lores.add("&9Causes &4fiery &9destruction upon impact");
		if (rarity == ITEM_RARITY.Legendary || rarity == ITEM_RARITY.Void)
			lores.add(" ");
//		lores.add("&9If Arrow is Players, " + durabilityLost + " durability will be lost");
//		lores.add(" ");
		lores.add("&7'Legend has it that this Shield was");
		lores.add("&7once owned by a mischievous archer");
		lores.add("&7who loved to pull pranks on his");
		lores.add("&7enemies. The Shield would always send");
		lores.add("&7their arrows right back at them, much");
		lores.add("&7to their surprise (and sometimes, horror!)'");

		Metods._ins.SetLores(stack, lores.toArray(new String[lores.size()]), false);

		ItemMeta meta = stack.getItemMeta();

		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_REFLECT_SHIELD, PersistentDataType.INTEGER, rarity.GetIndex());
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarity.toString());
		return stack;
	}

	public Hell_ReflectShieldController()
	{
		Instance = this;
		_ignoreSet = new HashSet<>();
		_ignoreSet.add(Material.AIR);
		_ignoreSet.add(Material.WATER);
		_ignoreSet.add(Material.LAVA);

		_reflected_arrows = new HashSet<>();
		_cds = new Cooldowns();
		DontLoseItems.Instance.getServer().getPluginManager().registerEvents(this, DontLoseItems.Instance);
		
		for(RarityItem rarityItem : _hellReflectShields)
		{
			Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Tools.toString(), CreateItem(rarityItem.Rarity));
		}

	}
	
	public ITEM_RARITY GetRarity(ItemStack stack)
	{
		return ITEM_RARITY.GetRarity(Metods._ins.getPersistenData(stack, _PD_HELL_REFLECT_SHIELD, PersistentDataType.INTEGER));
	}
	
	public boolean IsTier(ItemStack stack, ITEM_RARITY tier)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_TIER, PersistentDataType.INTEGER);
		if(i == null ) return false;
		
		if(i == tier.GetIndex()) return true;
		
		return false;
	}
	
	public Hell_ReflectShield GetShield(ItemStack stack)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_REFLECT_SHIELD, PersistentDataType.INTEGER);
		if(i == null ) return null;
		
		return _hellReflectShields[i];
	}
	public void OnBlockingArrow(EntityDamageByEntityEvent e)
	{
		if (!IsEnabled)
			return;

		if (!(e.getEntity() instanceof LivingEntity))
			return;

		LivingEntity entity = (LivingEntity) e.getEntity();

		if (!(e.getEntity() instanceof Player))
			return;

		if (e.getDamager().getType() != EntityType.ARROW)
			return;

		if (!((Player) entity).isBlocking())
			return;

		ItemStack shield = entity.getEquipment().getItemInOffHand();
		if (!IsHellReflectShield(shield))
			return;
		
		Hell_ReflectShield refShield = GetShield(shield);
		Arrow arrow = (Arrow) e.getDamager();
		// SendArrowBackV2(entity, (Entity)arrow.getShooter(), arrow, 10, 2, true);
		//ITEM_RARITY rarity = Manager_HellArmor.Instance.GetRarity(shield);
		
		OnHellShieldReflect(entity, (Entity) arrow.getShooter(), arrow, refShield.Rarity);

		int durMulti = refShield.GetUseDurabilityLost();//6 - rarity.GetIndex();
		Metods._ins.giveDamage(shield, Manager_HellArmor.Instance.ReflectShieldDurabilityLost * durMulti, true);
		e.setCancelled(true);

		return;

	}

	public void OnHellShieldReflect(LivingEntity reflecterEntity, Entity target, Arrow arrow, ITEM_RARITY rarity)
	{
		Hell_ReflectShieldController.Instance.OnArrowInit(reflecterEntity, target, arrow, rarity);
	}

	public boolean IsHellReflectShield(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_REFLECT_SHIELD, PersistentDataType.INTEGER) != null;
	}

	public ItemStack RemoveShield(ItemStack stack)
	{
		return Metods._ins.removePersistenData(stack, _PD_HELL_REFLECT_SHIELD);

	}

	public void AddArrow(Arrow arrow, ITEM_RARITY rarity)
	{
		Metods._ins.setPersistenData(arrow, _PD_REFLECTED_ARROW, PersistentDataType.INTEGER, 1);
		_reflected_arrows.add(new ArrowReflect(arrow, rarity));
	}

	private void RemoveArrow(ArrowReflect refArrow)
	{
		if (refArrow.Arrow.isOnGround() && refArrow.Rarity == ITEM_RARITY.Legendary)
		{
			CreateFireArea(refArrow.Arrow.getLocation());
		}
		refArrow.Arrow.remove();

		// System.out.println("removed");
	}

	public void OnReflectArrowLoop()
	{
		if (_reflected_arrows.isEmpty())
			return;

		Iterator<ArrowReflect> it = _reflected_arrows.iterator();
		while (it.hasNext())
		{
			ArrowReflect ar = it.next();
			Arrow arrow = ar.Arrow;
			if (arrow == null)
			{
				it.remove();
				RemoveArrow(ar);
				continue;
			}

			if (arrow.isDead())
			{
				it.remove();
				RemoveArrow(ar);
				continue;
			}

			if (arrow.isOnGround())
			{
				it.remove();
				RemoveArrow(ar);
				continue;
			}

			double speed = arrow.getVelocity().lengthSquared();

			if (_cds.isCooldownReady(ar.Str_uuid) || speed <= 0)
			{
				it.remove();
				RemoveArrow(ar);

				continue;
			}
			if (speed <= MAX_VELOCITY)
			{
				Vector currentVelocity = arrow.getVelocity();
				arrow.setVelocity(currentVelocity.multiply(ArrowSpeedIncrease));

			} else
			{
				arrow.setVelocity(arrow.getVelocity().normalize().multiply(MAX_VELOCITY));
			}

			if (ar.GetDistanceFromStart() < DistanceFireStarts)
				continue;

			if (ar.Rarity == ITEM_RARITY.Epic)
				continue;

			if (ar.Rarity != ITEM_RARITY.Legendary && ThreadLocalRandom.current().nextInt(100) < _mythicFireChance)
			{
				continue;
			}

			Location loc = arrow.getLocation();

			for (int y = 0; y < 10; y++)
			{
				Block b = loc.clone().add(0, -y, 0).getBlock();

				if (b == null || b.getType() == Material.AIR)
					continue;

				if (b.getType() == Material.FIRE)
					break;

				Block fireBlock = b.getRelative(BlockFace.UP);
				if (fireBlock.getType() == Material.AIR || fireBlock.getType().isFlammable())
				{
					IgniteBlock(fireBlock);
					break;
				}

			}

		}

		/// System.out.println("lool");

	}

	public void OnArrowInit(LivingEntity reflecterEntity, Entity target, Arrow arrow, ITEM_RARITY rarity)
	{

		int damageMulti = rarity.GetIndex() - 2;

		arrow.setDamage(arrow.getDamage() * damageMulti);
		arrow.setBounce(false);
		arrow.setShooter((LivingEntity) target);
		// arrow.setGlowing(true);
		arrow.setTicksLived((int) (20 * ArrowLifeTimeSecons));

		// arrow.setColor(Color.GREEN);
		arrow.setPierceLevel(10);

		Vector reflecter = reflecterEntity.getLocation().toVector();
		Vector direction = reflecter.subtract(target.getLocation().toVector());
		direction = direction.normalize();

		arrow.setVelocity(direction.clone().multiply(ArrowStartSpeed));
		arrow.setGravity(ArrowGravity);

		_cds.setCooldownInSeconds(arrow.getUniqueId().toString(), ArrowLifeTimeSecons);
		AddArrow(arrow, rarity);

	}

	private void IgniteBlock(Block fireBlock)
	{
		BlockBurnEvent burnEvent = new BlockBurnEvent(fireBlock, fireBlock);
		Bukkit.getServer().getPluginManager().callEvent(burnEvent);

		if (burnEvent.isCancelled())
			return;

		fireBlock.setType(Material.FIRE);

		if (fireBlock.getType() != Material.FIRE)
			return;

		fireBlock.setMetadata(_PD_REFLECTED_ARROW_FIRE, new FixedMetadataValue(ImusAPI._instance, 1));
		BlockState state = fireBlock.getState();

		Fire fire = (Fire) state.getBlockData();
		fire.setAge(15 - _fireTime);
		state.setBlockData(fire);
		state.update();
	}

	private void CreateFireArea(Location hitLoc)
	{
		for (Location loc : ImusUtilities.CreateSphere(hitLoc, FIRE_CIRCLE_RADIUS, _ignoreSet, null))
		{
			// if(loc.getBlock() == null || loc.getBlock().getType() == Material.AIR)
			// continue;

			Block firePos = loc.add(0, 1, 0).getBlock();

			if (firePos == null || firePos.getType() != Material.AIR)
				continue;

			IgniteBlock(firePos);
		}
	}

	private void CheckFire(Block b)
	{
		if (b.getType() != Material.FIRE)
			return;
		int ticks = 1;

		if (b.hasMetadata(_PD_FIRE_TICKS))
		{
			ticks = (int) b.getMetadata(_PD_FIRE_TICKS).get(0).value();
			b.setMetadata(_PD_FIRE_TICKS, new FixedMetadataValue(DontLoseItems.Instance, ++ticks));
		} else
		{
			b.setMetadata(_PD_FIRE_TICKS, new FixedMetadataValue(DontLoseItems.Instance, ticks));
		}

		if (ticks > 2)
		{
			b.setType(Material.AIR);
		} // System.out.println("FIRE SHUT DOWN");
	}

	@EventHandler
	private void OnBurnBlock(BlockBurnEvent e)
	{
		Block from = e.getIgnitingBlock();
		if (from == null)
			return;

		if (from.getType() != Material.FIRE)
			return;

		if (from.hasMetadata(_PD_REFLECTED_ARROW_FIRE))
		{
			CheckFire(from);
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	private void OnIgnite(BlockIgniteEvent e)
	{

		Block from = e.getIgnitingBlock();
		if (from == null)
			return;

		if (from.getType() != Material.FIRE)
			return;

		if (from.hasMetadata(_PD_REFLECTED_ARROW_FIRE))
		{
			CheckFire(from);
			e.setCancelled(true);
			return;
		}

	}

}
