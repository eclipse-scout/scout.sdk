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
package org.eclipse.scout.sdk.ws.jaxws.validator;

import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;

public final class ServletAliasValidator {

  private ServletAliasValidator() {
  }

  public static boolean validate(final String servletAlias, final IServletAliasValidation validation) {
    if (!StringUtility.hasText(servletAlias) || new Path(servletAlias).makeRelative().isEmpty()) {
      validation.onEmpty();
      return false;
    }

    if (!servletAlias.equals(PathNormalizer.toServletAlias(servletAlias))) {
      validation.onWrongSeparators();
      return false;
    }

    if (!servletAlias.matches("[\\w\\-/]*")) {
      validation.onIllegalCharacters();
      return false;
    }
    return true;
  }
}
