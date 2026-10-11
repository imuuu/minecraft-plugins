package imu.GS.menu.admin;

import imu.GS.GsService;
import imu.GS.data.GsDatabase;
import imu.GS.menu.Icons;
import imu.GS.prices.EnchantPrices;
import imu.GS.prices.GearTier;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Other.Metods;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * /gs modify enchantments: what each enchant is worth, gear multipliers and the bonus per extra enchant.
 *
 * <p>On a book: right click picks which value to change (max price, min price, book multiplier), left click changes
 * it, middle click changes the max level. "Override ALL" changes the picked value of every enchant.</p>
 */
public class EnchantPricesMenu extends AdminMenu
{
    private enum Field
    {
        MAX_PRICE("Max Price"),
        MIN_PRICE("Min Price"),
        BOOK_MULTIPLIER("Book Multiplier");

        final String label;

        Field(String label)
        {
            this.label = label;
        }
    }

    private static final int PAGE_SIZE = 45;
    private Field _field = Field.MAX_PRICE;
    private GearTier _gear = GearTier.DIAMOND;
    private int _page = 0;

    public EnchantPricesMenu(GsService service)
    {
        super(service, null, "&5===>Modifying Enchantment", 54);
    }

    private static List<Enchantment> enchantments()
    {
        List<Enchantment> list = new ArrayList<>(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream().toList());
        list.sort(Comparator.comparing(e -> e.getKey().toString()));
        return list;
    }

    @Override
    protected void refresh()
    {
        clearButtons();
        EnchantPrices prices = _service.getEnchants();
        List<Enchantment> enchants = enchantments();
        int pages = Math.max(1, (enchants.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        _page = Math.max(0, Math.min(_page, pages - 1));

        int first = _page * PAGE_SIZE;
        for (int i = first; i < Math.min(enchants.size(), first + PAGE_SIZE); i++)
        {
            Enchantment enchant = enchants.get(i);
            EnchantPrices.EnchantValue value = prices.getValue(enchant);
            List<String> lore = new ArrayList<>();
            lore.add("&6M1 &2Modify &6value &e& &6M2 &bMove &6Selector");
            lore.add(" ");
            lore.add(fieldLine(Field.MAX_PRICE, Icons.money(value.maxPrice())));
            lore.add(fieldLine(Field.MIN_PRICE, Icons.money(value.minPrice())));
            lore.add(fieldLine(Field.BOOK_MULTIPLIER, Dialogs.format(value.bookMultiplier())));
            lore.add("&7(Modify by M3) &9Max Level: &e" + value.maxLevel());
            addButton(new Button(i - first, Icons.icon(Material.ENCHANTED_BOOK, "&5" + enchant.getKey().getKey().toUpperCase(), lore),
                    e -> clickEnchants(List.of(enchant), e.getClick(), value)));
        }
        for (int slot = enchants.size() - first; slot < PAGE_SIZE; slot++)
            addButton(new Button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE)));

        ItemStack orange = Icons.filler(Material.ORANGE_STAINED_GLASS_PANE);
        for (int slot = PAGE_SIZE; slot < 54; slot++)
            addButton(new Button(slot, orange));

        addButton(new Button(45, Icons.icon(Material.BIRCH_SIGN, "&b<<"), e ->
        {
            _page--;
            refresh();
        }));
        addButton(new Button(53, Icons.icon(Material.BIRCH_SIGN, "&b>>"), e ->
        {
            _page++;
            refresh();
        }));

        List<String> gearLore = new ArrayList<>();
        gearLore.add("&6M1 &2Modify &6value &e& &6M2 &bMove &6Selector");
        gearLore.add("&7Enchants on gear are worth their price x this");
        for (GearTier tier : GearTier.values())
            gearLore.add((tier == _gear ? "&b&l==> " : "&9") + tier.name() + ": &e" + Dialogs.format(prices.getGearMultiplier(tier)));
        addButton(new Button(48, Icons.icon(Material.NETHERITE_CHESTPLATE, "&6Gear Multipliers", gearLore), e ->
        {
            if (e.isRightClick())
            {
                _gear = GearTier.values()[(_gear.ordinal() + 1) % GearTier.values().length];
                refresh();
                return;
            }
            GearTier tier = _gear;
            Dialogs.number(this, tier.name() + " multiplier", List.of("Enchants on " + tier.name() + " gear are worth price x this"),
                    prices.getGearMultiplier(tier), 0, 1000, v ->
                    {
                        prices.setGearMultiplier(tier, v);
                        _service.getDatabase().saveEnchantSetting("gear_" + tier.name(), v);
                        saved(tier.name());
                    });
        }));

        addButton(new Button(50, Icons.icon(Material.ANVIL, "&6Other Multipliers",
                "&9Each extra enchant adds &e" + Dialogs.format(prices.getEnchantCountPercent() * 100) + " % &9to the price",
                "&bClick &7to change"), e ->
                Dialogs.number(this, "Enchant count buff percent", List.of("Each enchant after the first adds this % to the price"),
                        prices.getEnchantCountPercent() * 100, 0, 1000, v ->
                        {
                            prices.setEnchantCountPercent(v / 100.0);
                            _service.getDatabase().saveEnchantSetting("enchant_count_percent", v / 100.0);
                            saved("Enchant Count Buff Percent");
                        })));

        addButton(new Button(52, Icons.icon(Material.PAPER, "&5Override ALL",
                "&6M1 &2Set &e" + _field.label + " &2of every enchant", "&6M2 &bMove &6Selector", "&6M3 &2Set max level of every enchant"),
                e -> clickEnchants(enchants, e.getClick(), new EnchantPrices.EnchantValue(0, 0, 0, EnchantPrices.EnchantValue.DEFAULT_BOOK_MULTIPLIER))));

        updateButtons(true);
    }

