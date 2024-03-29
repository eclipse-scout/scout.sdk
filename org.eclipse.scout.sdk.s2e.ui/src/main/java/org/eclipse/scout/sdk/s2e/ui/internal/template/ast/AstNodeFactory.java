/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.java.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.uniqueid.UniqueIds;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

/**
 * <h3>{@link AstNodeFactory}</h3>
 *
 * @since 5.2.0
 */
public class AstNodeFactory {

  public static final String SUPER_TYPE_GROUP = "SUPER_TYPE";
  public static final String TYPE_NAME_GROUP = "TYPE_NAME";
  public static final String NLS_KEY_GROUP = "NLS_KEY";
  public static final String ID_DATA_TYPE_GROUP = "ID_DATA_TYPE";
  public static final String ID_VALUE_GROUP = "ID_VALUE";
  public static final String CODE_DATA_TYPE_GROUP = "CODE_DATA_TYPE";
  public static final String MIN_GROUP = "MIN";
  public static final String MAX_GROUP = "MAX";
  public static final String CALENDAR_ITEM_PROVIDER_NAME_GROUP = "CALENDAR_ITEM_PROVIDER_NAME";
  public static final String GRID_H_GROUP = "GRID_H";
  public static final String LABEL_VISIBLE_GROUP = "LABEL_VISIBLE";
  public static final String WIDTH_VALUE_GROUP = "WIDTH_VALUE";
  public static final String MAX_LEN_GROUP = "MAX_LEN";
  public static final String VALUE_TYPE_GROUP = "VALUE_TYPE";
  public static final String KEY_STROKE_GROUP = "KEY_STROKE";
  public static final String RADIO_VALUE_TYPE_GROUP = "RADIO_VALUE_TYPE";
  public static final String RADIO_VALUE_GROUP = "RADIO_VALUE";
  public static final String AUTO_CHECK_GROUP = "AUTO_CHECK";
  public static final String MENU_TYPE_GROUP = "MENU_TYPE";
  public static final String EXTENDS_TYPE_GROUP = "EXTENDS_TYPE_GROUP";
  public static final String AUTO_FIT_GROUP = "AUTO_FIT";

  private final AST m_ast;
  private final TypeDeclaration m_declaringType;
  private final ITypeBinding m_declaringTypeBinding;
  private final ASTRewrite m_rewrite;
  private final CompilationUnit m_root;
  private final ImportRewrite m_importRewrite;
  private final ListRewrite m_importsRewrite;
  private final ImportRewriteContext m_context;
  private final EclipseEnvironment m_javaEnvProvider;
  private final CodeGenerationSettings m_settings;
  private final ICompilationUnit m_icu;
  private final IJavaProject m_javaProject;
  private final ILinkedPositionHolder m_linkedPosHolder;
  private final IScoutApi m_scoutApi;

  public AstNodeFactory(TypeDeclaration type, ICompilationUnit icu, EclipseEnvironment envProvider, IScoutApi api) {
    this(type, icu, envProvider, api, Ensure.notNull(type).resolveBinding());
  }

  public AstNodeFactory(TypeDeclaration type, ICompilationUnit icu, EclipseEnvironment envProvider, IScoutApi api, ITypeBinding declaringTypeBinding) {
    this(type, icu, envProvider, api, declaringTypeBinding, null);
  }

