/*
 * This file is part of TabTPS, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.xginko.flyspeedlimits.utils.tickdata;

import io.papermc.lib.PaperLib;
import me.xginko.flyspeedlimits.utils.Crafty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public final class SpigotReflection {

  private static SpigotReflection instance;
  private static Class<?> MinecraftServer_class;
  private static MethodHandle MinecraftServer_getServer_method;
  private static Field MinecraftServer_recentTps_field, MinecraftServer_recentTickTimes_field;

  public static SpigotReflection getInstance() {
    if (instance == null) {
      MinecraftServer_class = Crafty.needNMSClassOrElse("MinecraftServer", "net.minecraft.server.MinecraftServer");
      MinecraftServer_getServer_method = needStaticMethod(MinecraftServer_class, "getServer", MinecraftServer_class);
      MinecraftServer_recentTps_field = needField(MinecraftServer_class, "recentTps"); // Spigot added field
      MinecraftServer_recentTickTimes_field = tickTimesField();
      instance = new SpigotReflection();
    }
    return instance;
  }

  private static @NonNull Field tickTimesField() {
    final String tickTimes;
    final int ver = PaperLib.getMinecraftVersion();
    if (ver < 13) {
      tickTimes = "h";
    } else if (ver == 13) {
      tickTimes = "d";
    } else if (ver == 14 || ver == 15) {
      tickTimes = "f";
    } else if (ver == 16) {
      tickTimes = "h";
    } else if (ver == 17) {
      tickTimes = "n";
    } else if (ver == 18) {
      tickTimes = "o";
    } else if (ver == 19 || ver == 20 && PaperLib.getMinecraftPatchVersion() < 3) {
      tickTimes = "k";
    } else if (ver == 20 && PaperLib.getMinecraftPatchVersion() < 6) {
      tickTimes = "ac";
    } else if (ver == 20 || ver == 21) {
      tickTimes = "ab";
    } else {
      throw new IllegalStateException("Don't know tickTimes field name!");
    }
    return needField(MinecraftServer_class, tickTimes);
  }

  public double getAverageTickTime() {
    final Object server = invokeOrThrow(MinecraftServer_getServer_method);
    try {
      final long[] recentMspt = (long[]) MinecraftServer_recentTickTimes_field.get(server);
      return toMilliseconds(average(recentMspt));
    } catch (final IllegalAccessException e) {
      throw new IllegalStateException("Failed to get server mspt", e);
    }
  }

  private static double toMilliseconds(final double time) {
    return time * 1.0E-6D;
  }

  private static double average(final long @NonNull [] longs) {
    long i = 0L;
    for (final long l : longs) {
      i += l;
    }
    return i / (double) longs.length;
  }

  public double @NonNull [] getTPS() {
    final Object server = invokeOrThrow(MinecraftServer_getServer_method);
    try {
      return (double[]) MinecraftServer_recentTps_field.get(server);
    } catch (final IllegalAccessException e) {
      throw new IllegalStateException("Failed to get server TPS", e);
    }
  }

  private static @NonNull MethodHandle needStaticMethod(final @NonNull Class<?> holderClass, final @NonNull String methodName, final @NonNull Class<?> returnClass, final @NonNull Class<?> @NonNull ... parameterClasses) {
    return Objects.requireNonNull(
      Crafty.findStaticMethod(holderClass, methodName, returnClass, parameterClasses),
      String.format(
        "Could not locate static method '%s' in class '%s'",
        methodName,
        holderClass.getCanonicalName()
      )
    );
  }

  private static @NonNull Field needField(final @NonNull Class<?> holderClass, final @NonNull String fieldName) {
    final Field field;
    try {
      field = holderClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    } catch (final NoSuchFieldException e) {
      throw new IllegalStateException(String.format("Unable to find field '%s' in class '%s'", fieldName, holderClass.getCanonicalName()), e);
    }
  }

  private static @Nullable Object invokeOrThrow(final @NonNull MethodHandle methodHandle, final @Nullable Object @NonNull ... params) {
    try {
      if (params.length == 0) {
        return methodHandle.invoke();
      }
      return methodHandle.invokeWithArguments(params);
    } catch (final Throwable throwable) {
      throw new IllegalStateException(String.format("Unable to invoke method with args '%s'", Arrays.toString(params)), throwable);
    }
  }
}
