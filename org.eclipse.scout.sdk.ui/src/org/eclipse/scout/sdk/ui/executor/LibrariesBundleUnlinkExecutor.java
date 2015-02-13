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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.library.LibraryBundleUnlinkOperation;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.library.LibraryNodePage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link LibrariesBundleUnlinkExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class LibrariesBundleUnlinkExecutor extends AbstractExecutor {

  private Map<IScoutBundle, List<IPluginModelBase>> m_toUnlink;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_toUnlink = new HashMap<>(selection.size());
    Iterator it = selection.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      if (o instanceof LibraryNodePage) {
        LibraryNodePage lnp = (LibraryNodePage) o;
        IScoutBundle scoutBundle = lnp.getScoutBundle();
        if (!scoutBundle.isBinary()) {
          List<IPluginModelBase> list = m_toUnlink.get(scoutBundle);
          if (list == null) {
            list = new LinkedList<>();
            m_toUnlink.put(scoutBundle, list);
          }
          list.add(lnp.getPluginModel());
        }
      }
    }

    return m_toUnlink.size() > 0;
  }

  @Override
  public Object run(Shell shell, final IStructuredSelection selection, ExecutionEvent event) {
    OperationJob job = new OperationJob();
    for (Entry<IScoutBundle, List<IPluginModelBase>> workUnit : m_toUnlink.entrySet()) {
      LibraryBundleUnlinkOperation op = new LibraryBundleUnlinkOperation(workUnit.getKey(), workUnit.getValue());
      job.addOperation(op);
    }
    job.schedule();
    return null;
  }
}
