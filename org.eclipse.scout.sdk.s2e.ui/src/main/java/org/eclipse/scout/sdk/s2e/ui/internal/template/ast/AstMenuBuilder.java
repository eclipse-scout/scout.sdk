/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import static java.util.Collections.addAll;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstMenuBuilder}</h3>
 *
 * @since 5.2.0
 */
public class AstMenuBuilder extends AstTypeBuilder<AstMenuBuilder> {

  private MethodDeclaration m_execAction;

  protected AstMenuBuilder(AstNodeFactory owner) {
    super(owner);
  }

  @Override
  public AstMenuBuilder insert() {
    super.insert();

    // getConfiguredMenuTypes
    if (!AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), IScoutRuntimeTypes.AbstractButton)
        && !AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), IScoutRuntimeTypes.AbstractGroupBox)
        && !AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), IScoutRuntimeTypes.AbstractImageField)) {
      addGetConfiguredMenuTypes();
    }

    // execAction
    m_execAction = getFactory().newExecMethod("execAction")
        .in(get())
        .insert()
        .get();

    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, IScoutRuntimeTypes.IMenu);
    }

    return this;
  }

  public MethodDeclaration getExecAction() {
    return m_execAction;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfiguredMenuTypes() {
    AST ast = getFactory().getAst();

    Type iMenuTypeType = getFactory().newTypeReference(IScoutRuntimeTypes.IMenuType);
    Type setType = getFactory().newTypeReference(Set.class.getName());

    ParameterizedType returnType = ast.newParameterizedType(setType);
    WildcardType extendsIMenuType = ast.newWildcardType();
    extendsIMenuType.setBound(iMenuTypeType, true);
    returnType.typeArguments().add(extendsIMenuType);

    MethodInvocation hashSet = ast.newMethodInvocation();
    String methodName = "hashSet";
    Type collectionUtilityType = getFactory().newTypeReference(IScoutRuntimeTypes.CollectionUtility);
    hashSet.setName(ast.newSimpleName(methodName));
    String collectionUtilityRef = collectionUtilityType.toString();
    hashSet.setExpression(ast.newSimpleName(collectionUtilityRef));

    ReturnStatement returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(hashSet);
    Block body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod("getConfiguredMenuTypes")
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(returnType)
        .withBody(body)
        .in(get())
        .insert();

    // links positions
    ILinkedPositionHolder links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      int offset = collectionUtilityRef.length() + methodName.length() + 2;
      ITrackedNodePosition typeNamePos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(hashSet), offset, -offset - 1);
      links.addLinkedPosition(typeNamePos, true, AstNodeFactory.MENU_TYPE_GROUP);

      MenuTypeLinkedProposal menuTypeLinkedProposal = getMenuTypeLinkedProposal();
      if (menuTypeLinkedProposal != null) {
        getFactory().getImportRewrite().addImport(menuTypeLinkedProposal.m_typeFqn, getFactory().getContext());

        for (String defaultVal : menuTypeLinkedProposal.m_defaultValues) {
          hashSet.arguments().add(ast.newQualifiedName(ast.newSimpleName(menuTypeLinkedProposal.m_typeSimpleName), ast.newSimpleName(defaultVal)));
        }

        if (menuTypeLinkedProposal.m_menuTypeProposals.size() > 1) {
          for (String menuTypeProposal : menuTypeLinkedProposal.m_menuTypeProposals) {
            links.addLinkedPositionProposal(AstNodeFactory.MENU_TYPE_GROUP, menuTypeProposal);
          }
        }
      }
    }
  }

  private MenuTypeLinkedProposal getMenuTypeLinkedProposal() {
    ITypeBinding hierarchy = getFactory().getDeclaringTypeBinding();
    if (AstUtils.isInstanceOf(hierarchy, IScoutRuntimeTypes.AbstractTable)) {
      MenuTypeLinkedProposal tableMenuType = new MenuTypeLinkedProposal(IScoutRuntimeTypes.TableMenuType, IScoutRuntimeTypes.TableMenuType_SingleSelection, IScoutRuntimeTypes.TableMenuType_MultiSelection);
      tableMenuType.addProposal(IScoutRuntimeTypes.TableMenuType_EmptySpace);
      tableMenuType.addProposal(IScoutRuntimeTypes.TableMenuType_EmptySpace, IScoutRuntimeTypes.TableMenuType_Header);
      tableMenuType.addProposal(IScoutRuntimeTypes.TableMenuType_EmptySpace, IScoutRuntimeTypes.TableMenuType_SingleSelection, IScoutRuntimeTypes.TableMenuType_MultiSelection);
      return tableMenuType;
    }

    if (AstUtils.isInstanceOf(hierarchy, IScoutRuntimeTypes.AbstractValueField)) {
      MenuTypeLinkedProposal valueFieldMenuType = new MenuTypeLinkedProposal(IScoutRuntimeTypes.ValueFieldMenuType, IScoutRuntimeTypes.ValueFieldMenuType_NotNull);
      valueFieldMenuType.addProposal(IScoutRuntimeTypes.ValueFieldMenuType_Null);
      valueFieldMenuType.addProposal(IScoutRuntimeTypes.ValueFieldMenuType_Null, IScoutRuntimeTypes.ValueFieldMenuType_NotNull);
      return valueFieldMenuType;
    }

    if (AstUtils.isInstanceOf(hierarchy, IScoutRuntimeTypes.AbstractTree) || AstUtils.isInstanceOf(hierarchy, IScoutRuntimeTypes.AbstractTreeNode)) {
      MenuTypeLinkedProposal calMenuType = new MenuTypeLinkedProposal(IScoutRuntimeTypes.TreeMenuType, IScoutRuntimeTypes.TreeMenuType_SingleSelection, IScoutRuntimeTypes.TreeMenuType_MultiSelection);
      calMenuType.addProposal(IScoutRuntimeTypes.TreeMenuType_EmptySpace);
      calMenuType.addProposal(IScoutRuntimeTypes.TreeMenuType_SingleSelection, IScoutRuntimeTypes.TreeMenuType_MultiSelection, IScoutRuntimeTypes.TreeMenuType_EmptySpace);
      return calMenuType;
    }

    if (AstUtils.isInstanceOf(hierarchy, IScoutRuntimeTypes.AbstractTabBox)) {
      return new MenuTypeLinkedProposal(IScoutRuntimeTypes.TabBoxMenuType, IScoutRuntimeTypes.TabBoxMenuType_Header);
    }

    if (AstUtils.isInstanceOf(hierarchy, IScoutRuntimeTypes.AbstractCalendarItemProvider) || AstUtils.isInstanceOf(hierarchy, IScoutRuntimeTypes.AbstractCalendar)) {
      MenuTypeLinkedProposal calMenuType = new MenuTypeLinkedProposal(IScoutRuntimeTypes.CalendarMenuType, IScoutRuntimeTypes.CalendarMenuType_CalendarComponent);
      calMenuType.addProposal(IScoutRuntimeTypes.CalendarMenuType_EmptySpace);
      calMenuType.addProposal(IScoutRuntimeTypes.CalendarMenuType_CalendarComponent);
      calMenuType.addProposal(IScoutRuntimeTypes.CalendarMenuType_EmptySpace, IScoutRuntimeTypes.CalendarMenuType_CalendarComponent);
      return calMenuType;
    }
    return null;
  }

  private static final class MenuTypeLinkedProposal {
    private final String m_typeFqn;
    private final String m_typeSimpleName;
    private final List<String> m_defaultValues;
    private final List<String> m_menuTypeProposals;

    private MenuTypeLinkedProposal(String typeFqn, String... defaultValues) {
      m_typeFqn = typeFqn;
      m_typeSimpleName = JavaTypes.simpleName(typeFqn);
      m_defaultValues = new ArrayList<>(defaultValues.length);
      addAll(m_defaultValues, defaultValues);
      m_menuTypeProposals = new ArrayList<>();
      addProposal(defaultValues);
    }

    private void addProposal(String... simpleNames) {
      StringBuilder builder = new StringBuilder();
      if (simpleNames != null && simpleNames.length > 0) {
        builder.append(m_typeSimpleName).append(JavaTypes.C_DOT).append(simpleNames[0]);
        for (int i = 1; i < simpleNames.length; i++) {
          builder.append(", ").append(m_typeSimpleName).append(JavaTypes.C_DOT).append(simpleNames[i]);
        }
      }
      m_menuTypeProposals.add(builder.toString());
    }
  }
}
