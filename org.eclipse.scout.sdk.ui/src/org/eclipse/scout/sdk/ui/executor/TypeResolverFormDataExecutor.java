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

import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ProjectsTablePage;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.dto.formdata.MultipleFormDataUpdateOperation;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TypeResolverFormDataExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class TypeResolverFormDataExecutor extends AbstractExecutor {

  private ITypeResolver m_resolver;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    Object selectedElement = selection.getFirstElement();
    IScoutBundle scoutBundle = UiUtility.getScoutBundleFromSelection(selection);
    if (selectedElement instanceof ProjectsTablePage) {
      m_resolver = new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iForm = TypeUtility.getType(IRuntimeClasses.IForm);
          ICachedTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
          return formHierarchy.getAllSubtypes(iForm);
        }
      };
    }
    else if (selectedElement instanceof ITypeResolver) {
      m_resolver = (ITypeResolver) selectedElement;
    }

    return m_resolver != null && isEditable(scoutBundle);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    new OperationJob(new MultipleFormDataUpdateOperation(m_resolver)).schedule();
    return null;
  }

}
