package me.xginko.flyspeedlimits.struct;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public enum Permissions {

    // TODO: Bypass permissions for each flight type
    BYPASS(new Permission("speedlimit.bypass", PermissionDefault.FALSE)),

    RELOAD_CMD(new Permission("speedlimit.cmd.reload", PermissionDefault.OP)),
    VERSION_CMD(new Permission("speedlimit.cmd.version", PermissionDefault.OP)),
    NOTIFS_CMD(new Permission("speedlimit.cmd.notifs", PermissionDefault.TRUE));

    private final Permission permission;

    Permissions(Permission permission) {
        this.permission = permission;
    }

    public Permission bukkit() {
        return permission;
    }

    public String string() {
        return permission.getName();
    }

    public static void registerAll(PluginManager pluginManager) {
        for (Permissions perm : Permissions.values()) {
            try {
                pluginManager.addPermission(perm.bukkit());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static void unregisterAll(PluginManager pluginManager) {
        for (Permissions perm : Permissions.values()) {
            try {
                pluginManager.removePermission(perm.bukkit());
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
