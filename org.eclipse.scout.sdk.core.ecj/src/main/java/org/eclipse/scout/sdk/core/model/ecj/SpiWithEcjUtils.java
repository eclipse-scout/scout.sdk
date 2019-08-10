/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VoidTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.ecj.metavalue.MetaValueFactory;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.SdkException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static java.util.Collections.emptyList;

public final class SpiWithEcjUtils {

  private SpiWithEcjUtils() {
  }

  static TypeSpi bindingToInnerType(JavaEnvironmentWithEcj env, TypeBinding primaryTypeBinding, String innerTypes) {
    if (primaryTypeBinding == null) {
      return null;
    }

    TypeSpi result = bindingToType(env, primaryTypeBinding);

    // it is an inner type: step into
    StringTokenizer st = new StringTokenizer(innerTypes, "$", false);
    while (st.hasMoreTokens()) {
      String name = st.nextToken();

      TypeSpi innerType = null;
      for (TypeSpi t : result.getTypes()) {
        if (t.getElementName().equals(name)) {
          innerType = t;
          break;
        }
      }
      if (innerType == null) {
        return null;
      }
      result = innerType;
    }
    return result;
  }

  //public only for junit testing purposes
  public static TypeSpi bindingToType(JavaEnvironmentWithEcj env, TypeBinding b) {
    return bindingToType(env, b, null);
  }

  static TypeSpi bindingToType(JavaEnvironmentWithEcj env, TypeBinding b, BindingTypeWithEcj declaringType) {
    return bindingToType(env, b, declaringType, false);
  }

  static List<TypeParameterSpi> toTypeParameterSpi(TypeParameter[] typeParams, AbstractMemberWithEcj<?> method, JavaEnvironmentWithEcj env) {
    if (typeParams == null || typeParams.length < 1) {
      return emptyList();
    }
    List<TypeParameterSpi> result = new ArrayList<>(typeParams.length);
    for (int i = 0; i < typeParams.length; i++) {
      result.add(env.createDeclarationTypeParameter(method, typeParams[i], i));
    }
    return result;
  }

  static ISourceRange getJavaDocSource(ASTNode doc, TypeSpi declaringType, JavaEnvironmentWithEcj env) {
    if (doc == null) {
      return null;
    }
    CompilationUnitSpi cu = declaringType.getCompilationUnit();
    return env.getSource(cu, doc.sourceStart, doc.sourceEnd);
  }

  static MethodSpi findNewMethodIn(MethodSpi m) {
    AbstractJavaElementWithEcj<?> declaringType = (AbstractJavaElementWithEcj<?>) m.getDeclaringType();
    TypeSpi newType = (TypeSpi) declaringType.internalFindNewElement();
    if (newType != null) {
      String oldSig = m.wrap().identifier();
      for (MethodSpi newM : newType.getMethods()) {
        if (oldSig.equals(newM.wrap().identifier())) {
          return newM;
        }
      }
    }
    return null;
  }

  static TypeSpi bindingToType(JavaEnvironmentWithEcj env, TypeBinding b, BindingTypeWithEcj declaringType, boolean isWildCard) {
    if (b instanceof VoidTypeBinding) {
      return env.createVoidType();
    }
    if (b instanceof WildcardBinding) {
      WildcardBinding wb = (WildcardBinding) b;
      TypeBinding allBounds = wb.allBounds();
      if (allBounds == null) {
        // wildcard only binding: <?>
        return env.createWildcardOnlyType();
      }
      return bindingToType(env, allBounds, declaringType, true);
    }
    if (b instanceof ReferenceBinding) {
      // reference to complex type
      return env.createBindingType((ReferenceBinding) b, declaringType, isWildCard);
    }
    if (b instanceof BaseTypeBinding) {
      return env.createBindingBaseType((BaseTypeBinding) b);
    }
    if (b instanceof ArrayBinding) {
      return env.createBindingArrayType((ArrayBinding) b, isWildCard);
    }

    if (b == null) {
      throw new IllegalArgumentException("TypeBinding cannot be null");
    }
    throw new IllegalStateException("Unsupported binding type: " + b.getClass().getName());
  }

