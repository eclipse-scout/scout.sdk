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
package org.eclipse.scout.sdk.core.sourcebuilder;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AbstractMemberSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public abstract class AbstractMemberSourceBuilder extends AbstractAnnotatableSourceBuilder implements IMemberSourceBuilder {
  private int m_flags;

  public AbstractMemberSourceBuilder(IMember element) {
    super(element);
    setFlags(element.getFlags());
    if (element.getJavaDoc() != null) {
      setComment(new RawSourceBuilder(element.getJavaDoc().toString()));
    }
  }

  public AbstractMemberSourceBuilder(String elementName) {
    super(elementName);
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);
  }

  /**
   * {@link Flags}
   */
  @Override
  public void setFlags(int flags) {
    m_flags = flags;
  }

  /**
   * @return {@link Flags}
   */
  @Override
  public int getFlags() {
    return m_flags;
  }

}
