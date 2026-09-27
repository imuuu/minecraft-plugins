package imu.DontLoseItems.CustomItems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import imu.DontLoseItems.CustomItems.Entities.Throwable_DoubleSword;
import imu.DontLoseItems.CustomItems.Entities.Throwable_DoubleSword.Throwable_State;
import imu.DontLoseItems.CustomItems.RarityItems.Hell_Triple_Sword;
import imu.DontLoseItems.Enums.CATEGORY;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ImusUtilities;

public final class Hell_Sword_Controller
{

	private final String _PD_HELL_SWORD = "HELL_SWORD";
	private Cooldowns _cds;
	private RarityItem[] _hellSwords = {
//			new Hell_Double_Sword(ITEM_RARITY.Common, 		new double[] { 0 }), // not used
//			new Hell_Double_Sword(ITEM_RARITY.Uncommon, 	new double[] { 0 }), // not used
//			new Hell_Double_Sword(ITEM_RARITY.Rare, 		new double[] { 0 }), // not used
			new Hell_Triple_Sword(ITEM_RARITY.Epic, 		new double[] {}),
			new Hell_Triple_Sword(ITEM_RARITY.Mythic, 		new double[] {}),
			new Hell_Triple_Sword(ITEM_RARITY.Legendary,	new double[] {}), 
			new Hell_Triple_Sword(ITEM_RARITY.Void,			new double[] {}), 
			};
	
