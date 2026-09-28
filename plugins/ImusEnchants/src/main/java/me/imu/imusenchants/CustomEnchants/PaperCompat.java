package me.imu.imusenchants.CustomEnchants;

import org.bukkit.Bukkit;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.logging.Level;

// Paper-only API used through reflection, the project compiles against spigot-api.
// Every call has a Spigot fallback or turns itself off with a log line.
public class PaperCompat
{
	private static Method _retrieveHook;
	private static boolean _retrieveChecked;

	// Reels the fishing hook in as if the player right clicked (Paper FishHook#retrieve)
	public static boolean RetrieveHook(FishHook hook, EquipmentSlot hand)
	{
		if (!_retrieveChecked)
		{
			_retrieveChecked = true;
			try
			{
				_retrieveHook = FishHook.class.getMethod("retrieve", EquipmentSlot.class);
			}
			catch (NoSuchMethodException e)
			{
				Bukkit.getLogger().warning("[ImusEnchants] FishHook#retrieve not found, Auto Reel needs a newer Paper build");
			}
		}

		if (_retrieveHook == null)
			return false;

		try
		{
			_retrieveHook.invoke(hook, hand);
			return true;
		}
		catch (ReflectiveOperationException e)
		{
			return false;
		}
	}

	// Registers Paper's EntityKnockbackByEntityEvent. The handler gets the knocked back player and
	// the knockback vector, which can be scaled in place. Returns false when the event doesn't exist.
	@SuppressWarnings("unchecked")
	public static boolean RegisterKnockback(Plugin plugin, Listener owner, BiConsumer<Player, Vector> handler)
	{
		final Class<? extends Event> eventClass;
		final Method getEntity;
		final Method getAcceleration;
		try
		{
			eventClass = (Class<? extends Event>) Class.forName("com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent");
			getEntity = eventClass.getMethod("getEntity");
			getAcceleration = eventClass.getMethod("getAcceleration");
		}
		catch (ReflectiveOperationException e)
		{
			return false;
		}

		Bukkit.getPluginManager().registerEvent(eventClass, owner, EventPriority.HIGH, (listener, event) ->
		{
			if (!eventClass.isInstance(event))
				return;

			try
			{
				Object entity = getEntity.invoke(event);
				if (!(entity instanceof Player))
					return;

				handler.accept((Player) entity, (Vector) getAcceleration.invoke(event));
			}
			catch (ReflectiveOperationException e)
			{
				plugin.getLogger().log(Level.WARNING, "Knockback event failed", e);
			}
		}, plugin, true);
		return true;
	}
}
