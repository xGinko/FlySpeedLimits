package me.xginko.flyspeedlimits.events;

import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AsyncWrappedPlayerUpdateEvent extends WrappedPlayerEvent {

    private static final @NotNull HandlerList handlers = new HandlerList();

    public AsyncWrappedPlayerUpdateEvent(@NotNull WrappedPlayer wrappedPlayer) {
        super(true, wrappedPlayer);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
