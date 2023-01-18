/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
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
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
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
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VoidTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.ecj.metavalue.MetaValueFactory;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.java.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractSpiElement;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.java.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.java.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.java.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.java.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.java.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

public final class SpiWithEcjUtils {

  private static final String DEPRECATED_ANNOTATION_FQN = Deprecated.class.getName();

  private SpiWithEcjUtils() {
  }

  static List<TypeParameterSpi> createTypeParameters(AbstractMemberWithEcj<?> owner, TypeVariableBinding[] typeParams) {
    if (typeParams == null || typeParams.length < 1) {
      return emptyList();
    }

    List<TypeParameterSpi> result = new ArrayList<>(typeParams.length);
    var index = 0;
    var env = (JavaEnvironmentWithEcj) owner.getJavaEnvironment();
    for (var param : typeParams) {
      result.add(env.createBindingTypeParameter(owner, param, index));
      index++;
    }
    return result;
  }

  static List<TypeSpi> bindingsToTypes(JavaEnvironmentWithEcj env, TypeBinding[] bindings, Supplier<TypeBinding[]> newElementLookupStrategy) {
    return bindingsToTypes(env, bindings, null, newElementLookupStrategy);
  }

  static List<TypeSpi> bindingsToTypes(JavaEnvironmentWithEcj env, TypeBinding[] bindings, TypeSpi declaringType, Supplier<TypeBinding[]> newElementLookupStrategy) {
    if (bindings == null || bindings.length < 1) {
      return emptyList();
    }

    return Arrays.stream(bindings)
        .map(binding -> bindingToType(env, binding, declaringType, () -> findBindingWithKey(newElementLookupStrategy.get(), binding.signableName())))
        .filter(Objects::nonNull)
        .collect(toList());
  }

  static TypeBinding findBindingWithKey(TypeBinding[] newBindings, char[] key) {
    if (newBindings == null) {
      return null;
    }
    return Arrays.stream(newBindings)
        .filter(b -> Strings.equals(b.signableName(), key))
        .findAny()
        .orElse(null);
  }

  //public only for junit testing purposes
  static TypeSpi bindingToType(JavaEnvironmentWithEcj env, TypeBinding b, Supplier<? extends TypeBinding> newElementLookupStrategy) {
    return bindingToType(env, b, null, newElementLookupStrategy);
  }

  static TypeSpi bindingToType(JavaEnvironmentWithEcj env, TypeBinding b, TypeSpi declaringType, Supplier<? extends TypeBinding> newElementLookupStrategy) {
    return bindingToType(env, b, declaringType, false, newElementLookupStrategy);
  }

  static List<TypeParameterSpi> toTypeParameterSpi(TypeParameter[] typeParams, AbstractMemberWithEcj<?> method, JavaEnvironmentWithEcj env) {
    if (typeParams == null || typeParams.length < 1) {
      return emptyList();
    }
    return IntStream.range(0, typeParams.length)
        .mapToObj(i -> env.createDeclarationTypeParameter(method, typeParams[i], i))
        .collect(toList());
  }

  static ISourceRange createSourceRange(ASTNode node, CompilationUnitSpi cu, JavaEnvironmentWithEcj env) {
    if (node == null) {
      return null;
    }
    if (cu == null) {
      return null;
    }
    return env.getSource(cu, node.sourceStart(), node.sourceEnd());
  }

  static AnnotationSpi findNewAnnotationIn(AnnotatableSpi owner, String typeName) {
    var newOwner = (AnnotatableSpi) ((AbstractSpiElement<?>) owner).internalFindNewElement();
    if (newOwner == null) {
      return null;
    }
    // currently only annotations directly on the owner are supported. nested annotations cannot be resolved at the moment
    return newOwner.getAnnotations().stream()
        .filter(a -> typeName.equals(a.getElementName()))
        .findAny()
        .orElse(null);
  }

  static AnnotationElementSpi findNewAnnotationElementIn(AnnotationSpi annotation, String name) {
    var newDeclaringAnnotation = (AnnotationSpi) ((AbstractSpiElement<?>) annotation).internalFindNewElement();
    if (newDeclaringAnnotation == null) {
      return null;
    }
    return newDeclaringAnnotation.getValues().get(name);
  }

  static MethodSpi findNewMethodIn(TypeSpi declaringType, String methodId) {
    var newType = (TypeSpi) ((AbstractSpiElement<?>) declaringType).internalFindNewElement();
    if (newType == null) {
      return null;
    }

    return newType.getMethods().stream()
        .filter(newM -> methodId.equals(newM.getMethodId()))
        .findFirst()
        .orElse(null);
  }

