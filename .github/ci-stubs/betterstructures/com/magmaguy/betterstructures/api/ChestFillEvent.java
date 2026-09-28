package com.magmaguy.betterstructures.api;

import org.bukkit.block.Container;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

// Compile-only stand-in for BetterStructures' ChestFillEvent, used by CI because the plugin
// isn't published to any Maven repository. It is a provided dependency and never ends up in a
// plugin jar; on the server the real BetterStructures class is used. Only what the plugins in
// this repo call is declared here.
public class ChestFillEvent extends Event
{
    private static final HandlerList HANDLERS = new HandlerList();

    private final Container container;

    public ChestFillEvent(Container container)
    {
        this.container = container;
    }

    public Container getContainer()
    {
        return container;
    }

    @Override
    public HandlerList getHandlers()
    {
        return HANDLERS;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }
}
