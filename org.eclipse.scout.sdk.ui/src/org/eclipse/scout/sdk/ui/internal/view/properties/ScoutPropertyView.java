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
package org.eclipse.scout.sdk.ui.internal.view.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.sdk.ui.IScoutConstants;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.view.property.IMultiPropertyViewPart;
import org.eclipse.scout.sdk.ui.extensions.view.property.IPropertyViewPart;
import org.eclipse.scout.sdk.ui.extensions.view.property.ISinglePropertyViewPart;
import org.eclipse.scout.sdk.ui.internal.extensions.view.property.PropertyViewExtensionPoint;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.ExceptionSinglePagePropertyViewPart;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.UnknownSinglePagePropertyViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * Editor to change type properties
 */
public class ScoutPropertyView extends ViewPart {

  private HashCodeComparator hasCodeComparator = new HashCodeComparator();

  private Composite m_content;
  private IPropertyViewPart m_currentPart = null;

  private P_SelectionListener m_selectionListener;

  private IMemento m_memento;

  @Override
  public void init(IViewSite site) throws PartInitException {
    super.init(site);
    if (m_selectionListener == null) {
      m_selectionListener = new P_SelectionListener();
    }

    getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(IScoutConstants.SCOUT_EXPLORER_VIEW, m_selectionListener);
  }

  /**
   * Disposes the toolkit
   */
  @Override
  public void dispose() {
    if (m_currentPart != null) {
      m_currentPart.save(m_memento);
    }
    getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(IScoutConstants.SCOUT_EXPLORER_VIEW, m_selectionListener);
    m_selectionListener = null;
    super.dispose();
  }

  @Override
  public void createPartControl(Composite parent) {
    m_content = new Composite(parent, SWT.NONE);
    m_content.setLayout(new FillLayout());
    ISelection selection = getSite().getWorkbenchWindow().getSelectionService().getSelection(IScoutConstants.SCOUT_EXPLORER_VIEW);
    handleSelectionChanged(selection);
  }

  /**
   * Passing the focus request to the form.
   */
  @Override
  public void setFocus() {
    m_content.setFocus();
  }

  @Override
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    m_memento = memento;
    super.init(site, memento);
  }

  protected void handleSelectionChanged(ISelection selection) {
    // clear old
    IPage[] pagesa = getPagesOfSelection(selection);
    if (m_currentPart != null) {
      if (m_currentPart instanceof IMultiPropertyViewPart) {
        IMultiPropertyViewPart currentPart = (IMultiPropertyViewPart) m_currentPart;
        if (equalPagesIgnoreOrder(pagesa, currentPart.getPages())) {
          return;
        }
      }
      else if (m_currentPart instanceof ISinglePropertyViewPart) {
        ISinglePropertyViewPart currentPart = (ISinglePropertyViewPart) m_currentPart;
        if (pagesa != null && pagesa.length == 1 && pagesa[0].equals(currentPart.getPage())) {
          return;
        }

      }
      m_currentPart.save(m_memento);
      m_currentPart.dispose();
    }
    // create new
    try {
      if (pagesa == null || pagesa.length == 0) {
        m_currentPart = new UnknownSinglePagePropertyViewPart();
        m_currentPart.createPart(m_content);
      }
      else if (pagesa.length == 1) {
        ISinglePropertyViewPart singlePart = PropertyViewExtensionPoint.createSinglePageViewPart(pagesa[0]);
        singlePart.setPage(pagesa[0]);
        m_currentPart = singlePart;
        singlePart.createPart(m_content);
      }
      else if (pagesa.length > 1) {
        IMultiPropertyViewPart multiPart = PropertyViewExtensionPoint.createMultiPageViewPart(pagesa);
        m_currentPart = multiPart;
        if (multiPart != null) {
          multiPart.setPages(pagesa);
          multiPart.createPart(m_content);
        }
      }
    }
    catch (Throwable e) {
      //
      if (m_currentPart != null) {
        m_currentPart.dispose();
        m_currentPart = null;
      }
      StringBuilder selectionBuilder = new StringBuilder();
      selectionBuilder.append("[");
      if (pagesa != null) {
        for (int i = 0; i < pagesa.length; i++) {
          selectionBuilder.append(pagesa[i].getName());
          if (i < pagesa.length - 1) {
            selectionBuilder.append(", ");
          }
        }
      }
      selectionBuilder.append("]");
      ScoutSdkUi.logError("error during create property pages for " + selectionBuilder.toString(), e);
      m_currentPart = new ExceptionSinglePagePropertyViewPart();
      ((ExceptionSinglePagePropertyViewPart) m_currentPart).setThrowable(e);
      m_currentPart.createPart(m_content);
    }
    if (m_currentPart != null) {
      m_currentPart.init(m_memento);
    }
    m_content.layout(false);

//    m_content.layout(true);
  }

  private IPage[] getPagesOfSelection(ISelection selection) {
    List<IPage> pages = new ArrayList<IPage>(3);
    if (selection instanceof StructuredSelection) {
      StructuredSelection sel = (StructuredSelection) selection;
      for (Object element : sel.toList()) {
        if (element instanceof IPage) {
          pages.add((IPage) element);
        }
      }
    }
    return pages.toArray(new IPage[pages.size()]);
  }

  private boolean equalPagesIgnoreOrder(IPage[] pagesA, IPage[] pagesB) {
    if (pagesA == null && pagesB == null) {
      return true;
    }
    if (pagesA == null || pagesB == null) {
      return false;
    }
    Arrays.sort(pagesA, hasCodeComparator);
    Arrays.sort(pagesB, hasCodeComparator);
    return Arrays.equals(pagesA, pagesB);
  }

  private class P_SelectionListener implements ISelectionListener {
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
      handleSelectionChanged(selection);
    }
  } // end class P_SelectionListener

  private class HashCodeComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
      if (o1 == null && o2 == null) {
        return 0;
      }
      if (o1 == null) {
        return -1;
      }
      if (o2 == null) {
        return 1;
      }
      return o1.hashCode() - o2.hashCode();
    }
  }
}
