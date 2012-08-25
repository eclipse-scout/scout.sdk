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
package org.eclipse.scout.sdk.validation;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;

/**
 * <h3>{@link BundleValidator}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 11.03.2012
 */
public final class BundleValidator {

  public static IStatus validateNewBundleName(String bundleName) {
    // validate name
    if (StringUtility.isNullOrEmpty(bundleName)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("ProjectNameMissing"));
    }
    // no double points
    if (bundleName.contains("..")) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Project name is not valid. Valid project names are similar to 'org.eclipse.testapp'.");
    }
    // invalid characters
    if (!bundleName.matches("^[0-9a-zA-Z\\.\\_]*$")) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "The bundle name contains invalid characters. Valid bundle names are similar to (e.g. 'org.eclipse.testapp').");
    }
    // no start and end with number or special characters
    if (bundleName.matches("^[0-9\\_]{1}.*$") || bundleName.matches("^.*[\\_0-9]{1}$")) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Bundle name can not start or end with special characters or digits. Valid project names are similar to (e.g. 'org.eclipse.testapp').");
    }
    // already existing bundle name
    if (Platform.getBundle(bundleName) != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Bundle '" + bundleName + "' already exists.");
    }
    if (ResourcesPlugin.getWorkspace().getRoot().getProject(bundleName).exists()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Bundle '" + bundleName + "' already exists.");
    }
    // warn containing upper case characters
    if (bundleName.matches(".*[A-Z].*")) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, "Project name should contain only lower case characters (e.g. 'org.eclipse.testapp').");
    }
    return Status.OK_STATUS;
  }
}
