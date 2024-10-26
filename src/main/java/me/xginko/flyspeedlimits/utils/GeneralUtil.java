package me.xginko.flyspeedlimits.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Random;

public class GeneralUtil {

    public static final Random RANDOM = new Random();

    public static @NotNull String formatDuration(@NotNull Duration duration) {
        if (duration.isNegative()) duration = duration.negated();

        final int days = (int) (duration.toHours() % 24);
        final int hours = (int) (duration.toHours() % 24);
        final int minutes = (int) (duration.toMinutes() % 60);

        if (days > 0) {
            return String.format("%02dd %02dh %02dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%02dh %02dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%02dm %02ds", minutes, (int) (duration.getSeconds() % 60));
        } else {
            return String.format("%02ds", (int) (duration.getSeconds() % 60));
        }
    }
}
