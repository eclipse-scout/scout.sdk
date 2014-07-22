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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page;

import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.TableColumnWidthsPasteAction;
import org.eclipse.scout.sdk.ui.action.WellformScoutTypeAction;
import org.eclipse.scout.sdk.ui.action.create.SearchFormNewAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.dto.PageDataUpdateAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.childpage.TablePageChildPageTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.TableNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.util.jdt.AbstractElementChangedListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>PageWithTableNodePage</h3>
 */
public class PageWithTableNodePage extends AbstractScoutTypePage {
  public static final String METHOD_EXEC_CREATE_CHILD_PAGE = "execCreateChildPage";
  private P_MethodChangedListener m_methodChangedListener;

  public PageWithTableNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageTable));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.PAGE_WITH_TABLE_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_methodChangedListener != null) {
      JavaCore.removeElementChangedListener(m_methodChangedListener);
      m_methodChangedListener = null;
    }
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_methodChangedListener == null) {
      m_methodChangedListener = new P_MethodChangedListener();
      JavaCore.addElementChangedListener(m_methodChangedListener);
    }
    Set<IType> allSubtypes = ScoutTypeUtility.getTables(getType());
    if (allSubtypes.size() > 0) {
      TableNodePage tableNodePage = new TableNodePage();
      tableNodePage.setParent(this);
      tableNodePage.setType(CollectionUtility.firstElement(allSubtypes));
    }

    new BeanPropertyTablePage(this, getType());
    new TablePageChildPageTablePage(this, getType());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TypeRenameAction.class, ShowJavaReferencesAction.class, DeleteAction.class, SearchFormNewAction.class,
        WellformScoutTypeAction.class, TableColumnWidthsPasteAction.class, PageDataUpdateAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof DeleteAction) {
      DeleteAction action = (DeleteAction) menu;

      // add page data
      try {
        IType pageDataType = ScoutTypeUtility.findPageDataForPage(getType());
        if (TypeUtility.exists(pageDataType)) {
          action.addType(pageDataType);
        }
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logWarning("Could not parse page data of page '" + getType().getFullyQualifiedName() + "'.", e);
      }
      action.addType(getType());
      action.setName(getName());
    }
    else if (menu instanceof SearchFormNewAction) {
      ((SearchFormNewAction) menu).init(getType(), getScoutBundle());
    }
    else if (menu instanceof WellformScoutTypeAction) {
      ((WellformScoutTypeAction) menu).setType(getType());
    }
    else if (menu instanceof TableColumnWidthsPasteAction) {
      ((TableColumnWidthsPasteAction) menu).init(this);
    }
    else if (menu instanceof PageDataUpdateAction) {
      ((PageDataUpdateAction) menu).setPageDataOwner(getType());
    }
  }

  private class P_MethodChangedListener extends AbstractElementChangedListener {
    @Override
    protected boolean visit(int kind, int flags, IJavaElement e, CompilationUnit ast) {
      if (e != null && e.getElementType() == IJavaElement.METHOD) {
        IMethod method = (IMethod) e;
        IType declaringType = method.getDeclaringType();
        if (TypeUtility.exists(declaringType) && declaringType.equals(getType()) && METHOD_EXEC_CREATE_CHILD_PAGE.equals(method.getElementName())) {
          markStructureDirty();
          return false;
        }
      }
      return super.visit(kind, flags, e, ast);
    }
  } // end class P_MethodChangedListener

}