  static String qualifiedNameOf(char[] pck, char[] sourceName) {
    var pckLen = pck.length;
    var hasPackage = pckLen > 0;
    var fqnLength = pckLen + sourceName.length;
    if (hasPackage) {
      fqnLength++;
    }
    var fqn = new char[fqnLength];
    var nameInsertPos = 0;

    // package name
    if (hasPackage) {
      System.arraycopy(pck, 0, fqn, 0, pckLen);
      fqn[pckLen] = JavaTypes.C_DOT;
      nameInsertPos = pckLen + 1;
    }

    // class names (e.g. MyClass.Inner.Inner2)
    System.arraycopy(sourceName, 0, fqn, nameInsertPos, sourceName.length);

    // replace . to $ for nested classes
    for (var i = nameInsertPos; i < fqn.length; i++) {
      if (fqn[i] == JavaTypes.C_DOT) {
        fqn[i] = JavaTypes.C_DOLLAR;
      }
    }
    return new String(fqn);
  }

  static TypeBinding resolveTypeOfArgument(AbstractVariableDeclaration argument, BlockScope scope, AbstractJavaEnvironment env) {
    var type = argument.type;
    var result = type.resolvedType;
    if (result != null) {
      return result;
    }

    synchronized (env.lock()) {
      type.resolveType(scope);
    }
    return type.resolvedType;
  }

  static TypeSpi bindingToType(JavaEnvironmentWithEcj env, TypeBinding b, TypeSpi declaringType, boolean isWildCard, Supplier<? extends TypeBinding> newElementLookupStrategy) {
    if (b == null) {
      return null;
    }
    if (b instanceof VoidTypeBinding) {
      return env.createVoidType();
    }
    if (b instanceof WildcardBinding wb) {
      var allBounds = wb.allBounds();
      if (allBounds == null) {
        // wildcard only binding: <?>
        return env.createWildcardOnlyType();
      }
      return bindingToType(env, allBounds, declaringType, true, newElementLookupStrategy);
    }
    if (b instanceof ReferenceBinding) {
      // reference to complex type
      return env.createBindingType((ReferenceBinding) b, declaringType, isWildCard, () -> (ReferenceBinding) newElementLookupStrategy.get());
    }
    if (b instanceof BaseTypeBinding) {
      return env.createBindingBaseType((BaseTypeBinding) b);
    }
    if (b instanceof ArrayBinding) {
      return env.createBindingArrayType((ArrayBinding) b, isWildCard, () -> (ArrayBinding) newElementLookupStrategy.get());
    }
    throw new IllegalStateException("Unsupported binding type: " + b.getClass().getName());
  }

  @SuppressWarnings("squid:AssignmentInSubExpressionCheck")
  static int getTypeFlags(int modifiers, AllocationExpression allocation, boolean hasDeprecatedAnnotation) {
    var currentModifiers = modifiers;
    var isEnumInit = allocation != null && allocation.enumConstant != null;
    if (isEnumInit) {
      currentModifiers |= ClassFileConstants.AccEnum;
    }

    var deprecated = hasDeprecatedAnnotation || (currentModifiers & ClassFileConstants.AccDeprecated) != 0;
    currentModifiers &= ExtraCompilerModifiers.AccJustFlag;

    if (deprecated) {
      currentModifiers |= ClassFileConstants.AccDeprecated;
    }
    return currentModifiers & ~Flags.AccSuper;
  }

