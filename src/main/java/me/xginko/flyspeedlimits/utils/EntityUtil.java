package me.xginko.flyspeedlimits.utils;

import com.cryptomorin.xseries.XEntityType;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityUtil {

    public static final Set<EntityType> BUKKIT_BOATS = Arrays.stream(XEntityType.values())
            .filter(xEntityType -> xEntityType.name().toUpperCase().contains("BOAT"))
            .filter(XEntityType::isSupported)
            .map(XEntityType::get)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));

}
