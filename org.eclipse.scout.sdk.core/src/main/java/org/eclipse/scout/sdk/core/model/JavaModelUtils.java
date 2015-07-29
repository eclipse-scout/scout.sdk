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
package org.eclipse.scout.sdk.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
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
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.VoidTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 *
 */
public final class JavaModelUtils {
  private JavaModelUtils() {
  }

  public static IType bindingToType(TypeBinding b, ILookupEnvironment env) {
    return bindingToType(b, env, null);
  }

  static IType findTypeBySimpleName(String simpleName, Scope s, ILookupEnvironment env) {
    TypeBinding type = s.getType(simpleName.toCharArray());
    if (type instanceof MissingTypeBinding || type instanceof ProblemReferenceBinding) {
      return null;
    }
    return JavaModelUtils.bindingToType(type, env);
  }

  static IType bindingToType(TypeBinding b, ILookupEnvironment env, IType declaringType) {
    return bindingToType(b, env, declaringType, 0, false);
  }

  static IType bindingToType(TypeBinding b, ILookupEnvironment env, IType declaringType, int arrayDimension, boolean isWildCard) {
    if (b == null) {
      return null;
    }

    if (b instanceof VoidTypeBinding) {
      return IType.VOID;
    }
    else if (b instanceof WildcardBinding) {
      WildcardBinding wb = (WildcardBinding) b;
      TypeBinding allBounds = wb.allBounds();
      if (allBounds == null) {
        // wildcard only binding: <?>
        return new WildcardOnlyType(env);
      }
      return bindingToType(allBounds, env, declaringType, arrayDimension, true);
    }
    else if (b instanceof ReferenceBinding) {
      // reference to complex type
      return new BindingType((ReferenceBinding) b, declaringType, arrayDimension, isWildCard, env);
    }
    else if (b instanceof BaseTypeBinding) {
      return new BaseType((BaseTypeBinding) b, arrayDimension, env);
    }
    else if (b instanceof ArrayBinding) {
      ArrayBinding arr = (ArrayBinding) b;
      return bindingToType(arr.leafComponentType, env, declaringType, arr.dimensions, isWildCard);
    }

    return null;
  }

