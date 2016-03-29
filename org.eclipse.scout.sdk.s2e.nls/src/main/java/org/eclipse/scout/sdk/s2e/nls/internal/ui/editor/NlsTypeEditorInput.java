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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.editor;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * <h3>{@link NlsTypeEditorInput}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 2013-03-25
 */
public class NlsTypeEditorInput implements IEditorInput {

  private final IType m_type;

  public NlsTypeEditorInput(IType t) {
    m_type = t;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Object getAdapter(Class adapter) {
    return null;
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return null;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public IPersistableElement getPersistable() {
    return null;
  }

  @Override
  public String getToolTipText() {
    return "";
  }

  public IType getType() {
    return m_type;
  }

}
