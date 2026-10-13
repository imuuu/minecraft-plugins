package imu.iAPI.Npc;

import imu.iAPI.Menus.Paper.PaperMenus;
import imu.iAPI.Utilities.TextUtils;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.regex.Pattern;

/**
 * NPCs without an NPC plugin: a villager, or a mannequin that looks like a player and can wear any player's skin.
 * Clicking one runs its {@link NpcAction}; sneak + click opens its settings for players with ia.npc.edit.
 * Everything about an NPC is stored on the entity itself, so it lives and dies with its chunk like any mob.
 */
public class NpcManager implements Listener
{
    public static final String PERM_EDIT = "ia.npc.edit";

    public enum NpcType
    {
        VILLAGER,
        PLAYER
    }

    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private static final int LOOK_INTERVAL_TICKS = 3;
    private static final float MAX_YAW_STEP = 30f;
    private static final float MAX_PITCH_STEP = 15f;

    private final Plugin _plugin;
    private final NamespacedKey _keyAction;
    private final NamespacedKey _keyData;
    private final NamespacedKey _keySkin;
    private final NamespacedKey _keyHomeYaw;
    private final NamespacedKey _keyLook;

    private final Map<String, NpcAction> _actions = new LinkedHashMap<>();

    // Set while the settings menu changes a profession, so OnCareerChange lets it through
    private boolean _changingProfession = false;

    // Looking at players, from ImusAPI's config.yml
    private boolean _lookAtPlayers = true;
    private double _lookRange = 8;
    // NPCs turned away from their home direction, they turn back once nobody is near
    private final Set<UUID> _turnedNpcs = new HashSet<>();
    private BukkitTask _lookTask;

    public NpcManager(Plugin plugin)
    {
        _plugin = plugin;
        _keyAction = new NamespacedKey(plugin, "npc_action");
        _keyData = new NamespacedKey(plugin, "npc_data");
        _keySkin = new NamespacedKey(plugin, "npc_skin");
        _keyHomeYaw = new NamespacedKey(plugin, "npc_home_yaw");
        _keyLook = new NamespacedKey(plugin, "npc_look");
        registerAction(new CommandNpcAction());
    }

    public void loadSettings()
    {
        _lookAtPlayers = _plugin.getConfig().getBoolean("npc.look-at-players", true);
        _lookRange = Math.max(1, _plugin.getConfig().getDouble("npc.look-range", 8));

        if (_lookTask != null)
            _lookTask.cancel();
        _lookTask = Bukkit.getScheduler().runTaskTimer(_plugin, this::turnToPlayers, LOOK_INTERVAL_TICKS, LOOK_INTERVAL_TICKS);
    }

    // ==================== ACTIONS ====================

    /**
     * Makes the action available for NPCs. Call {@link #unregisterAction(String)} in onDisable.
     */
    public void registerAction(NpcAction action)
    {
        _actions.put(action.getId(), action);
    }

    /**
     * NPCs with this action keep it, they just do nothing until it is registered again.
     */
    public void unregisterAction(String id)
    {
        _actions.remove(id);
    }

    public NpcAction getAction(String id)
    {
        return id == null ? null : _actions.get(id);
    }

    public Collection<NpcAction> getActions()
    {
        return Collections.unmodifiableCollection(_actions.values());
    }

    public boolean canUseAction(Player player, NpcAction action)
    {
        return action.getPermission() == null || player.hasPermission(action.getPermission());
    }

    // ==================== NPC DATA ====================

    public boolean isNpc(Entity entity)
    {
        return entity instanceof LivingEntity && entity.getPersistentDataContainer().has(_keyAction, PersistentDataType.STRING);
    }

    public NpcType getType(LivingEntity npc)
    {
        return npc instanceof Mannequin ? NpcType.PLAYER : NpcType.VILLAGER;
    }

    public String getActionId(LivingEntity npc)
    {
        return npc.getPersistentDataContainer().get(_keyAction, PersistentDataType.STRING);
    }

    public String getData(LivingEntity npc)
    {
        String data = npc.getPersistentDataContainer().get(_keyData, PersistentDataType.STRING);
        return data == null ? "" : data;
    }

    public void setAction(LivingEntity npc, String actionId, String data)
    {
        PersistentDataContainer pdc = npc.getPersistentDataContainer();
        pdc.set(_keyAction, PersistentDataType.STRING, actionId);
        pdc.set(_keyData, PersistentDataType.STRING, data == null ? "" : data);
    }

