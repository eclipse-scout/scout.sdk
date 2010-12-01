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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.axis;

import java.util.HashSet;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AxisWebServiceConsumerNewAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchyChangedListener;

public class AxisWebServiceConsumerTablePage extends AbstractPage {
  private static final String SERVICE_INTERFACE_NAME = "org.apache.axis.client.Service";
  private static final String SERVICE_SKELETON_NAME = "org.apache.axis.wsdl.Skeleton";
  private IType m_interfaceType;
  private IType m_skeletonType;
  private ITypeHierarchyChangedListener m_temporaryGlobalInterfaceListener;
  private ITypeHierarchyChangedListener m_temporaryGlobalSkeletonListener;

  private ICachedTypeHierarchy m_skeletonHierarchy;
  private ICachedTypeHierarchy m_axisServcieHierarchy;
  private P_AxisLibraryListener m_libraryDetector;

  public AxisWebServiceConsumerTablePage(AbstractPage parent) {
    setParent(parent);
    // super(
    // parent,
    // parent.getBsiCaseProjectGroup().getSharedProject().getRootPackageName()+".services.wsconsumer",
    // parent.getBsiCaseProjectGroup().getServerProject().getRootPackageName()+".services.wsconsumer"
    // );
    setName(Texts.get("AxisWebServiceConsumerTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceLocators));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.AXIS_WEB_SERVICE_CONSUMER_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * server bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  protected boolean checkInterfaceAvailability() {
    if (m_interfaceType == null) {
      m_interfaceType = ScoutSdk.getType(SERVICE_INTERFACE_NAME);
      if (TypeUtility.exists(m_interfaceType)) {
        m_axisServcieHierarchy = ScoutSdk.getPrimaryTypeHierarchy(m_interfaceType);
        m_axisServcieHierarchy.addHierarchyListener(getPageDirtyListener());
        if (m_libraryDetector != null) {
          JavaCore.removeElementChangedListener(m_libraryDetector);
          m_libraryDetector = null;
        }
      }
      else {
        // listener to add types
        if (m_libraryDetector == null) {
          m_libraryDetector = new P_AxisLibraryListener();
          JavaCore.addElementChangedListener(m_libraryDetector);
        }
        return false;
      }
    }

    if (m_skeletonType == null) {
      m_skeletonType = ScoutSdk.getType(SERVICE_SKELETON_NAME);
      if (TypeUtility.exists(m_skeletonType)) {
        m_skeletonHierarchy = ScoutSdk.getPrimaryTypeHierarchy(m_skeletonType);
        m_skeletonHierarchy.addHierarchyListener(getPageDirtyListener());
        if (m_libraryDetector != null) {
          JavaCore.removeElementChangedListener(m_libraryDetector);
          m_libraryDetector = null;
        }
      }
      else {
        // listener to add types
        if (m_libraryDetector == null) {
          m_libraryDetector = new P_AxisLibraryListener();
          JavaCore.addElementChangedListener(m_libraryDetector);
        }
        return false;
      }
    }
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    if (!checkInterfaceAvailability()) {
      return;
    }
    HashSet<String> providerPackages = new HashSet<String>();
    if (TypeUtility.exists(m_skeletonType)) {
      IType[] skeletons = m_skeletonHierarchy.getAllSubtypes(m_skeletonType, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()));
      for (IType type : skeletons) {
        providerPackages.add(type.getPackageFragment().getElementName());
      }
    }
    if (TypeUtility.exists(m_interfaceType)) {
      IType[] interfaces = m_axisServcieHierarchy.getAllSubtypes(m_interfaceType, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()));
      for (IType type : interfaces) {
        if (!providerPackages.contains(type.getPackageFragment().getElementName())) {
          new AxisWebServiceConsumerNodePage(this, type);
        }
      }
    }
  }

  @Override
  public Action createNewAction() {
    return new AxisWebServiceConsumerNewAction(getOutlineView().getSite().getShell(), getScoutResource());
  }

  private class P_AxisLibraryListener implements IElementChangedListener {
    @Override
    public void elementChanged(ElementChangedEvent e) {
      visitDelta(e.getDelta());
    }

    private void visitDelta(IJavaElementDelta delta) {
      int flags = delta.getFlags();
      IJavaElement e = delta.getElement();
      if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
        for (IJavaElementDelta childDelta : delta.getAffectedChildren()) {
          visitDelta(childDelta);
        }
      }
      else {
        if (delta.getKind() == IJavaElementDelta.CHANGED && e.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
          IPackageFragmentRoot frag = (IPackageFragmentRoot) e;
          if (frag.isArchive() && frag.getElementName().contains("axis")) {
            markStructureDirty();
          }
        }
      }
    }

  }
}