  static int getMethodFlags(int modifiers, boolean isVarargs, boolean isDeprecated) {
    var currentModifiers = modifiers & (ExtraCompilerModifiers.AccJustFlag | ClassFileConstants.AccDeprecated | ExtraCompilerModifiers.AccDefaultMethod);
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
    var t = declaringTypeOf(owner);

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
      var d = sourceMethodOf((BindingMethodWithEcj) owner);
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
    var owner = annotationSpi.getOwner();
    if (owner instanceof AbstractTypeWithEcj) {
      var b = ((AbstractTypeWithEcj) owner).getInternalBinding();
      b = nvl(b.actualType(), b);
      if (b instanceof SourceTypeBinding) {
        declaredAnnotations = ((SourceTypeBinding) b).scope.referenceContext.annotations;
      }
    }
    else if (owner instanceof BindingMethodWithEcj) {
      var b = ((BindingMethodWithEcj) owner).getInternalBinding();
      var sourceMethod = sourceMethodOf(b);
      if (sourceMethod != null) {
        declaredAnnotations = sourceMethod.annotations;
      }
    }
    else if (owner instanceof BindingFieldWithEcj) {
      var b = ((BindingFieldWithEcj) owner).getInternalBinding();
      b = nvl(b.original(), b);
      if (b.sourceField() != null) {
        declaredAnnotations = b.sourceField().annotations;
      }
    }
    else if (owner instanceof BindingTypeParameterWithEcj) {
      var b = ((BindingTypeParameterWithEcj) owner).getInternalBinding();
      b = (TypeVariableBinding) nvl(b.original(), b);
      if (b.declaringElement instanceof SourceTypeBinding) {
        declaredAnnotations = ((SourceTypeBinding) b.declaringElement).scope.referenceContext.annotations;
      }
    }
    if (declaredAnnotations != null && annotationSpi instanceof BindingAnnotationWithEcj) {
      var binding = ((BindingAnnotationWithEcj) annotationSpi).getInternalBinding();

      //fast visit
      for (var decl : declaredAnnotations) {
        if (decl.getCompilerAnnotation() == binding) {
          return decl;
        }
      }

      //full visit
      var v = new FindAnnotationVisitor(binding);
      for (var decl : declaredAnnotations) {
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
    var declaringAnnotation = a.getDeclaringAnnotation();
    Annotation annotationDeclaration;
    if (declaringAnnotation instanceof DeclarationAnnotationWithEcj) {
      annotationDeclaration = ((DeclarationAnnotationWithEcj) declaringAnnotation).annotationDeclaration();
    }
    else {
      annotationDeclaration = ((BindingAnnotationWithEcj) declaringAnnotation).annotationDeclaration();
    }
    if (annotationDeclaration == null) {
      return null;
    }
    var v = new FindMemberValuePairVisitor(a.getInternalBinding());
    annotationDeclaration.traverse(v, (BlockScope) null);
    return v.getResult();
  }

  static List<BindingAnnotationWithEcj> createBindingAnnotations(AnnotatableSpi owner, Binding binding) {
    var lock = ((AbstractJavaEnvironment) owner.getJavaEnvironment()).lock();

    AnnotationBinding[] annotations;
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (lock) {
      annotations = binding.getAnnotations();
    }
    return createBindingAnnotations(owner, annotations);
  }

  static List<BindingAnnotationWithEcj> createBindingAnnotations(AnnotatableSpi owner, AnnotationBinding[] annotationBindings) {
    if (annotationBindings == null || annotationBindings.length < 1) {
      return emptyList();
    }
    var env = (JavaEnvironmentWithEcj) owner.getJavaEnvironment();
    return Arrays.stream(annotationBindings)
        .filter(Objects::nonNull)
        .map(annotation -> env.createBindingAnnotation(owner, annotation))
        .collect(toList());
  }

  static List<DeclarationAnnotationWithEcj> createDeclarationAnnotations(JavaEnvironmentWithEcj env, AnnotatableSpi owner, Annotation[] annotations) {
    if (annotations == null || annotations.length < 1) {
      return emptyList();
    }
    return Arrays.stream(annotations)
        .map(annotation -> env.createDeclarationAnnotation(owner, annotation))
        .collect(toList());
  }

  /**
   * transform this {@link Expression} to a compiled binding value of type {@link Constant}, {@link TypeBinding},
   * {@link FieldBinding}, {@link AnnotationBinding}
   */
  @SuppressWarnings("pmd:NPathComplexity")
  static Object compileExpression(Expression expression, ClassScope scopeForTypeLookup, JavaEnvironmentWithEcj env) {
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
      var array = ((ArrayInitializer) expression).expressions;
      if (array != null) {
        return Arrays.stream(array)
            .map(item -> compileExpression(item, scopeForTypeLookup, env))
            .toArray();
      }
      return DefaultProblem.EMPTY_VALUES;
    }
    if (expression instanceof UnaryExpression ue) {
      var inner = ue.expression;
      if (inner instanceof Literal) {
        var id = getTypeIdForLiteral((Literal) inner);
        if (id > 0) {
          var candidate = compileExpression(inner, scopeForTypeLookup, env);
          if (candidate instanceof Constant) {
            return Constant.computeConstantOperation((Constant) candidate, id, ((expression.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT));
          }
        }
      }
    }
    else if (expression instanceof ClassLiteralAccess) {
      var val = ((ClassLiteralAccess) expression).targetType;
      if (val == null) {
        var type = ((ClassLiteralAccess) expression).type;
        if (type != null) {
          if (type.resolvedType == null && scopeForTypeLookup != null) {
            synchronized (env.lock()) {
              type.resolveType(scopeForTypeLookup);
            }
          }
          val = type.resolvedType;
        }
      }
      return val;
    }
    if (expression instanceof Annotation annotation) {
      var compilerAnnotation = annotation.getCompilerAnnotation();
      if (compilerAnnotation == null) {
        synchronized (env.lock()) {
          annotation.resolveType(scopeForTypeLookup.referenceContext.staticInitializerScope);
        }
      }
      return annotation.getCompilerAnnotation();
    }
    if (expression instanceof Reference) {
      FieldBinding fieldBinding = null;
      if (expression instanceof NameReference) {
        var binding = ((NameReference) expression).binding;
        if (binding != null && binding.kind() == Binding.FIELD) {
          fieldBinding = (FieldBinding) binding;
        }
        else if (expression instanceof QualifiedNameReference) {
          var tokens = ((QualifiedNameReference) expression).tokens;
          var baseType = scopeForTypeLookup.getType(tokens, tokens.length - 1);
          if (baseType instanceof ReferenceBinding ref) {
            var field = ref.getField(tokens[tokens.length - 1], true);
            if (field != null) {
              return field;
            }
          }
          var str = CharOperation.toString(tokens);
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
   * from {@link #compileExpression(Expression, ClassScope, JavaEnvironmentWithEcj)} to a {@link IMetaValue} that can be
   * wrapped inside a {@link AnnotationElementSpi}
   */
  @SuppressWarnings("pmd:NPathComplexity")
  static IMetaValue resolveCompiledValue(JavaEnvironmentWithEcj env, AnnotatableSpi owner, Object compiledValue, Supplier<Object> compiledValueSupplier) {
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
      return MetaValueFactory.createFromType(bindingToType(env, (TypeBinding) compiledValue, () -> (TypeBinding) compiledValueSupplier.get()));
    }
    if (compiledValue instanceof FieldBinding fb) {
      // enum constants
      var type = bindingToType(env, fb.declaringClass, () -> withNewElement(FieldBinding.class, f -> f.declaringClass, compiledValueSupplier));
      var name = new String(fb.name);
      if (type != null) {
        for (var f : type.getFields()) {
          if (f.getElementName().equals(name)) {
            return MetaValueFactory.createFromEnum(f);
          }
        }
      }
      return MetaValueFactory.createUnknown("ENUM " + fb.declaringClass.debugName() + '#' + name);
    }
    if (compiledValue instanceof AnnotationBinding a) {
      // annotation binding
      return MetaValueFactory.createFromAnnotation(env.createBindingAnnotation(owner, a));
    }
    if (compiledValue.getClass().isArray()) {
      // arrays
      var n = Array.getLength(compiledValue);
      var metaArray = new IMetaValue[n];
      if (n > 0) {
        metaArray = IntStream.range(0, n)
            .mapToObj(i -> resolveCompiledValue(env, owner, getElementFromArray(compiledValue, i), () -> withNewElement(Object.class, t -> getElementFromArray(t, i), compiledValueSupplier)))
            .toArray(IMetaValue[]::new);
      }
      return MetaValueFactory.createArray(metaArray);
    }
    return MetaValueFactory.createUnknown(compiledValue);
  }

  @SuppressWarnings("unchecked")
  static <T, R> R withNewElement(Class<T> type, Function<T, R> function, Supplier<Object> valueSupplier) {
    var val = valueSupplier.get();
    if (val == null) {
      return null;
    }
    if (!type.isInstance(val)) {
      return null;
    }
    return function.apply((T) val);
  }

  static Object getElementFromArray(Object arr, int i) {
    if (i >= Array.getLength(arr)) {
      return null;
    }
    return Array.get(arr, i);
  }

  static int getTypeIdForLiteral(Literal l) {
    if (l instanceof StringLiteral) {
      return TypeIds.T_JavaLangString;
    }
    if (l instanceof NullLiteral) {
      return TypeIds.T_null;
    }
    if (l instanceof FalseLiteral || l instanceof TrueLiteral) {
      return TypeIds.T_boolean;
    }
    if (l instanceof IntLiteral) {
      return TypeIds.T_int;
    }
    if (l instanceof FloatLiteral) {
      return TypeIds.T_float;
    }
    if (l instanceof LongLiteral) {
      return TypeIds.T_long;
    }
    if (l instanceof DoubleLiteral) {
      return TypeIds.T_double;
    }
    if (l instanceof CharLiteral) {
      return TypeIds.T_char;
    }
    return -1;
  }

  static boolean hasDeprecatedAnnotation(Collection<? extends AnnotationSpi> annotations) {
    if (annotations == null || annotations.isEmpty()) {
      return false;
    }
    return annotations.stream()
        .filter(Objects::nonNull)
        .anyMatch(annotation -> DEPRECATED_ANNOTATION_FQN.equals(annotation.getType().getName()));
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
    public boolean visit(MemberValuePair pair, BlockScope scope) {
      if (pair.compilerElementPair == m_binding) {
        m_result = pair;
      }
      return m_result == null;
    }
  }

}
