package org.eclipse.scout.nls.sdk.services.ui.page;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.services.model.ws.project.ServiceNlsProjectProvider;
import org.eclipse.scout.nls.sdk.services.ui.action.TextProviderServiceNewAction;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.INodeVisitor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class TextServiceTablePage extends AbstractPage {

  private final IType abstractDynamicNlsTextProviderService = TypeUtility.getType(RuntimeClasses.AbstractDynamicNlsTextProviderService);
  private ICachedTypeHierarchy m_serviceHierarchy;

  public TextServiceTablePage() {
    setName(Texts.get("TextProviderServices"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Texts));
  }

  @Override
  public void unloadPage() {
    if (m_serviceHierarchy != null) {
      m_serviceHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_serviceHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_serviceHierarchy != null) {
      m_serviceHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TEXT_SERVICE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public int accept(INodeVisitor visitor) {
    return visitChildren(visitor);
  }

  @Override
  public void loadChildrenImpl() {
    if (m_serviceHierarchy == null) {
      m_serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(abstractDynamicNlsTextProviderService);
      m_serviceHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    try {
      IType[] services = ServiceNlsProjectProvider.getRegisteredTextProviderTypes();
      ITypeFilter filter = TypeFilters.getClassesInProject(getScoutResource().getJavaProject());
      for (IType type : services) {
        if (filter.accept(type)) {
          new TextServiceNodePage(this, type);
        }
      }
    }
    catch (JavaModelException e) {
      NlsCore.logWarning("Could not get the text provider service types.", e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TextProviderServiceNewAction.class};
  }
}
