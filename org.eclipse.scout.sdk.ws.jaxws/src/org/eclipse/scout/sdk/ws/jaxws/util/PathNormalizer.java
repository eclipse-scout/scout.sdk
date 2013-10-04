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
package org.eclipse.scout.sdk.ws.jaxws.util;

import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.StringUtility;

/**
 * Ensures leading and trailing file separators for the given file type.
 */
public class PathNormalizer {
  public static final String SLASH_SUFFIX = "/";

  private PathNormalizer() {
  }

  public static String toUrlPattern(String urlPattern) {
    if (!StringUtility.hasText(urlPattern)) {
      return null;
    }
    return new Path(urlPattern).makeAbsolute().removeTrailingSeparator().toString();
  }

  public static String toUrlPattern(String alias, String urlPattern) {
    if (!StringUtility.hasText(alias) || !StringUtility.hasText(urlPattern)) {
      return null;
    }
    return PathNormalizer.toUrlPattern(new Path(alias).append(urlPattern).toString());
  }

  public static String toServletAlias(String servletAlias) {
    if (!StringUtility.hasText(servletAlias)) {
      return null;
    }
    return new Path(servletAlias).makeAbsolute().removeTrailingSeparator().toString();
  }

  public static String toWsdlPath(String wsdlPath) {
    if (!StringUtility.hasText(wsdlPath)) {
      return null;
    }
    return new Path(wsdlPath).makeRelative().removeTrailingSeparator().toString();
  }

  /**
   * Appends a "/" at the end of the given non-empty target namespace if it does not exist.
   * 
   * @param targetNamespace
   *          name of the target namespace
   * @return target namespace with a "/" at the end.
   */
  public static String toTargetNamespace(String targetNamespace) {
    if (!StringUtility.hasText(targetNamespace)) {
      return null;
    }
    if (!targetNamespace.endsWith(SLASH_SUFFIX)) {
      return targetNamespace + SLASH_SUFFIX;
    }
    return targetNamespace;
  }

  /**
   * Removes the "/" at the end of a non-empty target namespace if one exists.
   * 
   * @param targetNamespace
   *          name of the target namespace
   * @return target namespace without a trailing "/" at the end.
   */
  public static String removeTrailingSeparatorFromTargetNamespace(String targetNamespace) {
    if (!StringUtility.hasText(targetNamespace)) {
      return null;
    }
    if (targetNamespace.endsWith(SLASH_SUFFIX)) {
      return targetNamespace.substring(0, targetNamespace.length() - 2);
    }
    return targetNamespace;
  }

  public static String toJarPath(String jarPath) {
    if (!StringUtility.hasText(jarPath)) {
      return null;
    }
    return new Path(jarPath).makeRelative().removeTrailingSeparator().toString();
  }
}
