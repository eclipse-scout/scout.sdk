/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.ws.handler.Handler;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.HandlerNewWizardAction;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class HandlerTablePage extends AbstractPage {

  private Map<ICachedTypeHierarchy, IType> m_handlerHierarchyMap;

  private ITypeHierarchyChangedListener m_hierarchyChangedListener;

  public HandlerTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Handlers"));
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsIcons.HandlerFolder));
    m_hierarchyChangedListener = new P_TypeHierarchyChangedListener();
    m_handlerHierarchyMap = new HashMap<ICachedTypeHierarchy, IType>();

    for (IType type : TypeUtility.getTypes(Handler.class.getName())) {
      ICachedTypeHierarchy hierarchy = TypeUtility.getPrimaryTypeHierarchy(type);
      hierarchy.addHierarchyListener(m_hierarchyChangedListener);
      m_handlerHierarchyMap.put(hierarchy, type);
    }
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.HANDLER_TABLE_PAGE;
  }

  @Override
  public void unloadPage() {
    if (m_hierarchyChangedListener != null) {
      for (ICachedTypeHierarchy hierarchy : m_handlerHierarchyMap.keySet()) {
        hierarchy.removeHierarchyListener(m_hierarchyChangedListener);
      }
    }
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache) {
      for (ICachedTypeHierarchy hierarchy : m_handlerHierarchyMap.keySet()) {
        hierarchy.invalidate();
      }
    }
    super.refresh(clearCache);
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(HandlerNewWizardAction.class);
  }

  @Override
  protected void loadChildrenImpl() {
    List<IType> types = new ArrayList<IType>();

    ITypeFilter filter = new ITypeFilter() {
      @Override
      public boolean accept(IType candidate) {
        try {
          if (!TypeUtility.exists(candidate)) {
            return false;
          }
          if (!candidate.isClass() || candidate.isInterface() || Flags.isAbstract(candidate.getFlags())) {
            return false;
          }
          if (!TypeUtility.isOnClasspath(candidate, getScoutBundle().getJavaProject())) {
            return false;
          }
          return true;
        }
        catch (JavaModelException e) {
          JaxWsSdk.logError(e);
          return false;
        }
      }
    };

    for (Entry<ICachedTypeHierarchy, IType> entry : m_handlerHierarchyMap.entrySet()) {
      ICachedTypeHierarchy hierarchy = entry.getKey();
      IType type = entry.getValue();
      types.addAll(hierarchy.getAllSubtypes(type, filter));
    }
    JaxWsSdkUtility.sortTypesByName(types, true);

    for (IType handlerType : types) {
      // skip internal classes
      if (!Signature.getQualifier(handlerType.getFullyQualifiedName()).contains("internal")) {
        // skipt authentication handlers
        if (!JaxWsSdkUtility.isJdtSubType(TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerConsumer).getFullyQualifiedName(), handlerType)
            && !JaxWsSdkUtility.isJdtSubType(TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerProvider).getFullyQualifiedName(), handlerType)) {
          new HandlerNodePage(this, handlerType);
        }
      }
    }
  }

  private class P_TypeHierarchyChangedListener implements ITypeHierarchyChangedListener {
    @Override
    public void hierarchyInvalidated() {
      markStructureDirty();
    }
  }
}