  @SuppressWarnings("squid:AssignmentInSubExpressionCheck")
  static int getTypeFlags(int modifiers, AllocationExpression allocation, boolean hasDeprecatedAnnotation) {
    int currentModifiers = modifiers;
    boolean isEnumInit = allocation != null && allocation.enumConstant != null;
    if (isEnumInit) {
      currentModifiers |= ClassFileConstants.AccEnum;
    }

    boolean deprecated = hasDeprecatedAnnotation || (currentModifiers & ClassFileConstants.AccDeprecated) != 0;
    currentModifiers &= ExtraCompilerModifiers.AccJustFlag;

    if (deprecated) {
      currentModifiers |= ClassFileConstants.AccDeprecated;
    }
    return currentModifiers & ~Flags.AccSuper;
  }

  static int getMethodFlags(int modifiers, boolean isVarargs, boolean isDeprecated) {
    int currentModifiers = modifiers & (ExtraCompilerModifiers.AccJustFlag | ClassFileConstants.AccDeprecated | ExtraCompilerModifiers.AccDefaultMethod);
    if (isVarargs) {
      currentModifiers |= ClassFileConstants.AccVarargs;
    }
    if (isDeprecated) {
      currentModifiers |= ClassFileConstants.AccDeprecated;
    }
    return currentModifiers;
  }

  /**
   * @param owner
   *          The owner whose declaring type should be returned.
   * @return the declaring type for this element. For {@link TypeSpi} this is the {@link TypeSpi} itself and NOT the
   *         enclosing type {@link TypeSpi#getDeclaringType()}
   */
  static TypeSpi declaringTypeOf(JavaElementSpi owner) {
    if (owner instanceof TypeSpi) {
      return (TypeSpi) owner;
    }
    if (owner instanceof MemberSpi) {
      return ((MemberSpi) owner).getDeclaringType();
    }
    if (owner instanceof TypeParameterSpi) {
      return declaringTypeOf(((TypeParameterSpi) owner).getDeclaringMember());
    }
    if (owner instanceof MethodParameterSpi) {
      return ((MethodParameterSpi) owner).getDeclaringMethod().getDeclaringType();
    }
    if (owner instanceof AnnotationSpi) {
      return declaringTypeOf(((AnnotationSpi) owner).getOwner());
    }
    if (owner instanceof AnnotationElementSpi) {
      return declaringTypeOf(((AnnotationElementSpi) owner).getDeclaringAnnotation());
    }
    throw new SdkException("Unknown owner type: {}", owner.getClass().getName());
  }

  static Scope memberScopeOf(JavaElementSpi owner) {
    if (owner instanceof TypeSpi) {
      return classScopeOf(owner);
    }
    if (owner instanceof MethodSpi) {
      return methodScopeOf(owner);
    }
    if (owner instanceof TypeParameterSpi) {
      return memberScopeOf(((TypeParameterSpi) owner).getDeclaringMember());
    }
    if (owner instanceof MethodParameterSpi) {
      return methodScopeOf(((MethodParameterSpi) owner).getDeclaringMethod());
    }
    if (owner instanceof AnnotationSpi) {
      return memberScopeOf(((AnnotationSpi) owner).getOwner());
    }
    throw new SdkException("Unknown owner type: {}", owner.getClass().getName());
  }

  static ClassScope classScopeOf(JavaElementSpi owner) {
    TypeSpi t = declaringTypeOf(owner);

    if (t instanceof DeclarationTypeWithEcj) {
      return ((DeclarationTypeWithEcj) t).getInternalTypeDeclaration().scope;
    }
    if (t instanceof AbstractTypeWithEcj) {
      Binding b = ((AbstractTypeWithEcj) t).getInternalBinding();
      if (b instanceof SourceTypeBinding) {
        return ((SourceTypeBinding) b).scope;
      }
    }
    return null;
  }

  static MethodScope methodScopeOf(JavaElementSpi owner) {
    if (owner instanceof BindingMethodWithEcj) {
      AbstractMethodDeclaration d = sourceMethodOf((BindingMethodWithEcj) owner);
      if (d != null) {
        return d.scope;
      }
    }
    else if (owner instanceof DeclarationMethodWithEcj) {
      return ((DeclarationMethodWithEcj) owner).getInternalMethodDeclaration().scope;
    }
    else if (owner instanceof TypeParameterSpi) {
      return methodScopeOf(((TypeParameterSpi) owner).getDeclaringMember());
    }
    else if (owner instanceof MethodParameterSpi) {
      return methodScopeOf(((MethodParameterSpi) owner).getDeclaringMethod());
    }
    else if (owner instanceof AnnotationSpi) {
      return methodScopeOf(((AnnotationSpi) owner).getOwner());
    }
    return null;
  }

