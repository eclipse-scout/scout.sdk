/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.compatibility;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

public final class PlatformVersionUtility {

  private static Version platformVersion = null;

  public static final String EMPTY_VERSION_STR = "0.0.0";
  public static final String ORG_ECLIPSE_PLATFORM = "org.eclipse.platform";

  private PlatformVersionUtility() {
  }

  /**
   * points to the newest supported version.
   *
   * @return
   */
  public static boolean isLatest() {
    return isLuna();
  }

  public static boolean isLuna() {
    return isLuna(getPlatformVersion());
  }

  public static boolean isLuna(Version v) {
    return v.getMajor() == 4 && v.getMinor() == 4;
  }

  public static boolean isKepler() {
    return isKepler(getPlatformVersion());
  }

  public static boolean isKepler(Version v) {
    return v.getMajor() == 4 && v.getMinor() == 3;
  }

  public static boolean isJunoOrLater() {
    return isJunoOrLater(getPlatformVersion());
  }

  public static boolean isJunoOrLater(Version v) {
    return (v.getMajor() == 3 && v.getMinor() == 8) || (v.getMajor() == 4 && v.getMinor() >= 2);
  }

  public static boolean isLunaOrLater(Version v) {
    return (v.getMajor() == 4 && v.getMinor() >= 4) || v.getMajor() > 4;
  }

  public static boolean isJuno() {
    return isJuno(getPlatformVersion());
  }

  public static boolean isJuno(Version v) {
    return (v.getMajor() == 3 && v.getMinor() == 8) || (v.getMajor() == 4 && v.getMinor() == 2);
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

  /**
   * Gets the version of the currently running platform.<br>
   * To get the version of the currently set target platform use
   * <code>org.eclipse.scout.sdk.util.jdt.JdtUtility.getTargetPlatformVersion()</code>
   *
   * @return The version of the currently running platform.
   */
  public static Version getPlatformVersion() {
    if (platformVersion == null) {
      Version v = Platform.getBundle(ORG_ECLIPSE_PLATFORM).getVersion();
      if (v.getMajor() == 3 && v.getMinor() == 3) {
        // eclipse galileo (3.5) uses platform version 3.3. older versions are not supported.
        v = new Version(3, 5, 0);
      }
      platformVersion = v;
    }
    return platformVersion;
  }

}
