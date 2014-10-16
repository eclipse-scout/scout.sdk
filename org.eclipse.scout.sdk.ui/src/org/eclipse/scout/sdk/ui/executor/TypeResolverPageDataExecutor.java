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
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.BundleNodeGroupTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ProjectsTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.AllPagesTablePage;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.dto.pagedata.MultiplePageDataUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TypeResolverPageDataExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class TypeResolverPageDataExecutor extends AbstractExecutor {

  private ITypeResolver m_resolver;

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    new OperationJob(new MultiplePageDataUpdateOperation(m_resolver)).schedule();
    return null;
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    final Object selectedElement = selection.getFirstElement();
    final IScoutBundle scoutBundle = UiUtility.getScoutBundleFromSelection(selection);
    if (selectedElement instanceof BundleNodeGroupTablePage) {
      m_resolver = new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);
          ICachedTypeHierarchy pageWithTableHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPageWithTable);
          Set<? extends IScoutBundle> childBundles = scoutBundle.getChildBundles(ScoutBundleFilters.getWorkspaceBundlesFilter(), true);
          return pageWithTableHierarchy.getAllSubtypes(iPageWithTable, ScoutTypeFilters.getClassesInScoutBundles(childBundles));
        }
      };
    }
    else if (selectedElement instanceof ProjectsTablePage) {
      m_resolver = new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);
          ICachedTypeHierarchy pageWithTableHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPageWithTable);
          return pageWithTableHierarchy.getAllSubtypes(iPageWithTable);
        }
      };
    }
    else if (selectedElement instanceof ClientNodePage) {
      m_resolver = new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);
          ICachedTypeHierarchy pageWithTableHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPageWithTable);
          return pageWithTableHierarchy.getAllSubtypes(iPageWithTable, ScoutTypeFilters.getClassesInScoutBundles(scoutBundle));
        }
      };
    }
    else if (selectedElement instanceof AllPagesTablePage) {
      m_resolver = new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);
          ICachedTypeHierarchy pageWithTableHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPageWithTable);
          return pageWithTableHierarchy.getAllSubtypes(iPageWithTable, ScoutTypeFilters.getClassesInScoutBundles(scoutBundle));
        }
      };
    }
    else if (selectedElement instanceof ITypeResolver) {
      m_resolver = (ITypeResolver) selectedElement;
    }

    return m_resolver != null && (scoutBundle == null || !scoutBundle.isBinary());
  }

}
