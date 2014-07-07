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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.jdt.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.outline.OutlineNewOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;

/**
 * <h3>{@link OutlineTemplateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public class OutlineTemplateOperation extends AbstractScoutProjectNewOperation {

  public static final String TEMPLATE_ID = "ID_OUTLINE_TEMPLATE";

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
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ResourcesPlugin.getWorkspace().checkpoint(false);
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    final IScoutBundle client = bundleGraph.getBundle(getProperties().getProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, String.class));
    final IScoutBundle server = bundleGraph.getBundle(getProperties().getProperty(CreateServerPluginOperation.PROP_BUNDLE_SERVER_NAME, String.class));
    final IScoutBundle shared = bundleGraph.getBundle(getProperties().getProperty(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, String.class));

    if (client == null) {
      // the projects could not be found. maybe the target platform could not be applied successfully
      ScoutSdk.logWarning("Outline Template could not applied because the client bundle could not be found. Check that the target platform is valid and contains the scout runtime.");
      return;
    }

    IType desktopType = TypeUtility.getType(client.getDefaultPackage(IDefaultTargetPackage.CLIENT_DESKTOP) + ".Desktop");
    if (TypeUtility.exists(desktopType)) {
      MethodOverrideOperation execOpenOp = new MethodOverrideOperation("execOpened", desktopType);
      execOpenOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          // mobile case
          source.append("//If it is a mobile or tablet device, the DesktopExtension in the mobile plugin takes care of starting the correct forms.\n");
          source.append("if (!").append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.UserAgentUtility)));
          source.append(".isDesktopDevice()) {").append(lineDelimiter);
          source.append("  return;").append(lineDelimiter);
          source.append("}").append(lineDelimiter).append(lineDelimiter);

          // outline tree
          source.append("// outline tree").append(lineDelimiter);
          String treeFormRef = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.DefaultOutlineTreeForm));
          source.append(treeFormRef).append(" treeForm = new ").append(treeFormRef).append("();").append(lineDelimiter);
          ScoutIconDesc icon = client.getIconProvider().getIcon("eclipse_scout");
          if (icon != null) {
            String iconsRef = validator.getTypeName(SignatureCache.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName()));
            source.append("treeForm.setIconId(" + iconsRef + "." + icon.getConstantField().getElementName() + ");").append(lineDelimiter);
          }
          source.append("treeForm.startView();").append(lineDelimiter);
          source.append(lineDelimiter);

          // outline table
          source.append("//outline table").append(lineDelimiter);
          String tableFormRef = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.DefaultOutlineTableForm));
          source.append(tableFormRef).append(" tableForm=new ").append(tableFormRef).append("();").append(lineDelimiter);
          if (icon != null) {
            String iconsRef = validator.getTypeName(SignatureCache.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName()));
            source.append("tableForm.setIconId(").append(iconsRef).append(".").append(icon.getConstantField().getElementName()).append(");").append(lineDelimiter);
          }
          source.append("tableForm.startView();").append(lineDelimiter);
          source.append(lineDelimiter);

          // activate first outline
          source.append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.IOutline))).append(" firstOutline = ");
          source.append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.CollectionUtility))).append(".firstElement(getAvailableOutlines());").append(lineDelimiter);
          source.append("if (firstOutline != null) {").append(lineDelimiter);
          source.append("  setOutline(firstOutline);").append(lineDelimiter);
          source.append("}").append(lineDelimiter);
        }
      });
      execOpenOp.setSibling(desktopType.getType("FileMenu"));
      execOpenOp.validate();
      execOpenOp.run(monitor, workingCopyManager);
      workingCopyManager.reconcile(desktopType.getCompilationUnit(), monitor);

      // create the outline
      OutlineNewOperation outlineOp = new OutlineNewOperation("StandardOutline", client.getDefaultPackage(IDefaultTargetPackage.CLIENT_OUTLINES), client.getJavaProject());
      outlineOp.setDesktopType(desktopType);
      outlineOp.setFormatSource(false);
      outlineOp.setNlsEntry(client.getNlsProject().getEntry("StandardOutline"));
      outlineOp.run(monitor, workingCopyManager);
      workingCopyManager.reconcile(desktopType.getCompilationUnit(), monitor);

      if (server != null && shared != null) {
        // create outline service
        ServiceNewOperation outlineServiceOp = new ServiceNewOperation("IStandardOutlineService", "StandardOutlineService");
        outlineServiceOp.addProxyRegistrationProject(client.getJavaProject());
        outlineServiceOp.addServiceRegistration(new ServiceRegistrationDescription(server.getJavaProject()));
        outlineServiceOp.setImplementationProject(server.getJavaProject());
        outlineServiceOp.setInterfaceProject(shared.getJavaProject());
        outlineServiceOp.setInterfacePackageName(shared.getDefaultPackage(IDefaultTargetPackage.SHARED_SERVICES));
        outlineServiceOp.addInterfaceInterfaceSignature(SignatureCache.createTypeSignature(IRuntimeClasses.IService));
        outlineServiceOp.setImplementationPackageName(server.getDefaultPackage(IDefaultTargetPackage.SERVER_SERVICES));
        outlineServiceOp.setImplementationSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IService, server.getJavaProject()));
        outlineServiceOp.run(monitor, workingCopyManager);
      }
    }
    else {
      ScoutSdk.logWarning("could not find desktop type");
    }
  }
}
