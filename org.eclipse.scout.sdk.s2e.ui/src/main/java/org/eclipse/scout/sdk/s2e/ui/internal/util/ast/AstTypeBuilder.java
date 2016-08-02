/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.util.ast;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.util.CoreScoutUtils;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerationContext;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstTypeBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class AstTypeBuilder<INSTANCE extends AstTypeBuilder<INSTANCE>> extends AbstractAstBuilder<INSTANCE> {

  private final INSTANCE m_return;

  // params
  private String m_nlsMethodName;
  private String m_typeName;
  private String m_readOnlyNameSuffix;
  private Type m_superType;
  private String m_orderDefinitionTypeFqn;
  private int m_pos;
  private boolean m_calcOrderNr; // default enabled
  private boolean m_createOrder;
  private boolean m_createClassId;
  private String m_proposalBaseFqn;

  // out
  private TypeDeclaration m_resultType;

  @SuppressWarnings("unchecked")
  protected AstTypeBuilder(AstNodeFactory owner) {
    super(owner);
    m_calcOrderNr = true;
    m_pos = -1; // invalid initial value
    m_return = (INSTANCE) this;
  }

  public INSTANCE withNlsMethod(String methodName) {
    m_nlsMethodName = methodName;
    return m_return;
  }

  public INSTANCE withName(String name) {
    m_typeName = name;
    return m_return;
  }

  public INSTANCE withReadOnlyNameSuffix(String readOnlyNameSuffix) {
    m_readOnlyNameSuffix = readOnlyNameSuffix;
    return m_return;
  }

  public INSTANCE withSuperType(Type superType) {
    m_superType = superType;
    return m_return;
  }

  public INSTANCE atPosition(int pos) {
    m_pos = pos;
    return m_return;
  }

  public INSTANCE withCalculatedOrder(boolean autoCalcOrder) {
    m_calcOrderNr = autoCalcOrder;
    return m_return;
  }

  public INSTANCE withOrderDefinitionType(String fqn) {
    m_orderDefinitionTypeFqn = fqn;
    return m_return;
  }

  public INSTANCE withOrder(boolean createOrder) {
    m_createOrder = createOrder;
    return m_return;
  }

  public INSTANCE withClassId(boolean createClassId) {
    m_createClassId = createClassId;
    return m_return;
  }

  public INSTANCE withProposalBaseFqn(String valueFieldInterfaceFqn) {
    m_proposalBaseFqn = valueFieldInterfaceFqn;
    return m_return;
  }

  public String getProposalBaseFqn() {
    return m_proposalBaseFqn;
  }

  public TypeDeclaration get() {
    return m_resultType;
  }

  public String getNlsMethodName() {
    return m_nlsMethodName;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public String getReadOnlySuffix() {
    return m_readOnlyNameSuffix;
  }

  public Type getSuperType() {
    return m_superType;
  }

  public String getOrderDefinitionType() {
    return m_orderDefinitionTypeFqn;
  }

  public int getInsertPosition() {
    return m_pos;
  }

  public boolean isCalculateOrderValue() {
    return m_calcOrderNr;
  }

  public boolean isCreateOrderAnnotation() {
    return m_createOrder;
  }

  public boolean isCreateClassIdAnnotation() {
    return m_createClassId;
  }

  @Override
  @SuppressWarnings("unchecked")
  public INSTANCE insert() {
    AST ast = getFactory().getAst();
    if (getReadOnlySuffix() == null) {
      withReadOnlyNameSuffix("");
    }
    boolean createClassId = isCreateClassIdAnnotation() && ClassIdGenerators.isAutomaticallyCreateClassIdAnnotation();

    m_resultType = ast.newTypeDeclaration();
    m_resultType.setInterface(false);
    for (ModifierKeyword mod : getModifiers()) {
      m_resultType.modifiers().add(ast.newModifier(mod));
    }
    SimpleName typeName = ast.newSimpleName(getTypeName() + getReadOnlySuffix());
    m_resultType.setName(typeName);
    if (getSuperType() != null) {
      m_resultType.setSuperclassType(getSuperType());
    }

    // linked positions for the type
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      ITrackedNodePosition typeNamePos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(m_resultType.getName()), 0, -getReadOnlySuffix().length());
      links.addLinkedPosition(typeNamePos, true, AstNodeFactory.TYPE_NAME_GROUP);
      if (getSuperType() != null) {
        ASTNode superTypeLinkNode = getSuperType();
        if (superTypeLinkNode instanceof ParameterizedType) {
          superTypeLinkNode = ((ParameterizedType) superTypeLinkNode).getType();
        }
        links.addLinkedPosition(getFactory().getRewrite().track(superTypeLinkNode), true, AstNodeFactory.SUPER_TYPE_GROUP);
      }
      if (getProposalBaseFqn() != null) {
        links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getProposalBaseFqn());
      }
    }

    // calc fully qualified name of the resulting type
    String declaringTypeFqn = null;
    if ((isCreateOrderAnnotation() && isCalculateOrderValue()) || createClassId) {
      declaringTypeFqn = AstUtils.getFullyQualifiedName(getDeclaringType(), getFactory().getType());
    }

    // order annotation
    if (isCreateOrderAnnotation()) {
      double newOrder = ISdkProperties.VIEW_ORDER_ANNOTATION_VALUE_STEP;
      if (isCalculateOrderValue()) {
        newOrder = CoreScoutUtils.getNewViewOrderValue(getFactory().getJavaEnvironment().findType(declaringTypeFqn), getOrderDefinitionType(), getInsertPosition());
      }

      SingleMemberAnnotation order = ast.newSingleMemberAnnotation();
      String orderRef = getFactory().getImportRewrite().addImport(IScoutRuntimeTypes.Order);
      order.setTypeName(ast.newName(orderRef));
      NumberLiteral orderValue = ast.newNumberLiteral(convertOrderToJavaSourceString(newOrder));
      order.setValue(orderValue);

      AstUtils.addAnnotationTo(order, m_resultType);
    }

    // classId annotation
    if (createClassId) {
      String fqn = new StringBuilder(declaringTypeFqn).append('$').append(getTypeName()).toString();
      SingleMemberAnnotation classIdAnnotation = getFactory().newClassIdAnnotation(new ClassIdGenerationContext(fqn));
      AstUtils.addAnnotationTo(classIdAnnotation, m_resultType);
    }

    // NLS method
    if (StringUtils.isNotBlank(getNlsMethodName())) {
      getFactory().newNlsMethod(getNlsMethodName())
          .in(m_resultType)
          .insert();
    }

    // add to the declaring node
    ListRewrite typeRewrite = getFactory().getRewrite().getListRewrite(getDeclaringType(), getDeclaringType().getBodyDeclarationsProperty());
    ASTNode previousNode = AstUtils.getPreviousNode(getDeclaringType(), getInsertPosition(), new P_EnsureElementInRewriteFilter(typeRewrite));
    if (previousNode == null) {
      typeRewrite.insertFirst(m_resultType, null);
    }
    else {
      typeRewrite.insertAfter(m_resultType, previousNode, null);
    }
    return m_return;
  }

  protected static String convertOrderToJavaSourceString(double order) {
    NumberFormat f = NumberFormat.getNumberInstance(Locale.ENGLISH);
    f.setGroupingUsed(false);
    String newOrderStr = f.format(order);

    final String zeroSuffix = ".0";
    if (newOrderStr.endsWith(zeroSuffix)) {
      newOrderStr = newOrderStr.substring(0, newOrderStr.length() - zeroSuffix.length());
    }

    if (order > Integer.MAX_VALUE && newOrderStr.indexOf('.') < 0) {
      // we must specify the double data type because we do not have a decimal separator and we do not fit into an integer literal.
      newOrderStr += 'd';
    }
    return newOrderStr;
  }

  private static final class P_EnsureElementInRewriteFilter implements IFilter<ASTNode> {
    private final Set<Object> m_elements;

    private P_EnsureElementInRewriteFilter(ListRewrite declaringTypeRewrite) {
      List<?> originalList = declaringTypeRewrite.getOriginalList();
      m_elements = new HashSet<>(originalList.size());
      for (Object element : originalList) {
        m_elements.add(element);
      }
    }

    @Override
    public boolean evaluate(ASTNode element) {
      return m_elements.contains(element);
    }
  }
}
