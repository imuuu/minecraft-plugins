package imu.GS.menu.admin;

import imu.GS.GsService;
import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.pricing.PriceRule;
import imu.GS.engine.shop.Listing;
import imu.GS.engine.shop.Shop;
import imu.GS.engine.shop.StockRules;
import imu.GS.menu.Icons;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes random stockable items for a shop. Generate, click the ones you want (they stay when you generate again),
 * then CONFIRM adds them to the shop.
 *
 * <pre>
 *  0-26 generated items: click to pick, middle click copies to you
 *  36 price range  37 stack range  39 enchants  43 fill amount range  44 fill time range
 *  45 back   49 generate (middle click; left/right change how many)   51 pick all / none   53 confirm
 * </pre>
 */
public class ItemGeneratorMenu extends AdminMenu
{
    private enum Restriction
    {
        /** Only enchants that fit the item and don't conflict */
        ALL,
        /** Enchants that fit the item, conflicts allowed */
        ALL_BUT_IGNORE_CONFLICTS,
        /** Any enchant on any item */
        NONE
    }

    private enum EnchantField
    {
        AMOUNT, MAX_LEVEL, MIN_LEVEL, RESTRICTION
    }

    private static final class Generated
    {
        ItemStack item;
        Double price;
        int maxStock;
        int fillAmount;
        int fillDelay;
        boolean picked;
    }

    private static final Material[] MATERIALS = Arrays.stream(Material.values())
            .filter(m -> m.isItem() && !m.isAir() && !m.isLegacy() && m != Material.DEBUG_STICK)
            .toArray(Material[]::new);

    // -1 = not set
    private double _priceMin = -1, _priceMax = -1;
    private int _stackMin = 64, _stackMax = 64;
    private int _fillMin = -1, _fillMax = -1;
    private int _fillTimeMin = -1, _fillTimeMax = -1;
    private int _enchantAmount = 0;
    private int _levelMin = -1, _levelMax = -1;
    private Restriction _restriction = Restriction.ALL;
    private EnchantField _enchantField = EnchantField.AMOUNT;
    private int _generateAmount = 1;
    private final Generated[] _generated = new Generated[27];

    public ItemGeneratorMenu(GsService service, Shop shop)
    {
        super(service, shop, "&9=== Item Generator ", 54);
    }