  static ExpressionValueDesc getConstantValue(Constant c) {
    if (c == null || Constant.NotAConstant.equals(c)) {
      return null;
    }

    switch (c.typeID()) {
      case TypeIds.T_char:
        return new ExpressionValueDesc(ExpressionValueType.Char, c.charValue());
      case TypeIds.T_byte:
        return new ExpressionValueDesc(ExpressionValueType.Byte, c.byteValue());
      case TypeIds.T_short:
        return new ExpressionValueDesc(ExpressionValueType.Short, c.shortValue());
      case TypeIds.T_boolean:
        return new ExpressionValueDesc(ExpressionValueType.Bool, c.booleanValue());
      case TypeIds.T_long:
        return new ExpressionValueDesc(ExpressionValueType.Long, c.longValue());
      case TypeIds.T_double:
        return new ExpressionValueDesc(ExpressionValueType.Double, c.doubleValue());
      case TypeIds.T_float:
        return new ExpressionValueDesc(ExpressionValueType.Float, c.floatValue());
      case TypeIds.T_int:
        return new ExpressionValueDesc(ExpressionValueType.Int, c.intValue());
      case TypeIds.T_JavaLangString:
        return new ExpressionValueDesc(ExpressionValueType.String, c.stringValue());
      default:
        throw new SdkException("Unknown constant type: " + c.typeID() + ", name: " + c.typeName());
    }
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

  static ExpressionValueDesc getAnnotationValue(Object o, String name, ClassScope scope, IAnnotation owner, ILookupEnvironment env) {
    if (o == null) {
      return null;
    }

    if (o instanceof TypeBinding) {
      // type
      IType type = JavaModelUtils.bindingToType((TypeBinding) o, env);
      return new ExpressionValueDesc(ExpressionValueType.Type, type);
    }
    else if (o instanceof Constant) {
      // primitives and string
      ExpressionValueDesc annotationValue = getConstantValue((Constant) o);
      if (annotationValue != null) {
        return annotationValue;
      }
    }
    else if (o instanceof FieldBinding) {
      // enum constants
      return new ExpressionValueDesc(ExpressionValueType.String, new String(((FieldBinding) o).name));
    }
    else if (o instanceof Annotation) {
      // annotation instance
      Annotation a = (Annotation) o;
      org.eclipse.scout.sdk.core.model.Annotation result = new org.eclipse.scout.sdk.core.model.Annotation(a, scope, owner.getOwner(), -1, env);
      return new ExpressionValueDesc(ExpressionValueType.Annotation, result);
    }
    else if (o instanceof AnnotationBinding) {
      AnnotationBinding a = (AnnotationBinding) o;
      BindingAnnotation result = new BindingAnnotation(a, owner.getOwner(), scope, -1, env);
      return new ExpressionValueDesc(ExpressionValueType.Annotation, result);
    }
    else if (o instanceof Object[]) {
      // arrays
      Object[] arr = (Object[]) o;
      if (arr.length > 0) {
        IAnnotationValue[] values = new IAnnotationValue[arr.length];
        for (int i = 0; i < values.length; i++) {
          Object rawValue = arr[i];
          if (rawValue instanceof Expression) {
            values[i] = new AnnotationValue(name, (Expression) rawValue, scope, owner, env);
          }
          else {
            values[i] = new ElementAnnotationValue(name, rawValue, scope, owner, env);
          }
        }
        return new ExpressionValueDesc(ExpressionValueType.Array, values);
      }
    }

    return new ExpressionValueDesc(ExpressionValueType.Unknown, o);
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

  static Object computeExpressionValue(Expression expression, ClassScope scope) {
    Object val = null;
    if (expression.constant != null && !Constant.NotAConstant.equals(expression.constant)) {
      val = expression.constant;
    }
    else if (expression instanceof Literal) {
      if (expression.constant == null) {
        ((Literal) expression).computeConstant();
      }
      val = expression.constant;
    }
    else if (expression instanceof ArrayInitializer) {
      val = ((ArrayInitializer) expression).expressions;
    }
    else if (expression instanceof UnaryExpression) {
      UnaryExpression ue = (UnaryExpression) expression;
      Expression inner = ue.expression;
      if (inner instanceof Literal) {
        int id = getTypeIdForLiteral((Literal) inner);
        if (id > 0) {
          Object candidate = computeExpressionValue(inner, scope);
          if (candidate instanceof Constant) {
            val = Constant.computeConstantOperation((Constant) candidate, id, ((expression.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT));
          }
        }
      }
    }
    else if (expression instanceof ClassLiteralAccess) {
      val = ((ClassLiteralAccess) expression).targetType;
      if (val == null) {
        TypeReference type = ((ClassLiteralAccess) expression).type;
        if (type != null) {
          if (type.resolvedType == null && scope != null) {
            type.resolveType(scope);
          }
          val = type.resolvedType;
        }
      }
    }
    else if (expression instanceof Annotation) {
      val = expression;
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
          String str = CharOperation.toString(((QualifiedNameReference) expression).tokens);
          return StringConstant.fromValue(str);
        }
      }
      if (fieldBinding != null && (fieldBinding.modifiers & ClassFileConstants.AccEnum) != 0) {
        val = fieldBinding;
      }
    }
    return val;
  }

  static ExpressionValueDesc getAnnotationValue(Expression expression, String name, ClassScope scope, IAnnotation owner, ILookupEnvironment env) {
    return getAnnotationValue(computeExpressionValue(expression, scope), name, scope, owner, env);
  }

  static List<IAnnotation> annotationBindingsToIAnnotations(AnnotationBinding[] annots, IAnnotatable owner, ILookupEnvironment env) {
    if (annots == null || annots.length < 1) {
      return new ArrayList<>(0);
    }
    List<IAnnotation> result = new ArrayList<>(annots.length);
    for (int i = 0; i < annots.length; i++) {
      result.add(new BindingAnnotation(annots[i], owner, null, i, env));
    }
    return result;
  }

  static List<IAnnotation> annotationsToIAnnotations(Annotation[] annotations, ClassScope scope, IAnnotatable owner, ILookupEnvironment env) {
    if (annotations == null || annotations.length < 1) {
      return new ArrayList<>(0);
    }
    List<IAnnotation> result = new ArrayList<>(annotations.length);
    for (int i = 0; i < annotations.length; i++) {
      result.add(new org.eclipse.scout.sdk.core.model.Annotation(annotations[i], scope, owner, i, env));
    }
    return result;
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

  static class ExpressionValueDesc {
    ExpressionValueType type;
    Object value;

    ExpressionValueDesc(ExpressionValueType t, Object v) {
      type = t;
      value = v;
    }
  }
}
