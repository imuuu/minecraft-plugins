package me.imu.imusenchants.Inventories;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.ImusEnchants;
import me.imu.imusenchants.Items.SlotCore;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;

// /ien admin: switch vanilla and custom enchants on or off for the whole server,
// middle click an enchant to get its book, and take Slot Cores
public class InventoryEnchantAdmin extends CustomInventory
{
	private enum Tab { VANILLA, CUSTOM }

	private static final int SLOT_TAB_VANILLA = 3;
	private static final int SLOT_TAB_CUSTOM = 5;
	private static final int SLOT_CORE = 8;
	private static final int FIRST_ENTRY_SLOT = 9;
	private static final int ENTRIES_PER_PAGE = 36;
	private static final int SLOT_PREVIOUS = 45;
	private static final int SLOT_ENABLE_ALL = 48;
	private static final int SLOT_CLOSE = 49;
	private static final int SLOT_DISABLE_ALL = 50;
	private static final int SLOT_NEXT = 53;

	private Tab _tab = Tab.VANILLA;
	private int _page = 0;

	public InventoryEnchantAdmin()
	{
		super(ImusEnchants.Instance, "&0Enchant Admin", 6 * 9);
	}

	@Override
	public INVENTORY_AREA setInventoryLock()
	{
		return INVENTORY_AREA.UPPER_INV;
	}

	@Override
	public void onAwake()
	{
	}

	@Override
	public void onOpen()
	{
		super.onOpen();
		Render();
	}

