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
package org.eclipse.scout.sdk.rap.operations.project.template;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.template.OutlineTemplateOperation;
import org.eclipse.scout.sdk.operation.project.template.SingleFormTemplateOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateMobileClientPluginOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateUiRapPluginOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;

/**
 * <h3>{@link MobileDesktopExtensionInstallOperation}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 08.03.2013
 */
public class MobileDesktopExtensionInstallOperation extends AbstractScoutProjectNewOperation {

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID) &&
        (OutlineTemplateOperation.TEMPLATE_ID.equals(getTemplateName()) || SingleFormTemplateOperation.TEMPLATE_ID.equals(getTemplateName()));
  }

  @Override
  public void init() {
  }

  @Override
  public String getOperationName() {
    return "Apply mobile desktop extension...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    IScoutBundle mobileClient = bundleGraph.getBundle(getProperties().getProperty(CreateMobileClientPluginOperation.PROP_MOBILE_BUNDLE_CLIENT_NAME, String.class));

    String homeFormFqn = bundleGraph.getBundle(getProperties().getProperty(CreateMobileClientPluginOperation.PROP_MOBILE_BUNDLE_CLIENT_NAME, String.class)).getPackageName("ui.forms") + ".MobileHomeForm";
    final String homeFormSignature = SignatureCache.createTypeSignature(homeFormFqn);

    String pck = mobileClient.getDefaultPackage(IDefaultTargetPackage.CLIENT_DESKTOP);
    final String desktopExtensionName = "DesktopExtension";
    PrimaryTypeNewOperation desktopExtensionOp = new PrimaryTypeNewOperation(desktopExtensionName, pck, mobileClient.getJavaProject());
    desktopExtensionOp.setIcuCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    desktopExtensionOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    desktopExtensionOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IDesktopExtension, mobileClient));
    desktopExtensionOp.setFlags(Flags.AccPublic);
    // fields
    final String homeFormFieldName = "m_homeForm";
    desktopExtensionOp.addFieldSourceBuilder(FieldSourceBuilderFactory.createFieldSourceBuilder(homeFormFieldName, homeFormSignature, Flags.AccPrivate, null));
    IFieldSourceBuilder activeFieldBuilder = FieldSourceBuilderFactory.createFieldSourceBuilder("m_active", Signature.SIG_BOOLEAN, Flags.AccPrivate, null);
    desktopExtensionOp.addFieldSourceBuilder(activeFieldBuilder);
    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(desktopExtensionName);
    constructorBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    constructorBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("setActive(!").append(validator.getTypeName(SignatureCache.createTypeSignature("org.eclipse.scout.rt.shared.ui.UserAgentUtility")));
        source.append(".isDesktopDevice());").append(lineDelimiter);
      }
    });
    desktopExtensionOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
    // isActive
    IMethodSourceBuilder isActiveBuilder = MethodSourceBuilderFactory.createGetter(activeFieldBuilder);
    desktopExtensionOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(isActiveBuilder), isActiveBuilder);
    // setActive
    IMethodSourceBuilder setActiveBuilder = MethodSourceBuilderFactory.createSetter(activeFieldBuilder);
    desktopExtensionOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(setActiveBuilder), setActiveBuilder);

    // execGuiAttached
    IMethodSourceBuilder execGuiAttachedBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(desktopExtensionOp.getSourceBuilder(), "execGuiAttached");
    execGuiAttachedBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("if (!isActive()) {").append(lineDelimiter);
        source.append("  return super.execGuiAttached();").append(lineDelimiter);
        source.append("}").append(lineDelimiter).append(lineDelimiter);
        source.append("if (").append(homeFormFieldName).append(" == null) {").append(lineDelimiter);
        source.append("  ").append(homeFormFieldName).append(" = new ").append(validator.getTypeName(homeFormSignature)).append("();").append(lineDelimiter);
        source.append("  ").append(homeFormFieldName).append(".startView();").append(lineDelimiter);
        source.append("}").append(lineDelimiter);
        source.append("return ").append(validator.getTypeName(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.ui.desktop.ContributionCommand"))).append(".Continue;").append(lineDelimiter);

      }
    });
    desktopExtensionOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execGuiAttachedBuilder), execGuiAttachedBuilder);
    // execGuiDetached method
    IMethodSourceBuilder execGuiDetachedBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(desktopExtensionOp.getSourceBuilder(), "execGuiDetached");
    execGuiDetachedBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("if (!isActive()) {").append(lineDelimiter);
        source.append("  return super.execGuiDetached();").append(lineDelimiter);
        source.append("}").append(lineDelimiter).append(lineDelimiter);
        source.append("if (").append(homeFormFieldName).append(" != null) {").append(lineDelimiter);
        source.append("  ").append(homeFormFieldName).append(".doClose();").append(lineDelimiter);
        source.append("}").append(lineDelimiter);
        source.append("return ").append(validator.getTypeName(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.ui.desktop.ContributionCommand"))).append(".Continue;").append(lineDelimiter);
      }
    });
    desktopExtensionOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execGuiDetachedBuilder), execGuiDetachedBuilder);

    desktopExtensionOp.setFormatSource(true);
    desktopExtensionOp.validate();
    desktopExtensionOp.run(monitor, workingCopyManager);
    IType desktopExtension = desktopExtensionOp.getCreatedType();

    registerDesktopExtension(desktopExtension, mobileClient, monitor, workingCopyManager);
  }

  private void registerDesktopExtension(IType desktopExtension, IScoutBundle mobileClient, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    HashMap<String, String> properties = new HashMap<String, String>(2);
    properties.put("active", "true");
    properties.put("class", desktopExtension.getFullyQualifiedName());
    PluginModelHelper pmh = new PluginModelHelper(mobileClient.getSymbolicName());
    pmh.PluginXml.addSimpleExtension(IRuntimeClasses.EXTENSION_POINT_DESKTOP_EXTENSIONS, IRuntimeClasses.EXTENSION_ELEMENT_DESKTOP_EXTENSION, properties);
    pmh.save();
  }
}
