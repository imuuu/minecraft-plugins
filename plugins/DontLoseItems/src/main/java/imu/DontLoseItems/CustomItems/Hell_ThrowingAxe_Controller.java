package imu.DontLoseItems.CustomItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import imu.DontLoseItems.CustomItems.Entities.Throwable_Axe;
import imu.DontLoseItems.CustomItems.RarityItems.Hell_Double_Axe;
import imu.DontLoseItems.Enums.CATEGORY;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;

public final class Hell_ThrowingAxe_Controller
{

	private final String _PD_HELL_AXE = "HELL_AXE";
	private Cooldowns _cds;
	
	private HashMap<UUID, AxeDamage> _axeDamages;
	private RarityItem[] _hellAxe = 
		{

			new Hell_Double_Axe(ITEM_RARITY.Epic, 			new double[] {}),
			new Hell_Double_Axe(ITEM_RARITY.Mythic, 		new double[] {}),
			new Hell_Double_Axe(ITEM_RARITY.Legendary,		new double[] {}), 
			new Hell_Double_Axe(ITEM_RARITY.Void,			new double[] {}), 
			};
	
	private HashSet<Throwable_Axe> _throwable;
	
	public Hell_ThrowingAxe_Controller()
	{
		_cds = new Cooldowns();
		_throwable = new HashSet<>();
		_axeDamages =  new HashMap<>();
		for(RarityItem rarityItem : _hellAxe)
		{
			Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Tools.toString(), CreateItem(rarityItem.Rarity));
		}
	}
	
	public class AxeDamage
	{
		private double _damage = 1;
		private final double MAX_DAMAGE = 10;
		
		public double GetDamage()
		{
			return _damage;
		}
		
		public void SetDamage(double damage)
		{
			this._damage = damage;
			if(_damage > MAX_DAMAGE) _damage = MAX_DAMAGE;
		}
		
		public void AddDamage(double damage)
		{
			_damage += damage;
			if(_damage > MAX_DAMAGE) _damage = MAX_DAMAGE;
		}
		
		public void ResetDamage()
		{
			_damage = 1;
		}
	}
	
	public RarityItem GetRarityItem(ITEM_RARITY rarity)
	{
		for (RarityItem sword : _hellAxe)
		{
			if (sword.Rarity == rarity)
			{
				return sword;
			}
		}
		return _hellAxe[0];
	}
	public ItemStack CreateItem(ITEM_RARITY rarity)
	{
		Hell_Double_Axe rarityItem = (Hell_Double_Axe) GetRarityItem(rarity);
		ItemStack stack = rarityItem.GetItemStack();
		
		String colorCode = rarity == ITEM_RARITY.Void ? "&5" : "&9";
		ArrayList<String> lores = new ArrayList<>();
		lores.add(" ");
		lores.add("&9Able to throw a second axe");
		lores.add("&9with a base &cdamage &9of &2" + rarityItem.GetDamageBase());
		lores.add(" ");
		lores.add(colorCode+"Throw force of &2" + rarityItem.GetThrowForce());
		lores.add(" ");
		lores.add(colorCode+"Throw cooldown of &2" + rarityItem.GetUseCooldown() + " &9seconds");
		lores.add(" ");
		lores.add("&9Every mob hit &eincreases &cdamage");
		lores.add(" ");
		lores.add("&9But every &7miss &3resets &9the &cdamage");
		lores.add(" ");
		
		if(rarity == ITEM_RARITY.Void)
		{
			lores.add("&5Damage increases faster to maximum");
			lores.add(" ");
			lores.add("&5Able to throw other void tier axe");
			lores.add("&5from other hand");
			lores.add(" ");
		}
		
		lores.add("&7'Forged in the fiery depths of the Nether,");
		lores.add("&7this axe is imbued with a deadly throwing ability.");
		lores.add("&7It can cleave through even the strongest");
		lores.add("&7of demonic armor, making it a fearsome weapon");
		lores.add("&7for any adventurer daring enough to wield it'");

		Metods._ins.SetLores(stack, lores.toArray(new String[lores.size()]), false);

		Metods._ins.setPersistenData(stack, _PD_HELL_AXE, PersistentDataType.INTEGER, rarity.GetIndex());

		return stack;
	}

	public boolean IsHellAxe(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_AXE, PersistentDataType.INTEGER) != null;
	}
	
	public ITEM_RARITY GetRarity(ItemStack stack)
	{
		return ITEM_RARITY.GetRarity(Metods._ins.getPersistenData(stack, _PD_HELL_AXE, PersistentDataType.INTEGER));
	}
	
	public boolean IsTier(ItemStack stack, ITEM_RARITY tier)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_AXE, PersistentDataType.INTEGER);
		if(i == null ) return false;
		
		if(i == tier.GetIndex()) return true;
		
		return false;
	}
	
	public ItemStack RemoveHellAxe(ItemStack stack)
	{
		return Metods._ins.removePersistenData(stack, _PD_HELL_AXE);
	}
	
	private void RemoveThrowable(Throwable_Axe td)
	{		
		if(td.ArmorStand != null) 			td.ArmorStand.remove();
		if(td.Projectile != null) 			td.Projectile.remove();
		if(td.Projectile_AxePos != null) 	td.Projectile_AxePos.remove();
	}
	
	public void OnDisable()
	{
		Iterator<Throwable_Axe> it = _throwable.iterator();
		while (it.hasNext())
		{
			Throwable_Axe ar = it.next();
			if(ar.ArmorStand != null) 			ar.ArmorStand.remove();
			if(ar.Projectile != null) 			ar.Projectile.remove();
			if(ar.Projectile_AxePos != null) 	ar.Projectile_AxePos.remove();
		}
		
		_throwable.clear();
		_axeDamages.clear();
	}
	public void OnThrowLoop()
	{
		if (_throwable.isEmpty())
			return;

		Iterator<Throwable_Axe> it = _throwable.iterator();
		while (it.hasNext())
		{
			
			Throwable_Axe ar = it.next();
			
			ArmorStand armorStand = ar.ArmorStand;
			
			if (armorStand == null || ar.Projectile == null || ar.Projectile.isDead() || !ar.Projectile.isValid() || ar.Projectile.getTicksLived() > 20 * 60)
			{
				it.remove();
				RemoveThrowable(ar);
				continue;
			}
			Location loc = armorStand.getLocation().clone();
			
			if(ar.Player.getLocation().distance(loc) > ar.MAX_DISTANCE_ACTIVE) 
			{
				it.remove();
				RemoveThrowable(ar);
				continue;
			}
			
			EulerAngle rot = armorStand.getRightArmPose();
			EulerAngle rotNew = rot.add(1, 0, 0);
			armorStand.setRightArmPose(rotNew);
			
			armorStand.teleport(loc.clone().add(ar.Direction.clone().multiply(0.1)));
			loc.setX(ar.Projectile.getLocation().getX());
			loc.setY(ar.Projectile.getLocation().getY()-0.5);
			loc.setZ(ar.Projectile.getLocation().getZ());
			armorStand.teleport(loc);
			
			
			if(ar.Projectile_AxePos == null || !ar.Projectile_AxePos.isValid() || ar.Projectile.isDead())
			{
				if(ar.Projectile_AxePos != null) ar.Projectile_AxePos.remove();
			}
			else if(ar.Projectile_AxePos.getLocation().distance(ar.Player.getLocation()) > 2)
			{

				ar.HD_axe.SpawnDamageParticle(ar.Projectile_AxePos.getLocation().clone(), ar.DAMAGE);
			}
			
		}
	}
	
	private AxeDamage GetDamage(Player player)
	{
		if(!_axeDamages.containsKey(player.getUniqueId())) _axeDamages.put(player.getUniqueId(), new AxeDamage());
		
		return _axeDamages.get(player.getUniqueId());
	}
	private void InitThrow(Player player,ItemStack stack)
	{
		String str_uuid = player.getUniqueId().toString();
		if(!_cds.isCooldownReady(str_uuid)) return;
		
		ArmorStand armorStand = (ArmorStand)player.getWorld().spawnEntity(player.getLocation().add(0, -1, 0), EntityType.ARMOR_STAND);

		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setSmall(true);
		armorStand.setMarker(true);
		
		armorStand.getEquipment().setItemInMainHand(stack.clone());
		double pitch = player.getLocation().getPitch();
		armorStand.setRightArmPose(new EulerAngle(Math.toRadians(-10+pitch), Math.toRadians(0), Math.toRadians(0)));
		
		ITEM_RARITY rarity = RarityItem.GetRarity(stack);
		Hell_Double_Axe axe = (Hell_Double_Axe)GetRarityItem(rarity);
		Metods._ins.setPersistenData(armorStand, _PD_HELL_AXE, PersistentDataType.INTEGER, rarity.GetIndex() );
		
		double throwForce = axe.GetThrowForce();
		Location endLoc = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(throwForce));
		
		Vector dir = endLoc.clone().subtract(player.getLocation()).toVector().normalize();

		Throwable_Axe td = new Throwable_Axe(player,armorStand,dir.clone(),axe);

	    Snowball projectile = player.launchProjectile(Snowball.class);
	    projectile.setShooter(player);
	   
		projectile.setVelocity(dir.clone().multiply(throwForce));

		Metods._ins.setPersistenData(projectile, _PD_HELL_AXE, PersistentDataType.INTEGER, rarity.GetIndex() );
		Projectile extra = LaunchExtraSnowball(player, throwForce);
		
		td.Projectile_AxePos = extra;
		SendPacket_RemoveEntity(player, new Entity[] {projectile, extra});
		td.Projectile = projectile;
		td.DAMAGE = GetDamage(player).GetDamage();
		_throwable.add(td);
		
		_cds.setCooldownInSeconds(str_uuid, axe.GetUseCooldown());
	}
	private Projectile LaunchExtraSnowball(Player player, double force)
	{
		Location loc = player.getLocation();

		// get the player's direction vector
		Vector direction = loc.getDirection().normalize();

		// rotate the direction vector by 90 degrees around the y-axis to get the right side vector
		double x = direction.getX();
		double z = direction.getZ();
		double y = direction.getY();
		direction.setX(-z * 0.2);
		direction.setZ(x * 0.2);
		direction.setY(y+1);

		Location rightSide = loc.clone().add(direction.clone().multiply(0.5)).add(direction.clone().multiply(1));

		Snowball snowball = player.getWorld().spawn(rightSide, Snowball.class);
		snowball.setInvulnerable(true);
		
		snowball.setVelocity(loc.clone().getDirection().multiply(force)); // adjust the velocity to your li
		
		return snowball;
	}
	private void SendPacket_RemoveEntity(Player player, Entity[] entitis)
	{
		PacketContainer destroyPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		PacketContainer useItem = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ANIMATION);


		List<Integer> entityIds = new ArrayList<>();
		for(Entity ent : entitis)
		{
			entityIds.add(ent.getEntityId());
		}

		destroyPacket.getIntLists().write(0, entityIds);
		
		useItem.getIntegers().write(0, player.getEntityId());
		useItem.getIntegers().write(1, 0);
		
		try
		{
			for (Player p : Bukkit.getOnlinePlayers()) 
			{
				if(p.getWorld() == player.getWorld())
				{
					ProtocolLibrary.getProtocolManager().sendServerPacket(p, destroyPacket);
					ProtocolLibrary.getProtocolManager().sendServerPacket(p, useItem);
				}
			}
		} 
		catch (Exception e)
		{
			
		}
		
	}
	
	
	public void OnProjectileHit(ProjectileHitEvent e)
	{
		Projectile prot = e.getEntity();
		if(!(prot instanceof Snowball)) return;
		
		Integer rarityIndex = Metods._ins.getPersistenData(prot, _PD_HELL_AXE, PersistentDataType.INTEGER);
		if(rarityIndex == null) return;
		
		
		if(!(prot.getShooter() instanceof Player)) return;
		
		ITEM_RARITY rarity = ITEM_RARITY.values()[rarityIndex];
		Hell_Double_Axe axe = (Hell_Double_Axe)GetRarityItem(rarity);
		
		Player player = (Player) prot.getShooter();
		
		if(e.getHitEntity() != null && e.getHitEntity() instanceof LivingEntity && e.getHitEntity().getType() != EntityType.ARMOR_STAND)
		{
			LivingEntity livEnt = (LivingEntity) e.getHitEntity();
			
			AxeDamage dmg = GetDamage(player);
			double givenDamage = axe.GetDamageBase() * dmg.GetDamage();
			livEnt.damage(givenDamage, player);
			dmg.AddDamage(axe.GetDamageIncrease());
			return;
		}
		
		GetDamage(player).ResetDamage();
	
	}
	
	public void OnThrow(PlayerInteractEvent e)
	{
		if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		if(!IsHellAxe(e.getItem())) return;
				
		ItemStack stack = e.getItem();
		
		InitThrow(e.getPlayer(),stack);
				
	}
	
	public void OnPlayerQuit(PlayerQuitEvent e)
	{
		if(_axeDamages.containsKey(e.getPlayer().getUniqueId())) _axeDamages.remove(e.getPlayer().getUniqueId());
		
	}

	
}
