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
package org.eclipse.scout.sdk.ui.internal.extensions.technology.laf;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.util.OrganizeImportOperation;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.extensions.technology.ScoutTechnologyResource;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link RayoUiSwingEnvTechnologyHandler}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 19.02.2012
 */
public class RayoUiSwingEnvTechnologyHandler extends AbstractScoutTechnologyHandler {

  private final static Pattern RAYO_ENV_REGEX = Pattern.compile("public.*class.*extends.*RayoSwingEnvironment.*", Pattern.DOTALL);

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, final boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IScoutTechnologyResource res : resources) {
      IType swingEnv = getType(res.getBundle(), res.getResource());
      setSuperClass(swingEnv, selected, monitor, workingCopyManager);
    }
  }

  private void setSuperClass(IType swingEnv, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ICompilationUnit cu = swingEnv.getCompilationUnit();
    workingCopyManager.register(cu, monitor);
    String source = cu.getSource();
    Document document = new Document(source);
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setIgnoreMethodBodies(true);
    parser.setSource(source.toCharArray());
    CompilationUnit root = (CompilationUnit) parser.createAST(monitor);

    root.recordModifications();
    Type newSuperClassType = null;
    if (selected) {
      newSuperClassType = root.getAST().newSimpleType(root.getAST().newQualifiedName(root.getAST().newName("com.bsiag.scout.rt.ui.swing.rayo"), root.getAST().newSimpleName("RayoSwingEnvironment")));
    }
    else {
      newSuperClassType = root.getAST().newSimpleType(root.getAST().newQualifiedName(root.getAST().newName("org.eclipse.scout.rt.ui.swing"), root.getAST().newSimpleName("DefaultSwingEnvironment")));
    }

    for (Object type : root.types()) {
      if (type instanceof TypeDeclaration) {
        TypeDeclaration td = (TypeDeclaration) type;
        td.setSuperclassType(newSuperClassType);
      }
    }

    TextEdit edits = root.rewrite(document, cu.getJavaProject().getOptions(true));
    try {
      edits.apply(document);
    }
    catch (MalformedTreeException e) {
      throw new CoreException(new ScoutStatus(e));
    }
    catch (BadLocationException e) {
      throw new CoreException(new ScoutStatus(e));
    }
    cu.getBuffer().setContents(document.get());

    OrganizeImportOperation o = new OrganizeImportOperation(cu);
    o.validate();
    o.run(monitor, workingCopyManager);
  }

  private IType getType(IScoutBundle bundle, IFile f) {
    for (IType candidate : getSwingEnvironments(bundle)) {
      if (candidate.getResource().equals(f)) {
        return candidate;
      }
    }
    return null;
  }

  @Override
  public TriState getSelection(IScoutProject project) {
    IType[] swingEnvs = getSwingEnvironments(project.getUiSwingBundle());
    if (swingEnvs == null || swingEnvs.length == 0) {
      return TriState.FALSE;
    }

    TriState ret = TriState.parseTriState(isRayoEnvironment(swingEnvs[0]));
    for (int i = 1; i < swingEnvs.length; i++) {
      TriState tmp = TriState.parseTriState(isRayoEnvironment(swingEnvs[i]));
      if (ret != tmp) {
        return TriState.UNDEFINED;
      }
    }
    return ret;
  }

  private boolean isRayoEnvironment(IType swingEnv) {
    if (!TypeUtility.exists(swingEnv)) {
      return false;
    }

    try {
      String source = swingEnv.getSource();
      return RAYO_ENV_REGEX.matcher(source).matches();
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("unable to read source of SwingEnvironment class: " + swingEnv, e);
      return false;
    }
  }

  @Override
  public boolean isActive(IScoutProject project) {
    return project.getClientBundle() != null && project.getClientBundle().getProject().exists() &&
        project.getUiSwingBundle() != null && project.getUiSwingBundle().getProject().exists();
  }

  private IType[] getSwingEnvironments(IScoutBundle bundle) {
    IType baseType = TypeUtility.getType(RuntimeClasses.ISwingEnvironment);
    if (TypeUtility.exists(baseType)) {
      IPrimaryTypeTypeHierarchy hierarchy = TypeUtility.getPrimaryTypeHierarchy(baseType);
      return hierarchy.getAllSubtypes(baseType, TypeFilters.getClassesInProject(bundle.getJavaProject()));
    }
    return new IType[]{};
  }

  @Override
  protected void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list) {
    for (IType swingEnvType : getSwingEnvironments(project.getUiSwingBundle())) {
      IResource res = swingEnvType.getResource();
      if (res.getType() == IResource.FILE) {
        IFile javaRes = (IFile) res;
        ScoutTechnologyResource tr = new ScoutTechnologyResource(project.getUiSwingBundle(), javaRes);
        list.add(tr);
      }
    }
  }
}
