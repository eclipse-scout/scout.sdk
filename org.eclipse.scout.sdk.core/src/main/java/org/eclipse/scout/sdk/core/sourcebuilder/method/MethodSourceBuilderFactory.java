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
package org.eclipse.scout.sdk.core.sourcebuilder.method;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.eclipse.scout.sdk.core.model.Flags;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IMethodParameter;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.model.MethodFilters;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 * <h3>{@link MethodSourceBuilderFactory}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public final class MethodSourceBuilderFactory {
  private MethodSourceBuilderFactory() {
  }

  public static IMethodSourceBuilder createConstructorSourceBuilder(String typeName) {
    return createConstructorSourceBuilder(typeName, Flags.AccPublic);
  }

  public static IMethodSourceBuilder createConstructorSourceBuilder(String typeName, int flags, MethodParameterDescription... parameters) {
    MethodSourceBuilder constructorSourceBuilder = new MethodSourceBuilder(typeName);
    constructorSourceBuilder.setFlags(flags);
    if (parameters != null) {
      Set<MethodParameterDescription> params = new LinkedHashSet<>(parameters.length);
      for (MethodParameterDescription p : parameters) {
        params.add(p);
      }
      constructorSourceBuilder.setParameters(params);
    }
    return constructorSourceBuilder;
  }

  private static IMethod getMethodToOverride(ITypeSourceBuilder typeSourceBuilder, ILookupEnvironment lookupContext, Predicate/*<IMethod>*/ methodFilter) {
    List<String> interfaceSignatures = typeSourceBuilder.getInterfaceSignatures();

    List<String> superSignatures = new ArrayList<>(interfaceSignatures.size() + 1);
    superSignatures.add(typeSourceBuilder.getSuperTypeSignature());
    superSignatures.addAll(interfaceSignatures);

    return getMethodToOverride(superSignatures, lookupContext, methodFilter);
  }

  private static IMethod getMethodToOverride(List<String> superSignatures, ILookupEnvironment lookupContext, Predicate/*<IMethod>*/ filter) {
    for (String superCandidate : superSignatures) {
      if (superCandidate != null) {
        IType superType = lookupContext.findType(SignatureUtils.toFullyQualifiedName(superCandidate));
        if (superType != null) {
          IMethod methodToOverride = CoreUtils.findMethodInSuperHierarchy(superType, filter);
          if (methodToOverride != null) {
            return methodToOverride;
          }
        }
      }
    }
    return null;
  }

  public static IMethodSourceBuilder createOverrideMethodSourceBuilder(ITypeSourceBuilder typeSourceBuilder, ILookupEnvironment lookupContext, String methodName) {
    return createOverrideMethodSourceBuilder(typeSourceBuilder, lookupContext, methodName, null);
  }

  public static IMethodSourceBuilder createOverrideMethodSourceBuilder(ITypeSourceBuilder typeSourceBuilder, ILookupEnvironment lookupContext, String methodName, Predicate/*<IMethod>*/ methodFilter) {
    Predicate/*<IMethod>*/ filter = null;
    if (methodFilter == null) {
      filter = MethodFilters.getNameFilter(methodName);
    }
    else {
      filter = MethodFilters.getMultiMethodFilter(MethodFilters.getNameFilter(methodName), methodFilter);
    }

    IMethod methodToOverride = getMethodToOverride(typeSourceBuilder, lookupContext, filter);
    if (methodToOverride == null) {
      return null;
    }

    IMethodSourceBuilder builder = createMethodSourceBuilder(methodToOverride);
    builder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOverrideAnnotationSourceBuilder());
    return builder;
  }

  public static IMethodSourceBuilder createMethodSourceBuilder(IMethod method) {
    return createMethodSourceBuilder(method, null);
  }

  public static IMethodSourceBuilder createOverrideMethodSourceBuilder(String methodName, IType declaringType) {
    IMethod methodToOverride = CoreUtils.findMethodInSuperHierarchy(declaringType, MethodFilters.getNameFilter(methodName));
    if (methodToOverride == null) {
      return null;
    }
    IMethodSourceBuilder builder = createMethodSourceBuilder(methodToOverride);
    builder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOverrideAnnotationSourceBuilder());
    return builder;
  }

  public static IMethodSourceBuilder createMethodSourceBuilder(IMethod method, IMethodBodySourceBuilder bodySourceBuilder) {
    MethodSourceBuilder builder = new MethodSourceBuilder(method.getName());

    // return type
    builder.setReturnTypeSignature(SignatureUtils.getResolvedSignature(method.getReturnType()));

    // exceptions
    Set<IType> excpetions = method.getExceptionTypes();
    List<String> resolvedExceptionSignatures = new ArrayList<>(excpetions.size());
    for (IType t : excpetions) {
      resolvedExceptionSignatures.add(SignatureUtils.getResolvedSignature(t));
    }
    builder.setExceptionSignatures(resolvedExceptionSignatures);

    // parameters
    List<IMethodParameter> parameters = method.getParameters();
    if (parameters.size() > 0) {
      Set<MethodParameterDescription> desc = new HashSet<>(parameters.size());
      for (IMethodParameter m : parameters) {
        MethodParameterDescription d = new MethodParameterDescription(m.getName(), SignatureUtils.getResolvedSignature(m.getType()));
        if (m.getFlags() != Flags.AccDefault) {
          d.setFlags(m.getFlags());
        }
        desc.add(d);
      }
      builder.setParameters(desc);
    }

    // flags
    int flags = method.getFlags() & ~(Flags.AccTransient | Flags.AccBridge | Flags.AccAbstract);
    if (Flags.isInterface(method.getDeclaringType().getFlags()) && Flags.isPackageDefault(flags)) {
      flags = flags | Flags.AccPublic;
    }
    builder.setFlags(flags);

    // add default body
    if (bodySourceBuilder == null && !(Flags.isInterface(method.getDeclaringType().getFlags()) || Flags.isAbstract(method.getFlags()))) {
      bodySourceBuilder = MethodBodySourceBuilderFactory.createSuperCallMethodBody(true);
    }
    builder.setMethodBodySourceBuilder(bodySourceBuilder);

    return builder;
  }

  public static IMethodSourceBuilder createGetter(IFieldSourceBuilder fieldSourceBuilder) {
    return createGetter(fieldSourceBuilder.getElementName(), fieldSourceBuilder.getSignature());
  }

  public static IMethodSourceBuilder createGetter(String fieldName, String signature) {
    StringBuilder methodName = new StringBuilder();
    if (ISignatureConstants.SIG_BOOLEAN.equals(signature)) {
      methodName.append("is");
    }
    else {
      methodName.append("get");
    }
    String field = fieldName.replaceFirst("m\\_", "");
    if (field.length() > 0) {
      methodName.append(Character.toUpperCase(field.charAt(0)));
    }
    if (field.length() > 1) {
      methodName.append(field.substring(1));
    }
    IMethodSourceBuilder getterBuilder = new MethodSourceBuilder(methodName.toString());
    getterBuilder.setReturnTypeSignature(signature);
    getterBuilder.setFlags(Flags.AccPublic);
    getterBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodGetterCommentBuilder());
    getterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return " + fieldName + ";"));
    return getterBuilder;
  }

  public static IMethodSourceBuilder createSetter(IFieldSourceBuilder fieldSourceBuilder) {
    return createSetter(fieldSourceBuilder.getElementName(), fieldSourceBuilder.getSignature());
  }

  public static IMethodSourceBuilder createSetter(String fieldName, String signature) {
    StringBuilder methodName = new StringBuilder();
    methodName.append("set");
    String field = fieldName.replaceFirst("m\\_", "");
    String paramName = CoreUtils.ensureValidParameterName(field);
    methodName.append(CoreUtils.ensureStartWithUpperCase(field));
    IMethodSourceBuilder setterBuilder = new MethodSourceBuilder(methodName.toString());
    setterBuilder.setFlags(Flags.AccPublic);
    setterBuilder.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    setterBuilder.addParameter(new MethodParameterDescription(paramName, signature));
    setterBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodSetterCommentBuilder());
    setterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody(fieldName + " = " + paramName + ";"));
    return setterBuilder;
  }
}
