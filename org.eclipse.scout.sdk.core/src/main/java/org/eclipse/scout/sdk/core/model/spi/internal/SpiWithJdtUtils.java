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
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
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
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VoidTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationValueSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.util.SameCompositeObject;

/**
 *
 */
public final class SpiWithJdtUtils {
  private SpiWithJdtUtils() {
  }

  @SafeVarargs
  static <T> T coalesce(T... values) {
    if (values != null) {
      for (T value : values) {
        if (value != null) {
          return value;
        }
      }
    }
    return null;
  }

  static TypeBinding findTypeBinding(String fqn, AstCompiler compiler) {
    Validate.notNull(fqn);
    if (fqn.length() <= 7) {
      switch (fqn) {
        case IJavaRuntimeTypes._boolean: {
          return TypeBinding.BOOLEAN;
        }
        case IJavaRuntimeTypes._char: {
          return TypeBinding.CHAR;
        }
        case IJavaRuntimeTypes._byte: {
          return TypeBinding.BYTE;
        }
        case IJavaRuntimeTypes._short: {
          return TypeBinding.SHORT;
        }
        case IJavaRuntimeTypes._int: {
          return TypeBinding.INT;
        }
        case IJavaRuntimeTypes._long: {
          return TypeBinding.LONG;
        }
        case IJavaRuntimeTypes._float: {
          return TypeBinding.FLOAT;
        }
        case IJavaRuntimeTypes._double: {
          return TypeBinding.DOUBLE;
        }
        case IJavaRuntimeTypes._void: {
          return TypeBinding.VOID;
        }
      }
    }
    char[][] lookupName = CharOperation.splitOn('.', fqn.toCharArray());
    ReferenceBinding binding = compiler.lookupEnvironment.getType(lookupName);
    if (binding instanceof MissingTypeBinding) {
      return null;
    }
    return binding;
  }

