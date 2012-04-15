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
package org.eclipse.scout.sdk.operation.project.template;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.outline.OutlineNewOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link OutlineTemplateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public class OutlineTemplateOperation extends AbstractScoutProjectNewOperation {

  public final static String TEMPLATE_ID = "ID_OUTLINE_TEMPLATE";

  private IScoutProject m_scoutProject;

  @Override
  public String getOperationName() {
    return "Apply outline template...";
  }

  @Override
  public boolean isRelevant() {
    return TEMPLATE_ID.equals(getTemplateName());
  }

  @Override
  public void init() {
    m_scoutProject = getScoutProject();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_scoutProject == null) {
      throw new IllegalArgumentException("scout project must not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ResourcesPlugin.getWorkspace().checkpoint(false);
    IType desktopType = TypeUtility.getType(m_scoutProject.getClientBundle().getBundleName() + IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_DESKTOP + ".Desktop");
    if (TypeUtility.exists(desktopType)) {
      MethodOverrideOperation execOpenOp = new MethodOverrideOperation(desktopType, "execOpened") {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder sourceBuilder = new StringBuilder();
          sourceBuilder.append("// outline tree\n");
          String treeFormRef = validator.getTypeName(Signature.createTypeSignature(RuntimeClasses.DefaultOutlineTreeForm, true));
          sourceBuilder.append(treeFormRef + " treeForm = new " + treeFormRef + "();\n");
          ScoutIconDesc icon = m_scoutProject.getIconProvider().getIcon("eclipse_scout");
          if (icon != null) {
            String iconsRef = validator.getTypeName(Signature.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName(), true));
            sourceBuilder.append("treeForm.setIconId(" + iconsRef + "." + icon.getConstantField().getElementName() + ");\n");
          }
          sourceBuilder.append("treeForm.startView();\n");
          sourceBuilder.append("\n");
          // tree form
          sourceBuilder.append("//outline table\n");
          String tableFormRef = validator.getTypeName(Signature.createTypeSignature(RuntimeClasses.DefaultOutlineTableForm, true));
          sourceBuilder.append(tableFormRef + " tableForm=new " + tableFormRef + "();\n");
          if (icon != null) {
            String iconsRef = validator.getTypeName(Signature.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName(), true));
            sourceBuilder.append("tableForm.setIconId(" + iconsRef + "." + icon.getConstantField().getElementName() + ");\n");
          }
          sourceBuilder.append("tableForm.startView();\n");
          sourceBuilder.append("\n");
          sourceBuilder.append("if (getAvailableOutlines().length > 0) {\n");
          sourceBuilder.append("setOutline(getAvailableOutlines()[0]);\n");
          sourceBuilder.append("}\n");
          return sourceBuilder.toString();
        }
      };
      execOpenOp.setSibling(desktopType.getType("FileMenu"));
      execOpenOp.setFormatSource(true);
      execOpenOp.validate();
      execOpenOp.run(monitor, workingCopyManager);
      workingCopyManager.reconcile(desktopType.getCompilationUnit(), monitor);
      // create the outline
      OutlineNewOperation outlineOp = new OutlineNewOperation();
      outlineOp.setAddToDesktop(true);
      outlineOp.setClientBundle(getScoutProject().getClientBundle());
      outlineOp.setDesktopType(desktopType);
      outlineOp.setFormatSource(true);
      outlineOp.setTypeName("StandardOutline");
      outlineOp.run(monitor, workingCopyManager);
      workingCopyManager.reconcile(desktopType.getCompilationUnit(), monitor);
      if (getScoutProject().getServerBundle() != null && getScoutProject().getSharedBundle() != null) {
        // create outline service
        ServiceNewOperation outlineServiceOp = new ServiceNewOperation();
        outlineServiceOp.addProxyRegistrationBundle(getScoutProject().getClientBundle());
        outlineServiceOp.addServiceRegistrationBundle(getScoutProject().getServerBundle());
        outlineServiceOp.setImplementationBundle(getScoutProject().getServerBundle());
        outlineServiceOp.setInterfaceBundle(getScoutProject().getSharedBundle());
        outlineServiceOp.setServiceInterfaceName("IStandardOutlineService");
        outlineServiceOp.setServiceInterfacePackageName(getScoutProject().getSharedBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_OUTLINE));
        outlineServiceOp.setServiceInterfaceSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.IService2, true));
        outlineServiceOp.setServiceName("StandardOutlineService");
        outlineServiceOp.setServicePackageName(getScoutProject().getServerBundle().getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_OUTLINE));
        outlineServiceOp.setServiceSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractService, true));

        outlineServiceOp.run(monitor, workingCopyManager);

      }

    }
    else {
      ScoutSdk.logWarning("could not find desktop type");
    }
  }
}
