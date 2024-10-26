package me.xginko.flyspeedlimits.utils;

import com.cryptomorin.xseries.XEntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityUtil {

    public static final Set<org.bukkit.entity.EntityType> BUKKIT_BOATS = Arrays.stream(XEntityType.values())
            .filter(xEntityType -> xEntityType.name().toUpperCase().contains("BOAT"))
            .filter(XEntityType::isSupported)
            .map(XEntityType::get)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(org.bukkit.entity.EntityType.class)));

    public static final Set<com.github.retrooper.packetevents.protocol.entity.type.EntityType> PACKET_BOATS = Sets.newHashSet(
            EntityTypes.BOAT,
            EntityTypes.CHEST_BOAT);
}
