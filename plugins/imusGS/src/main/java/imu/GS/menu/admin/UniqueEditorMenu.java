package imu.GS.menu.admin;

import imu.iAPI.Utilities.TextUtils;
import imu.GS.GsService;
import imu.GS.menu.Icons;
import imu.GS.prices.UniqueItems;
import org.bukkit.Material;
import imu.iAPI.Menus.ClickKind;

import java.util.List;

/**
 * Price of one unique item. The ore buttons change it: left click +, shift + left +5x, right click -,
 * shift + right -5x, middle click +10x. Click the item to confirm.
 */
public class UniqueEditorMenu extends AdminMenu
{
    private static final Material[] ORES = { Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE };
    private static final double[] STEPS = { 0.1, 1, 10, 100, 1000 };

    private final UniqueItems.Unique _unique;
    private double _price;

    public UniqueEditorMenu(GsService service, UniqueItems.Unique unique)
    {
        super(service, null, "Modify", 27);
        _unique = unique;
        _price = unique.price();
    }

    @Override
    protected void draw()
    {
        for (int slot = 0; slot < 27; slot++)
            button(slot, Icons.filler(Material.BLACK_STAINED_GLASS_PANE));

        button(9, Icons.icon(Material.RED_STAINED_GLASS_PANE, "&b<< BACK"), e -> openNext(new UniquesMenu(_service)));
        button(8, Icons.icon(Material.SLIME_BALL, "&bCopy to your inv"),
                e -> getPlayer().getInventory().addItem(_unique.item().clone()));
        button(12, Icons.icon(Material.WATER_BUCKET, "&bRESET PRICE", "&7Back to " + Icons.money(_unique.price())), e ->
        {
            _price = _unique.price();
            refresh();
        });
        button(13, Icons.withLore(_unique.item(), List.of("&b======Unique======", "&5price : &6" + Icons.money(_price),
                "&b======Unique======", "&aCONFIRM by CLICK!")), e ->
        {
            _service.getUniques().setPrice(_unique.key(), _price);
            _service.getDatabase().saveUnique(_unique.key(), _price);
            _service.refreshMenus();
            getPlayer().sendMessage(TextUtils.Color("&eUnique item has been changed!"));
            openNext(new UniquesMenu(_service));
        });
        button(14, Icons.icon(Material.PAPER, "&bWrite the price"), e ->
                Dialogs.number(this, "Unique price", List.of("Price of one item"), _price, 0, 1e12, value -> _price = Math.round(value * 100) / 100.0));
        button(26, Icons.icon(Material.LAVA_BUCKET, "&cRemove as unique item", "", "&bShift click &7to remove"), e ->
        {
            if (!e.isShift())
                return;
            _service.getUniques().remove(_unique.key());
            _service.getDatabase().deleteUnique(_unique.key());
            _service.refreshMenus();
            openNext(new UniquesMenu(_service));
        });

        for (int i = 0; i < ORES.length; i++)
        {
            double step = STEPS[i];
            button(20 + i, Icons.icon(ORES[i], "&e===> &a+&b/&c- &6" + Dialogs.format(step),
                    "&bM1: &a+" + Dialogs.format(step) + "  &bSM1: &a+" + Dialogs.format(step * 5),
                    "&bM2: &c-" + Dialogs.format(step) + "  &bSM2: &c-" + Dialogs.format(step * 5),
                    "&bM3: &a+" + Dialogs.format(step * 10),
                    "&9Price now: &6" + Icons.money(_price)), e ->
            {
                ClickKind click = e.kind();
                double change = switch (click)
                {
                    case LEFT -> step;
                    case SHIFT_LEFT -> step * 5;
                    case RIGHT -> -step;
                    case SHIFT_RIGHT -> -step * 5;
                    case MIDDLE -> step * 10;
                    default -> 0;
                };
                _price = Math.max(0, Math.round((_price + change) * 100) / 100.0);
                refresh();
            });
        }
    }
}
