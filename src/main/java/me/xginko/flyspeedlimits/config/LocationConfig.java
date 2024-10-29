package me.xginko.flyspeedlimits.config;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import me.xginko.flyspeedlimits.FlySpeedLimits;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LocationConfig {

    public final FlightConfig flight;

    private final Set<String> worlds;
    private final double minX, maxX, minY, maxY, minZ, maxZ;

    public LocationConfig(String configPath) {
        Config config = FlySpeedLimits.config();
        this.worlds = new HashSet<>(config.getList(configPath + ".locational-conditions.worlds",
                Arrays.asList("world", "world_nether", "world_the_end")));
        this.maxX = config.getDouble(configPath + ".locational-conditions.max-x", 30_000_000);
        this.minX = config.getDouble(configPath + ".locational-conditions.min-x", -30_000_000);
        this.maxY = config.getDouble(configPath + ".locational-conditions.max-y", 30_000_000);
        this.minY = config.getDouble(configPath + ".locational-conditions.min-y", -30_000_000);
        this.maxZ = config.getDouble(configPath + ".locational-conditions.max-z", 30_000_000);
        this.minZ = config.getDouble(configPath + ".locational-conditions.min-z", -30_000_000);
        this.flight = new FlightConfig(configPath);
    }

    public boolean appliesAt(Location location) {
        return  worlds.contains(location.getWorld().getName())
                && location.getX() >= minX && location.getX() <= maxX
                && location.getY() >= minY && location.getY() <= maxY
                && location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public static void addExamples(String configPath) {
        ConfigFile configFile = FlySpeedLimits.config().master();
        // Spawn
        configFile.addExample(configPath + ".spawn.locational-conditions.worlds",
                Arrays.asList("world", "world_nether", "world_the_end"));
        configFile.addExample(configPath + ".spawn.locational-conditions.max-x", 3000);
        configFile.addExample(configPath + ".spawn.locational-conditions.min-x", -3000);
        configFile.addExample(configPath + ".spawn.locational-conditions.max-y", 3000);
        configFile.addExample(configPath + ".spawn.locational-conditions.min-y", -3000);
        configFile.addExample(configPath + ".spawn.locational-conditions.max-z", 3000);
        configFile.addExample(configPath + ".spawn.locational-conditions.min-z", -3000);
        // Nether ceiling
        configFile.addExample(configPath + ".nether-ceiling.locational-conditions.worlds",
                Collections.singletonList("world_nether"));
        configFile.addExample(configPath + ".nether-ceiling.locational-conditions.max-x", 30_000_000);
        configFile.addExample(configPath + ".nether-ceiling.locational-conditions.min-x", -30_000_000);
        configFile.addExample(configPath + ".nether-ceiling.locational-conditions.max-y", 30_000_000);
        configFile.addExample(configPath + ".nether-ceiling.locational-conditions.min-y", 127);
        configFile.addExample(configPath + ".nether-ceiling.locational-conditions.max-z", 30_000_000);
        configFile.addExample(configPath + ".nether-ceiling.locational-conditions.min-z", -30_000_000);
        // Global
        configFile.addExample(configPath + ".global.locational-conditions.worlds",
                Arrays.asList("world", "world_nether", "world_the_end"));
        configFile.addExample(configPath + ".global.locational-conditions.max-x", 30_000_000);
        configFile.addExample(configPath + ".global.locational-conditions.min-x", -30_000_000);
        configFile.addExample(configPath + ".global.locational-conditions.max-y", 30_000_000);
        configFile.addExample(configPath + ".global.locational-conditions.min-y", -30_000_000);
        configFile.addExample(configPath + ".global.locational-conditions.max-z", 30_000_000);
        configFile.addExample(configPath + ".global.locational-conditions.min-z", -30_000_000);
    }
}

