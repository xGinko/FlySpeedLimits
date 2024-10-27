package me.xginko.flyspeedlimits.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.inventory.ItemStack;

public class MaterialUtil {

    public static boolean isElytra(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() == XMaterial.ELYTRA.parseMaterial();
    }

}
