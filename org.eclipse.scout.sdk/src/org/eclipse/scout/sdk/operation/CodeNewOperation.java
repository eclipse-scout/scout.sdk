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
package org.eclipse.scout.sdk.operation;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.field.FieldCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>CodeNewOperation</h3> ...
 */
public class CodeNewOperation implements IOperation {

  final IType iCode = TypeUtility.getType(RuntimeClasses.ICode);

  // in members
  private final IType m_declaringType;
  private String m_nextCodeId;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private String m_genericTypeSignature;
  private IJavaElement m_sibling;
  private boolean m_formatSource;
  // out members
  private IType m_createdCode;

  public CodeNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public CodeNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "Create Code '" + getTypeName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  @SuppressWarnings("unchecked")
  private static void setStaticModifierRec(TypeDeclaration td) {
    @SuppressWarnings("rawtypes")
	List l = td.modifiers();
    Modifier modStatic = td.getAST().newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
    boolean isStatic = false;
    for (Object o : l) { // check if a static modifier is already present (contains does not work)
      if (o instanceof Modifier) {
        if (((Modifier) o).isStatic()) {
          isStatic = true;
          break;
        }
      }
    }
    if (!isStatic) {
      td.modifiers().add(modStatic);
    }
    for (TypeDeclaration child : td.getTypes()) {
      setStaticModifierRec(child);
    }
  }

  private void setStaticModifierForAllCodes() throws CoreException {
    ICompilationUnit cu = getDeclaringType().getCompilationUnit();
    String source = cu.getSource();
    Document document = new Document(source);
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(source.toCharArray());
    CompilationUnit root = (CompilationUnit) parser.createAST(null);

    root.recordModifications();
    for (Object type : root.types()) {
      if (type instanceof TypeDeclaration) {
        for (TypeDeclaration c : ((TypeDeclaration) type).getTypes())
          setStaticModifierRec(c);
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
    String newSource = document.get();
    cu.getBuffer().setContents(newSource);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // to correct legacy codes: in older SDK releases nested Code classes in a CodeType have not been declared static.
    // Go through all legacy codes in the current compilation unit and make them static -> old codes are migrated to the new static style.
    // This is needed because if a new child Code is created inside an old (not static) Code, this leads to compilation errors.
    setStaticModifierForAllCodes();

    OrderedInnerTypeNewOperation codeOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType());
    codeOp.setFormatSource(false);
    codeOp.setSibling(getSibling());
    codeOp.setOrderDefinitionType(iCode);
    codeOp.setSuperTypeSignature(getSuperTypeSignature());
    codeOp.setTypeModifiers(Flags.AccPublic | Flags.AccStatic);
    codeOp.validate();
    codeOp.run(monitor, workingCopyManager);
    m_createdCode = codeOp.getCreatedType();

    FieldCreateOperation versionUidOp = new FieldCreateOperation(getCreatedCode(), "serialVersionUID", false);
    versionUidOp.setFlags(Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    versionUidOp.setSignature(Signature.SIG_LONG);
    versionUidOp.setSimpleInitValue("1L");
    versionUidOp.validate();
    versionUidOp.run(monitor, workingCopyManager);

    IJavaElement nlsMethodSibling = null;
    String[] typeArguments = Signature.getTypeArguments(getSuperTypeSignature());
    if (typeArguments != null && typeArguments.length > 0) {
      String typeSig = getGenericTypeSignature();
      if (typeSig.equals(typeArguments[0])) {
        final boolean isCodeIdUndef = StringUtility.isNullOrEmpty(getNextCodeId());
        final String todo = isCodeIdUndef ? ScoutUtility.getCommentBlock("Auto-generated value") : "";
        final String codeId = isCodeIdUndef ? "null" : getNextCodeId();
        FieldCreateOperation idOp = new FieldCreateOperation(getCreatedCode(), "ID", false) {
          @Override
          public void buildSource(StringBuilder builder, IImportValidator validator) throws JavaModelException {
            super.buildSource(builder, validator);
            builder.append(todo);
          }
        };

        idOp.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
        idOp.setSignature(typeSig);
        idOp.setSimpleInitValue(codeId);
        idOp.validate();
        idOp.run(monitor, workingCopyManager);

        MethodOverrideOperation getIdOp = new MethodOverrideOperation(getCreatedCode(), "getId", false);
        getIdOp.setSimpleBody("return ID;");
        getIdOp.setReturnTypeSignature(getGenericTypeSignature());
        getIdOp.validate();
        getIdOp.run(monitor, workingCopyManager);
        nlsMethodSibling = getIdOp.getCreatedMethod();
      }

    }
    if (getNlsEntry() != null) {
      final IJavaElement finalSibing = nlsMethodSibling;
      NlsTextMethodUpdateOperation confTextOp = new NlsTextMethodUpdateOperation(getCreatedCode(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TEXT, false) {
        @Override
        protected IJavaElement computeSibling() {
          return finalSibing;
        }
      };
      confTextOp.setNlsEntry(getNlsEntry());
      confTextOp.validate();
      confTextOp.run(monitor, workingCopyManager);
    }

    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedCode(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedCode() {
    return m_createdCode;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setNextCodeId(String nextCodeId) {
    m_nextCodeId = nextCodeId;
  }

  public String getNextCodeId() {
    return m_nextCodeId;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setGenericTypeSignature(String genericTypeSignature) {
    m_genericTypeSignature = genericTypeSignature;
  }

  public String getGenericTypeSignature() {
    return m_genericTypeSignature;
  }
}
