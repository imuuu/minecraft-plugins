package me.imu.imusenchants.Inventories;

import imu.iAPI.Config.ConfigEntriesMenu;
import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

// /ien admin: switch vanilla and custom enchants on or off for the whole server,
// middle click an enchant to get its book, and take Slot Cores
public class InventoryEnchantAdmin extends Menu
{
	private enum Tab { VANILLA, CUSTOM }

	static final int SLOT_TAB_VANILLA = 3;
	static final int SLOT_TAB_CUSTOM = 5;
	static final int SLOT_CORE = 8;
	static final int SLOT_SETTINGS = 0;
	static final int SLOT_PREVIOUS = 45;
	static final int SLOT_ENABLE_ALL = 48;
	static final int SLOT_CLOSE = 49;
	static final int SLOT_DISABLE_ALL = 50;
	static final int SLOT_NEXT = 53;

	private final EnchantAdminGame _game;
	private final Pager<EnchantAdminGame.Entry> _pager = Pager.<EnchantAdminGame.Entry>rows(1, 4)
			.arrows(Icon.of(Material.ARROW, "&ePrevious page"), Icon.of(Material.ARROW, "&eNext page"));
	private Tab _tab = Tab.VANILLA;

	public InventoryEnchantAdmin(EnchantAdminGame game)
	{
		super("&0Enchant Admin", 6);
		_game = game;
	}

	@Override
	protected void render(Canvas canvas)
	{
		canvas.fill(Icon.filler(Material.BLACK_STAINED_GLASS_PANE));

		canvas.set(SLOT_TAB_VANILLA, TabIcon(Tab.VANILLA), click -> SwitchTab(Tab.VANILLA));
		canvas.set(SLOT_TAB_CUSTOM, TabIcon(Tab.CUSTOM), click -> SwitchTab(Tab.CUSTOM));
		canvas.set(SLOT_CORE, _game.coreIcon().addLore("&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&eClick &8» &f1 core", "&eShift-click &8» &f64 cores"),
				click -> _game.giveCores(click.viewer(), click.isShift() ? 64 : 1));
		if (_game.settings() != null)
			canvas.set(SLOT_SETTINGS, Icon.of(Material.COMPARATOR, "&bSettings"), click -> click.open(new ConfigEntriesMenu(_game.settings())));

		_pager.render(canvas, Entries(), entry -> Button.of(EntryIcon(entry), click -> ClickEntry(click, entry)), SLOT_PREVIOUS, SLOT_NEXT);

		canvas.set(SLOT_ENABLE_ALL, Icon.of(Material.LIME_DYE, "&aEnable all on this tab"), click -> SetAll(click, true));
		canvas.set(SLOT_DISABLE_ALL, Icon.of(Material.GRAY_DYE, "&cDisable all on this tab"), click -> SetAll(click, false));
		canvas.set(SLOT_CLOSE, Icon.of(Material.BARRIER, "&cClose"), Click::close);
	}

	private List<EnchantAdminGame.Entry> Entries()
	{
		return _tab == Tab.VANILLA ? _game.vanilla() : _game.custom();
	}

	private void ClickEntry(Click click, EnchantAdminGame.Entry entry)
	{
		if (IsMiddleClick(click))
		{
			_game.giveBook(click.viewer(), entry.key());
			return;
		}
		_game.setEnabled(entry.key(), !_game.isEnabled(entry.key()));
	}

	private void SwitchTab(Tab tab)
	{
		if (_tab == tab)
			return;

		_tab = tab;
		_pager.setPage(0);
	}

	private void SetAll(Click click, boolean enabled)
	{
		for (EnchantAdminGame.Entry entry : Entries())
			_game.setEnabled(entry.key(), enabled);

		click.message((enabled ? "&aEnabled" : "&cDisabled") + " &7all " + (_tab == Tab.VANILLA ? "vanilla" : "custom") + " enchants");
	}

	// Middle click in survival and creative; creative players have no middle click in
	// menus without a pick-block key, so shift + right click works as well
	private static boolean IsMiddleClick(Click click)
	{
		return click.kind() == ClickKind.MIDDLE || click.kind() == ClickKind.SHIFT_RIGHT;
	}

	private Icon TabIcon(Tab tab)
	{
		boolean selected = _tab == tab;
		String name = (selected ? "&a&l" : "&7") + (tab == Tab.VANILLA ? "Vanilla Enchants" : "Custom Enchants");
		return Icon.of(tab == Tab.VANILLA ? Material.ENCHANTING_TABLE : Material.AMETHYST_SHARD, name, selected ? "&aSelected" : "&eClick to open")
				.glow(selected);
	}

	private Icon EntryIcon(EnchantAdminGame.Entry entry)
	{
		boolean enabled = _game.isEnabled(entry.key());
		List<String> lore = new ArrayList<>(entry.details());
		lore.add("&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		lore.add(enabled ? "&a&lENABLED" : "&c&lDISABLED");
		lore.add("&eClick &8» &f" + (enabled ? "disable" : "enable"));
		lore.add("&bMiddle-click &8» &fget book");
		lore.add("&bShift + right-click &8» &fget book");
		Icon icon = enabled ? entry.book() : Icon.of(Material.GRAY_DYE);
		return icon.name((enabled ? entry.color() : "&7") + entry.name()).lore(lore.toArray(String[]::new));
	}
}