  public AstNodeFactory(TypeDeclaration type, ICompilationUnit icu, EclipseEnvironment envProvider, IScoutApi api, ITypeBinding declaringTypeBinding, ILinkedPositionHolder linkHolder) {
    m_declaringType = Ensure.notNull(type);
    m_declaringTypeBinding = Ensure.notNull(declaringTypeBinding);
    m_javaEnvProvider = Ensure.notNull(envProvider);
    m_icu = Ensure.notNull(icu);
    m_scoutApi = Ensure.notNull(api);

    m_ast = m_declaringType.getAST();
    m_rewrite = ASTRewrite.create(m_ast);
    m_root = (CompilationUnit) type.getRoot();

    m_importRewrite = CodeStyleConfiguration.createImportRewrite(m_root, true);
    if (m_root.getAST().hasResolvedBindings()) {
      m_importRewrite.setUseContextToFilterImplicitImports(true);
    }

    m_importsRewrite = m_rewrite.getListRewrite(m_root, CompilationUnit.IMPORTS_PROPERTY);
    m_context = new ContextSensitiveImportRewriteContext(m_root, m_importRewrite);
    m_javaProject = m_icu.getJavaProject();
    m_settings = JavaPreferencesSettings.getCodeGenerationSettings(m_javaProject);
    m_linkedPosHolder = linkHolder;
  }

  public AstTypeBuilder<?> newType(String name) {
    return new AstTypeBuilder<>(this).withName(Ensure.notNull(name));
  }

  public AstMethodBuilder<?> newMethod(String name) {
    return new AstMethodBuilder<>(this).withName(Ensure.notNull(name));
  }

  public AstInnerTypeGetterBuilder newInnerTypeGetter() {
    return new AstInnerTypeGetterBuilder(this);
  }

