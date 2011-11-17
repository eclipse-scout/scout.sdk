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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
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
public class OutlineTemplateOperation implements IOperation {

  IScoutProject m_scoutProject;

  public OutlineTemplateOperation(IScoutProject project) {
    m_scoutProject = project;
  }

  @Override
  public String getOperationName() {
    return "Applay outline template...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getScoutProject() == null) {
      throw new IllegalArgumentException("scout project must not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IType desktopType = TypeUtility.getType(getScoutProject().getClientBundle().getBundleName() + IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_DESKTOP + ".Desktop");
    if (TypeUtility.exists(desktopType)) {
      MethodOverrideOperation execOpenOp = new MethodOverrideOperation(desktopType, "execOpened") {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder sourceBuilder = new StringBuilder();
          sourceBuilder.append("// outline tree\n");
          String treeFormRef = validator.getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.DefaultOutlineTreeForm, true));
          sourceBuilder.append(treeFormRef + " treeForm = new " + treeFormRef + "();\n");
          ScoutIconDesc icon = getScoutProject().getIconProvider().getIcon("eclipse_scout");
          if (icon != null) {
            String iconsRef = validator.getSimpleTypeRef(Signature.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName(), true));
            sourceBuilder.append("treeForm.setIconId(" + iconsRef + "." + icon.getConstantField().getElementName() + ");\n");
          }
          sourceBuilder.append("treeForm.startView();\n");
          sourceBuilder.append("\n");
          // tree form
          sourceBuilder.append("//outline table\n");
          String tableFormRef = validator.getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.DefaultOutlineTableForm, true));
          sourceBuilder.append(tableFormRef + " tableForm=new " + tableFormRef + "();\n");
          if (icon != null) {
            String iconsRef = validator.getSimpleTypeRef(Signature.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName(), true));
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
    }
    else {
      ScoutSdk.logWarning("could not find desktop type");
    }
  }

  /**
   * @return the scoutProject
   */
  public IScoutProject getScoutProject() {
    return m_scoutProject;
  }

  /**
   * @param scoutProject
   *          the scoutProject to set
   */
  public void setScoutProject(IScoutProject scoutProject) {
    m_scoutProject = scoutProject;
  }

}
