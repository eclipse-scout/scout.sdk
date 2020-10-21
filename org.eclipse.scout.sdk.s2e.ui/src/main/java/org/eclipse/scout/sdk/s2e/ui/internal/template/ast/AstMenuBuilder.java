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
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
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
    if (!AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), getFactory().getScoutApi().AbstractButton().fqn())
        && !AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), getFactory().getScoutApi().AbstractGroupBox().fqn())
        && !AstUtils.isInstanceOf(getFactory().getDeclaringTypeBinding(), getFactory().getScoutApi().AbstractImageField().fqn())) {
      addGetConfiguredMenuTypes();
    }

    // execAction
    m_execAction = getFactory().newExecMethod(getFactory().getScoutApi().AbstractAction().execActionMethodName())
        .in(get())
        .insert()
        .get();

    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      links.addLinkedPositionProposalsHierarchy(AstNodeFactory.SUPER_TYPE_GROUP, getFactory().getScoutApi().IMenu().fqn());
    }

    return this;
  }

  public MethodDeclaration getExecAction() {
    return m_execAction;
  }

  @SuppressWarnings("unchecked")
  protected void addGetConfiguredMenuTypes() {
    var ast = getFactory().getAst();

    var scoutApi = getFactory().getScoutApi();
    var iMenuTypeType = getFactory().newTypeReference(scoutApi.IMenuType().fqn());
    var setType = getFactory().newTypeReference(Set.class.getName());

    var returnType = ast.newParameterizedType(setType);
    var extendsIMenuType = ast.newWildcardType();
    extendsIMenuType.setBound(iMenuTypeType, true);
    returnType.typeArguments().add(extendsIMenuType);

    var hashSet = ast.newMethodInvocation();
    var methodName = getFactory().getScoutApi().CollectionUtility().hashSetMethodName();
    var collectionUtilityType = getFactory().newTypeReference(scoutApi.CollectionUtility().fqn());
    hashSet.setName(ast.newSimpleName(methodName));
    var collectionUtilityRef = collectionUtilityType.toString();
    hashSet.setExpression(ast.newSimpleName(collectionUtilityRef));

    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(hashSet);
    var body = ast.newBlock();
    body.statements().add(returnStatement);

    getFactory().newMethod(getFactory().getScoutApi().AbstractMenu().getConfiguredMenuTypesMethodName())
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(returnType)
        .withBody(body)
        .in(get())
        .insert();

    // links positions
    var links = getFactory().getLinkedPositionHolder();
    if (links != null && isCreateLinks()) {
      var offset = collectionUtilityRef.length() + methodName.length() + 2;
      ITrackedNodePosition typeNamePos = new WrappedTrackedNodePosition(getFactory().getRewrite().track(hashSet), offset, -offset - 1);
      links.addLinkedPosition(typeNamePos, true, AstNodeFactory.MENU_TYPE_GROUP);

      var menuTypeLinkedProposal = getMenuTypeLinkedProposal();
      if (menuTypeLinkedProposal != null) {
        getFactory().getImportRewrite().addImport(menuTypeLinkedProposal.m_typeFqn, getFactory().getContext());

        for (var defaultVal : menuTypeLinkedProposal.m_defaultValues) {
          hashSet.arguments().add(ast.newQualifiedName(ast.newSimpleName(menuTypeLinkedProposal.m_typeSimpleName), ast.newSimpleName(defaultVal)));
        }

        if (menuTypeLinkedProposal.m_menuTypeProposals.size() > 1) {
          for (var menuTypeProposal : menuTypeLinkedProposal.m_menuTypeProposals) {
            links.addLinkedPositionProposal(AstNodeFactory.MENU_TYPE_GROUP, menuTypeProposal);
          }
        }
      }
    }
  }

  private MenuTypeLinkedProposal getMenuTypeLinkedProposal() {
    var hierarchy = getFactory().getDeclaringTypeBinding();
    var scoutApi = getFactory().getScoutApi();
    if (AstUtils.isInstanceOf(hierarchy, scoutApi.AbstractTable().fqn())) {
      var tableMenuType = new MenuTypeLinkedProposal(scoutApi.TableMenuType().fqn(), scoutApi.TableMenuType().SingleSelection(), scoutApi.TableMenuType().MultiSelection());
      tableMenuType.addProposal(scoutApi.TableMenuType().EmptySpace());
      tableMenuType.addProposal(scoutApi.TableMenuType().EmptySpace(), scoutApi.TableMenuType().Header());
      tableMenuType.addProposal(scoutApi.TableMenuType().EmptySpace(), scoutApi.TableMenuType().SingleSelection(), scoutApi.TableMenuType().MultiSelection());
      return tableMenuType;
    }

    if (AstUtils.isInstanceOf(hierarchy, scoutApi.AbstractValueField().fqn())) {
      var valueFieldMenuType = new MenuTypeLinkedProposal(scoutApi.ValueFieldMenuType().fqn(), scoutApi.ValueFieldMenuType().NotNull());
      valueFieldMenuType.addProposal(scoutApi.ValueFieldMenuType().Null());
      valueFieldMenuType.addProposal(scoutApi.ValueFieldMenuType().Null(), scoutApi.ValueFieldMenuType().NotNull());
      return valueFieldMenuType;
    }

    if (AstUtils.isInstanceOf(hierarchy, scoutApi.AbstractTree().fqn()) || AstUtils.isInstanceOf(hierarchy, scoutApi.AbstractTreeNode().fqn())) {
      var calMenuType = new MenuTypeLinkedProposal(scoutApi.TreeMenuType().fqn(), scoutApi.TreeMenuType().SingleSelection(), scoutApi.TreeMenuType().MultiSelection());
      calMenuType.addProposal(scoutApi.TreeMenuType().EmptySpace());
      calMenuType.addProposal(scoutApi.TreeMenuType().SingleSelection(), scoutApi.TreeMenuType().MultiSelection(), scoutApi.TreeMenuType().EmptySpace());
      return calMenuType;
    }

    if (AstUtils.isInstanceOf(hierarchy, scoutApi.AbstractTabBox().fqn())) {
      return new MenuTypeLinkedProposal(scoutApi.TabBoxMenuType().fqn(), scoutApi.TabBoxMenuType().Header());
    }

    if (AstUtils.isInstanceOf(hierarchy, scoutApi.AbstractCalendarItemProvider().fqn()) || AstUtils.isInstanceOf(hierarchy, scoutApi.AbstractCalendar().fqn())) {
      var calMenuType = new MenuTypeLinkedProposal(scoutApi.CalendarMenuType().fqn(), scoutApi.CalendarMenuType().CalendarComponent());
      calMenuType.addProposal(scoutApi.CalendarMenuType().EmptySpace());
      calMenuType.addProposal(scoutApi.CalendarMenuType().CalendarComponent());
      calMenuType.addProposal(scoutApi.CalendarMenuType().EmptySpace(), scoutApi.CalendarMenuType().CalendarComponent());
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
      var builder = "";
      if (simpleNames != null && simpleNames.length > 0) {
        builder = IntStream.range(1, simpleNames.length)
            .mapToObj(i -> ", " + m_typeSimpleName + JavaTypes.C_DOT + simpleNames[i])
            .collect(joining("", m_typeSimpleName + JavaTypes.C_DOT + simpleNames[0], ""));
      }
      m_menuTypeProposals.add(builder);
    }
  }
}