  static TypeSpi bindingToInnerType(JavaEnvironmentWithJdt env, TypeBinding primaryTypeBinding, String[] parts) {
    if (primaryTypeBinding == null) {
      return null;
    }

    TypeSpi result = bindingToType(env, primaryTypeBinding);

    // it is an inner type: step into
    StringTokenizer st = new StringTokenizer(parts[1], String.valueOf(ISignatureConstants.C_DOLLAR), false);
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
  public static TypeSpi bindingToType(JavaEnvironmentWithJdt env, TypeBinding b) {
    return bindingToType(env, b, null);
  }

  static TypeSpi bindingToType(JavaEnvironmentWithJdt env, TypeBinding b, BindingTypeWithJdt declaringType) {
    return bindingToType(env, b, declaringType, false);
  }

  static TypeSpi bindingToType(JavaEnvironmentWithJdt env, TypeBinding b, BindingTypeWithJdt declaringType, boolean isWildCard) {
    if (b == null) {
      return null;
    }

    if (b instanceof VoidTypeBinding) {
      return env.createVoidType();
    }
    else if (b instanceof WildcardBinding) {
      WildcardBinding wb = (WildcardBinding) b;
      TypeBinding allBounds = wb.allBounds();
      if (allBounds == null) {
        // wildcard only binding: <?>
        return env.createWildcardOnlyType();
      }
      return bindingToType(env, allBounds, declaringType, true);
    }
    else if (b instanceof ReferenceBinding) {
      // reference to complex type
      return env.createBindingType((ReferenceBinding) b, declaringType, isWildCard);
    }
    else if (b instanceof BaseTypeBinding) {
      return env.createBindingBaseType((BaseTypeBinding) b);
    }
    else if (b instanceof ArrayBinding) {
      return env.createBindingArrayType((ArrayBinding) b, isWildCard);
    }

    return null;
  }

  static int getTypeFlags(int modifiers, QualifiedAllocationExpression allocation, boolean hasDeprecatedAnnotation) {
    int currentModifiers = modifiers;
    boolean isEnumInit = allocation != null && allocation.enumConstant != null;
    if (isEnumInit) {
      currentModifiers |= ClassFileConstants.AccEnum;
    }

    boolean deprecated = hasDeprecatedAnnotation || (currentModifiers & ClassFileConstants.AccDeprecated) != 0;
    currentModifiers = currentModifiers & ExtraCompilerModifiers.AccJustFlag;

    if (deprecated) {
      currentModifiers |= ClassFileConstants.AccDeprecated;
    }
    return currentModifiers &= ~Flags.AccSuper;
  }

  static int getMethodFlags(int modifiers, boolean isVarargs, boolean isDeprecated) {
    int currentModifiers = modifiers;
    currentModifiers &= ExtraCompilerModifiers.AccJustFlag | ClassFileConstants.AccDeprecated;
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
   * @return the declaring type for this element. For {@link TypeSpi} this is the {@link TypeSpi} itself and NOT the
   *         enclosing type {@link TypeSpi#getDeclaringType()}
   */
  static TypeSpi declaringTypeOf(JavaElementSpi owner) {
    if (owner instanceof TypeSpi) {
      return (TypeSpi) owner;
    }
    else if (owner instanceof MemberSpi) {
      return ((MemberSpi) owner).getDeclaringType();
    }
    else if (owner instanceof TypeParameterSpi) {
      return declaringTypeOf(((TypeParameterSpi) owner).getDeclaringMember());
    }
    else if (owner instanceof MethodParameterSpi) {
      return ((MethodParameterSpi) owner).getDeclaringMethod().getDeclaringType();
    }
    else if (owner instanceof AnnotationSpi) {
      return declaringTypeOf(((AnnotationSpi) owner).getOwner());
    }
    else if (owner instanceof AnnotationValueSpi) {
      return declaringTypeOf(((AnnotationValueSpi) owner).getDeclaringAnnotation());
    }
    return null;
  }

  static Scope memberScopeOf(JavaElementSpi owner) {
    if (owner instanceof TypeSpi) {
      return classScopeOf(owner);
    }
    else if (owner instanceof MethodSpi) {
      return methodScopeOf(owner);
    }
    else if (owner instanceof TypeParameterSpi) {
      return memberScopeOf(((TypeParameterSpi) owner).getDeclaringMember());
    }
    else if (owner instanceof MethodParameterSpi) {
      return methodScopeOf(((MethodParameterSpi) owner).getDeclaringMethod());
    }
    else if (owner instanceof AnnotationSpi) {
      return memberScopeOf(((AnnotationSpi) owner).getOwner());
    }
    return null;
  }

  static ClassScope classScopeOf(JavaElementSpi owner) {
    TypeSpi t = declaringTypeOf(owner);

    if (t instanceof BindingTypeWithJdt) {
      Binding b = ((BindingTypeWithJdt) t).getInternalBinding();
      if (b instanceof SourceTypeBinding) {
        return ((SourceTypeBinding) b).scope;
      }
    }
    else if (t instanceof DeclarationTypeWithJdt) {
      return ((DeclarationTypeWithJdt) t).getInternalTypeDeclaration().scope;
    }
    return null;
  }

  static MethodScope methodScopeOf(JavaElementSpi owner) {
    if (owner instanceof BindingMethodWithJdt) {
      AbstractMethodDeclaration d = ((BindingMethodWithJdt) owner).getInternalBinding().sourceMethod();
      if (d != null) {
        return d.scope;
      }
    }
    else if (owner instanceof DeclarationMethodWithJdt) {
      return ((DeclarationMethodWithJdt) owner).getInternalMethodDeclaration().scope;
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

  static Annotation findAnnotationDeclaration(BindingAnnotationWithJdt a) {
    Annotation[] declaredAnnotations = null;
    AnnotatableSpi owner = a.getOwner();
    if (owner instanceof BindingTypeWithJdt) {
      ReferenceBinding b = ((BindingTypeWithJdt) owner).getInternalBinding();
      b = coalesce(b.actualType(), b);
      if (b instanceof SourceTypeBinding) {
        declaredAnnotations = ((SourceTypeBinding) b).scope.referenceContext.annotations;
      }
    }
    else if (owner instanceof BindingMethodWithJdt) {
      MethodBinding b = ((BindingMethodWithJdt) owner).getInternalBinding();
      b = coalesce(b.original(), b);
      if (b.sourceMethod() != null) {
        declaredAnnotations = b.sourceMethod().annotations;
      }
    }
    else if (owner instanceof BindingFieldWithJdt) {
      FieldBinding b = ((BindingFieldWithJdt) owner).getInternalBinding();
      b = coalesce(b.original(), b);
      if (b.sourceField() != null) {
        declaredAnnotations = b.sourceField().annotations;
      }
    }
    else if (owner instanceof BindingTypeParameterWithJdt) {
      TypeVariableBinding b = ((BindingTypeParameterWithJdt) owner).getInternalBinding();
      b = (TypeVariableBinding) coalesce(b.original(), b);
      if (b.declaringElement instanceof SourceTypeBinding) {
        declaredAnnotations = ((SourceTypeBinding) b.declaringElement).scope.referenceContext.annotations;
      }
    }
    //fast visit
    if (declaredAnnotations != null) {
      AnnotationBinding binding = a.getInternalBinding();
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

  static MemberValuePair findAnnotationValueDeclaration(BindingAnnotationValueWithJdt a) {
    Annotation annotationDeclaration = findAnnotationDeclaration(a.getDeclaringAnnotation());
    if (annotationDeclaration == null) {
      return null;
    }
    FindMemberValuePairVisitor v = new FindMemberValuePairVisitor(a.getInternalBinding());
    annotationDeclaration.traverse(v, (BlockScope) null);
    return v.getResult();
  }

  static List<BindingAnnotationWithJdt> createBindingAnnotations(JavaEnvironmentWithJdt env, AnnotatableSpi owner, AnnotationBinding[] annots) {
    if (annots == null || annots.length < 1) {
      return new ArrayList<>(0);
    }
    List<BindingAnnotationWithJdt> result = new ArrayList<>(annots.length);
    for (int i = 0; i < annots.length; i++) {
      result.add(env.createBindingAnnotation(owner, annots[i]));
    }
    return result;
  }

  static List<DeclarationAnnotationWithJdt> createDeclarationAnnotations(JavaEnvironmentWithJdt env, AnnotatableSpi owner, Annotation[] annotations) {
    if (annotations == null || annotations.length < 1) {
      return new ArrayList<>(0);
    }
    List<DeclarationAnnotationWithJdt> result = new ArrayList<>(annotations.length);
    for (int i = 0; i < annotations.length; i++) {
      result.add(env.createDeclarationAnnotation(owner, annotations[i]));
    }
    return result;
  }

  /**
   * transform this {@link Expression} to a compiled binding value of type {@link Constant}, {@link TypeBinding},
   * {@link FieldBinding}, {@link AnnotationBinding}
   */
  static Object compileExpression(Expression expression, ClassScope scopeForTypeLookup) {
    if (expression == null) {
      return null;
    }
    else if (expression instanceof Literal) {
      if (expression.constant == null) {
        ((Literal) expression).computeConstant();
      }
      return expression.constant;
    }
    else if (expression instanceof ArrayInitializer) {
      Expression[] array = ((ArrayInitializer) expression).expressions;
      int n = array.length;
      Object[] values = new Object[n];
      if (n > 0) {
        for (int i = 0; i < n; i++) {
          values[i] = compileExpression(array[i], scopeForTypeLookup);
        }
      }
      return values;
    }
    else if (expression instanceof UnaryExpression) {
      UnaryExpression ue = (UnaryExpression) expression;
      Expression inner = ue.expression;
      if (inner instanceof Literal) {
        int id = getTypeIdForLiteral((Literal) inner);
        if (id > 0) {
          Object candidate = compileExpression(inner, scopeForTypeLookup);
          if (candidate instanceof Constant) {
            Constant opConstant = Constant.computeConstantOperation((Constant) candidate, id, ((expression.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT));
            return opConstant;
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
      return ((Annotation) expression).getCompilerAnnotation();
    }
    else if (expression instanceof Reference) {
      FieldBinding fieldBinding = null;
      if (expression instanceof FieldReference) {
        fieldBinding = ((FieldReference) expression).fieldBinding();
      }
      else if (expression instanceof NameReference) {
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
      //bug fix! jdt does not always set the enum flag
      if (fieldBinding != null /* && (fieldBinding.modifiers & ClassFileConstants.AccEnum) != 0 */) {
        return fieldBinding;
      }
    }
    return ElementValuePair.getValue(expression);
  }

  /**
   * transform a raw annotation value from {@link ElementValuePair#getValue(Expression)} or compiled expression value
   * from {@link #compileExpression(Expression, ClassScope)} to a {@link IMetaValue} that can be wrapped inside a
   * {@link AnnotationValueSpi}
   */
  static IMetaValue resolveCompiledValue(JavaEnvironmentWithJdt env, AnnotatableSpi owner, Object compiledValue) {
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
    else if (compiledValue instanceof TypeBinding) {
      // type
      return MetaValueFactory.createFromType(bindingToType(env, (TypeBinding) compiledValue));
    }
    else if (compiledValue instanceof FieldBinding) {
      // enum constants
      FieldBinding fb = (FieldBinding) compiledValue;
      TypeSpi type = bindingToType(env, fb.declaringClass);
      String name = new String(fb.name);
      for (FieldSpi f : type.getFields()) {
        if (f.getElementName().equals(name)) {
          return MetaValueFactory.createFromEnum(f);
        }
      }
      return MetaValueFactory.createUnknown("ENUM " + fb.declaringClass.debugName() + "#" + name);
    }
    else if (compiledValue instanceof AnnotationBinding) {
      // annotation binding
      AnnotationBinding a = (AnnotationBinding) compiledValue;
      return MetaValueFactory.createFromAnnotation(env.createBindingAnnotation(owner, a));
    }
    else if (compiledValue.getClass().isArray()) {
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

  static String[] splitToPrimaryType(String fqn) {
    // check for inner types
    int firstDollarPos = fqn.indexOf(ISignatureConstants.C_DOLLAR);
    if (firstDollarPos > 0) {
      String primaryType = fqn.substring(0, firstDollarPos);
      String innerTypePart = fqn.substring(firstDollarPos + 1);
      return new String[]{primaryType, innerTypePart};
    }
    return new String[]{fqn};
  }

  static boolean hasDeprecatedAnnotation(AnnotationBinding[] annotations) {
    if (annotations == null) {
      return false;
    }
    for (int i = 0, length = annotations.length; i < length; i++) {
      AnnotationBinding annotation = annotations[i];
      if (CharOperation.equals(annotation.getAnnotationType().sourceName, TypeConstants.JAVA_LANG_DEPRECATED[2])) {
        return true;
      }
    }
    return false;
  }

  static boolean hasDeprecatedAnnotation(Annotation[] annotations) {
    if (annotations == null) {
      return false;
    }
    for (int i = 0, length = annotations.length; i < length; i++) {
      Annotation annotation = annotations[i];
      if (CharOperation.equals(annotation.type.getLastToken(), TypeConstants.JAVA_LANG_DEPRECATED[2])) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the (cached) default values {@link AnnotationValueSpi#isDefaultValue()} for the annotation in correct
   *         source order of the annotation type declaration
   */
  static LinkedHashMap<String, ElementValuePair> getBindingAnnotationSyntheticDefaultValues(JavaEnvironmentWithJdt env, ReferenceBinding annotationType) {
    Map<Object, Object> cache = env.getPerformanceCache();
    SameCompositeObject key = new SameCompositeObject(annotationType, "defaultEvp");
    @SuppressWarnings("unchecked")
    LinkedHashMap<String, ElementValuePair> defaultValues = (LinkedHashMap<String, ElementValuePair>) cache.get(key);
    if (defaultValues == null) {
      MethodBinding[] valueMethods = annotationType.methods();
      //sort
      valueMethods = Arrays.copyOf(valueMethods, valueMethods.length);
      Arrays.sort(valueMethods, new Comparator<MethodBinding>() {
        @Override
        public int compare(MethodBinding m1, MethodBinding m2) {
          AbstractMethodDeclaration d1 = m1.sourceMethod();
          AbstractMethodDeclaration d2 = m2.sourceMethod();
          if (d1 != null && d2 != null) {
            return new Integer(d1.declarationSourceStart).compareTo(new Integer(d2.declarationSourceStart));
          }
          return 0;
        }
      });
      defaultValues = new LinkedHashMap<>(valueMethods.length);
      for (MethodBinding mb : valueMethods) {
        String name = new String(mb.selector);
        Object value = mb.getDefaultValue();
        if (value != null) {
          defaultValues.put(name, new ElementValuePair(mb.selector, value, mb));
        }
        else {
          defaultValues.put(name, null);
        }
      }
      cache.put(key, defaultValues);
    }
    return new LinkedHashMap<>(defaultValues);
  }

  static LinkedHashMap<String, MemberValuePair> getDeclarationAnnotationSyntheticDefaultValues(JavaEnvironmentWithJdt env, TypeBinding typeBinding) {
    Map<Object, Object> cache = env.getPerformanceCache();
    SameCompositeObject key = new SameCompositeObject(typeBinding, "defaultMvp");
    @SuppressWarnings("unchecked")
    LinkedHashMap<String, MemberValuePair> defaultValues = (LinkedHashMap<String, MemberValuePair>) cache.get(key);
    if (defaultValues == null) {
      MethodBinding[] valueMethods = ((ReferenceBinding) typeBinding).methods();
      defaultValues = new LinkedHashMap<>(valueMethods.length);
      //sort
      valueMethods = Arrays.copyOf(valueMethods, valueMethods.length);
      Arrays.sort(valueMethods, new Comparator<MethodBinding>() {
        @Override
        public int compare(MethodBinding m1, MethodBinding m2) {
          AbstractMethodDeclaration d1 = m1.sourceMethod();
          AbstractMethodDeclaration d2 = m2.sourceMethod();
          if (d1 != null && d2 != null) {
            return new Integer(d1.declarationSourceStart).compareTo(new Integer(d2.declarationSourceStart));
          }
          return 0;
        }
      });
      for (MethodBinding mb : valueMethods) {
        String name = new String(mb.selector);
        AbstractMethodDeclaration md0 = mb.sourceMethod();
        if (md0 instanceof AnnotationMethodDeclaration) {
          AnnotationMethodDeclaration md = (AnnotationMethodDeclaration) md0;
          if (md.defaultValue != null) {
            defaultValues.put(name, new MemberValuePair(mb.selector, md.defaultValue.sourceStart, md.defaultValue.sourceEnd, md.defaultValue));
          }
          else {
            defaultValues.put(name, null);
          }
        }
      }
      cache.put(key, defaultValues);
    }
    return new LinkedHashMap<>(defaultValues);
  }

  static class FindAnnotationVisitor extends ASTVisitor {
    private final AnnotationBinding m_binding;
    private Annotation m_result;

    public FindAnnotationVisitor(AnnotationBinding binding) {
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

  static class FindMemberValuePairVisitor extends ASTVisitor {
    private final ElementValuePair m_binding;
    private MemberValuePair m_result;

    public FindMemberValuePairVisitor(ElementValuePair binding) {
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

  public static String createMethodId(MethodSpi m) {
    StringBuilder buf = new StringBuilder();
    buf.append(m.getElementName());
    buf.append('(');
    for (MethodParameterSpi p : m.getParameters()) {
      buf.append(Signature.createTypeSignature(p.getDataType().getName()));
      buf.append(',');
    }
    buf.append(')');
    return buf.toString();
  }

}
