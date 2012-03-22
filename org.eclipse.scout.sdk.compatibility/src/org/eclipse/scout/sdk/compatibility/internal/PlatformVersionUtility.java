package org.eclipse.scout.sdk.compatibility.internal;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

public final class PlatformVersionUtility {

  private final static Version PLATFORM_VERSION = Platform.getBundle("org.eclipse.platform").getVersion();

  public static boolean isPlatformJuno() {
    return (getPlatformVersion().getMajor() == 3 && getPlatformVersion().getMinor() == 8) ||
        (getPlatformVersion().getMajor() == 4 && getPlatformVersion().getMinor() == 2);
  }

  public static boolean isPlatformE4() {
    return getPlatformVersion().getMajor() == 4;
  }

  public static boolean isPlatformIndigo() {
    return getPlatformVersion().getMajor() == 3 && getPlatformVersion().getMinor() == 7;
  }

  public static boolean isPlatformHelios() {
    return getPlatformVersion().getMajor() == 3 && getPlatformVersion().getMinor() == 6;
  }

  public static boolean isPlatformGalileo() {
    return getPlatformVersion().getMajor() == 3 && getPlatformVersion().getMinor() == 5;
  }

  public static Version getPlatformVersion() {
    return PLATFORM_VERSION;
  }
}
