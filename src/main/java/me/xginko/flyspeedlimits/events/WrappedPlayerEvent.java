package me.xginko.flyspeedlimits.events;

import me.xginko.flyspeedlimits.manager.WrappedPlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class WrappedPlayerEvent extends Event {

    private final @NotNull WrappedPlayer wrappedPlayer;

    public WrappedPlayerEvent(@NotNull WrappedPlayer wrappedPlayer) {
        super(true); // Since this gets updated in its own Thread
        this.wrappedPlayer = wrappedPlayer;
    }

    public WrappedPlayerEvent(boolean isAsync, @NotNull WrappedPlayer wrappedPlayer) {
        super(isAsync);
        this.wrappedPlayer = wrappedPlayer;
    }

    public @NotNull WrappedPlayer getWrappedPlayer() {
        return wrappedPlayer;
    }
}
