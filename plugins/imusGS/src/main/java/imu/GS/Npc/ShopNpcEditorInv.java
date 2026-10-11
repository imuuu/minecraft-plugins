package imu.GS.Npc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import imu.GS.Main.Main;
import imu.GS.Npc.ShopNpcManager.NpcType;
import imu.GS.ShopUtl.Shop;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.TextInputDialog;

/**
 * Settings of one shop NPC: name, linked shop, villager or player look, skin, position. Opened with
 * /gs create npc or by sneak-clicking an NPC.
 */
public class ShopNpcEditorInv extends CustomInventory
{
	private final ShopNpcManager _npcs;
	private UUID _npcId;

	public ShopNpcEditorInv(Main main, ShopNpcManager npcs, LivingEntity npc)
	{
		super(main, "&9Shop NPC", 3 * 9);
		_npcs = npcs;
		_npcId = npc.getUniqueId();
	}

	@Override
	public INVENTORY_AREA setInventoryLock()
	{
		return INVENTORY_AREA.UPPER_LOWER_INV;
	}

	@Override
	public void onAwake()
	{
	}

	@Override
	public void onOpen()
	{
		super.onOpen();
		Refresh();
	}

	/** The NPC, or null (and the menu closes) when it no longer exists. */
	LivingEntity GetNpc()
	{
		Entity entity = Bukkit.getEntity(_npcId);
		if(entity instanceof LivingEntity npc && npc.isValid()) return npc;

		getPlayer().sendMessage(Metods.msgC("&cThe NPC is gone (removed or its chunk unloaded)."));
		Bukkit.getScheduler().runTask(getPlugin(), () -> getPlayer().closeInventory());
		return null;
	}

	void Refresh()
	{
		LivingEntity npc = GetNpc();
		if(npc == null) return;

		clearButtons();
		for(int i = 0; i < getSize(); i++) addButton(getEmptyButton(i, Material.BLACK_STAINED_GLASS_PANE));

		Shop shop = _npcs.GetShop(npc);
		NpcType type = _npcs.GetType(npc);

		addButton(new Button(10, Icon(Material.NAME_TAG, "&6&lName",
				"&9Now: &r"+_npcs.GetName(npc), "", "&bClick &7to change"),
				e -> AskName()));

		addButton(new Button(11, Icon(Material.CHEST, "&6&lShop",
				"&9Opens: "+(shop == null ? "&cnothing, the shop was deleted" : "&a"+shop.GetName()), "", "&bClick &7to link another shop"),
				e -> AskShop()));

		addButton(new Button(12, Icon(type == NpcType.PLAYER ? Material.PLAYER_HEAD : Material.VILLAGER_SPAWN_EGG, "&6&lLooks like",
				"&9Now: &a"+(type == NpcType.PLAYER ? "a player" : "a villager"), "", "&bClick &7to switch"),
				e ->
				{
					_npcId = _npcs.ChangeType(npc, type == NpcType.PLAYER ? NpcType.VILLAGER : NpcType.PLAYER, getPlayer()).getUniqueId();
					Refresh();
				}));

		if(npc instanceof Villager villager)
		{
			addButton(new Button(14, Icon(Material.EMERALD, "&6&lProfession",
					"&9Now: &a"+KeyName(ShopNpcManager.Professions(), villager.getProfession()), "", "&bLeft click &7next, &bright click &7previous"),
					e ->
					{
						_npcs.SetProfession(villager, Cycle(ShopNpcManager.Professions(), villager.getProfession(), e));
						Refresh();
					}));

			addButton(new Button(15, Icon(Material.GRASS_BLOCK, "&6&lClothes",
					"&9Now: &a"+KeyName(ShopNpcManager.VillagerTypes(), villager.getVillagerType()), "", "&bLeft click &7next, &bright click &7previous"),
					e ->
					{
						villager.setVillagerType(Cycle(ShopNpcManager.VillagerTypes(), villager.getVillagerType(), e));
						Refresh();
					}));
		}

		if(npc instanceof Mannequin mannequin)
		{
			String skin = _npcs.GetSkinName(mannequin);
			addButton(new Button(14, Icon(Material.PLAYER_HEAD, "&6&lSkin",
					"&9Now: &a"+(skin == null ? "default" : skin), "", "&bLeft click &7to use a player's skin", "&bRight click &7for the default skin"),
					e ->
					{
						if(e.isRightClick())
						{
							_npcs.ResetSkin(mannequin);
							Refresh();
							return;
						}
						AskSkin();
					}));

			addButton(new Button(15, Icon(Material.OAK_SIGN, "&6&lText under the name",
					"&9Now: &r"+_npcs.GetSubtitle(mannequin), "", "&bClick &7to change"),
					e -> AskSubtitle()));
		}

		addButton(new Button(19, Icon(Material.ENDER_PEARL, "&6&lMove here",
				"&7Moves the NPC to where you stand,", "&7facing the way you look"),
				e ->
				{
					npc.teleport(getPlayer().getLocation());
					npc.setRotation(getPlayer().getLocation().getYaw(), 0);
				}));

		addButton(new Button(20, Icon(Material.COMPASS, "&6&lFace me", "&7Turns the NPC to look at you"),
				e -> _npcs.Face(npc, getPlayer().getLocation())));

		addButton(new Button(22, Icon(Material.OAK_DOOR, "&6&lDone"),
				e -> Bukkit.getScheduler().runTask(getPlugin(), () -> getPlayer().closeInventory())));

		addButton(new Button(26, Icon(Material.LAVA_BUCKET, "&c&lRemove NPC", "&7The shop itself is kept", "", "&bShift click &7to remove"),
				e ->
				{
					if(!e.isShiftClick()) return;
					npc.remove();
					getPlayer().sendMessage(Metods.msgC("&9The shop NPC was removed."));
					Bukkit.getScheduler().runTask(getPlugin(), () -> getPlayer().closeInventory());
				}));

		updateButtons(true);
	}

