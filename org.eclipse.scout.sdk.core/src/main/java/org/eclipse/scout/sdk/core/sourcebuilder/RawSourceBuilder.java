/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.sourcebuilder;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link RawSourceBuilder}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class RawSourceBuilder implements ISourceBuilder {
  private String m_content;

  public RawSourceBuilder(String content) {
    setContent(content);
  }

  public String getContent() {
    return m_content;
  }

  public void setContent(String content) {
    if (content == null) {
      m_content = null;
    }
    else {
      m_content = content.trim();
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    if (m_content != null) {
      source.append(m_content.replace("\n", lineDelimiter));
    }
    else {
      source.append("null");
    }
  }
}