	private HashSet<Throwable_DoubleSword> _throwable;

	
	public Hell_Sword_Controller()
	{
		_cds = new Cooldowns();
		_throwable = new HashSet<>();
		for(RarityItem rarityItem : _hellSwords)
		{
			Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Tools.toString(), CreateItem(rarityItem.Rarity));
		}
	}

	public RarityItem GetRarityItem(ITEM_RARITY rarity)
	{
		for (RarityItem sword : _hellSwords)
		{
			if (sword.Rarity == rarity)
			{
				return sword;
			}
		}
		return _hellSwords[0];
	}
	public ItemStack CreateItem(ITEM_RARITY rarity)
	{
		Hell_Triple_Sword rarityItem = (Hell_Triple_Sword) GetRarityItem(rarity);
		ItemStack stack = rarityItem.GetItemStack();
		String colorCode = rarity == ITEM_RARITY.Void ? "&5" : "&9";
		
		ArrayList<String> lores = new ArrayList<>();
		lores.add(" ");
		lores.add("&9Able to throw the double swords");
		lores.add(" ");
		lores.add(colorCode+"Throw distance of &4"+(int)rarityItem.GetThrowDistance());
		lores.add(" ");
		lores.add(colorCode+"Stuck time on wall is &2"+(int)rarityItem.GetDotTimeMultiplier()+"&9 s");
		lores.add(" ");
		if(rarityItem.HasDotDamageEntityMultiplier())
		{
			lores.add(colorCode+"Every stacked mob &eincreases");
			lores.add("&cfire "+colorCode+"dot damage by 2 hears");
			lores.add(colorCode+"when swords are stuck to block");
			lores.add(" ");
		}
		
		lores.add("&9If used on player, cooldown is &5"+rarityItem.PVP_CooldownSeconds+" &9s");
		lores.add(" ");
		
		lores.add("&7'Forged in the flames of the Nether,");
		lores.add("&7this sword was once wielded by a");
		lores.add("&7great warrior who vanquished hordes");
		lores.add("&7of demons with its throwing ability.'");

		Metods._ins.SetLores(stack, lores.toArray(new String[lores.size()]), false);

		Metods._ins.setPersistenData(stack, _PD_HELL_SWORD, PersistentDataType.INTEGER, rarity.GetIndex());

		return stack;
	}

	public boolean IsHellSword(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_SWORD, PersistentDataType.INTEGER) != null;
	}
	
	public ITEM_RARITY GetRarity(ItemStack stack)
	{
		return ITEM_RARITY.GetRarity(Metods._ins.getPersistenData(stack, _PD_HELL_SWORD, PersistentDataType.INTEGER));
	}
	
	public boolean IsTier(ItemStack stack, ITEM_RARITY tier)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_SWORD, PersistentDataType.INTEGER);
		if(i == null ) return false;
		
		if(i == tier.GetIndex()) return true;
		
		return false;
	}
	
	public ItemStack RemoveHellSword(ItemStack stack)
	{
		return Metods._ins.removePersistenData(stack, _PD_HELL_SWORD);
	}
	
	private void RemoveThrowable(Throwable_DoubleSword td)
	{
		
		td.ArmorStand.remove();
	}
	
	public void OnDisable()
	{
		Iterator<Throwable_DoubleSword> it = _throwable.iterator();
		while (it.hasNext())
		{
			Throwable_DoubleSword ar = it.next();
			if(ar.ArmorStand != null) ar.ArmorStand.remove();
		}
		
		_throwable.clear();
	}
	public void OnThrowLoop()
	{
		if (_throwable.isEmpty())
			return;

		Iterator<Throwable_DoubleSword> it = _throwable.iterator();
		while (it.hasNext())
		{
			Throwable_DoubleSword ar = it.next();
			ArmorStand armorStand = ar.ArmorStand;
			
			if (armorStand == null || ar.Player == null || ar.Player.getWorld() != ar.ArmorStand.getWorld())
			{
				it.remove();
				RemoveThrowable(ar);
				continue;
			}
			
			
			Location loc = armorStand.getLocation().clone();
			
			if(ar.Player.getLocation().distance(loc) > ar.MAX_DISTANCE_ACTIVE) 
			{
				ar._entities.clear();
				ar.SetState(Throwable_State.RETURNING);
			}
			
			//particles
			Particle.DustOptions dustOptions = null;
			switch (ar.GetState())
			{
			case TO_TARGET:
				dustOptions = new DustOptions(Color.fromBGR(169, 14, 245), 1.5f);
				loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(0,0.6f,0), 1, dustOptions);
				loc.getWorld().spawnParticle(Particle.DRIP_LAVA, loc.clone().add(0,0.6f,0), 5, 0.3,0.2,0.3);
				armorStand.teleport(loc.clone().add(ar.Direction.clone().multiply(ar.MoveSpeed)));
				break;
			case DOT_DAMAGE:
				if(ar.IsDotTimeDone())
				{
					ar.SetState(Throwable_State.RETURNING);
					//ar._entities.clear();
				}
				break;
			case RETURNING:
				dustOptions = new DustOptions(Color.fromBGR(255, 80, 144), 2f);
				loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(0,0.6f,0), 1, dustOptions);
				
				//if(ar.GetLastState() != Throwable_State.DOT_DAMAGE) break;
				
				if(!ar.HB_sword.HasBringEntityClose()) 
				{
					ar._entities.clear();
				}
				//loc.getWorld().spawnParticle(Particle.DRIP_LAVA, loc.clone().add(0,0.6f,0), 5, 0.3,0.2,0.3);
				break;
			
			

			
			}
			
			
			if(ar.GetEndLoc() != null && ImusUtilities.IsPositionBehind(armorStand.getLocation(), ar.GetEndLoc(), ar.Direction.clone()))
			{
				//System.out.println("Got the destination");
//				it.remove();
//				RemoveThrowable(ar);
				ar.SetState(Throwable_State.RETURNING);
				//continue;
			}
			
			if(ar.GetState() == Throwable_State.TO_TARGET && ar.TargetBlock == null)
			{
				//System.out.println("searching");
				Block testBlock = armorStand.getTargetBlockExact(1);
				
				if(testBlock != null) testBlock = testBlock.getRelative(BlockFace.UP);
				
				if(testBlock != null && !testBlock.isPassable())
				{				
					ar.TargetBlock = GetCorrectTargetBlock(ar.GetStartLoc(), testBlock);
				}
			}
			
			if(ar.GetState() == Throwable_State.TO_TARGET && ar.TargetBlock != null && ImusUtilities.IsPositionBehind(armorStand.getLocation(), ar.GetTargetBlock(), ar.Direction.clone()))
			{
				ar.SetState(Throwable_State.DOT_DAMAGE);
				
			}
			
			
			Iterator<LivingEntity> ent = ar._entities.iterator();
	        while (ent.hasNext()) 
	        {
	        	LivingEntity entity = ent.next();
	        	
				if(entity == null) continue;
				
				
				Location enLoc = loc.clone();//loc.clone().add(entityOffset.OffSet);

				enLoc.setPitch(entity.getLocation().getPitch());
				enLoc.setYaw(entity.getLocation().getYaw());
				
				entity.teleport(enLoc);
				
				if(ar.GetState() == Throwable_State.DOT_DAMAGE)
				{
					if(entity.getFireTicks() <= 1)
					{
						entity.setFireTicks(20);
						if(ar.HB_sword.HasDotDamageEntityMultiplier()) 
						{
							double damage = 2 * ar._entities.size();
							entity.damage(damage);
						}
					}
				}
				else
				{
					if(entity.getFireTicks() <= 1)
					{
						entity.setFireTicks(20);
					}
				}
				
				
				entity.setMaximumNoDamageTicks(0);

				if(entity.isDead()) { ent.remove(); }

			}
			
			if(ar.GetState() == Throwable_State.RETURNING)
			{
				Player player = ar.Player;
				if(player == null)
				{
					it.remove();
					RemoveThrowable(ar);
					continue;
				}
				
				
				Vector direction = player.getLocation().subtract(loc.clone()).toVector().normalize();
				
				ar.Direction = direction;
				ar.SetEndLoc(null);

				float[] yawPitch = ImusUtilities.GetYawPitch(direction.clone().multiply(-1));
				Location tp = loc.clone();
				
				tp.setYaw(yawPitch[0]);
				tp.setPitch(yawPitch[1]);
				armorStand.teleport(tp.add(ar.Direction.clone().multiply(ar.GetReturnSpeed())));
				
				if(armorStand.getLocation().distance(player.getLocation()) < 1)
				{
					it.remove();
					RemoveThrowable(ar);
					//System.out.println("removed by close player");
				}
				
				//continue;
			}
			if(ar.GetState() == Throwable_State.DOT_DAMAGE) continue;
			
			
			
			if(ar._entities.size() > ar.MAXIMUM_ENTETIES) continue;
			
			//stacking mobs close
			for(Entity entity : loc.getWorld().getNearbyEntities(ar.GetBoundingBox())) //loc.getChunk().getEntities()
			{
				if(entity instanceof ArmorStand) continue;
				
				if(!(entity instanceof LivingEntity)) continue;
				
				if(entity.getLocation().distance(armorStand.getLocation()) > 1) continue;
				
				if(ar._ingnoreUUIDS.contains(entity.getUniqueId())) continue;
				
				if(!ImusAPI.EntitiesNoBosses.contains(entity.getType())) continue;
				
				boolean alreadyExist = false;
				for(LivingEntity lEntity : ar._entities)
				{
					if(lEntity.getUniqueId().equals(entity.getUniqueId())) {alreadyExist = true; break;}
				}
				if(alreadyExist) continue;
				
				if(ar.HB_sword.Enable_PVP && entity instanceof Player)
				{
					SetCooldown(ar.Player, ar.HB_sword.GetMaterial(), ar.HB_sword.PVP_CooldownSeconds);
				}
				ar._entities.add((LivingEntity)entity);
				
			}
			

		}
	}
	
	

	private void InitThrow(Player player,ItemStack stack)
	{
		if(!_cds.isCooldownReady(player.getUniqueId().toString())) return;
		
		ArmorStand armorStand = (ArmorStand)player.getWorld().spawnEntity(player.getLocation().add(0, 0.5f, 0), EntityType.ARMOR_STAND);
		armorStand.setArms(true);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setSmall(false);
		armorStand.setMarker(true);

		armorStand.getEquipment().setItemInMainHand(stack.clone());
		armorStand.getEquipment().setItemInOffHand(stack.clone());;
		armorStand.getEquipment().setChestplate(stack.clone());
	
		double pitch = player.getLocation().getPitch();
		armorStand.setRightArmPose(new EulerAngle(Math.toRadians(-10+pitch), Math.toRadians(0), Math.toRadians(0)));
		armorStand.setLeftArmPose(new EulerAngle(Math.toRadians(-10+pitch), Math.toRadians(0), Math.toRadians(0)));

		ITEM_RARITY rarity = RarityItem.GetRarity(stack);
		Hell_Triple_Sword sword = (Hell_Triple_Sword)GetRarityItem(rarity);
		Metods._ins.setPersistenData(armorStand, _PD_HELL_SWORD, PersistentDataType.INTEGER, rarity.GetIndex() );
		
		int throwDistance = sword.GetThrowDistance();
		Location endLoc = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(throwDistance));
		
		Vector dir = endLoc.clone().subtract(player.getLocation()).toVector().normalize();

		Throwable_DoubleSword td = new Throwable_DoubleSword(player,armorStand, endLoc, dir.clone(),sword);
		
		Block b = player.getTargetBlockExact(throwDistance+3);

		if(b != null ) 
		{
			if(b.isPassable()) b = null;

			if(b != null)
			{
				td.TargetBlock = GetCorrectTargetBlock(player.getLocation().clone(), b);
			}
			
		}
		
		SetCooldown(player, sword.GetMaterial(), sword.GetUseCooldown());
		td.SetDotTimeTicks((int)(td.GetDotTime() * sword.GetDotTimeMultiplier()));

		_throwable.add(td);
		
	}
	public Location GetCorrectTargetBlock(Location startLoc, Block block)
	{
		Vector blockLoc = new Vector(block.getLocation().getBlockX()+0.5, block.getLocation().getBlockY()-0.5, block.getLocation().getBlockZ()+0.5);
		double distance = blockLoc.clone().distance(startLoc.toVector());
		Vector bDir = blockLoc.clone().subtract(startLoc.toVector()).normalize();
		Vector vector = bDir.clone().multiply(distance-1);
		
		vector = vector.add(startLoc.toVector());

		return new Location(block.getWorld(), vector.getX(), vector.getY(), vector.getZ());

	}
	public void OnThrow(PlayerInteractEvent e)
	{
		
		if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		if(!IsHellSword(e.getItem())) return;
		
		ItemStack stack = e.getItem();
		
		InitThrow(e.getPlayer(),stack);

	}
	
	private void SetCooldown(Player player, Material type, double seconds)
	{
		player.setCooldown(type,(int)(20*seconds));
		_cds.setCooldownInSeconds(player.getUniqueId().toString(), seconds);
	}

	
}
