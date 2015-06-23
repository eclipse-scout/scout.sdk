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
package org.eclipse.scout.sdk.core.signature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IMethodParameter;
import org.eclipse.scout.sdk.core.model.IType;

/**
 *
 */
public final class SignatureUtils {

  private SignatureUtils() {
  }

  /**
   * Gets if the given signature is unresolved.
   *
   * @param signature
   *          The signature to check.
   * @return <code>true</code> if the given signature is unresolved. <code>false</code> if it is resolved or
   *         <code>null</code>.
   */
  public static boolean isUnresolved(String signature) {
    if (signature == null) {
      return false;
    }
    if (startsWith(signature, Signature.C_EXTENDS)) {
      signature = signature.substring(1);
    }
    return startsWith(signature, Signature.C_UNRESOLVED);
  }

  private static boolean startsWith(String stringToSearchIn, char charToFind) {
    return stringToSearchIn != null && !stringToSearchIn.isEmpty() && stringToSearchIn.charAt(0) == charToFind;
  }

  public static String toFullyQualifiedName(String sig) {
    String nameUpToPrimaryType = Signature.getSignatureSimpleName(sig).replace(Signature.C_DOT, Signature.C_DOLLAR); // ensure to keep $ for inner types.
    String pck = Signature.getSignatureQualifier(sig);
    return new StringBuilder(nameUpToPrimaryType.length() + 1 + pck.length()).append(pck).append('.').append(nameUpToPrimaryType).toString();
  }

  public static String unboxPrimitiveSignature(String signature) {
    if (Signature.getTypeSignatureKind(signature) == Signature.BASE_TYPE_SIGNATURE) {
      if (Signature.SIG_BOOLEAN.equals(signature)) {
        signature = Signature.createTypeSignature(Boolean.class.getName());
      }
      else if (Signature.SIG_BYTE.equals(signature)) {
        signature = Signature.createTypeSignature(Byte.class.getName());
      }
      else if (Signature.SIG_CHAR.equals(signature)) {
        signature = Signature.createTypeSignature(Character.class.getName());
      }
      else if (Signature.SIG_DOUBLE.equals(signature)) {
        signature = Signature.createTypeSignature(Double.class.getName());
      }
      else if (Signature.SIG_FLOAT.equals(signature)) {
        signature = Signature.createTypeSignature(Float.class.getName());
      }
      else if (Signature.SIG_INT.equals(signature)) {
        signature = Signature.createTypeSignature(Integer.class.getName());
      }
      else if (Signature.SIG_LONG.equals(signature)) {
        signature = Signature.createTypeSignature(Long.class.getName());
      }
      else if (Signature.SIG_SHORT.equals(signature)) {
        signature = Signature.createTypeSignature(Short.class.getName());
      }
    }
    return signature;
  }

  public static String getResolvedSignature(IType t) {
    if (t == null) {
      return null;
    }

    StringBuilder builder = new StringBuilder();
    getResolvedSignatureRec(t, builder);

    int len = builder.length();
    char[] expr = new char[len];
    builder.getChars(0, len, expr, 0);
    return Signature.createTypeSignature(expr, true);
  }

  protected static void getResolvedSignatureRec(IType type, StringBuilder builder) {
    String name = type.getName();
    boolean isWildCardOnly = name == null; // name may be null for wildcard only types (<?>)
    if (type.isWildcardType()) {
      builder.append('?');
      if (isWildCardOnly) {
        return; // no further processing needed.
      }
      builder.append(" extends ");
    }
    if (!isWildCardOnly) {
      builder.append(name);
    }

    // generics
    List<IType> typeArgs = type.getTypeArguments();
    if (typeArgs.size() > 0) {
      builder.append(Signature.C_GENERIC_START);
      getResolvedSignatureRec(typeArgs.get(0), builder);
      for (int i = 1; i < typeArgs.size(); i++) {
        builder.append(Signature.C_COMMA);
        getResolvedSignatureRec(typeArgs.get(i), builder);
      }
      builder.append(Signature.C_GENERIC_END);
    }

    // arrays
    for (int i = 0; i < type.getArrayDimension(); i++) {
      builder.append(Signature.C_ARRAY).append(Signature.C_ARRAY_END);
    }
  }

  /**
   * @param signature
   *          fully parameterized signature
   * @param validator
   *          an import validator to decide simple name vs. fully qualified name.
   * @return the type reference
   * @see {@link IImportValidator}
   */
  public static String getTypeReference(String signature, IImportValidator validator) {
    StringBuilder sigBuilder = new StringBuilder();
    int arrayCount = 0;
    switch (Signature.getTypeSignatureKind(signature)) {
      case Signature.WILDCARD_TYPE_SIGNATURE:
        sigBuilder.append("?");
        if (signature.length() > 1) {
          sigBuilder.append(" extends ");
          sigBuilder.append(getTypeReference(signature.substring(1), validator));
        }
        break;
      case Signature.ARRAY_TYPE_SIGNATURE:
        arrayCount = Signature.getArrayCount(signature);
        sigBuilder.append(getTypeReference(signature.substring(arrayCount), validator));
        break;
      case Signature.BASE_TYPE_SIGNATURE:
        sigBuilder.append(Signature.getSignatureSimpleName(signature));
        break;
      case Signature.TYPE_VARIABLE_SIGNATURE:
        sigBuilder.append(toFullyQualifiedName(signature));
        break;
      default:
        String[] typeArguments = Signature.getTypeArguments(signature);
        signature = Signature.getTypeErasure(signature);
        sigBuilder.append(validator.getTypeName(signature));
        if (typeArguments != null && typeArguments.length > 0) {
          sigBuilder.append(Signature.C_GENERIC_START);
          sigBuilder.append(getTypeReference(typeArguments[0], validator));
          for (int i = 1; i < typeArguments.length; i++) {
            sigBuilder.append(", ");
            sigBuilder.append(getTypeReference(typeArguments[i], validator));
          }
          sigBuilder.append(Signature.C_GENERIC_END);
        }
        break;
    }
    for (int i = 0; i < arrayCount; i++) {
      sigBuilder.append("[]");
    }
    return sigBuilder.toString();
  }

  /**
   * Returns a unique identifier of a method. The identifier looks like 'methodname(param1Signature,param2Signature)'.
   *
   * @param method
   *          The method for which the identifier should be created
   * @return The created identifier
   * @throws CoreException
   */
  public static String getMethodIdentifier(IMethod method) {
    List<IMethodParameter> parameters = method.getParameters();
    List<String> signatures = new ArrayList<>(parameters.size());
    for (IMethodParameter mp : parameters) {
      signatures.add(getResolvedSignature(mp.getType()));
    }
    return getMethodIdentifier(method.getName(), signatures);
  }

  /**
   * Returns an unique identifier for a method with given name and given parameter signatures. The identifier looks like
   * 'methodname(sigOfParam1,sigOfParam2)'.
   *
   * @param methodName
   *          The method name.
   * @param resolvedParamSignatures
   *          The parameter signatures of the method.
   * @return The created identifier
   */
  public static String getMethodIdentifier(String methodName, List<String> resolvedParamSignatures) {
    StringBuilder methodIdBuilder = new StringBuilder();
    methodIdBuilder.append(methodName);
    methodIdBuilder.append('(');

    Iterator<String> iterator = resolvedParamSignatures.iterator();
    if (iterator.hasNext()) {
      methodIdBuilder.append(iterator.next());
      while (iterator.hasNext()) {
        methodIdBuilder.append(',');
        methodIdBuilder.append(iterator.next());
      }
    }

    methodIdBuilder.append(')');
    return methodIdBuilder.toString();
  }
}
