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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.NamingUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.UiSwingNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.UiSwtNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientLookupCallTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientServiceNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientSessionNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.DesktopNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.DesktopOutlineTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.MenuNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.MenuTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.OutlineNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.OutlineTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ToolButtonTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.FormHandlerNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.FormHandlerTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.FormNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.FormTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.SearchFormTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.AbstractBoxNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.attribute.AttributeNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.attribute.AttributeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.entity.EntityNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.composer.entity.EntityTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.AllPagesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithNodeNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithTableNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.childpage.NodePageChildPageTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.ColumnNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.ColumnTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.TableNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.wizard.WizardNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.wizard.WizardStepNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.wizard.WizardStepTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.wizard.WizardTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.ServerNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.ServerSessionNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.CommonServicesNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.bookmark.BookmarkStorageServiceNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.bookmark.BookmarkStorageServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.calendar.CalendarServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.sql.SqlServiceNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.sql.SqlServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.custom.CustomServiceNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.custom.CustomServicePackageNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.custom.CustomServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.lookup.LookupServiceNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.lookup.LookupServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.outline.OutlineServiceNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.outline.OutlineServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.process.ProcessServiceNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.process.ProcessServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.CodeNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.CodeTypeNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.CodeTypeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.IconNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.LookupCallTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.NlsTextsNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.PermissionNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.PermissionTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedContextPropertyNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedContextPropertyTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.INodeVisitor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.IProjectNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.member.IPropertyBean;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public class EditorSelectionVisitor implements INodeVisitor {

  public IPage m_nodeToSelect;

  private Iterator<IJavaElement> m_elementIterator;
  private IJavaElement m_currentElement;
  private HashMap<IJavaElement, ITypeHierarchy> m_typeHierarchyCache;
  private HashSet<IPage> m_visitedNodes;
  private ArrayList<IJavaElement> m_elements;

  public EditorSelectionVisitor(IJavaElement element) {
    m_typeHierarchyCache = new HashMap<IJavaElement, ITypeHierarchy>();
    m_visitedNodes = new HashSet<IPage>();
    IMethod declaringMethod = null;
    if (element.getElementType() == IJavaElement.METHOD) {
      declaringMethod = (IMethod) element;
    }
    else {
      declaringMethod = (IMethod) element.getAncestor(IJavaElement.METHOD);
    }
    ArrayList<IJavaElement> collector = new ArrayList<IJavaElement>();
    IType declaringType = null;
    if (declaringMethod != null) {
      collector.add(declaringMethod);
      declaringType = declaringMethod.getDeclaringType();
    }
    else if (element.getElementType() == IJavaElement.TYPE) {
      declaringType = (IType) element;
    }
    else {
      declaringType = (IType) element.getAncestor(IJavaElement.TYPE);
    }
    if (declaringType != null) {
      recCollectDeclaringTypes(declaringType, collector);
      m_elements = collector;
      m_elementIterator = collector.iterator();
      m_currentElement = m_elementIterator.next();
    }
  }

  public IPage findPageToSelect(IPage startPage) {
    IPage visitPage = startPage;
    IJavaElement currElement = m_currentElement;
    while (visitPage != null) {
      if (visitPage instanceof ITypePage) {
        ITypePage tp = (ITypePage) visitPage;
        while (currElement != null) {
          if (CompareUtility.equals(currElement, tp.getType())) {
            m_currentElement = currElement;
            break;
          }
          if (m_elementIterator.hasNext()) {
            currElement = m_elementIterator.next();
          }
          else {
            currElement = null;
          }
        }
      }
      else {
        currElement = null;
      }
      if (currElement != null) {
        startPage = visitPage;
        break;
      }
      else {
        m_elementIterator = m_elements.iterator();
        currElement = m_elementIterator.next();
        visitPage = visitPage.getParent();
      }
    }

    if (m_currentElement == null) {
      m_elementIterator = m_elements.iterator();
      m_currentElement = m_elementIterator.next();
    }
    if (m_currentElement != null) {
      IPage page = findPageToSelectRec(startPage);
      return page;
    }
    return null;
  }

  private IPage findPageToSelectRec(IPage startPage) {
    if (startPage != null) {
      startPage.accept(this);
      if (m_nodeToSelect != null) {
        return m_nodeToSelect;
      }
      else {
        return findPageToSelectRec(startPage.getParent());
      }
    }
    return m_nodeToSelect;
  }

  private void recCollectDeclaringTypes(IType type, ArrayList<IJavaElement> collector) {
    if (TypeUtility.exists(type)) {
      collector.add(0, type);
      recCollectDeclaringTypes(type.getDeclaringType(), collector);
    }
  }

  public IPage getNodeToSelect() {
    return m_nodeToSelect;
  }

  private ITypeHierarchy getCachedTypeHierarchy(IJavaElement element) {
    ITypeHierarchy hierarchy = m_typeHierarchyCache.get(element);
    if (hierarchy == null && element.getElementType() == IJavaElement.TYPE) {
      IType type = (IType) element;
      try {
        hierarchy = type.newSupertypeHierarchy(null);
        m_typeHierarchyCache.put(element, hierarchy);
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logError("could not build supertype hierarchy of '" + type.getFullyQualifiedName() + "'.", e);
      }
    }
    return hierarchy;
  }

  @Override
  public int visit(IPage page) {
    if (m_visitedNodes.contains(page)) {
      return CANCEL_SUBTREE;
    }
    m_visitedNodes.add(page);
    if (page instanceof ScoutExplorerRootNodePage) {
      return CONTINUE;
    }
    else if (page instanceof UiSwingNodePage) {
      return visitBundleNodePage(((UiSwingNodePage) page).getScoutResource());
    }
    else if (page instanceof UiSwtNodePage) {
      return visitBundleNodePage(((UiSwtNodePage) page).getScoutResource());
    }
    else if (page instanceof IProjectNodePage) {
      return visitProjectNode((IProjectNodePage) page);
    }
    else if (page instanceof IconNodePage) {
      return CANCEL_SUBTREE;
    }
    else if (page instanceof NlsTextsNodePage) {
      return CANCEL_SUBTREE;
    }
    else if (page instanceof ClientNodePage) {
      return visitBundleNodePage(((ClientNodePage) page).getScoutResource());
    }
    else if (page instanceof SharedNodePage) {
      return visitBundleNodePage(((SharedNodePage) page).getScoutResource());
    }
    else if (page instanceof ServerNodePage) {
      return visitBundleNodePage(((ServerNodePage) page).getScoutResource());
    }
    else if (page instanceof ClientSessionNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof DesktopNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof MenuTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IMenu));
    }
    else if (page instanceof MenuNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof ToolButtonTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IToolButton));
    }
    else if (page instanceof DesktopOutlineTablePage) {
      return visitDesktopOutlineTablePage((DesktopOutlineTablePage) page);
    }
    else if (page instanceof SearchFormTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.ISearchForm));
    }
    else if (page instanceof FormTablePage) {
      return visitFormTablePage((FormTablePage) page);
    }
    else if (page instanceof FormNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof BeanPropertyTablePage) {
      return visitBeanPropertyTablePage((BeanPropertyTablePage) page);
    }
    else if (page instanceof BeanPropertyNodePage) {

    }
    else if (page instanceof AbstractBoxNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof KeyStrokeTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IKeyStroke));
    }
    else if (page instanceof KeyStrokeNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof AttributeTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IComposerAttribute));
    }
    else if (page instanceof AttributeNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof EntityTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IComposerEntity));
    }
    else if (page instanceof EntityNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof AbstractFormFieldNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof FormHandlerTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IFormHandler));
    }
    else if (page instanceof FormHandlerNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof SearchFormTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.ISearchForm));
    }
    else if (page instanceof WizardTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IWizard));
    }
    else if (page instanceof WizardNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);

    }
    else if (page instanceof WizardStepTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IWizardStep));
    }
    else if (page instanceof WizardStepNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof ClientLookupCallTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.LocalLookupCall));
    }
    else if (page instanceof ClientServiceTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IService));
    }
    else if (page instanceof ClientServiceNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof OutlineTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IOutline));
    }
    else if (page instanceof OutlineNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof AllPagesTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IPage));
    }
    else if (page instanceof NodePageChildPageTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IPage));
    }
    else if (page instanceof PageWithNodeNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof PageWithTableNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof TableNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof ColumnTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IColumn));
    }
    else if (page instanceof ColumnNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof SharedContextPropertyTablePage) {
      return visitShareContextPropertyTablePage((SharedContextPropertyTablePage) page);
    }
    else if (page instanceof SharedContextPropertyNodePage) {
      return visitSharedContextPropertyNodePage((SharedContextPropertyNodePage) page);
    }
    else if (page instanceof PermissionTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.BasicPermission));
    }
    else if (page instanceof PermissionNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof CodeTypeTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.ICodeType));
    }
    else if (page instanceof CodeTypeNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof CodeNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof ServerSessionNodePage) {
      return visitPageWithType((AbstractScoutTypePage) page);
    }
    else if (page instanceof LookupServiceTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.ILookupService));
    }
    else if (page instanceof LookupServiceNodePage) {
      return visitServiceNodePage((LookupServiceNodePage) page);
    }
    else if (page instanceof LookupCallTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.LookupCall));
    }
    else if (page instanceof OutlineServiceTablePage) {
      return visitOutlineServiceTablePage((OutlineServiceTablePage) page);
    }
    else if (page instanceof OutlineServiceNodePage) {
      return visitServiceNodePage((OutlineServiceNodePage) page);
    }
    else if (page instanceof ProcessServiceTablePage) {
      return visitProcessServiceTablePage((ProcessServiceTablePage) page);
    }
    else if (page instanceof ProcessServiceNodePage) {
      return visitServiceNodePage((ProcessServiceNodePage) page);
    }
    else if (page instanceof CommonServicesNodePage) {
      return visitServerServicesCommonNodePage((CommonServicesNodePage) page);
    }
    else if (page instanceof SqlServiceTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.ISqlService));
    }
    else if (page instanceof SqlServiceNodePage) {
      return visitServiceNodePage((SqlServiceNodePage) page);
    }
    else if (page instanceof BookmarkStorageServiceTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.IBookmarkStorageService));
    }
    else if (page instanceof BookmarkStorageServiceNodePage) {
      return visitServiceNodePage((BookmarkStorageServiceNodePage) page);
    }
    else if (page instanceof CalendarServiceTablePage) {
      return visitTypeInHierarchyPage(ScoutSdk.getType(RuntimeClasses.ICalendarService));
    }
    else if (page instanceof CustomServiceTablePage) {
      return visitCustomServiceTablePage((CustomServiceTablePage) page);
    }
    else if (page instanceof CustomServiceNodePage) {
      return visitServiceNodePage((CustomServiceNodePage) page);
    }
    else if (page instanceof CustomServicePackageNodePage) {
      return visitCustomServicePackageNodePage((CustomServicePackageNodePage) page);
    }
    ScoutSdkUi.logWarning("not visited node '" + page.getClass().getName() + "'.");
    return CANCEL;
  }

  private int visitProjectNode(IProjectNodePage page) {
    if (page.getScoutResource().contains(m_currentElement)) {
      return CONTINUE_BRANCH;
    }
    return CANCEL_SUBTREE;
  }

  private int visitBundleNodePage(IScoutBundle bundle) {
    if (bundle.getProject().exists(m_currentElement.getResource().getProjectRelativePath())) {
      return CONTINUE_BRANCH;
    }
    return CANCEL_SUBTREE;
  }

  protected int visitTypeInHierarchyPage(IType superType) {
    ITypeHierarchy hierarchy = getCachedTypeHierarchy(m_currentElement);
    if (hierarchy != null && hierarchy.contains(superType)) {
      return CONTINUE_BRANCH;
    }
    return CANCEL_SUBTREE;
  }

  protected int visitPageWithType(AbstractScoutTypePage page) {
    if (CompareUtility.equals(m_currentElement, page.getType())) {
      m_nodeToSelect = page;
      if (m_elementIterator.hasNext()) {
        m_currentElement = m_elementIterator.next();
        return CONTINUE_BRANCH;
      }
      else {
        return CANCEL;
      }
    }
    return CANCEL_SUBTREE;
  }

  private int visitDesktopOutlineTablePage(DesktopOutlineTablePage page) {
    ITypeHierarchy hierarchy = getCachedTypeHierarchy(m_currentElement);
    if (hierarchy.contains(ScoutSdk.getType(RuntimeClasses.IOutline))) {
      IType desktopType = page.getDesktopType();
      IMethod outlineMethods = TypeUtility.getMethod(desktopType, "getConfiguredOutlines");
      IType[] allNewOccurences = TypeUtility.getNewTypeOccurencesInMethod(outlineMethods);
      for (IType typeOccurence : allNewOccurences) {
        if (typeOccurence.equals(desktopType)) {
          return CONTINUE_BRANCH;
        }
      }
    }
    return CANCEL_SUBTREE;
  }

  private int visitFormTablePage(FormTablePage page) {
    ITypeHierarchy hierarchy = getCachedTypeHierarchy(m_currentElement);
    if (hierarchy != null && hierarchy.contains(ScoutSdk.getType(RuntimeClasses.IForm)) && !hierarchy.contains(ScoutSdk.getType(RuntimeClasses.ISearchForm))) {
      return CONTINUE_BRANCH;
    }
    return CANCEL_SUBTREE;
  }

  protected int visitBeanPropertyTablePage(BeanPropertyTablePage page) {
    if (m_currentElement.getElementType() == IJavaElement.METHOD) {
      IMethod method = (IMethod) m_currentElement;
      if (TypeUtility.exists(method.getAnnotation(NamingUtility.getSimpleName(RuntimeClasses.ConfigPropertyValue)))) {
        return CONTINUE_BRANCH;
      }
    }
    return CANCEL_SUBTREE;
  }

  protected int visitBeanPropertyNodePage(BeanPropertyNodePage page) {
    IPropertyBean desc = page.getPropertyDescriptor();
    if (m_currentElement.equals(desc.getReadMethod()) || m_currentElement.equals(desc.getWriteMethod())) {
      m_nodeToSelect = page;
      if (m_elementIterator.hasNext()) {
        m_currentElement = m_elementIterator.next();
        return CONTINUE_BRANCH;
      }
      else {
        return CANCEL;
      }
    }
    return CANCEL_SUBTREE;
  }

  private boolean isType(IJavaElement element) {
    return element != null && element.getElementType() == IJavaElement.TYPE;
  }

  protected int visitShareContextPropertyTablePage(SharedContextPropertyTablePage page) {
    // since client session or server session is already checked only method has to be proved
    if (m_currentElement != null && m_currentElement.getElementType() == IJavaElement.METHOD) {
      return CONTINUE;
    }
    return CANCEL_SUBTREE;
  }

  protected int visitSharedContextPropertyNodePage(SharedContextPropertyNodePage page) {
    if (m_currentElement != null && m_currentElement.getElementType() == IJavaElement.METHOD) {
      IPropertyBean serverBeanDesc = page.getServerDesc();
      if (m_currentElement.equals(serverBeanDesc.getReadMethod()) || m_currentElement.equals(serverBeanDesc.getWriteMethod())) {
        if (m_elementIterator.hasNext()) {
          m_currentElement = m_elementIterator.next();
          m_nodeToSelect = page;
          return CONTINUE_BRANCH;
        }
      }
    }
    return CANCEL;

  }

  protected int visitOutlineServiceTablePage(OutlineServiceTablePage page) {
    if (isType(m_currentElement)) {
      IType currentElement = (IType) m_currentElement;
      ITypeHierarchy hierarchy = getCachedTypeHierarchy(currentElement);
      if (hierarchy != null && hierarchy.contains(ScoutSdk.getType(RuntimeClasses.IService))) {
        IScoutBundle serverBundle = page.getScoutResource();
        if (currentElement.getPackageFragment().getElementName().equals(serverBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_OUTLINE))) {
          return CONTINUE_BRANCH;
        }

      }
    }
    return CANCEL_SUBTREE;
  }

  protected int visitProcessServiceTablePage(ProcessServiceTablePage page) {
    if (isType(m_currentElement)) {
      IType currentElement = (IType) m_currentElement;
      ITypeHierarchy hierarchy = getCachedTypeHierarchy(currentElement);
      if (hierarchy != null && hierarchy.contains(ScoutSdk.getType(RuntimeClasses.IService))) {
        IScoutBundle serverBundle = page.getScoutResource();
        if (currentElement.getPackageFragment().getElementName().equals(serverBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_PROCESS))) {
          return CONTINUE_BRANCH;
        }

      }
    }
    return CANCEL_SUBTREE;
  }

  protected int visitCustomServiceTablePage(CustomServiceTablePage page) {
    if (isType(m_currentElement)) {
      IType currentElement = (IType) m_currentElement;
      ITypeHierarchy hierarchy = getCachedTypeHierarchy(currentElement);
      if (hierarchy != null && hierarchy.contains(ScoutSdk.getType(RuntimeClasses.IService))) {
        IScoutBundle serverBundle = page.getScoutResource();
        if (currentElement.getPackageFragment().getElementName().startsWith(serverBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_CUSTOM))) {
          return CONTINUE_BRANCH;
        }

      }
    }
    return CANCEL_SUBTREE;
  }

  protected int visitServiceNodePage(AbstractServiceNodePage page) {
    if (CompareUtility.equals(m_currentElement, page.getInterfaceType()) || CompareUtility.equals(m_currentElement, page.getType())) {
      if (m_elementIterator.hasNext()) {
        m_currentElement = m_elementIterator.next();
        return CONTINUE_BRANCH;
      }
      else {
        m_nodeToSelect = page;
        return CANCEL;
      }
    }
    return CANCEL_SUBTREE;
  }

  protected int visitServerServicesCommonNodePage(CommonServicesNodePage page) {
    if (isType(m_currentElement)) {
      IType currentElement = (IType) m_currentElement;
      ITypeHierarchy hierarchy = getCachedTypeHierarchy(currentElement);
      if (hierarchy != null && hierarchy.contains(ScoutSdk.getType(RuntimeClasses.IService))) {
        IScoutBundle serverBundle = page.getScoutResource();
        if (currentElement.getPackageFragment().getElementName().startsWith(serverBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_COMMON))) {
          return CONTINUE_BRANCH;
        }

      }
    }
    return CANCEL_SUBTREE;
  }

  protected int visitCustomServicePackageNodePage(CustomServicePackageNodePage page) {
    if (isType(m_currentElement)) {
      IType currentElement = (IType) m_currentElement;
      ITypeHierarchy hierarchy = getCachedTypeHierarchy(currentElement);
      if (hierarchy != null && hierarchy.contains(ScoutSdk.getType(RuntimeClasses.IService))) {
        IScoutBundle serverBundle = page.getScoutResource();
        if (currentElement.getPackageFragment().getElementName().startsWith(serverBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_CUSTOM))) {
          return CONTINUE_BRANCH;
        }

      }
    }
    return CANCEL_SUBTREE;

  }

  private boolean isPropertyMethod(IJavaElement element) {
    if (TypeUtility.exists(element) && element.getElementType() == IJavaElement.METHOD) {
      IMethod method = (IMethod) element;
      if (TypeUtility.hasAnnotation(method, RuntimeClasses.FormData)) {
        return true;
      }
    }
    return false;
  }

}
