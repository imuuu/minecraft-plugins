package me.imu.imusminiquests.Quests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A set of materials or entity types parsed from config strings:
 * <ul>
 *     <li>{@code OAK_LOG} / {@code ZOMBIE} - one material or entity type</li>
 *     <li>{@code *_ORE} - every name matching the wildcard</li>
 *     <li>{@code #logs} / {@code #minecraft:skeletons} - a vanilla (or datapack) tag</li>
 *     <li>{@code @hostile} - every hostile mob (entities only)</li>
 *     <li>{@code *} or an empty list - anything (players are never counted as "anything")</li>
 * </ul>
 */
public class TargetSet
{
    private final Set<Material> _materials = EnumSet.noneOf(Material.class);
    private final Set<EntityType> _entities = EnumSet.noneOf(EntityType.class);
    private final List<String> _rawTargets;
    private boolean _any;
    private boolean _anyHostile;

    private TargetSet(List<String> rawTargets)
    {
        _rawTargets = List.copyOf(rawTargets);
    }

    public static TargetSet parseMaterials(List<String> targets, Logger log, String context)
    {
        TargetSet set = new TargetSet(targets);
        if (targets.isEmpty()) set._any = true;

        for (String raw : targets)
        {
            String target = raw.trim();
            if (target.equals("*"))
            {
                set._any = true;
            }
            else if (target.startsWith("#"))
            {
                NamespacedKey key = NamespacedKey.fromString(target.substring(1).toLowerCase(Locale.ROOT));
                Tag<Material> tag = key == null ? null : Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class);
                if (tag == null && key != null) tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, key, Material.class);
                if (tag == null) log.warning(context + ": unknown block/item tag " + target);
                else set._materials.addAll(tag.getValues());
            }
            else if (target.contains("*"))
            {
                Pattern pattern = wildcard(target);
                for (Material m : Material.values())
                {
                    if (!m.isLegacy() && pattern.matcher(m.name()).matches()) set._materials.add(m);
                }
            }
            else
            {
                Material m = Material.matchMaterial(target);
                if (m == null) log.warning(context + ": unknown material " + target);
                else set._materials.add(m);
            }
        }
        return set;
    }

    public static TargetSet parseEntities(List<String> targets, Logger log, String context)
    {
        TargetSet set = new TargetSet(targets);
        if (targets.isEmpty()) set._any = true;

        for (String raw : targets)
        {
            String target = raw.trim();
            if (target.equals("*"))
            {
                set._any = true;
            }
            else if (target.equalsIgnoreCase("@hostile"))
            {
                set._anyHostile = true;
            }
            else if (target.startsWith("#"))
            {
                NamespacedKey key = NamespacedKey.fromString(target.substring(1).toLowerCase(Locale.ROOT));
                Tag<EntityType> tag = key == null ? null : Bukkit.getTag(Tag.REGISTRY_ENTITY_TYPES, key, EntityType.class);
                if (tag == null) log.warning(context + ": unknown entity tag " + target);
                else set._entities.addAll(tag.getValues());
            }
            else if (target.contains("*"))
            {
                Pattern pattern = wildcard(target);
                for (EntityType type : EntityType.values())
                {
                    if (type != EntityType.UNKNOWN && pattern.matcher(type.name()).matches()) set._entities.add(type);
                }
            }
            else
            {
                try
                {
                    set._entities.add(EntityType.valueOf(target.toUpperCase(Locale.ROOT)));
                }
                catch (IllegalArgumentException e)
                {
                    log.warning(context + ": unknown entity type " + target);
                }
            }
        }
        return set;
    }

    private static Pattern wildcard(String target)
    {
        String regex = Arrays.stream(target.toUpperCase(Locale.ROOT).split("\\*", -1))
                .map(Pattern::quote)
                .reduce((a, b) -> a + ".*" + b)
                .orElse("");
        return Pattern.compile(regex);
    }

    public boolean matches(Material material)
    {
        return _any || _materials.contains(material);
    }

    public boolean matches(Entity entity)
    {
        if (_entities.contains(entity.getType())) return true;
        if (_anyHostile && entity instanceof Enemy) return true;
        return _any && entity.getType() != EntityType.PLAYER;
    }

    public boolean isAny() {return _any;}

    public Set<Material> getMaterials() {return Collections.unmodifiableSet(_materials);}

    /**
     * The targets as written in the config, for generated lore texts.
     */
    public List<String> getRawTargets() {return _rawTargets;}
}