    @Override
    protected void refresh()
    {
        clearButtons();
        for (int slot = 0; slot < 27; slot++)
        {
            Generated generated = _generated[slot];
            if (generated == null)
            {
                addButton(new Button(slot, Icons.filler(Material.BLUE_STAINED_GLASS_PANE)));
                continue;
            }
            List<String> lore = List.of("&bM3 &9=> Copy to Inv", "&9PICKED : " + (generated.picked ? "&aTRUE" : "&cFALSE"),
                    "&9Price: &e" + (generated.price == null ? "material price" : Icons.money(generated.price)),
                    "&9Stack Amount: &e" + generated.maxStock,
                    "&9Fill Amount: &e" + (generated.fillAmount < 0 ? "none" : generated.fillAmount),
                    "&9Fill Time: &e" + (generated.fillDelay < 0 ? "none" : generated.fillDelay + " min"));
            ItemStack stack = Icons.withLore(generated.item, lore);
            if (generated.picked)
                Icons.glow(stack);
            addButton(new Button(slot, stack, e ->
            {
                if (e.getClick() == ClickType.MIDDLE)
                {
                    getPlayer().getInventory().addItem(generated.item.clone());
                    return;
                }
                generated.picked = !generated.picked;
                refresh();
            }));
        }

        for (int slot = 27; slot < 54; slot++)
            addButton(new Button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE)));

        addButton(range(36, Material.GOLD_INGOT, "&6Set Price Range", _priceMin < 0 ? "NONE => Use material price"
                : Dialogs.format(_priceMin) + " - " + Dialogs.format(_priceMax), "price", true));
        addButton(range(37, Material.NETHERITE_SCRAP, "&6Set Stack Amount Range", _stackMin + " - " + _stackMax, "stack", false));
        addButton(range(43, Material.WATER_BUCKET, "&6Set fill amount range", _fillMin < 0 ? "NONE" : _fillMin + " - " + _fillMax, "fill", false));
        addButton(range(44, Material.CLOCK, "&6Set fill time range", _fillTimeMin < 0 ? "NONE" : _fillTimeMin + " - " + _fillTimeMax + " min", "time", false));
        addButton(enchantButton());

        addButton(new Button(45, Icons.icon(Material.RED_STAINED_GLASS_PANE, "&c<== BACK"), e -> openNext(new ShopEditorMenu(_service, _shop))));
        addButton(new Button(49, Icons.icon(Material.BEACON, "&bM3 &6=> &5Generate Items",
                "&bM1 &a+1 &bSM1 &a+5", "&bM2 &c-1 &bSM2 &c-5", "&9Generation Amount: &e" + _generateAmount), e ->
        {
            switch (e.getClick())
            {
                case MIDDLE -> generate();
                case LEFT -> _generateAmount += 1;
                case SHIFT_LEFT -> _generateAmount += 5;
                case RIGHT -> _generateAmount -= 1;
                case SHIFT_RIGHT -> _generateAmount -= 5;
                default -> { }
            }
            _generateAmount = Math.max(0, Math.min(27, _generateAmount));
            refresh();
        }));
        addButton(new Button(51, Icons.icon(Material.PAPER, "&9Select &bM1: &aALL  &bM2: &cNONE"), e ->
        {
            for (Generated generated : _generated)
            {
                if (generated != null)
                    generated.picked = !e.isRightClick();
            }
            refresh();
        }));
        addButton(new Button(53, Icons.icon(Material.GREEN_STAINED_GLASS_PANE, "&9CONFIRM and add to shop"), e -> confirm()));
        updateButtons(true);
    }

    private Button range(int slot, Material material, String name, String value, String which, boolean decimals)
    {
        return new Button(slot, Icons.icon(material, name, "&bM1: &aSet &bM2: &cReset", "&9Set to: &a" + value), e ->
        {
            if (e.isRightClick())
            {
                setRange(which, -1, -1);
                refresh();
                return;
            }
            Dialogs.text(this, name.replace("&6", ""), List.of("min max, separated by a space"), "", text ->
            {
                String[] parts = text.trim().split("\\s+");
                if (parts.length != 2)
                    return "Write two numbers: min max";
                try
                {
                    double min = Double.parseDouble(parts[0]);
                    double max = Double.parseDouble(parts[1]);
                    if (min < 0 || max < min)
                        return "Min can't be negative or over max";
                    if (!decimals && (min != Math.rint(min) || max != Math.rint(max)))
                        return "Whole numbers only";
                    setRange(which, min, max);
                    return null;
                } catch (NumberFormatException ex)
                {
                    return "Write two numbers: min max";
                }
            });
        });
    }

    private void setRange(String which, double min, double max)
    {
        switch (which)
        {
            case "price" ->
            {
                _priceMin = min;
                _priceMax = max;
            }
            case "stack" ->
            {
                _stackMin = min < 0 ? 64 : (int) min;
                _stackMax = max < 0 ? 64 : Math.max(1, (int) max);
                _stackMin = Math.max(1, _stackMin);
            }
            case "fill" ->
            {
                _fillMin = (int) min;
                _fillMax = (int) max;
            }
            case "time" ->
            {
                _fillTimeMin = (int) min;
                _fillTimeMax = (int) max;
            }
            default -> { }
        }
    }

    private Button enchantButton()
    {
        List<String> lore = new ArrayList<>();
        lore.add("&bM3 &9moves the selector");
        lore.add("&bM1 &a+1 &bSM1 &a+5 &bM2 &c-1 &bSM2 &c-5");
        lore.add(line(EnchantField.AMOUNT, "Enchants per item", String.valueOf(_enchantAmount)));
        lore.add(line(EnchantField.MAX_LEVEL, "Max level", _levelMax < 0 ? "vanilla" : String.valueOf(_levelMax)));
        lore.add(line(EnchantField.MIN_LEVEL, "Min level", _levelMin < 0 ? "vanilla" : String.valueOf(_levelMin)));
        lore.add(line(EnchantField.RESTRICTION, "Restriction", _restriction.name()));
        return new Button(39, Icons.icon(Material.ENCHANTING_TABLE, "&6Enchants", lore), e ->
        {
            ClickType click = e.getClick();
            if (click == ClickType.MIDDLE)
            {
                _enchantField = EnchantField.values()[(_enchantField.ordinal() + 1) % EnchantField.values().length];
                refresh();
                return;
            }
            int change = switch (click)
            {
                case LEFT -> 1;
                case SHIFT_LEFT -> 5;
                case RIGHT -> -1;
                case SHIFT_RIGHT -> -5;
                default -> 0;
            };
            switch (_enchantField)
            {
                case AMOUNT -> _enchantAmount = Math.max(0, _enchantAmount + change);
                case MAX_LEVEL -> _levelMax = Math.max(Math.max(-1, _levelMin), _levelMax + change);
                case MIN_LEVEL -> _levelMin = Math.max(-1, _levelMax < 0 ? _levelMin + change : Math.min(_levelMax, _levelMin + change));
                case RESTRICTION -> _restriction = Restriction.values()[(_restriction.ordinal() + 1) % Restriction.values().length];
            }
            refresh();
        });
    }

    private String line(EnchantField field, String name, String value)
    {
        return (field == _enchantField ? "&b&l==> " : "&9") + name + ": &e" + value;
    }

    // Generating =====================================================================================================

    private void generate()
    {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int slot = 0; slot < 27; slot++)
        {
            if (_generated[slot] != null && _generated[slot].picked)
                continue;
            if (slot >= _generateAmount)
            {
                _generated[slot] = null;
                continue;
            }

            Generated generated = new Generated();
            generated.item = new ItemStack(MATERIALS[random.nextInt(MATERIALS.length)]);
            if (_enchantAmount > 0 && (ImusAPI._metods.isTool(generated.item) || ImusAPI._metods.isArmor(generated.item)))
                enchant(generated.item, random);
            generated.price = _priceMin < 0 ? null : Math.round(random.nextDouble(_priceMin, _priceMax + 1e-9) * 100) / 100.0;
            generated.maxStock = random.nextInt(_stackMin, _stackMax + 1);
            generated.fillAmount = _fillMin < 0 ? -1 : random.nextInt(_fillMin, _fillMax + 1);
            generated.fillDelay = _fillTimeMin < 0 ? -1 : random.nextInt(_fillTimeMin, _fillTimeMax + 1);
            _generated[slot] = generated;
        }
    }

    private void enchant(ItemStack item, ThreadLocalRandom random)
    {
        List<Enchantment> candidates = new ArrayList<>(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream().toList());
        Collections.shuffle(candidates);
        ItemMeta meta = item.getItemMeta();
        int added = 0;
        for (Enchantment enchant : candidates)
        {
            if (added >= _enchantAmount)
                break;
            if (_restriction != Restriction.NONE && !enchant.canEnchantItem(item))
                continue;
            if (_restriction == Restriction.ALL && meta.getEnchants().keySet().stream().anyMatch(enchant::conflictsWith))
                continue;
            int min = _levelMin < 0 ? 1 : _levelMin;
            int max = _levelMax < 0 ? enchant.getMaxLevel() : _levelMax;
            int level = random.nextInt(Math.max(1, min), Math.max(Math.max(1, min), max) + 1);
            meta.addEnchant(enchant, level, true);
            added++;
        }
        item.setItemMeta(meta);
    }

    private void confirm()
    {
        int count = 0;
        for (Generated generated : _generated)
        {
            if (generated == null || !generated.picked)
                continue;
            PriceRule price = generated.price == null ? PriceRule.MATERIAL : new PriceRule.Fixed(generated.price);
            Listing listing = new Listing(UUID.randomUUID(), ItemKeys.of(generated.item), generated.maxStock, price);
            _shop.addListing(listing);
            _shop.setStock(listing, new StockRules(generated.maxStock, generated.fillAmount, generated.fillDelay));
            count++;
        }
        getPlayer().sendMessage(Metods.msgC("&9" + count + " &aitems added to the shop"));
        _service.warnAboutBuyBackProfit(getPlayer(), _shop);
        openNext(new ShopEditorMenu(_service, _shop));
    }
}
