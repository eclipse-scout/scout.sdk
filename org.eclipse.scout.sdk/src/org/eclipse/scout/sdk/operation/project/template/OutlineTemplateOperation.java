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
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.outline.OutlineNewOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link OutlineTemplateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public class OutlineTemplateOperation extends AbstractScoutProjectNewOperation {

  public final static String TEMPLATE_ID = "ID_OUTLINE_TEMPLATE";

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
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ResourcesPlugin.getWorkspace().checkpoint(false);
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    final IScoutBundle client = bundleGraph.getBundle(getProperties().getProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, String.class));
    final IScoutBundle server = bundleGraph.getBundle(getProperties().getProperty(CreateServerPluginOperation.PROP_BUNDLE_SERVER_NAME, String.class));
    final IScoutBundle shared = bundleGraph.getBundle(getProperties().getProperty(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, String.class));

    IType desktopType = TypeUtility.getType(client.getDefaultPackage(IDefaultTargetPackage.CLIENT_DESKTOP) + ".Desktop");
    if (TypeUtility.exists(desktopType)) {
      MethodOverrideOperation execOpenOp = new MethodOverrideOperation(desktopType, "execOpened", false) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder sourceBuilder = new StringBuilder();
          sourceBuilder.append("// outline tree\n");
          String treeFormRef = validator.getTypeName(SignatureCache.createTypeSignature(RuntimeClasses.DefaultOutlineTreeForm));
          sourceBuilder.append(treeFormRef + " treeForm = new " + treeFormRef + "();\n");
          ScoutIconDesc icon = client.getIconProvider().getIcon("eclipse_scout");
          if (icon != null) {
            String iconsRef = validator.getTypeName(SignatureCache.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName()));
            sourceBuilder.append("treeForm.setIconId(" + iconsRef + "." + icon.getConstantField().getElementName() + ");\n");
          }
          sourceBuilder.append("treeForm.startView();\n");
          sourceBuilder.append("\n");
          // tree form
          sourceBuilder.append("//outline table\n");
          String tableFormRef = validator.getTypeName(SignatureCache.createTypeSignature(RuntimeClasses.DefaultOutlineTableForm));
          sourceBuilder.append(tableFormRef + " tableForm=new " + tableFormRef + "();\n");
          if (icon != null) {
            String iconsRef = validator.getTypeName(SignatureCache.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName()));
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
      execOpenOp.validate();
      execOpenOp.run(monitor, workingCopyManager);
      workingCopyManager.reconcile(desktopType.getCompilationUnit(), monitor);

      // create the outline
      String name = "StandardOutline";
      NlsEntry entry = new NlsEntry(name, client.getNlsProject());
      entry.addTranslation(Language.LANGUAGE_DEFAULT, "Standard");
      client.getNlsProject().updateRow(entry, monitor);

      OutlineNewOperation outlineOp = new OutlineNewOperation();
      outlineOp.setAddToDesktop(true);
      outlineOp.setClientBundle(client);
      outlineOp.setDesktopType(desktopType);
      outlineOp.setFormatSource(false);
      outlineOp.setTypeName(name);
      outlineOp.setPackageName(client.getDefaultPackage(IDefaultTargetPackage.CLIENT_OUTLINES));
      outlineOp.setNlsEntry(entry);
      outlineOp.run(monitor, workingCopyManager);
      workingCopyManager.reconcile(desktopType.getCompilationUnit(), monitor);

      if (server != null && shared != null) {
        // create outline service
        ServiceNewOperation outlineServiceOp = new ServiceNewOperation();
        outlineServiceOp.addProxyRegistrationBundle(client);
        outlineServiceOp.addServiceRegistrationBundle(server);
        outlineServiceOp.setImplementationBundle(server);
        outlineServiceOp.setInterfaceBundle(shared);
        outlineServiceOp.setServiceInterfaceName("IStandardOutlineService");
        outlineServiceOp.setServiceInterfacePackageName(shared.getDefaultPackage(IDefaultTargetPackage.SHARED_SERVICES));
        outlineServiceOp.setServiceInterfaceSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.IService2));
        outlineServiceOp.setServiceName("StandardOutlineService");
        outlineServiceOp.setServicePackageName(server.getDefaultPackage(IDefaultTargetPackage.SERVER_SERVICES));
        outlineServiceOp.setServiceSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService, server.getJavaProject()));
        outlineServiceOp.run(monitor, workingCopyManager);
      }
    }
    else {
      ScoutSdk.logWarning("could not find desktop type");
    }
  }
}