	// ==================== TEXT INPUT ====================


	void AskName()
	{
		LivingEntity npc = GetNpc();
		if(npc == null) return;

		ShowDialog("NPC name", List.of("Colour codes with & work"), _npcs.GetName(npc), 64, (target, text) ->
		{
			if(text.isBlank()) return "The name can't be empty";
			_npcs.SetName(target, text);
			return null;
		});
	}

	void AskShop()
	{
		LivingEntity npc = GetNpc();
		if(npc == null) return;

		List<String> lines = new ArrayList<>();
		lines.add("Shops:");
		List<String> names = _npcs.ShopNames();
		for(int i = 0; i < Math.min(10, names.size()); i++) lines.add("&f"+names.get(i));
		if(names.size() > 10) lines.add("&8... and "+(names.size()-10)+" more");

		Shop current = _npcs.GetShop(npc);
		ShowDialog("Shop the NPC opens", lines, current == null ? "" : current.GetName(), 100, (target, text) ->
		{
			Shop shop = ((Main)getPlugin()).get_shopManager().GetShop(text);
			if(shop == null) return "No shop named "+text;
			_npcs.SetShop(target, shop);
			return null;
		});
	}

	void AskSkin()
	{
		LivingEntity npc = GetNpc();
		if(npc == null) return;

		String current = _npcs.GetSkinName(npc);
		ShowDialog("Skin", List.of("Name of the Minecraft player whose skin the NPC wears"), current == null ? "" : current, 16, (target, text) ->
		{
			String name = text.trim();
			if(!ShopNpcManager.IsValidPlayerName(name)) return "Not a Minecraft player name";
			if(!(target instanceof Mannequin mannequin)) return null;

			getPlayer().sendMessage(Metods.msgC("&7Looking up the skin of &e"+name+"&7..."));
			_npcs.SetSkin(mannequin, name, getPlayer(), null);
			return null;
		});
	}

	void AskSubtitle()
	{
		LivingEntity npc = GetNpc();
		if(!(npc instanceof Mannequin mannequin)) return;

		ShowDialog("Text under the name", List.of("Colour codes with & work. Empty = no text"),_npcs.GetSubtitle(mannequin), 64, (target, text) ->
		{
			if(target instanceof Mannequin m) _npcs.SetSubtitle(m, text);
			return null;
		});
	}

	interface NpcEdit
	{
		/** @return null when the text was accepted, otherwise the problem */
		String Apply(LivingEntity npc, String text);
	}

	/** Asks for text in a dialog. The dialog replaces this menu, which reopens when the dialog closes. */
	void ShowDialog(String title, List<String> lines, String initial, int maxLength, NpcEdit edit)
	{
		TextInputDialog.show(getPlayer(), title, lines, initial, maxLength, text ->
		{
			Entity entity = Bukkit.getEntity(_npcId);
			if(!(entity instanceof LivingEntity npc) || !npc.isValid()) return null;
			return edit.Apply(npc, text);
		}, () -> open(getPlayer()));
	}

	// ==================== HELPERS ====================

	static ItemStack Icon(Material material, String name, String... lore)
	{
		ItemStack stack = new ItemStack(material);
		ItemUtils.SetDisplayName(stack, name);
		for(String line : lore) ItemUtils.AddLore(stack, line, true);
		ItemUtils.HideFlag(stack, ItemFlag.HIDE_ATTRIBUTES);
		return stack;
	}

	static <T extends org.bukkit.Keyed> String KeyName(Registry<T> registry, T value)
	{
		return registry.getKeyOrThrow(value).getKey().replace('_', ' ');
	}

	/** The next value of the registry, or the previous one on right click. */
	static <T extends org.bukkit.Keyed> T Cycle(Registry<T> registry, T current, InventoryClickEvent e)
	{
		List<T> values = registry.stream().toList();
		int index = values.indexOf(current);
		int step = e.isRightClick() ? -1 : 1;
		return values.get(Math.floorMod(index + step, values.size()));
	}
}