    private String fieldLine(Field field, String value)
    {
        return (field == _field ? "&b&l==> " : "&9") + field.label + ": &e" + value;
    }

    private void clickEnchants(List<Enchantment> enchants, ClickType click, EnchantPrices.EnchantValue shown)
    {
        EnchantPrices prices = _service.getEnchants();
        if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT)
        {
            _field = Field.values()[(_field.ordinal() + 1) % Field.values().length];
            refresh();
            return;
        }

        if (click == ClickType.MIDDLE)
        {
            Dialogs.wholeNumber(this, "Max level", List.of("Level where the price reaches its max", "-1 = the enchant's own max level"),
                    shown.maxLevel(), -1, 255, level ->
                    {
                        for (Enchantment enchant : enchants)
                        {
                            EnchantPrices.EnchantValue v = prices.getValue(enchant);
                            int max = level < 0 ? enchant.getMaxLevel() : level;
                            prices.setValue(enchant, new EnchantPrices.EnchantValue(max, v.minPrice(), v.maxPrice(), v.bookMultiplier()));
                        }
                        save(enchants);
                    });
            return;
        }

        Field field = _field;
        double current = switch (field)
        {
            case MAX_PRICE -> shown.maxPrice();
            case MIN_PRICE -> shown.minPrice();
            case BOOK_MULTIPLIER -> shown.bookMultiplier();
        };
        Dialogs.text(this, field.label, List.of(field == Field.BOOK_MULTIPLIER
                ? "On an enchanted book the enchant is worth its price x this"
                : "Min price is the price at level 1, max price at the max level"), Dialogs.format(current), text ->
        {
            double value;
            try
            {
                value = Double.parseDouble(text.trim().replace(',', '.'));
            } catch (NumberFormatException e)
            {
                return "Need to be a number!";
            }
            if (value < 0)
                return "Can't be negative";

            List<String> problems = new ArrayList<>();
            for (Enchantment enchant : enchants)
            {
                EnchantPrices.EnchantValue v = prices.getValue(enchant);
                double min = field == Field.MIN_PRICE ? value : v.minPrice();
                double max = field == Field.MAX_PRICE ? value : v.maxPrice();
                if (min > max)
                    problems.add(enchant.getKey().getKey());
            }
            if (!problems.isEmpty())
                return "Max must be at least min: " + String.join(", ", problems);

            for (Enchantment enchant : enchants)
            {
                EnchantPrices.EnchantValue v = prices.getValue(enchant);
                prices.setValue(enchant, switch (field)
                {
                    case MAX_PRICE -> new EnchantPrices.EnchantValue(v.maxLevel(), v.minPrice(), value, v.bookMultiplier());
                    case MIN_PRICE -> new EnchantPrices.EnchantValue(v.maxLevel(), value, v.maxPrice(), v.bookMultiplier());
                    case BOOK_MULTIPLIER -> new EnchantPrices.EnchantValue(v.maxLevel(), v.minPrice(), v.maxPrice(), value);
                });
            }
            save(enchants);
            return null;
        });
    }

    private void save(List<Enchantment> enchants)
    {
        List<GsDatabase.EnchantRow> rows = new ArrayList<>();
        for (Enchantment enchant : enchants)
            rows.add(new GsDatabase.EnchantRow(enchant.getKey().toString(), _service.getEnchants().getValue(enchant)));
        _service.getDatabase().saveEnchants(rows);
        saved(enchants.size() == 1 ? enchants.get(0).getKey().getKey().toUpperCase() : "All");
    }

    private void saved(String what)
    {
        _service.refreshMenus();
        getPlayer().sendMessage(Metods.msgC("&b" + what + "&3 data has been saved"));
    }
}