  static AbstractMethodDeclaration sourceMethodOf(BindingMethodWithEcj b) {
    return sourceMethodOf(b.getInternalBinding());
  }

  static AbstractMethodDeclaration sourceMethodOf(MethodBinding b) {
    return nvl(b.original(), b).sourceMethod();
  }

  static <T> T nvl(T a, T b) {
    if (a == null) {
      return b;
    }
    return a;
  }

  static Annotation findAnnotationDeclaration(AnnotationSpi annotationSpi) {
    Annotation[] declaredAnnotations = null;
    AnnotatableSpi owner = annotationSpi.getOwner();
    if (owner instanceof BindingTypeWithEcj) {
      ReferenceBinding b = ((BindingTypeWithEcj) owner).getInternalBinding();
      b = nvl(b.actualType(), b);
      if (b instanceof SourceTypeBinding) {
        declaredAnnotations = ((SourceTypeBinding) b).scope.referenceContext.annotations;
      }
    }
    else if (owner instanceof BindingMethodWithEcj) {
      MethodBinding b = ((BindingMethodWithEcj) owner).getInternalBinding();
      AbstractMethodDeclaration sourceMethod = sourceMethodOf(b);
      if (sourceMethod != null) {
        declaredAnnotations = sourceMethod.annotations;
      }
    }
    else if (owner instanceof BindingFieldWithEcj) {
      FieldBinding b = ((BindingFieldWithEcj) owner).getInternalBinding();
      b = nvl(b.original(), b);
      if (b.sourceField() != null) {
        declaredAnnotations = b.sourceField().annotations;
      }
    }
    else if (owner instanceof BindingTypeParameterWithEcj) {
      TypeVariableBinding b = ((BindingTypeParameterWithEcj) owner).getInternalBinding();
      b = (TypeVariableBinding) nvl(b.original(), b);
      if (b.declaringElement instanceof SourceTypeBinding) {
        declaredAnnotations = ((SourceTypeBinding) b.declaringElement).scope.referenceContext.annotations;
      }
    }
    if (declaredAnnotations != null && annotationSpi instanceof BindingAnnotationWithEcj) {
      AnnotationBinding binding = ((BindingAnnotationWithEcj) annotationSpi).getInternalBinding();

      //fast visit
      for (Annotation decl : declaredAnnotations) {
        if (decl.getCompilerAnnotation() == binding) {
          return decl;
        }
      }

      //full visit
      FindAnnotationVisitor v = new FindAnnotationVisitor(binding);
      for (Annotation decl : declaredAnnotations) {
        decl.traverse(v, (BlockScope) null);
        if (v.getResult() != null) {
          break;
        }
      }
      return v.getResult();
    }
    return null;
  }

  static MemberValuePair findAnnotationValueDeclaration(BindingAnnotationElementWithEcj a) {
    Annotation annotationDeclaration = findAnnotationDeclaration(a.getDeclaringAnnotation());
    if (annotationDeclaration == null) {
      return null;
    }
    FindMemberValuePairVisitor v = new FindMemberValuePairVisitor(a.getInternalBinding());
    annotationDeclaration.traverse(v, (BlockScope) null);
    return v.getResult();
  }

  static List<BindingAnnotationWithEcj> createBindingAnnotations(JavaEnvironmentWithEcj env, AnnotatableSpi owner, AnnotationBinding[] annots) {
    if (annots == null || annots.length < 1) {
      return emptyList();
    }
    List<BindingAnnotationWithEcj> result = new ArrayList<>(annots.length);
    for (AnnotationBinding annot : annots) {
      result.add(env.createBindingAnnotation(owner, annot));
    }
    return result;
  }

