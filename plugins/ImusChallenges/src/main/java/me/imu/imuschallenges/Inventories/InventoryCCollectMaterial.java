package me.imu.imuschallenges.Inventories;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import imu.iAPI.Menus.Viewer;
import org.bukkit.Material;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * /ic view materials: who collected how many materials first, one player's materials, and the materials nobody
 * has yet. Admins can take a material off a player, or leave it out of the challenge, with a right click.
 */
public class InventoryCCollectMaterial extends Menu
{
    /**
     * Admins can remove and exclude materials. Ops have it, as it isn't registered.
     */
    public static final String PERM_ADMIN = "ic.collection.admin";
    static final int PREVIOUS = 45, OWN = 47, UNKNOWN = 49, PLAYERS = 51, NEXT = 53;

    private enum View { PLAYERS, COLLECTED, UNKNOWN }

    private final CollectMenuGame _game;
    private final Pager<Button> _pager = Pager.rows(0, 5);
    private View _view = View.PLAYERS;
    private List<Button> _buttons = List.of();
    // Materials changed this time, shown marked
    private final Set<String> _changed = new HashSet<>();
    // Answers to an earlier view that come late are dropped
    private int _request;

    public InventoryCCollectMaterial(CollectMenuGame game)
    {
        super("&9Collect Material Challenge", 6);
        _game = game;
    }

    @Override
    protected void onOpen(Viewer viewer)
    {
        if (_request == 0)
            showPlayers(viewer);
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fillRow(5, Icon.filler(Material.ORANGE_STAINED_GLASS_PANE));
        canvas.set(OWN, Icon.of(Material.PLAYER_HEAD, "&eCollected Materials"), click -> showCollected(click.viewer(), click.viewer().id(), click.viewer().name()));
        canvas.set(UNKNOWN, Icon.of(Material.DIAMOND_BLOCK, "&5Unknown Materials"), click -> showUnknown(click.viewer()));
        canvas.set(PLAYERS, Icon.of(Material.PLAYER_HEAD, "&ePlayers Materials"), click -> showPlayers(click.viewer()));
        _pager.render(canvas, _buttons, button -> button, PREVIOUS, NEXT);
    }

    private int next(View view)
    {
        _view = view;
        _buttons = List.of();
        _changed.clear();
        _pager.setPage(0);
        refresh();
        return ++_request;
    }

    private void show(int request, List<Button> buttons)
    {
        if (request != _request)
            return;
        _buttons = buttons;
        refresh();
    }

    private void showPlayers(Viewer viewer)
    {
        int request = next(View.PLAYERS);
        _game.playerCounts(counts ->
        {
            List<Button> buttons = new ArrayList<>();
            for (CollectMenuGame.PlayerCount count : counts)
            {
                Icon icon = Icon.of(Material.PLAYER_HEAD, "&e" + count.name(), "&7=========================", "&9Materials: &6" + count.count(),
                        "    ", "    ", "&9(&eClick&9) To See More");
                buttons.add(Button.of(icon, click -> showCollected(click.viewer(), count.id(), count.name())));
            }
            show(request, buttons);
        });
    }

    private void showCollected(Viewer viewer, UUID player, String name)
    {
        int request = next(View.COLLECTED);
        boolean admin = viewer.hasPermission(PERM_ADMIN);
        _game.collected(player, collected ->
        {
            if (collected == null)
            {
                viewer.message("&9" + name + " is not online!");
                return;
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            List<Button> buttons = new ArrayList<>();
            int number = collected.size();
            for (CollectMenuGame.Collected material : collected)
            {
                List<String> lore = new ArrayList<>(List.of("&9===> &6" + number--, "&e" + dateFormat.format(new Date(material.time()))));
                if (admin)
                    lore.addAll(List.of(" ", " ", "&6=== OP ONLY ===", "&4 REMOVE FROM DATABASE BY ==> &bM2"));
                buttons.add(materialButton(material.material(), lore, admin, "&cRemoved", click ->
                        _game.removeCollected(player, material.material())));
            }
            show(request, buttons);
        });
    }

    private void showUnknown(Viewer viewer)
    {
        int request = next(View.UNKNOWN);
        boolean admin = viewer.hasPermission(PERM_ADMIN);
        _game.uncollected(materials ->
        {
            List<Button> buttons = new ArrayList<>();
            int number = materials.size();
            for (String material : materials)
            {
                List<String> lore = new ArrayList<>(List.of("&9=> &2" + number--, " ", "&9=> Called &6" + material));
                if (admin)
                    lore.addAll(List.of(" ", " ", "&6=== OP ONLY ===", "&4 ADD TO EXCLUDED BY ==> &bM2"));
                buttons.add(materialButton(material, lore, admin, "&cExcluded", click ->
                        _game.exclude(material, () -> click.message("&9Added &6" + material + " &9to excluded database!"))));
            }
            show(request, buttons);
        });
    }

    /**
     * A material, which admins change with a right click; it is then marked and can't be clicked again.
     */
    private Button materialButton(String material, List<String> lore, boolean admin, String changedName, Consumer<Click> change)
    {
        Icon icon = Icon.of(material).lore(lore.toArray(String[]::new));
        if (!admin)
            return Button.display(icon);
        return new Button(icon, click ->
        {
            if (!click.isRight() || !click.viewer().hasPermission(PERM_ADMIN) || !_changed.add(material))
                return;
            change.accept(click);
            int index = indexOf(click);
            if (index >= 0)
                _buttons.set(index, Button.display(icon.name(changedName).glow(true)));
        });
    }

    private int indexOf(Click click)
    {
        int index = _pager.page() * _pager.pageSize() + click.slot();
        return index < _buttons.size() ? index : -1;
    }

    /**
     * Which list is shown, for tests.
     */
    String view()
    {
        return _view.name();
    }
}
