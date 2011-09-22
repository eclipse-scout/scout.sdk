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
package org.eclipse.scout.sdk.workspace;

public final class ScoutBundleFilters {

  private ScoutBundleFilters() {
  }

  public static IScoutBundleFilter getOnClasspath(final IScoutBundle refBundle) {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return refBundle.isOnClasspath(bundle);
      }
    };
  }

  public static IScoutBundleFilter getUiSwingFilter() {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return bundle.getType() == IScoutBundle.BUNDLE_UI_SWING;
      }
    };
  }

  public static IScoutBundleFilter getUiSwtFilter() {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return bundle.getType() == IScoutBundle.BUNDLE_UI_SWT;
      }
    };
  }

  public static IScoutBundleFilter getClientFilter() {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return bundle.getType() == IScoutBundle.BUNDLE_CLIENT;
      }
    };
  }

  public static IScoutBundleFilter getSharedFilter() {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return bundle.getType() == IScoutBundle.BUNDLE_SHARED;
      }
    };
  }

  public static IScoutBundleFilter getServerFilter() {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return bundle.getType() == IScoutBundle.BUNDLE_SERVER;
      }
    };
  }

  public static IScoutBundleFilter getAcceptAllFilter() {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return true;
      }
    };
  }

  public static IScoutBundleFilter getNameMatchingBundle(IScoutBundle refBundle, int bundleType) {
    String bundleName = refBundle.getBundleName();
    switch (bundleType) {
      case IScoutBundle.BUNDLE_CLIENT:
        bundleName = bundleName.replaceAll("^(.*\\.)(client|shared|server)(\\.[^.]*)$", "$1client$3");
        break;
      case IScoutBundle.BUNDLE_SHARED:
        bundleName = bundleName.replaceAll("^(.*\\.)(client|shared|server)(\\.[^.]*)$", "$1shared$3");
        break;
      case IScoutBundle.BUNDLE_SERVER:
        bundleName = bundleName.replaceAll("^(.*\\.)(client|shared|server)(\\.[^.]*)$", "$1server$3");
        break;
      default:
        return null;
    }
    final String finalId = bundleName;
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return bundle.getBundleName().equals(finalId);
      }
    };
  }

}
