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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.formdata.MultipleFormDataUpdateOperation;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link MultipleUpdateFormDataAction}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 03.03.2011
 */
public class MultipleUpdateFormDataAction extends AbstractScoutHandler {

  private ITypeResolver m_typeResolver;
  private IType[] m_types = null;

  public MultipleUpdateFormDataAction() {
    super(Texts.get("UpdateAllFormdatas"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolLoading), null, false, Category.UDPATE);
  }

  @Override
  public boolean isVisible() {
    if (getTypeResolver() != null) {
      m_types = getTypeResolver().getTypes();
    }
    if (m_types == null || m_types.length < 1) {
      return false;
    }
    for (IType t : m_types) {
      if (!isEditable(t)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    MultipleFormDataUpdateOperation op = new MultipleFormDataUpdateOperation(m_types);
    OperationJob job = new OperationJob(op);
    job.schedule();
    return null;
  }

  /**
   * @return the typeResolver
   */
  public ITypeResolver getTypeResolver() {
    return m_typeResolver;
  }

  public void setTypeResolver(ITypeResolver typeResolver) {
    m_typeResolver = typeResolver;
  }
}