  public AstCodeBuilder newCode(String name) {
    return new AstCodeBuilder(this)
        .withClassId(true)
        .withName(name)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.STATIC_KEYWORD)
        .withNlsMethod(getScoutApi().AbstractCode().getConfiguredTextMethodName())
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().ICode().fqn());
  }

  public AstButtonBuilder newButton(String name) {
    return new AstButtonBuilder(this)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withClassId(true)
        .withName(name);
  }

  public AstBigDecimalFieldBuilder newBigDecimalField(String name) {
    return new AstBigDecimalFieldBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstImageFieldBuilder newImageField(String name) {
    return new AstImageFieldBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstCalendarFieldBuilder newCalendarField(String name) {
    return new AstCalendarFieldBuilder(this)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withClassId(true)
        .withName(name);
  }

  public AstColumnBuilder newColumn(String name) {
    return new AstColumnBuilder(this)
        .withClassId(true)
        .withName(name)
        .withNlsMethod(getScoutApi().AbstractColumn().getConfiguredHeaderTextMethodName())
        .withCalculatedOrder(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IColumn().fqn());
  }

  public AstDateFieldBuilder newDateField(String name) {
    return new AstDateFieldBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstLabelFieldBuilder newLabelField(String name) {
    return new AstLabelFieldBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstLongFieldBuilder newLongField(String name) {
    return new AstLongFieldBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstStringFieldBuilder newStringField(String name) {
    return new AstStringFieldBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstValueFieldBuilder<?> newValueField(String name) {
    return new AstValueFieldBuilder<>(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstFormHandlerBuilder newFormHandler(String name) {
    return new AstFormHandlerBuilder(this)
        .withName(name)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(false)
        .withOrderDefinitionType(null)
        .withCalculatedOrder(false);
  }

  public AstKeyStrokeBuilder newKeyStroke(String name) {
    return new AstKeyStrokeBuilder(this)
        .withClassId(true)
        .withName(name)
        .withCalculatedOrder(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IKeyStroke().fqn());
  }

  public AstListBoxBuilder newListBox(String name) {
    return new AstListBoxBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstRadioButtonBuilder newRadioButton(String name) {
    return new AstRadioButtonBuilder(this)
        .withClassId(true)
        .withName(name)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withOrder(true);
  }

  public AstSequenceBoxBuilder newSequenceBox(String name) {
    return new AstSequenceBoxBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstTreeFieldBuilder newTreeField(String name) {
    return new AstTreeFieldBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstTableFieldBuilder newTableField(String name) {
    return new AstTableFieldBuilder(this)
        .withClassId(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withOrderDefinitionType(getScoutApi().IFormField().fqn())
        .withName(name);
  }

  public AstMenuBuilder newMenu(String name) {
    return new AstMenuBuilder(this)
        .withClassId(true)
        .withName(name)
        .withCalculatedOrder(true)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(true)
        .withNlsMethod(getScoutApi().AbstractAction().getConfiguredTextMethodName())
        .withOrderDefinitionType(getScoutApi().IMenu().fqn());
  }

  public AstTypeBuilder<?> newExtension(String name) {
    return new AstExtensionBuilder(this)
        .withName(name)
        .withNlsMethod(null)
        .withCalculatedOrder(false)
        .withModifiers(ModifierKeyword.PUBLIC_KEYWORD)
        .withOrder(false)
        .withOrderDefinitionType(null);
  }

  public Expression newDefaultValueExpression(String fqn, boolean useUniqueId) {
    var unboxed = JavaTypes.unboxToPrimitive(fqn);
    var isPrimitive = JavaTypes.isPrimitive(unboxed);
    if (useUniqueId) {
      var uniqueId = UniqueIds.next(fqn);
      if (Strings.hasText(uniqueId)) {
        if (isPrimitive) {
          return getAst().newNumberLiteral(uniqueId);
        }
        var literal = getAst().newStringLiteral();
        literal.setEscapedValue(uniqueId);
        return literal;
      }
    }

    if (!isPrimitive) {
      return getAst().newNullLiteral();
    }

    if (JavaTypes._void.equals(unboxed)) {
      return null;
    }

    if (JavaTypes._boolean.equals(unboxed)) {
      return getAst().newBooleanLiteral(false);
    }

    var defaultValue = JavaTypes.defaultValueOf(unboxed);
    return getAst().newNumberLiteral(defaultValue);
  }

  public Type newTypeReference(String fqn) {
    var type = getImportRewrite().addImport(fqn, getContext());
    return getAst().newSimpleType(getAst().newSimpleName(type));
  }

  @SuppressWarnings("unchecked")
  public NormalAnnotation newFormDataIgnoreAnnotation() {
    var formDataRef = getImportRewrite().addImport(getScoutApi().FormData().fqn(), getContext());
    var formData = getAst().newNormalAnnotation();
    formData.setTypeName(getAst().newName(formDataRef));

    var sdkCommand = getAst().newMemberValuePair();
    sdkCommand.setName(getAst().newSimpleName(getScoutApi().FormData().sdkCommandElementName()));
    sdkCommand.setValue(getAst().newQualifiedName(getAst().newQualifiedName(getAst().newSimpleName(formDataRef),
        getAst().newSimpleName(SdkCommand.class.getSimpleName())),
        getAst().newSimpleName(SdkCommand.IGNORE.toString())));

    formData.values().add(sdkCommand);
    return formData;
  }

  public SingleMemberAnnotation newClassIdAnnotation(String fqn) {
    var classIdFqn = getScoutApi().ClassId().fqn();
    var classIdRef = getImportRewrite().addImport(classIdFqn, getContext());

    var classIdAnnotation = getAst().newSingleMemberAnnotation();
    classIdAnnotation.setTypeName(getAst().newName(classIdRef));

    // value
    var newId = ClassIds.next(fqn);
    if (Strings.isBlank(newId)) {
      newId = "UNDEFINED";
    }
    var id = getAst().newStringLiteral();
    id.setLiteralValue(newId);
    classIdAnnotation.setValue(id);

    return classIdAnnotation;
  }

  public AstMethodBuilder<?> newGetConfiguredGridH(int gridHValue) {
    return newGetConfigured(gridHValue, getScoutApi().AbstractFormField().getConfiguredGridHMethodName(), GRID_H_GROUP);
  }

  public AstMethodBuilder<?> newGetConfiguredWidth(int width) {
    return newGetConfigured(width, getScoutApi().AbstractColumn().getConfiguredWidthMethodName(), WIDTH_VALUE_GROUP);
  }

  @SuppressWarnings("unchecked")
  protected AstMethodBuilder<?> newGetConfigured(int value, String name, String group) {
    var literal = getAst().newNumberLiteral(Integer.toString(value));
    var returnStatement = getAst().newReturnStatement();
    returnStatement.setExpression(literal);

    var body = getAst().newBlock();
    body.statements().add(returnStatement);

    var links = getLinkedPositionHolder();
    if (links != null) {
      links.addLinkedPosition(getRewrite().track(literal), true, group);
    }

    return newMethod(name)
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(getAst().newPrimitiveType(PrimitiveType.INT))
        .withBody(body);
  }

  /**
   * Creates a AbstractAction.combineKeyStrokes() method with the given elements as parameters.
   * <p>
   * <b>Examples:</b>
   * <ul>
   * <li>for input ["CONTROL", "C"]: {@code AbstractAction.combineKeyStrokes(IKeyStroke.CONTROL, "C")}</li>
   * <li>for input ["ALT", "F6"]: {@code AbstractAction.combineKeyStrokes(IKeyStroke.ALT, IKeyStroke.F6)}</li>
   * </ul>
   *
   * @param elementsToCombine
   *          Elements to pass to the combineKeyStrokes method. Elements with length=1 are considered to be string
   *          literals, all other elements are inserted as references to IKeyStroke
   * @return The created {@link MethodInvocation}
   */
  @SuppressWarnings("unchecked")
  public MethodInvocation newCombineKeyStrokes(String... elementsToCombine) {
    var ast = getAst();
    var iKeyStroke = getImportRewrite().addImport(getScoutApi().IKeyStroke().fqn());
    var combineKeyStrokes = ast.newMethodInvocation();
    combineKeyStrokes.setName(ast.newSimpleName(getScoutApi().AbstractAction().combineKeyStrokesMethodName()));
    var keyStrokeRef = newTypeReference(getScoutApi().AbstractAction().fqn());
    combineKeyStrokes.setExpression(ast.newSimpleName(keyStrokeRef.toString()));
    List<Expression> arguments = combineKeyStrokes.arguments();

    for (var element : elementsToCombine) {
      if (element.length() < 2) {
        // String literal value
        var literal = ast.newStringLiteral();
        literal.setLiteralValue(element);
        arguments.add(literal);
      }
      else {
        // reference to IKeyStroke
        arguments.add(ast.newQualifiedName(ast.newName(iKeyStroke), ast.newSimpleName(element)));
      }
    }
    return combineKeyStrokes;
  }

  @SuppressWarnings("unchecked")
  public AstMethodBuilder<?> newNlsMethod(String methodName) {
    var ast = getAst();
    var get = ast.newMethodInvocation();
    var textsApi = getScoutApi().TEXTS();
    get.setName(ast.newSimpleName(textsApi.getMethodName()));
    var textsRef = getImportRewrite().addImport(textsApi.fqn(), getContext());
    get.setExpression(ast.newName(textsRef));
    var nlsKeyString = ast.newStringLiteral();
    nlsKeyString.setLiteralValue("MyNlsKey");
    get.arguments().add(nlsKeyString);

    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(get);
    var body = ast.newBlock();
    body.statements().add(returnStatement);

    var links = getLinkedPositionHolder();
    if (links != null) {
      ITrackedNodePosition nlsKeyLiteralPos = new WrappedTrackedNodePosition(getRewrite().track(nlsKeyString), 1, -2);
      links.addLinkedPosition(nlsKeyLiteralPos, true, NLS_KEY_GROUP);
    }

    return newMethod(methodName)
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(newTypeReference(String.class.getName()))
        .withBody(body);
  }

  public AstMethodBuilder<?> newExecMethod(String name) {
    return newMethod(name)
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(getAst().newPrimitiveType(PrimitiveType.VOID));
  }

  @SuppressWarnings("unchecked")
  public AstMethodBuilder<?> newGetConfiguredLabelVisible() {
    var ast = getAst();
    var literal = ast.newBooleanLiteral(false);
    var returnStatement = ast.newReturnStatement();
    returnStatement.setExpression(literal);

    var body = ast.newBlock();
    body.statements().add(returnStatement);

    var links = getLinkedPositionHolder();
    if (links != null) {
      links.addLinkedPosition(getRewrite().track(literal), true, LABEL_VISIBLE_GROUP);
      links.addLinkedPositionProposalsBoolean(LABEL_VISIBLE_GROUP);
    }

    return newMethod(getScoutApi().AbstractFormField().getConfiguredLabelVisibleMethodName())
        .withModifiers(ModifierKeyword.PROTECTED_KEYWORD)
        .withOverride(true)
        .withReturnType(ast.newPrimitiveType(PrimitiveType.BOOLEAN))
        .withBody(body);
  }

  @SuppressWarnings("unchecked")
  public FieldDeclaration newSerialVersionUid() {
    var ast = getAst();
    var fragment = ast.newVariableDeclarationFragment();
    fragment.setName(ast.newSimpleName(FieldGenerator.SERIAL_VERSION_UID));
    fragment.setInitializer(ast.newNumberLiteral("1L"));

    var declaration = ast.newFieldDeclaration(fragment);
    declaration.setType(ast.newPrimitiveType(PrimitiveType.LONG));
    declaration.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
    declaration.modifiers().add(ast.newModifier(ModifierKeyword.STATIC_KEYWORD));
    declaration.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
    return declaration;
  }

  public MarkerAnnotation newOverrideAnnotation() {
    var overrideRef = getImportRewrite().addImport(Override.class.getName(), getContext());
    var marker = getAst().newMarkerAnnotation();
    marker.setTypeName(getAst().newName(overrideRef));
    return marker;
  }

  public ITypeBinding resolveTypeBinding(String fqn) {

    var wellKnownType = getAst().resolveWellKnownType(fqn);
    if (wellKnownType != null) {
      return wellKnownType;
    }

    try {
      var resolver = AstUtils.getBindingResolver(getAst());
      var unitScope = AstUtils.getCompilationUnitScope(resolver);
      var reference = unitScope.environment.getResolvedType(CharOperation.splitOn(JavaTypes.C_DOT, fqn.toCharArray()), unitScope);

      var getTypeBinding = resolver.getClass().getDeclaredMethod("getTypeBinding", TypeBinding.class);
      getTypeBinding.setAccessible(true);
      return (ITypeBinding) getTypeBinding.invoke(resolver, reference);
    }
    catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException t) {
      throw new SdkException(t);
    }
  }

  public ImportRewriteContext getContext() {
    return m_context;
  }

  public ILinkedPositionHolder getLinkedPositionHolder() {
    return m_linkedPosHolder;
  }

  public AST getAst() {
    return m_ast;
  }

  public TypeDeclaration getType() {
    return m_declaringType;
  }

  public CompilationUnit getRoot() {
    return m_root;
  }

  public ICompilationUnit getIcu() {
    return m_icu;
  }

  public ImportRewrite getImportRewrite() {
    return m_importRewrite;
  }

  public ASTRewrite getRewrite() {
    return m_rewrite;
  }

  public ListRewrite getImportsRewriteList() {
    return m_importsRewrite;
  }

  public ITypeBinding getDeclaringTypeBinding() {
    return m_declaringTypeBinding;
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public EclipseEnvironment getScoutElementProvider() {
    return m_javaEnvProvider;
  }

  public IScoutApi getScoutApi() {
    return m_scoutApi;
  }

  protected boolean isCreateOverrideAnnotationSetting() {
    return m_settings != null && m_settings.overrideAnnotation;
  }

  protected boolean isCreateCommentsSetting() {
    return m_settings != null && m_settings.createComments;
  }
}