	private void Render()
	{
		clearButtons();

		ItemStack background = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemUtils.SetDisplayNameEmpty(background);
		for (int i = 0; i < getSize(); i++)
			addButton(new Button(i, background.clone()));

		addButton(new Button(SLOT_TAB_VANILLA, TabItem(Tab.VANILLA), e -> SwitchTab(Tab.VANILLA)));
		addButton(new Button(SLOT_TAB_CUSTOM, TabItem(Tab.CUSTOM), e -> SwitchTab(Tab.CUSTOM)));
		addButton(new Button(SLOT_CORE, CoreItem(), e -> Give(SlotCore.Create(e.isShiftClick() ? 64 : 1))));

		int count = _tab == Tab.VANILLA ? EnchantSettings.GetAllVanilla().size() : CustomEnchantRegistry.GetAll().size();
		int pages = Math.max(1, (count + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
		_page = Math.max(0, Math.min(_page, pages - 1));

		if (_tab == Tab.VANILLA)
			RenderVanilla();
		else
			RenderCustom();

		if (_page > 0)
			addButton(new Button(SLOT_PREVIOUS, Named(Material.ARROW, "&ePrevious page"), e -> { _page--; Render(); }));
		if (_page < pages - 1)
			addButton(new Button(SLOT_NEXT, Named(Material.ARROW, "&eNext page"), e -> { _page++; Render(); }));

		addButton(new Button(SLOT_ENABLE_ALL, Named(Material.LIME_DYE, "&aEnable all on this tab"), e -> SetAll(true)));
		addButton(new Button(SLOT_DISABLE_ALL, Named(Material.GRAY_DYE, "&cDisable all on this tab"), e -> SetAll(false)));
		addButton(new Button(SLOT_CLOSE, Named(Material.BARRIER, "&cClose"), e -> getPlayer().closeInventory()));

		updateButtons(true);
	}

	private void RenderVanilla()
	{
		List<Enchantment> all = EnchantSettings.GetAllVanilla();
		int start = _page * ENTRIES_PER_PAGE;
		for (int i = start; i < Math.min(all.size(), start + ENTRIES_PER_PAGE); i++)
		{
			Enchantment enchant = all.get(i);
			addButton(new Button(FIRST_ENTRY_SLOT + i - start, VanillaItem(enchant), e ->
			{
				if (IsMiddleClick(e))
				{
					Give(VanillaBook(enchant));
					return;
				}
				EnchantSettings.SetEnabled(enchant, !EnchantSettings.IsEnabled(enchant));
				Render();
			}));
		}
	}

	private void RenderCustom()
	{
		List<CustomEnchant> all = new ArrayList<>(CustomEnchantRegistry.GetAll());
		int start = _page * ENTRIES_PER_PAGE;
		for (int i = start; i < Math.min(all.size(), start + ENTRIES_PER_PAGE); i++)
		{
			CustomEnchant enchant = all.get(i);
			addButton(new Button(FIRST_ENTRY_SLOT + i - start, CustomItem(enchant), e ->
			{
				if (IsMiddleClick(e))
				{
					Give(CustomEnchantBook.Create(enchant, enchant.GetMaxLevel()));
					return;
				}
				EnchantSettings.SetEnabled(enchant, !EnchantSettings.IsEnabled(enchant));
				Render();
			}));
		}
	}

	private void SwitchTab(Tab tab)
	{
		if (_tab == tab)
			return;

		_tab = tab;
		_page = 0;
		Render();
	}

	private void SetAll(boolean enabled)
	{
		if (_tab == Tab.VANILLA)
		{
			for (Enchantment enchant : EnchantSettings.GetAllVanilla())
				EnchantSettings.SetEnabled(enchant, enabled);
		}
		else
		{
			for (CustomEnchant enchant : CustomEnchantRegistry.GetAll())
				EnchantSettings.SetEnabled(enchant, enabled);
		}

		getPlayer().sendMessage(Metods.msgC((enabled ? "&aEnabled" : "&cDisabled") + " &7all "
				+ (_tab == Tab.VANILLA ? "vanilla" : "custom") + " enchants"));
		Render();
	}

	// ClickType.MIDDLE in survival and creative; creative players have no middle click in
	// menus without a pick-block key, so shift + right click works as well
	private static boolean IsMiddleClick(InventoryClickEvent e)
	{
		return e.getClick() == ClickType.MIDDLE || e.getClick() == ClickType.SHIFT_RIGHT;
	}

	private void Give(ItemStack stack)
	{
		InvUtil.AddItemToInventoryOrDrop(getPlayer(), stack);
	}

	private static ItemStack VanillaBook(Enchantment enchant)
	{
		ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
		meta.addStoredEnchant(enchant, enchant.getMaxLevel(), true);
		book.setItemMeta(meta);
		return book;
	}

	private static ItemStack CoreItem()
	{
		ItemStack stack = SlotCore.Create(1);
		ItemUtils.AddLore(stack, "&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
		ItemUtils.AddLore(stack, "&eClick to get one, shift-click for 64", true);
		return stack;
	}

	private ItemStack TabItem(Tab tab)
	{
		boolean selected = _tab == tab;
		ItemStack stack = new ItemStack(tab == Tab.VANILLA ? Material.ENCHANTING_TABLE : Material.AMETHYST_SHARD);
		ItemUtils.SetDisplayName(stack, (selected ? "&a&l" : "&7") + (tab == Tab.VANILLA ? "Vanilla Enchants" : "Custom Enchants"));
		ItemUtils.AddLore(stack, selected ? "&aSelected" : "&eClick to open", true);
		if (selected)
			ItemUtils.AddGlow(stack);
		return stack;
	}

	private static ItemStack VanillaItem(Enchantment enchant)
	{
		boolean enabled = EnchantSettings.IsEnabled(enchant);
		ItemStack stack = new ItemStack(enabled ? Material.ENCHANTED_BOOK : Material.GRAY_DYE);
		if (enabled)
		{
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
			meta.addStoredEnchant(enchant, enchant.getMaxLevel(), true);
			stack.setItemMeta(meta);
		}

		ItemUtils.SetDisplayName(stack, (enabled ? "&e" : "&7") + PrettyName(enchant));
		ItemUtils.AddLore(stack, "&7Max level: &f" + CustomEnchant.ToRoman(enchant.getMaxLevel()), true);
		AddStatusLore(stack, enabled);
		return stack;
	}

	private static ItemStack CustomItem(CustomEnchant enchant)
	{
		boolean enabled = EnchantSettings.IsEnabled(enchant);
		ItemStack stack = new ItemStack(enabled ? Material.ENCHANTED_BOOK : Material.GRAY_DYE);
		ItemUtils.SetDisplayName(stack, (enabled ? (enchant.IsCurse() ? "&c" : "&d") : "&7") + enchant.GetName());
		ItemUtils.AddLore(stack, "&7" + enchant.GetDescription(enchant.GetMaxLevel()), true);
		ItemUtils.AddLore(stack, "&7Max level: &f" + CustomEnchant.ToRoman(enchant.GetMaxLevel()), true);
		ItemUtils.AddLore(stack, "&7For: &f" + enchant.GetAppliesToText(), true);
		if (enchant.IsCurse())
			ItemUtils.AddLore(stack, "&4Curse", true);
		AddStatusLore(stack, enabled);
		return stack;
	}

	private static void AddStatusLore(ItemStack stack, boolean enabled)
	{
		ItemUtils.AddLore(stack, "&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
		ItemUtils.AddLore(stack, enabled ? "&a&lENABLED" : "&c&lDISABLED", true);
		ItemUtils.AddLore(stack, "&eClick to " + (enabled ? "disable" : "enable"), true);
		ItemUtils.AddLore(stack, "&bMiddle-click or shift + right-click to get the book", true);
	}

	private static ItemStack Named(Material material, String name)
	{
		ItemStack stack = new ItemStack(material);
		ItemUtils.SetDisplayName(stack, name);
		return stack;
	}

	private static String PrettyName(Enchantment enchant)
	{
		StringBuilder sb = new StringBuilder();
		for (String word : enchant.getKey().getKey().split("_"))
		{
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return sb.toString();
	}
}