  static List<DeclarationAnnotationWithEcj> createDeclarationAnnotations(JavaEnvironmentWithEcj env, AnnotatableSpi owner, Annotation[] annotations) {
    if (annotations == null || annotations.length < 1) {
      return emptyList();
    }
    List<DeclarationAnnotationWithEcj> result = new ArrayList<>(annotations.length);
    for (Annotation annotation : annotations) {
      result.add(env.createDeclarationAnnotation(owner, annotation));
    }
    return result;
  }

  /**
   * transform this {@link Expression} to a compiled binding value of type {@link Constant}, {@link TypeBinding},
   * {@link FieldBinding}, {@link AnnotationBinding}
   */
  @SuppressWarnings("pmd:NPathComplexity")
  static Object compileExpression(Expression expression, ClassScope scopeForTypeLookup) {
    if (expression == null) {
      return null;
    }
    if (expression instanceof NullLiteral) {
      return expression;
    }
    if (expression instanceof Literal) {
      if (expression.constant == null) {
        ((Literal) expression).computeConstant();
      }
      return expression.constant;
    }
    if (expression instanceof ArrayInitializer) {
      Expression[] array = ((ArrayInitializer) expression).expressions;
      if (array != null) {
        int n = array.length;
        Object[] values = new Object[n];
        for (int i = 0; i < n; i++) {
          values[i] = compileExpression(array[i], scopeForTypeLookup);
        }
        return values;
      }
      return DefaultProblem.EMPTY_VALUES;
    }
    if (expression instanceof UnaryExpression) {
      UnaryExpression ue = (UnaryExpression) expression;
      Expression inner = ue.expression;
      if (inner instanceof Literal) {
        int id = getTypeIdForLiteral((Literal) inner);
        if (id > 0) {
          Object candidate = compileExpression(inner, scopeForTypeLookup);
          if (candidate instanceof Constant) {
            return Constant.computeConstantOperation((Constant) candidate, id, ((expression.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT));
          }
        }
      }
    }
    else if (expression instanceof ClassLiteralAccess) {
      TypeBinding val = ((ClassLiteralAccess) expression).targetType;
      if (val == null) {
        TypeReference type = ((ClassLiteralAccess) expression).type;
        if (type != null) {
          if (type.resolvedType == null && scopeForTypeLookup != null) {
            type.resolveType(scopeForTypeLookup);
          }
          val = type.resolvedType;
        }
      }
      return val;
    }
    if (expression instanceof Annotation) {
      Annotation annotation = (Annotation) expression;
      AnnotationBinding compilerAnnotation = annotation.getCompilerAnnotation();
      if (compilerAnnotation == null) {
        annotation.resolveType(scopeForTypeLookup.referenceContext.staticInitializerScope);
      }
      return annotation.getCompilerAnnotation();
    }
    if (expression instanceof Reference) {
      FieldBinding fieldBinding = null;
      if (expression instanceof NameReference) {
        Binding binding = ((NameReference) expression).binding;
        if (binding != null && binding.kind() == Binding.FIELD) {
          fieldBinding = (FieldBinding) binding;
        }
        else if (expression instanceof QualifiedNameReference) {
          char[][] tokens = ((QualifiedNameReference) expression).tokens;
          TypeBinding baseType = scopeForTypeLookup.getType(tokens, tokens.length - 1);
          if (baseType instanceof ReferenceBinding) {
            ReferenceBinding ref = (ReferenceBinding) baseType;
            FieldBinding field = ref.getField(tokens[tokens.length - 1], true);
            if (field != null) {
              return field;
            }
          }
          String str = CharOperation.toString(tokens);
          return StringConstant.fromValue(str);
        }
      }
      else {
        fieldBinding = ((Reference) expression).fieldBinding();
      }
      if (fieldBinding != null) {
        return fieldBinding;
      }
    }
    return ElementValuePair.getValue(expression);
  }

  /**
   * transform a raw annotation value from {@link ElementValuePair#getValue(Expression)} or compiled expression value
   * from {@link #compileExpression(Expression, ClassScope)} to a {@link IMetaValue} that can be wrapped inside a
   * {@link AnnotationElementSpi}
   */
  @SuppressWarnings("pmd:NPathComplexity")
  static IMetaValue resolveCompiledValue(JavaEnvironmentWithEcj env, AnnotatableSpi owner, Object compiledValue) {
    if (compiledValue == null || Constant.NotAConstant.equals(compiledValue)) {
      return null;
    }
    if (compiledValue instanceof NullLiteral) {
      return MetaValueFactory.createNull();
    }

    if (compiledValue instanceof Constant) {
      // primitives and string
      return MetaValueFactory.createFromConstant((Constant) compiledValue);
    }
    if (compiledValue instanceof TypeBinding) {
      // type
      return MetaValueFactory.createFromType(bindingToType(env, (TypeBinding) compiledValue));
    }
    if (compiledValue instanceof FieldBinding) {
      // enum constants
      FieldBinding fb = (FieldBinding) compiledValue;
      TypeSpi type = bindingToType(env, fb.declaringClass);
      String name = new String(fb.name);
      for (FieldSpi f : type.getFields()) {
        if (f.getElementName().equals(name)) {
          return MetaValueFactory.createFromEnum(f);
        }
      }
      return MetaValueFactory.createUnknown("ENUM " + fb.declaringClass.debugName() + '#' + name);
    }
    if (compiledValue instanceof AnnotationBinding) {
      // annotation binding
      AnnotationBinding a = (AnnotationBinding) compiledValue;
      return MetaValueFactory.createFromAnnotation(env.createBindingAnnotation(owner, a));
    }
    if (compiledValue.getClass().isArray()) {
      // arrays
      int n = Array.getLength(compiledValue);
      IMetaValue[] metaArray = new IMetaValue[n];
      if (n > 0) {
        for (int i = 0; i < n; i++) {
          metaArray[i] = resolveCompiledValue(env, owner, Array.get(compiledValue, i));
        }
      }
      return MetaValueFactory.createArray(metaArray);
    }
    return MetaValueFactory.createUnknown(compiledValue);
  }

  static int getTypeIdForLiteral(Literal l) {
    if (l instanceof LongLiteral) {
      return TypeIds.T_long;
    }
    if (l instanceof IntLiteral) {
      return TypeIds.T_int;
    }
    if (l instanceof FloatLiteral) {
      return TypeIds.T_float;
    }
    if (l instanceof DoubleLiteral) {
      return TypeIds.T_double;
    }
    if (l instanceof CharLiteral) {
      return TypeIds.T_char;
    }
    return -1;
  }

  static boolean hasDeprecatedAnnotation(AnnotationBinding[] annotations) {
    if (annotations == null) {
      return false;
    }
    for (AnnotationBinding annotation : annotations) {
      if (annotation != null && annotation.getAnnotationType() != null && CharOperation.equals(annotation.getAnnotationType().sourceName, TypeConstants.JAVA_LANG_DEPRECATED[2])) {
        return true;
      }
    }
    return false;
  }

  static boolean hasDeprecatedAnnotation(Annotation[] annotations) {
    if (annotations == null) {
      return false;
    }
    for (Annotation annotation : annotations) {
      if (CharOperation.equals(annotation.type.getLastToken(), TypeConstants.JAVA_LANG_DEPRECATED[2])) {
        return true;
      }
    }
    return false;
  }

  private static final class FindAnnotationVisitor extends ASTVisitor {
    private final AnnotationBinding m_binding;
    private Annotation m_result;

    private FindAnnotationVisitor(AnnotationBinding binding) {
      m_binding = binding;
    }

    public Annotation getResult() {
      return m_result;
    }

    @Override
    public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
      return internalVisit(annotation);
    }

    @Override
    public boolean visit(NormalAnnotation annotation, BlockScope scope) {
      return internalVisit(annotation);
    }

    @Override
    public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
      return internalVisit(annotation);
    }

    private boolean internalVisit(Annotation annotation) {
      if (annotation.getCompilerAnnotation() == m_binding) {
        m_result = annotation;
      }
      return m_result == null;
    }
  }

  private static final class FindMemberValuePairVisitor extends ASTVisitor {
    private final ElementValuePair m_binding;
    private MemberValuePair m_result;

    private FindMemberValuePairVisitor(ElementValuePair binding) {
      m_binding = binding;
    }

    public MemberValuePair getResult() {
      return m_result;
    }

    @Override
    public boolean visit(MemberValuePair pair, BlockScope scope0) {
      if (pair.compilerElementPair == m_binding) {
        m_result = pair;
      }
      return m_result == null;
    }
  }

}
