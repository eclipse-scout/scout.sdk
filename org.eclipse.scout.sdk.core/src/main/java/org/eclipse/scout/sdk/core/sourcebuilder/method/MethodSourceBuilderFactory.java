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
import java.util.List;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.IMethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.MethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Filters;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.MethodFilters;

/**
 * <h3>{@link MethodSourceBuilderFactory}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public final class MethodSourceBuilderFactory {
  private MethodSourceBuilderFactory() {
  }

  public static IMethodSourceBuilder createConstructor(String typeName) {
    return createConstructor(typeName, Flags.AccPublic);
  }

  public static IMethodSourceBuilder createConstructor(String typeName, int flags, IMethodParameterSourceBuilder... parameters) {
    MethodSourceBuilder constructorSourceBuilder = new MethodSourceBuilder(typeName);
    constructorSourceBuilder.setFlags(flags);
    if (parameters != null) {
      for (IMethodParameterSourceBuilder p : parameters) {
        constructorSourceBuilder.addParameter(p);
      }
    }
    return constructorSourceBuilder;
  }

  private static IMethod getMethodToOverride(ITypeSourceBuilder typeSourceBuilder, IJavaEnvironment lookupContext, IFilter<IMethod> methodFilter) {
    List<String> interfaceSignatures = typeSourceBuilder.getInterfaceSignatures();

    List<String> superSignatures = new ArrayList<>(interfaceSignatures.size() + 1);
    superSignatures.add(typeSourceBuilder.getSuperTypeSignature());
    superSignatures.addAll(interfaceSignatures);

    return getMethodToOverride(superSignatures, lookupContext, methodFilter);
  }

  private static IMethod getMethodToOverride(List<String> superSignatures, IJavaEnvironment lookupContext, IFilter<IMethod> filter) {
    for (String superCandidate : superSignatures) {
      if (superCandidate != null) {
        IType superType = lookupContext.findType(SignatureUtils.toFullyQualifiedName(superCandidate));
        if (superType != null) {
          IMethod methodToOverride = superType.methods().withSuperTypes(true).withFilter(filter).first();
          if (methodToOverride != null) {
            return methodToOverride;
          }
        }
      }
    }
    return null;
  }

  public static IMethodSourceBuilder createOverride(ITypeSourceBuilder typeSourceBuilder, IJavaEnvironment lookupContext, String methodName) {
    return createOverride(typeSourceBuilder, lookupContext, methodName, null);
  }

  public static IMethodSourceBuilder createOverride(ITypeSourceBuilder typeSourceBuilder, IJavaEnvironment lookupContext, String methodName, IFilter<IMethod> methodFilter) {
    IFilter<IMethod> filter = null;
    if (methodFilter == null) {
      filter = MethodFilters.name(methodName);
    }
    else {
      filter = Filters.and(MethodFilters.name(methodName), methodFilter);
    }

    IMethod methodToOverride = getMethodToOverride(typeSourceBuilder, lookupContext, filter);
    if (methodToOverride == null) {
      return null;
    }

    return createOverride(methodToOverride);
  }

  public static IMethodSourceBuilder createOverride(String methodName, IType declaringType) {
    IMethod methodToOverride = declaringType.methods().withSuperTypes(true).withName(methodName).first();
    if (methodToOverride == null) {
      return null;
    }
    return createOverride(methodToOverride);
  }

  /**
   * @param method
   * @return a new method source builder that overrides the parameter method
   */
  public static IMethodSourceBuilder createOverride(IMethod method) {
    final MethodSourceBuilder builder = new MethodSourceBuilder(method.elementName());

    // return type
    builder.setReturnTypeSignature(SignatureUtils.getTypeSignature(method.returnType()));

    // exceptions
    List<IType> excpetions = method.exceptionTypes();
    List<String> resolvedExceptionSignatures = new ArrayList<>(excpetions.size());
    for (IType t : excpetions) {
      resolvedExceptionSignatures.add(SignatureUtils.getTypeSignature(t));
    }
    builder.setExceptionSignatures(resolvedExceptionSignatures);

    // parameters
    List<IMethodParameter> parameters = method.parameters().list();
    if (parameters.size() > 0) {
      for (IMethodParameter m : parameters) {
        IMethodParameterSourceBuilder d = new MethodParameterSourceBuilder(m.elementName(), SignatureUtils.getTypeSignature(m.dataType()));
        if (m.flags() != Flags.AccDefault) {
          d.setFlags(m.flags());
        }
        builder.addParameter(d);
      }
    }

    // flags
    int flags = method.flags() & ~(Flags.AccTransient | Flags.AccBridge | Flags.AccAbstract);
    if (Flags.isInterface(method.declaringType().flags()) && Flags.isPackageDefault(flags)) {
      flags = flags | Flags.AccPublic;
    }
    builder.setFlags(flags);

    // add default body
    if (!(Flags.isInterface(method.declaringType().flags()) || Flags.isAbstract(method.flags()))) {
      builder.setBody(MethodBodySourceBuilderFactory.createSuperCall(builder, true));
    }

    //Override annotation
    builder.addAnnotation(AnnotationSourceBuilderFactory.createOverride());

    return builder;
  }

  public static IMethodSourceBuilder createGetter(IFieldSourceBuilder fieldSourceBuilder) {
    return createGetter(fieldSourceBuilder.getElementName(), fieldSourceBuilder.getSignature());
  }

  public static IMethodSourceBuilder createGetter(String fieldName, String signature) {
    return createGetter(fieldName, signature, Flags.AccPublic, true);
  }

  public static IMethodSourceBuilder createGetter(String fieldName, String signature, int flags, boolean autoCreateBody) {
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
    getterBuilder.setFlags(flags);
    getterBuilder.setComment(CommentSourceBuilderFactory.createDefaultGetterMethodComment(getterBuilder));
    if (autoCreateBody) {
      getterBuilder.setBody(new RawSourceBuilder("return " + fieldName + ";"));
    }
    return getterBuilder;
  }

  public static IMethodSourceBuilder createSetter(IFieldSourceBuilder fieldSourceBuilder) {
    return createSetter(fieldSourceBuilder.getElementName(), fieldSourceBuilder.getSignature());
  }

  public static IMethodSourceBuilder createSetter(String fieldName, String signature) {
    return createSetter(fieldName, signature, Flags.AccPublic, true);
  }

  public static IMethodSourceBuilder createSetter(String fieldName, String signature, int flags, boolean autoCreateBody) {
    StringBuilder methodName = new StringBuilder();
    methodName.append("set");
    String field = fieldName.replaceFirst("m\\_", "");
    String paramName = CoreUtils.ensureValidParameterName(field);
    methodName.append(CoreUtils.ensureStartWithUpperCase(field));
    IMethodSourceBuilder setterBuilder = new MethodSourceBuilder(methodName.toString());
    setterBuilder.setFlags(flags);
    setterBuilder.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    setterBuilder.addParameter(new MethodParameterSourceBuilder(paramName, signature));
    setterBuilder.setComment(CommentSourceBuilderFactory.createDefaultSetterMethodComment(setterBuilder));
    if (autoCreateBody) {
      setterBuilder.setBody(new RawSourceBuilder(fieldName + " = " + paramName + ";"));
    }
    return setterBuilder;
  }
}
