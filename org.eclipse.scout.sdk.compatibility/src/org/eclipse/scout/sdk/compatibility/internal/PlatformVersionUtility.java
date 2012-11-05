package org.eclipse.scout.sdk.compatibility.internal;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

public final class PlatformVersionUtility {

  private static Version platformVersion = null;

  public static boolean isJunoOrLater() {
    return isJunoOrLater(getPlatformVersion());
  }

  public static boolean isJunoOrLater(Version v) {
    return (v.getMajor() == 3 && v.getMinor() == 8) ||
        (v.getMajor() == 4 && v.getMinor() >= 2);
  }

  public static boolean isJuno() {
    return isJuno(getPlatformVersion());
  }

  public static boolean isJuno(Version v) {
    return (v.getMajor() == 3 && v.getMinor() == 8) ||
        (v.getMajor() == 4 && v.getMinor() == 2);
  }

  public static boolean isE4() {
    return isE4(getPlatformVersion());
  }

  public static boolean isE4(Version v) {
    return v.getMajor() == 4;
  }

  public static boolean isIndigo() {
    return isIndigo(getPlatformVersion());
  }

  public static boolean isIndigo(Version v) {
    return v.getMajor() == 3 && v.getMinor() == 7;
  }

  public static boolean isHelios() {
    return isHelios(getPlatformVersion());
  }

  public static boolean isHelios(Version v) {
    return v.getMajor() == 3 && v.getMinor() == 6;
  }

  public static boolean isGalileo() {
    return isGalileo(getPlatformVersion());
  }

  public static boolean isGalileo(Version v) {
    return v.getMajor() == 3 && v.getMinor() == 5;
  }

  public static Version getPlatformVersion() {
    if (platformVersion == null) {
      Version v = Platform.getBundle("org.eclipse.platform").getVersion();
      if (v.getMajor() == 3 && v.getMinor() == 3) {
        // eclipse galileo (3.5) uses platform version 3.3. older versions are not supported.
        v = new Version(3, 5, 0);
      }
      platformVersion = v;
    }
    return platformVersion;
  }
}
