/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.action.dto;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.dto.formdata.MultipleFormDataUpdateOperation;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TypeResolverFormDataAction}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 02.10.2013
 */
public class TypeResolverFormDataAction extends MultipleUpdateFormDataAction {
  private ITypeResolver m_typeResolver;
  private IScoutBundle m_bundle;

  public void init(ITypeResolver typeResolver, IScoutBundle bundle) {
    m_typeResolver = typeResolver;
    m_bundle = bundle;
  }

  @Override
  public boolean isVisible() {
    return m_typeResolver != null && (m_bundle == null || !m_bundle.isBinary());
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    setOperation(new MultipleFormDataUpdateOperation(m_typeResolver));
    return super.execute(shell, selection, event);
  }
}