    /**
     * Name with &amp; colour codes.
     */
    public String getName(LivingEntity npc)
    {
        Component name = npc.customName();
        return name == null ? "" : LEGACY.serialize(name);
    }

    public void setName(LivingEntity npc, String name)
    {
        npc.customName(LEGACY.deserialize(name));
        npc.setCustomNameVisible(true);
    }

    /**
     * The text under a player NPC's name, with &amp; colour codes.
     */
    public String getSubtitle(Mannequin npc)
    {
        Component description = npc.getDescription();
        return description == null ? "" : LEGACY.serialize(description);
    }

    public void setSubtitle(Mannequin npc, String subtitle)
    {
        npc.setDescription(LEGACY.deserialize(subtitle));
    }

    /**
     * Name of the player whose skin the NPC wears, null for the default skin. A villager keeps it, so the
     * skin comes back when it is turned into a player NPC again.
     */
    public String getSkinName(LivingEntity npc)
    {
        return npc.getPersistentDataContainer().get(_keySkin, PersistentDataType.STRING);
    }

    public static Registry<Villager.Profession> professions()
    {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.VILLAGER_PROFESSION);
    }

    public static Registry<Villager.Type> villagerTypes()
    {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.VILLAGER_TYPE);
    }

    // ==================== CREATING ====================

    /**
     * Spawns a villager NPC where the player stands, facing the way they look, and opens its settings.
     */
    public LivingEntity createAndEdit(Player player, String actionId, String data, String name)
    {
        Location loc = player.getLocation();
        loc.setPitch(0);
        LivingEntity npc = spawn(NpcType.VILLAGER, loc, actionId, data, name);
        openEditor(player, npc);
        return npc;
    }

    public LivingEntity spawn(NpcType type, Location loc, String actionId, String data, String name)
    {
        LivingEntity npc = spawnEntity(type, loc);
        setAction(npc, actionId, data);
        setName(npc, name);
        setHomeYaw(npc, loc.getYaw());
        return npc;
    }

    /**
     * Turns an existing entity into an NPC, e.g. one a plugin made before it used this manager.
     */
    public void adopt(LivingEntity entity, String actionId, String data)
    {
        setup(entity);
        setAction(entity, actionId, data);
    }

    public void openEditor(Player player, LivingEntity npc)
    {
        PaperMenus.open(player, new NpcEditorInventory(this, npc));
    }

    LivingEntity spawnEntity(NpcType type, Location loc)
    {
        if (type == NpcType.PLAYER)
        {
            return loc.getWorld().spawn(loc, Mannequin.class, mannequin ->
            {
                setup(mannequin);
                mannequin.setImmovable(true);
                mannequin.setDescription(Component.empty());
            });
        }

        return loc.getWorld().spawn(loc, Villager.class, villager ->
        {
            setup(villager);
            villager.setProfession(Villager.Profession.NONE);
        });
    }

    void setup(LivingEntity npc)
    {
        npc.setAI(false);
        npc.setInvulnerable(true);
        npc.setSilent(true);
        npc.setCollidable(false);
        npc.setPersistent(true);
        npc.setRemoveWhenFarAway(false);
        npc.setCustomNameVisible(true);
    }

    /**
     * Replaces the NPC with one of the other type. Returns the new entity.
     */
    public LivingEntity changeType(LivingEntity npc, NpcType type, Player editor)
    {
        if (getType(npc) == type)
            return npc;

        Location loc = npc.getLocation();
        float homeYaw = getHomeYaw(npc);
        loc.setYaw(homeYaw);
        loc.setPitch(0);

        LivingEntity replacement = spawnEntity(type, loc);
        setAction(replacement, getActionId(npc), getData(npc));
        setName(replacement, getName(npc));
        setHomeYaw(replacement, homeYaw);
        setLooksAtPlayers(replacement, looksAtPlayers(npc));
        String skin = getSkinName(npc);
        if (skin != null)
            replacement.getPersistentDataContainer().set(_keySkin, PersistentDataType.STRING, skin);
        npc.remove();

        if (replacement instanceof Mannequin mannequin && skin != null)
            setSkin(mannequin, skin, editor);
        return replacement;
    }

    /**
     * Looks up the skin of a player name from Mojang in the background and puts it on the NPC.
     * The skin is stored on the entity, so it isn't fetched again.
     */
    public void setSkin(Mannequin npc, String playerName, Player editor)
    {
        final UUID npcId = npc.getUniqueId();
        PlayerProfile profile = Bukkit.createProfile(playerName);
        profile.update().whenComplete((result, error) ->
        {
            if (!_plugin.isEnabled())
                return;
            Bukkit.getScheduler().runTask(_plugin, () ->
            {
                if (error != null || result == null || !result.hasTextures())
                {
                    editor.sendMessage(TextUtils.Color("&cNo skin found for player &e" + playerName));
                    return;
                }
                if (!(Bukkit.getEntity(npcId) instanceof Mannequin mannequin))
                    return;

                mannequin.setProfile(ResolvableProfile.resolvableProfile(result));
                mannequin.getPersistentDataContainer().set(_keySkin, PersistentDataType.STRING,
                        result.getName() != null ? result.getName() : playerName);
                editor.sendMessage(TextUtils.Color("&aThe NPC now has the skin of &e" + playerName));
            });
        });
    }

    public void resetSkin(Mannequin npc)
    {
        npc.setProfile(Mannequin.defaultProfile());
        npc.getPersistentDataContainer().remove(_keySkin);
    }

    public static boolean isValidPlayerName(String name)
    {
        return PLAYER_NAME.matcher(name).matches();
    }

    public void setProfession(Villager npc, Villager.Profession profession)
    {
        _changingProfession = true;
        try
        {
            npc.setProfession(profession);
            // A villager without a level drops its profession
            if (npc.getVillagerLevel() < 1)
                npc.setVillagerLevel(1);
        } finally
        {
            _changingProfession = false;
        }
    }

    /**
     * Turns the NPC to look at the location, and makes that its home direction.
     */
    public void face(LivingEntity npc, Location target)
    {
        float yaw = yawTowards(npc.getLocation(), target);
        npc.setRotation(yaw, 0);
        setHomeYaw(npc, yaw);
    }

    /**
     * Moves the NPC, facing the location's direction from now on.
     */
    public void moveTo(LivingEntity npc, Location loc)
    {
        npc.teleport(loc);
        npc.setRotation(loc.getYaw(), 0);
        setHomeYaw(npc, loc.getYaw());
    }

    // ==================== LOOKING AT PLAYERS ====================

    /**
     * The direction the NPC faces when nobody is near.
     */
    public void setHomeYaw(LivingEntity npc, float yaw)
    {
        npc.getPersistentDataContainer().set(_keyHomeYaw, PersistentDataType.FLOAT, yaw);
    }

    float getHomeYaw(LivingEntity npc)
    {
        Float yaw = npc.getPersistentDataContainer().get(_keyHomeYaw, PersistentDataType.FLOAT);
        if (yaw != null)
            return yaw;

        setHomeYaw(npc, npc.getYaw());
        return npc.getYaw();
    }

    public boolean looksAtPlayers(LivingEntity npc)
    {
        Byte look = npc.getPersistentDataContainer().get(_keyLook, PersistentDataType.BYTE);
        return look == null || look != 0;
    }

    public void setLooksAtPlayers(LivingEntity npc, boolean look)
    {
        npc.getPersistentDataContainer().set(_keyLook, PersistentDataType.BYTE, (byte) (look ? 1 : 0));
        if (!look)
            _turnedNpcs.add(npc.getUniqueId()); // turns back home on the next update
    }

    public boolean isLookingEnabled()
    {
        return _lookAtPlayers;
    }

    void turnToPlayers()
    {
        // The nearest player of every NPC that has one within range
        Map<LivingEntity, Player> nearest = new HashMap<>();
        Map<LivingEntity, Double> nearestDist = new HashMap<>();
        if (_lookAtPlayers)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (player.getGameMode() == GameMode.SPECTATOR)
                    continue;

                for (Entity entity : player.getNearbyEntities(_lookRange, _lookRange, _lookRange))
                {
                    if (!isNpc(entity))
                        continue;
                    LivingEntity npc = (LivingEntity) entity;
                    if (!looksAtPlayers(npc))
                        continue;

                    double dist = npc.getLocation().distanceSquared(player.getLocation());
                    if (dist > _lookRange * _lookRange)
                        continue;
                    Double best = nearestDist.get(npc);
                    if (best == null || dist < best)
                    {
                        nearest.put(npc, player);
                        nearestDist.put(npc, dist);
                    }
                }
            }
        }

        for (Map.Entry<LivingEntity, Player> entry : nearest.entrySet())
        {
            LivingEntity npc = entry.getKey();
            Location eyes = npc.getEyeLocation();
            Location target = entry.getValue().getEyeLocation();
            getHomeYaw(npc); // remember the home direction before the first turn
            turnTowards(npc, yawTowards(eyes, target), pitchTowards(eyes, target));
            _turnedNpcs.add(npc.getUniqueId());
        }

        // Nobody near any more: back to the home direction
        Iterator<UUID> it = _turnedNpcs.iterator();
        while (it.hasNext())
        {
            Entity entity = Bukkit.getEntity(it.next());
            if (!(entity instanceof LivingEntity npc) || !npc.isValid())
            {
                it.remove();
                continue;
            }
            if (nearest.containsKey(npc))
                continue;

            if (turnTowards(npc, getHomeYaw(npc), 0))
                it.remove();
        }
    }

    /**
     * Turns a step towards the rotation. Returns true when it got there.
     */
    boolean turnTowards(LivingEntity npc, float yaw, float pitch)
    {
        float yawDiff = wrapDegrees(yaw - npc.getYaw());
        float pitchDiff = pitch - npc.getPitch();
        if (Math.abs(yawDiff) < 0.5f && Math.abs(pitchDiff) < 0.5f)
            return true;

        float newYaw = npc.getYaw() + clamp(yawDiff, MAX_YAW_STEP);
        float newPitch = npc.getPitch() + clamp(pitchDiff, MAX_PITCH_STEP);
        npc.setRotation(newYaw, newPitch);
        npc.setBodyYaw(newYaw);
        return Math.abs(yawDiff) <= MAX_YAW_STEP && Math.abs(pitchDiff) <= MAX_PITCH_STEP;
    }

    static float yawTowards(Location from, Location to)
    {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    static float pitchTowards(Location from, Location to)
    {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        return (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
    }

    static float wrapDegrees(float degrees)
    {
        float wrapped = degrees % 360f;
        if (wrapped >= 180f)
            wrapped -= 360f;
        if (wrapped < -180f)
            wrapped += 360f;
        return wrapped;
    }

    static float clamp(float value, float max)
    {
        return Math.max(-max, Math.min(max, value));
    }

    // ==================== EVENTS ====================

    void use(Player player, LivingEntity npc)
    {
        if (player.isSneaking() && player.hasPermission(PERM_EDIT))
        {
            // Inventories must not be opened inside the click event itself
            Bukkit.getScheduler().runTask(_plugin, () -> openEditor(player, npc));
            return;
        }

        NpcAction action = getAction(getActionId(npc));
        if (action == null)
        {
            player.sendMessage(TextUtils.Color("&7Nothing happens."));
            if (player.hasPermission(PERM_EDIT))
                player.sendMessage(TextUtils.Color("&7The plugin of this NPC's action isn't loaded. Sneak + click it to change the action."));
            return;
        }
        String data = getData(npc);
        Bukkit.getScheduler().runTask(_plugin, () -> action.use(player, npc, data));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEntityEvent e)
    {
        if (!isNpc(e.getRightClicked()))
            return;

        // Also stops villager trading, name tags and leads
        e.setCancelled(true);
        if (e.getHand() != EquipmentSlot.HAND)
            return;

        use(e.getPlayer(), (LivingEntity) e.getRightClicked());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAttack(PrePlayerAttackEntityEvent e)
    {
        if (!isNpc(e.getAttacked()))
            return;

        e.setCancelled(true);
        use(e.getPlayer(), (LivingEntity) e.getAttacked());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent e)
    {
        // Invulnerable already, but plugins can still get here; /kill still works
        if (isNpc(e.getEntity()) && e.getCause() != EntityDamageEvent.DamageCause.KILL)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCombust(EntityCombustEvent e)
    {
        if (isNpc(e.getEntity()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCareerChange(VillagerCareerChangeEvent e)
    {
        if (isNpc(e.getEntity()) && !_changingProfession)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTransform(EntityTransformEvent e)
    {
        // Lightning (witch) and zombies (zombie villager) would replace the NPC
        if (isNpc(e.getEntity()))
            e.setCancelled(true);
    }
}
