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
package org.eclipse.scout.sdk.ui.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataDtoUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link PageDataUpdateExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class PageDataUpdateExecutor extends AbstractExecutor {

  private IType m_pageDataOwner;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_pageDataOwner = UiUtility.getTypeFromSelection(selection);
    return isEditable(m_pageDataOwner) && m_pageDataOwner.getDeclaringType() == null;
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    try {
      PageDataAnnotation pageDataAnnotation = ScoutTypeUtility.findPageDataAnnotation(m_pageDataOwner, TypeUtility.getSupertypeHierarchy(m_pageDataOwner));
      if (pageDataAnnotation != null && !StringUtility.isNullOrEmpty(pageDataAnnotation.getPageDataTypeSignature())) {
        OperationJob job = new OperationJob(new PageDataDtoUpdateOperation(m_pageDataOwner, pageDataAnnotation));
        job.schedule();
      }
      else {
        MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        box.setMessage(Texts.get("CheckPageData"));
        box.open();
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("unable to calculate page data type for update of element '" + m_pageDataOwner + "'.", e);
    }
    return null;
  }

}
