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
package org.eclipse.scout.sdk.ui.internal.view.outline;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.sdk.ui.internal.view.outline.job.LoadOutlineChildrenJob;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Display;

public class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
  private IPage m_invisibleRoot;
  private boolean m_autoLoadChildren;

  public ViewContentProvider() {
    m_autoLoadChildren = true;
  }

  public void setAutoLoadChildren(boolean b) {
    m_autoLoadChildren = b;
  }

  public boolean isAutoLoadChildren() {
    return m_autoLoadChildren;
  }

  public void setRoot(IPage invisibleRoot) {
    m_invisibleRoot = invisibleRoot;
  }

  public IPage getRoot() {
    return m_invisibleRoot;
  }

  public void inputChanged(Viewer v, Object oldInput, Object newInput) {
  }

  public void dispose() {
  }

  /**
   * root elements
   */
  public Object[] getElements(Object parent) {
    /*
     * if(!m_invisibleRoot.isChildrenLoaded()){
     * LoadPageChildrenJob job=new LoadPageChildrenJob(m_invisibleRoot);
     * job.schedule();
     * try{
     * job.join();
     * }
     * catch(InterruptedException e){
     * }
     * }
     * return m_invisibleRoot.getVisibleChildArray();
     */
    return new Object[]{m_invisibleRoot};
  }

  public Object getParent(Object child) {
    if (child instanceof IPage) {
      return ((IPage) child).getParent();
    }
    return null;
  }

  public Object[] getChildren(Object parent) {
    if (parent instanceof IPage) {
      IPage node = (IPage) parent;
      if (isAutoLoadChildren()) {
        if (!node.isChildrenLoaded()) {
          Job job = new LoadOutlineChildrenJob(node);
          job.schedule();
          if (Thread.currentThread() == Display.getCurrent().getThread()) {
            try {
              job.join();
            }
            catch (InterruptedException e) {
            }
          }
        }
      }
      return node.getChildArray();
    }
    return new Object[0];
  }

  public boolean hasChildren(Object parent) {
    if (parent instanceof IPage) {
      IPage node = (IPage) parent;
      if (node.isChildrenLoaded()) {
        return node.hasChildren();
      }
      return true;
    }
    return false;
  }

}
