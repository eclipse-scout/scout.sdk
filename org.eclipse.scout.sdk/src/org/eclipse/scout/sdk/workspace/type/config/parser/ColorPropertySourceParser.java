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
package org.eclipse.scout.sdk.workspace.type.config.parser;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.graphics.RGB;

/**
 * <h3>{@link ColorPropertySourceParser}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 27.02.2013
 */
public class ColorPropertySourceParser implements IPropertySourceParser<RGB> {

  @Override
  public String formatSourceValue(RGB value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    if (value == null) {
      return "null";
    }
    StringBuilder rgbSt = new StringBuilder(6);
    rgbSt.append(Integer.toHexString((value.red << 16) | (value.green << 8) | (value.blue)));
    while (rgbSt.length() < 6) {
      rgbSt.insert(0, '0');
    }
    rgbSt.insert(0, '"');
    rgbSt.append('"');
    return rgbSt.toString().toUpperCase();
  }

  @Override
  public RGB parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    String input = PropertyMethodSourceUtility.parseReturnParameterString(source, context, superTypeHierarchy);
    if (input == null || input.length() == 0) {
      return null;
    }
    if (!input.matches("(|[A-Fa-f0-9]{6})")) {
      throw new CoreException(new ScoutStatus(input));
    }
    int i = Integer.parseInt(input, 16);
    return new RGB((i >> 16) & 0xff, (i >> 8) & 0xff, (i) & 0xff);
  }
}
