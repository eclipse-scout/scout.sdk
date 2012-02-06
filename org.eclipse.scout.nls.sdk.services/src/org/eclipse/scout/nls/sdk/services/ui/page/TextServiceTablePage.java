package org.eclipse.scout.nls.sdk.services.ui.page;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.services.internal.NlsSdkService;
import org.eclipse.scout.nls.sdk.services.model.ws.project.ServiceNlsProjectProvider;
import org.eclipse.scout.nls.sdk.services.ui.action.TextProviderServiceNewAction;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.type.PackageContentChangedListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.INodeVisitor;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class TextServiceTablePage extends AbstractPage {

  private PackageContentChangedListener m_packageContentListener;
  private IPackageFragment m_textServicePackage;

  public TextServiceTablePage() {
    setName(Texts.get("TextProviderServices"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Texts));
  }

  @Override
  public void unloadPage() {
    if (m_packageContentListener != null) {
      JavaCore.removeElementChangedListener(m_packageContentListener);
      m_packageContentListener = null;
    }
  }

  @Override
  public void refresh(boolean clearCache) {
    super.refresh(clearCache);
  }

  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public String getPageId() {
    return getClass().getName();
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
    m_textServicePackage = getScoutResource().getPackageFragment(getScoutResource().getPackageName(NlsSdkService.TEXT_SERVICE_PACKAGE_SUFFIX));
    m_packageContentListener = new PackageContentChangedListener(this, m_textServicePackage);
    JavaCore.addElementChangedListener(m_packageContentListener);

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
