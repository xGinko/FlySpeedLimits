package me.xginko.flyspeedlimits.events;

import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WrappedPlayerUpdateEvent extends WrappedPlayerEvent {

    private static final @NotNull HandlerList handlers = new HandlerList();

    public WrappedPlayerUpdateEvent(@NotNull WrappedPlayer wrappedPlayer) {
        super(wrappedPlayer);
    }

    public WrappedPlayerUpdateEvent(boolean isAsync, @NotNull WrappedPlayer wrappedPlayer) {
        super(isAsync, wrappedPlayer);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
