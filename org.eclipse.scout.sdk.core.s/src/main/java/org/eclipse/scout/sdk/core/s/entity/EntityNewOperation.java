/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.entity;

import java.util.function.BiConsumer;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.form.FormNewOperation;
import org.eclipse.scout.sdk.core.s.page.PageNewOperation;
import org.eclipse.scout.sdk.core.s.service.ServiceNewOperation;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;

public class EntityNewOperation implements BiConsumer<IEnvironment, IProgress> {

  // in
  private String m_entityName;
  private String m_clientPackage;
  private String m_sharedPackage;
  private String m_serverPackage;

  // optional
  private IClasspathEntry m_clientSourceFolder;
  private IClasspathEntry m_sharedSourceFolder;
  private IClasspathEntry m_serverSourceFolder;

  private IClasspathEntry m_sharedGeneratedSourceFolder;

  private IClasspathEntry m_clientTestSourceFolder;
  private IClasspathEntry m_sharedTestSourceFolder;
  private IClasspathEntry m_serverTestSourceFolder;

  private IClasspathEntry m_clientMainTestSourceFolder;
  private IClasspathEntry m_sharedMainTestSourceFolder;
  private IClasspathEntry m_serverMainTestSourceFolder;

  // operations
  private FormNewOperation m_formNewOperation;
  private PageNewOperation m_pageNewOperation;
  private ServiceNewOperation m_serviceNewOperation;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    validateOperation();
    prepareOperation();
    prepareProgress(progress);
    executeOperation(env, progress);
  }

  protected void validateOperation() {
    Ensure.notBlank(getEntityName(), "No entity name provided");
    Ensure.notBlank(getClientPackage(), "No client package provided");
  }

  protected void prepareOperation() {
    setSharedPackage(ScoutTier.Client.convert(ScoutTier.Shared, getClientPackage()));
    setServerPackage(ScoutTier.Client.convert(ScoutTier.Server, getClientPackage()));
  }

  protected void prepareProgress(IProgress progress) {
    progress.init(getTotalWork(), toString());
  }

  protected void executeOperation(IEnvironment env, IProgress progress) {
    var runServiceNewOperation = runServiceNewOperation();
    if (runServiceNewOperation) {
      setServiceNewOperation(createServiceNewOperation());
      prepareServiceNewOperation();
    }

    // form
    setFormNewOperation(createFormNewOperation());
    if (prepareFormNewOperation()) {
      getFormNewOperation().accept(env, progress.newChild(1));
    }

    // page
    setPageNewOperation(createPageNewOperation());
    if (preparePageNewOperation()) {
      getPageNewOperation().accept(env, progress.newChild(1));
    }

    if (runServiceNewOperation) {
      getServiceNewOperation().accept(env, progress.newChild(1));
    }
  }

  protected int getTotalWork() {
    var result = 1; // FormNewOperation
    result += 1; // PageNewOperation
    if (runServiceNewOperation()) {
      result += 1; // ServiceNewOperation
    }
    return result;
  }

  protected static void warnMissingInputFor(String element, String input) {
    SdkLog.warning("No {} will be created, because the {} was not provided or could not be determined.", element, input);
  }

  protected static boolean requireInputFor(String element, String input, Object inputObj, boolean warn) {
    if (inputObj != null) {
      return true;
    }
    if (warn) {
      warnMissingInputFor(element, input);
    }
    return false;
  }

  protected boolean requireClientPackageFor(String element, boolean warn) {
    return requireInputFor(element, "client package", getClientPackage(), warn);
  }

  protected boolean requireSharedPackageFor(String element, boolean warn) {
    return requireInputFor(element, "shared package", getSharedPackage(), warn);
  }

  protected boolean requireClientSourceFolderFor(String element, boolean warn) {
    return requireInputFor(element, "client source folder", getClientSourceFolder(), warn);
  }

  protected boolean requireSharedSourceFolderFor(String element, boolean warn) {
    return requireInputFor(element, "shared source folder", getSharedSourceFolder(), warn);
  }

  protected boolean requireServerSourceFolderFor(String element, boolean warn) {
    return requireInputFor(element, "server source folder", getServerSourceFolder(), warn);
  }

  protected FormNewOperation createFormNewOperation() {
    return new FormNewOperation();
  }

  protected boolean checkFormNewOperationPrerequisites(boolean warn) {
    if (!requireClientPackageFor("Form", warn)) {
      return false;
    }
    return requireClientSourceFolderFor("Form", warn);
  }

  protected boolean prepareFormNewOperation() {
    var op = getFormNewOperation();
    if (op == null) {
      return false;
    }
    if (!checkFormNewOperationPrerequisites(true)) {
      return false;
    }

    op.setFormName(getEntityName().concat(ISdkConstants.SUFFIX_FORM));
    op.setSuperType(getFormSuperType(getClientSourceFolder().javaEnvironment()));
    op.setClientPackage(getClientPackage());
    op.setClientSourceFolder(getClientSourceFolder());
    op.setClientTestSourceFolder(getClientTestSourceFolder());

    op.setSharedSourceFolder(getSharedSourceFolder());
    op.setFormDataSourceFolder(getSharedGeneratedSourceFolder() != null ? getSharedGeneratedSourceFolder() : getSharedSourceFolder());

    if (getServerSourceFolder() != null) {
      op.setServerSession(getServerSourceFolder().javaEnvironment().requireApi(IScoutApi.class).IServerSession().fqn());
    }
    op.setServerSourceFolder(getServerSourceFolder());
    op.setServerTestSourceFolder(getServerTestSourceFolder());

    op.setCreateFormData(getSharedSourceFolder() != null);
    op.setCreatePermissions(getSharedSourceFolder() != null);
    op.setCreateOrAppendService(getSharedSourceFolder() != null && getServerSourceFolder() != null);

    op.setServiceNewOperation(getServiceNewOperation());

    return true;
  }

  protected String getFormSuperType(IJavaEnvironment javaEnv) {
    return javaEnv.api(IScoutApi.class).get().AbstractForm().fqn();
  }

  protected PageNewOperation createPageNewOperation() {
    return new PageNewOperation();
  }

  protected boolean checkPageNewOperationPrerequisites(boolean warn) {
    if (!requireClientPackageFor("Page", warn)) {
      return false;
    }
    return requireClientSourceFolderFor("Page", warn);
  }

  protected boolean preparePageNewOperation() {
    var op = getPageNewOperation();
    if (op == null) {
      return false;
    }
    if (!checkPageNewOperationPrerequisites(true)) {
      return false;
    }

    op.setPageName(getEntityName().concat(ISdkConstants.SUFFIX_PAGE_WITH_TABLE));
    op.setSuperType(getPageSuperType(getClientSourceFolder().javaEnvironment()));
    op.setPackage(getClientPackage());
    op.setClientSourceFolder(getClientSourceFolder());

    op.setSharedSourceFolder(getSharedSourceFolder());
    op.setPageDataSourceFolder(getSharedGeneratedSourceFolder() != null ? getSharedGeneratedSourceFolder() : getSharedSourceFolder());

    if (getServerSourceFolder() != null) {
      op.setServerSession(getServerSourceFolder().javaEnvironment().requireApi(IScoutApi.class).IServerSession().fqn());
    }
    op.setServerSourceFolder(getServerSourceFolder());
    op.setTestSourceFolder(getServerTestSourceFolder());

    op.setCreateAbstractPage(false);

    op.setServiceNewOperation(getServiceNewOperation());

    return true;
  }

  protected String getPageSuperType(IJavaEnvironment javaEnv) {
    return javaEnv.api(IScoutApi.class).get().AbstractPageWithTable().fqn();
  }

  @SuppressWarnings("MethodMayBeStatic")
  protected ServiceNewOperation createServiceNewOperation() {
    return new ServiceNewOperation();
  }

  protected boolean checkServiceNewOperationPrerequisites(boolean warn) {
    if (!requireSharedPackageFor("Service", warn)) {
      return false;
    }
    if (!requireSharedSourceFolderFor("Service", warn)) {
      return false;
    }
    return requireServerSourceFolderFor("Service", warn);
  }

  protected boolean prepareServiceNewOperation() {
    var op = getServiceNewOperation();
    if (op == null) {
      return false;
    }
    if (!checkServiceNewOperationPrerequisites(false)) {
      return false;
    }

    op.setServiceName(getEntityName());
    op.setSharedPackage(getSharedPackage());
    op.setSharedSourceFolder(getSharedSourceFolder());
    op.setServerSourceFolder(getServerSourceFolder());

    op.setTestSourceFolder(getServerTestSourceFolder());
    if (getServerSourceFolder() != null) {
      op.setServerSession(getServerSourceFolder().javaEnvironment().requireApi(IScoutApi.class).IServerSession().fqn());
    }
    op.setCreateTest(getServerTestSourceFolder() != null);

    return true;
  }

  public boolean runServiceNewOperation() {
    return isCreateSingleService() && checkServiceNewOperationPrerequisites(false);
  }

  protected boolean isCreateSingleService() {
    return true;
  }

  public String getEntityName() {
    return m_entityName;
  }

  public void setEntityName(String entityName) {
    m_entityName = entityName;
  }

  public String getClientPackage() {
    return m_clientPackage;
  }

  public void setClientPackage(String clientPackage) {
    m_clientPackage = clientPackage;
  }

  public String getSharedPackage() {
    return m_sharedPackage;
  }

  protected void setSharedPackage(String sharedPackage) {
    m_sharedPackage = sharedPackage;
  }

  public String getServerPackage() {
    return m_serverPackage;
  }

  protected void setServerPackage(String serverPackage) {
    m_serverPackage = serverPackage;
  }

  public IClasspathEntry getClientSourceFolder() {
    return m_clientSourceFolder;
  }

  public void setClientSourceFolder(IClasspathEntry clientSourceFolder) {
    m_clientSourceFolder = clientSourceFolder;
  }

  public IClasspathEntry getClientTestSourceFolder() {
    return m_clientTestSourceFolder;
  }

  public void setClientTestSourceFolder(IClasspathEntry clientTestSourceFolder) {
    m_clientTestSourceFolder = clientTestSourceFolder;
  }

  public IClasspathEntry getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IClasspathEntry sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  public IClasspathEntry getSharedTestSourceFolder() {
    return m_sharedTestSourceFolder;
  }

  public void setSharedTestSourceFolder(IClasspathEntry sharedTestSourceFolder) {
    m_sharedTestSourceFolder = sharedTestSourceFolder;
  }

  public IClasspathEntry getServerSourceFolder() {
    return m_serverSourceFolder;
  }

  public void setServerSourceFolder(IClasspathEntry serverSourceFolder) {
    m_serverSourceFolder = serverSourceFolder;
  }

  public IClasspathEntry getServerTestSourceFolder() {
    return m_serverTestSourceFolder;
  }

  public void setServerTestSourceFolder(IClasspathEntry serverTestSourceFolder) {
    m_serverTestSourceFolder = serverTestSourceFolder;
  }

  public IClasspathEntry getClientMainTestSourceFolder() {
    return m_clientMainTestSourceFolder;
  }

  public void setClientMainTestSourceFolder(IClasspathEntry clientMainTestSourceFolder) {
    m_clientMainTestSourceFolder = clientMainTestSourceFolder;
  }

  public IClasspathEntry getSharedMainTestSourceFolder() {
    return m_sharedMainTestSourceFolder;
  }

  public void setSharedMainTestSourceFolder(IClasspathEntry sharedMainTestSourceFolder) {
    m_sharedMainTestSourceFolder = sharedMainTestSourceFolder;
  }

  public IClasspathEntry getServerMainTestSourceFolder() {
    return m_serverMainTestSourceFolder;
  }

  public void setServerMainTestSourceFolder(IClasspathEntry serverMainTestSourceFolder) {
    m_serverMainTestSourceFolder = serverMainTestSourceFolder;
  }

  public IClasspathEntry getSharedGeneratedSourceFolder() {
    return m_sharedGeneratedSourceFolder;
  }

  public void setSharedGeneratedSourceFolder(IClasspathEntry sharedGeneratedSourceFolder) {
    m_sharedGeneratedSourceFolder = sharedGeneratedSourceFolder;
  }

  public FormNewOperation getFormNewOperation() {
    return m_formNewOperation;
  }

  protected void setFormNewOperation(FormNewOperation formNewOperation) {
    m_formNewOperation = formNewOperation;
  }

  public PageNewOperation getPageNewOperation() {
    return m_pageNewOperation;
  }

  protected void setPageNewOperation(PageNewOperation pageNewOperation) {
    m_pageNewOperation = pageNewOperation;
  }

  public ServiceNewOperation getServiceNewOperation() {
    return m_serviceNewOperation;
  }

  protected void setServiceNewOperation(ServiceNewOperation serviceNewOperation) {
    m_serviceNewOperation = serviceNewOperation;
  }

  @Override
  public String toString() {
    return "Create new Entity";
  }
}
