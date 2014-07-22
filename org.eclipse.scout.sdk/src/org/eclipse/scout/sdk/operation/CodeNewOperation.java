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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.ast.AstUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>CodeNewOperation</h3>
 */
public class CodeNewOperation implements IOperation {

  // in members
  private final IType m_declaringType;
  private String m_nextCodeId;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
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
  public void validate() {
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  private void setStaticModifierForAllCodes() throws CoreException {
    ICompilationUnit cu = getDeclaringType().getCompilationUnit();
    String source = cu.getSource();
    Document document = new Document(source);
    ASTParser parser = AstUtility.newParser();
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setCompilerOptions(cu.getJavaProject().getOptions(true));
    parser.setIgnoreMethodBodies(true);
    parser.setResolveBindings(false);
    parser.setSource(cu);

    CompilationUnit root = (CompilationUnit) parser.createAST(null);
    root.recordModifications();

    root.accept(new ASTVisitor() {
      @SuppressWarnings("unchecked")
      @Override
      public boolean visit(TypeDeclaration node) {
        if (!isStatic(node) && node.getParent().getNodeType() != ASTNode.COMPILATION_UNIT) {
          Modifier modStatic = node.getAST().newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
          node.modifiers().add(modStatic);
        }
        return true;
      }

      private boolean isStatic(TypeDeclaration td) {
        List l = td.modifiers();
        for (Object o : l) { // check if a static modifier is already present (contains does not work)
          if (o instanceof Modifier) {
            if (((Modifier) o).isStatic()) {
              return true;
            }
          }
        }
        return false;
      }
    });

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

    IType iCode = TypeUtility.getType(IRuntimeClasses.ICode);
    OrderedInnerTypeNewOperation codeOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType());
    codeOp.setFormatSource(false);
    codeOp.setSibling(getSibling());
    codeOp.setOrderDefinitionType(iCode);
    codeOp.setSuperTypeSignature(getSuperTypeSignature());
    codeOp.setFlags(Flags.AccPublic | Flags.AccStatic);
    // version id
    codeOp.addFieldSourceBuilder(FieldSourceBuilderFactory.createSerialVersionUidBuilder());
    // nls text
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(codeOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TEXT);
      nlsSourceBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      codeOp.addMethodSourceBuilder(nlsSourceBuilder);
    }
    // id
    IMethodSourceBuilder getIdMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(codeOp.getSourceBuilder(), "getId");
    String idSignature = getIdMethodBuilder.getReturnTypeSignature();
    // id field
    FieldSourceBuilder idFieldBuilder = new FieldSourceBuilder("ID") {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        super.createSource(source, lineDelimiter, ownerProject, validator);
        if ("null".equals(getValue())) {
          source.append(ScoutUtility.getCommentBlock("Auto-generated value"));
        }
      }
    };
    idFieldBuilder.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    idFieldBuilder.setSignature(idSignature);
    idFieldBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesFieldCommentBuilder());
    String nextCodeId = getNextCodeId();
    if (StringUtility.isNullOrEmpty(nextCodeId)) {
      idFieldBuilder.setValue("null");
    }
    else {
      idFieldBuilder.setValue(nextCodeId);
    }
    codeOp.addFieldSourceBuilder(idFieldBuilder);
    // getId method
    getIdMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return ID;"));
    codeOp.addMethodSourceBuilder(getIdMethodBuilder);
    // create type
    codeOp.validate();
    codeOp.run(monitor, workingCopyManager);
    m_createdCode = codeOp.getCreatedType();

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

}
