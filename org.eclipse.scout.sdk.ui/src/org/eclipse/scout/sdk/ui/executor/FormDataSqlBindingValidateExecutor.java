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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.sql.binding.FormDataSqlBindingValidator;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.BundleNodeGroupTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.ServerNodePage;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link FormDataSqlBindingValidateExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class FormDataSqlBindingValidateExecutor extends AbstractExecutor {

  private ITypeResolver m_resolver = null;

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    new Job("Validate FormData SQL Bindings") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          new FormDataSqlBindingValidator(m_resolver.getTypes()).run(monitor);
        }
        catch (Exception e) {
          ScoutSdkUi.logError("could not execute formdata sql binding validation.", e);
        }
        return Status.OK_STATUS;
      }
    }.schedule();
    return null;
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    final Object selectedElement = selection.getFirstElement();
    if (selectedElement instanceof BundleNodeGroupTablePage) {
      m_resolver = new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iService = TypeUtility.getType(IRuntimeClasses.IService);
          ICachedTypeHierarchy serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
          IScoutBundle scoutBundle = ((BundleNodeGroupTablePage) selectedElement).getScoutBundle();
          Set<? extends IScoutBundle> serverBundles = scoutBundle.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), true);
          return serviceHierarchy.getAllSubtypes(iService, ScoutTypeFilters.getClassesInScoutBundles(serverBundles));
        }
      };
    }
    else if (selectedElement instanceof ServerNodePage) {
      m_resolver = new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iService = TypeUtility.getType(IRuntimeClasses.IService);
          ICachedTypeHierarchy serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
          IScoutBundle bundle = ((ServerNodePage) selectedElement).getScoutBundle();
          return serviceHierarchy.getAllSubtypes(iService, ScoutTypeFilters.getClassesInScoutBundles(bundle));
        }
      };
    }
    else if (selectedElement instanceof AbstractServiceNodePage) {
      m_resolver = new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          return CollectionUtility.hashSet(((AbstractServiceNodePage) selectedElement).getType());
        }
      };
    }
    else if (selectedElement instanceof ITypeResolver) {
      m_resolver = (ITypeResolver) selectedElement;
    }

    return m_resolver != null && isEditable(UiUtility.getScoutBundleFromSelection(selection));
  }
}
