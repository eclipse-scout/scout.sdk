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

import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * <h3>{@link SignatureUtils}</h3> Helper methods to deal with Signatures.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 * @see Signature
 * @see ISignatureConstants
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
    int pos = 0;
    if (!signature.isEmpty() && signature.charAt(0) == ISignatureConstants.C_EXTENDS) {
      pos = 1;
    }
    return signature.length() > pos && signature.charAt(pos) == ISignatureConstants.C_UNRESOLVED;
  }

  /**
   * Gets the signature of the given {@link IType}.
   *
   * @param t
   *          The {@link IType} or <code>null</code>.
   * @return The signature of the given {@link IType} or <code>null</code> if the given {@link IType} is
   *         <code>null</code>.
   */
  public static String getTypeSignature(IType t) {
    if (t == null) {
      return null;
    }
    return t.signature();
  }

  /**
   * Returns a unique identifier of the given {@link IMethod}. The identifier looks like
   * 'methodName(param1Signature,param2Signature)'.
   *
   * @param method
   *          The {@link IMethod} for which the identifier should be created
   * @return The created identifier
   */
  public static String createMethodIdentifier(IMethod method) {
    return createMethodIdentifier(method, false);
  }

  /**
   * Returns a unique identifier of the given {@link IMethod}. The identifier looks like
   * 'methodName(param1Signature,param2Signature)'.
   *
   * @param method
   *          The {@link IMethod} for which the identifier should be created
   * @param useErasureOnly
   *          If <code>true</code> only the type erasure is used for all method parameter signatures.
   * @return The created identifier
   */
  public static String createMethodIdentifier(IMethod method, boolean useErasureOnly) {
    List<IMethodParameter> parameters = method.parameters().list();
    List<String> signatures = new ArrayList<>(parameters.size());
    for (IMethodParameter mp : parameters) {
      String typeSignature = getTypeSignature(mp.dataType());
      if (typeSignature != null) {
        if (useErasureOnly) {
          typeSignature = Signature.getTypeErasure(typeSignature);
        }
        signatures.add(typeSignature);
      }
    }
    return createMethodIdentifier(method.elementName(), signatures);
  }

  /**
   * Returns an unique identifier for a method with given name and given parameter signatures. The identifier looks like
   * 'methodName(sigOfParam1,sigOfParam2)'.
   *
   * @param methodName
   *          The method name. Must not be <code>null</code.>
   * @param resolvedParamSignatures
   *          The parameter signatures of the method.
   * @return The created identifier
   */
  public static String createMethodIdentifier(String methodName, Iterable<String> resolvedParamSignatures) {
    StringBuilder methodIdBuilder = new StringBuilder();
    methodIdBuilder.append(methodName);
    methodIdBuilder.append('(');

    if (resolvedParamSignatures != null) {
      Iterator<String> iterator = resolvedParamSignatures.iterator();
      if (iterator.hasNext()) {
        methodIdBuilder.append(iterator.next());
        while (iterator.hasNext()) {
          methodIdBuilder.append(',');
          methodIdBuilder.append(iterator.next());
        }
      }
    }

    methodIdBuilder.append(')');
    return methodIdBuilder.toString();
  }

  /**
   * Same as {@link Signature#toString()} but it preserves the dollar sign ($) for inner types. <br>
   * <br>
   * <b>Example:</b>
   *
   * <pre>
   * toFullyQualifiedName("[Ljava.lang.String;") -> "java.lang.String[]"
   * toFullyQualifiedName("Lorg.eclipse.scout.TopLevelClass$InnerOne$InnerTwo;") -> "org.eclipse.scout.TopLevelClass$InnerOne$InnerTwo"
   * toFullyQualifiedName("I") -> "int"
   * toFullyQualifiedName("+QObject;") -> "? extends Object"
   * </pre>
   *
   * @param signature
   *          The signature to convert to a fully qualified name.
   * @return The fully qualified name of the given signature or <code>null</code> if the given signature is
   *         <code>null</code>.
   */
  public static String toFullyQualifiedName(String signature) {
    if (signature == null) {
      return null;
    }

    String[] typeArguments = Signature.getTypeArguments(signature);
    String sig = Signature.getTypeErasure(signature);

    String nameUpToPrimaryType = Signature.getSignatureSimpleName(sig).replace(ISignatureConstants.C_DOT, ISignatureConstants.C_DOLLAR); // ensure to keep $ for inner types.
    String pck = Signature.getSignatureQualifier(sig);

    StringBuilder buf = new StringBuilder(signature.length());
    if (pck.length() > 0) {
      buf.append(pck);
      buf.append('.');
    }
    buf.append(nameUpToPrimaryType);
    if (typeArguments.length > 0) {
      buf.append(ISignatureConstants.C_GENERIC_START);
      buf.append(toFullyQualifiedName(typeArguments[0]));
      for (int i = 1; i < typeArguments.length; i++) {
        buf.append(',');
        buf.append(toFullyQualifiedName(typeArguments[i]));
      }
      buf.append(ISignatureConstants.C_GENERIC_END);
    }
    return buf.toString();
  }

  /**
   * Converts the given base type signature (primitive types) to the corresponding wrapper class signature.
   *
   * @param signature
   *          The primitive signature. Must be a valid signature (not <code>null</code>).
   * @return The boxed version of the given primitive signature or the input if it cannot be boxed.
   */
  @SuppressWarnings("pmd:NPathComplexity")
  public static String boxPrimitiveSignature(String signature) {
    if (Signature.getTypeSignatureKind(signature) != ISignatureConstants.BASE_TYPE_SIGNATURE) {
      return signature;
    }

    if (ISignatureConstants.SIG_BOOLEAN.equals(signature)) {
      return ISignatureConstants.SIG_JAVA_LANG_BOOLEAN;
    }
    if (ISignatureConstants.SIG_BYTE.equals(signature)) {
      return ISignatureConstants.SIG_JAVA_LANG_BYTE;
    }
    if (ISignatureConstants.SIG_CHAR.equals(signature)) {
      return ISignatureConstants.SIG_JAVA_LANG_CHARACTER;
    }
    if (ISignatureConstants.SIG_DOUBLE.equals(signature)) {
      return ISignatureConstants.SIG_JAVA_LANG_DOUBLE;
    }
    if (ISignatureConstants.SIG_FLOAT.equals(signature)) {
      return ISignatureConstants.SIG_JAVA_LANG_FLOAT;
    }
    if (ISignatureConstants.SIG_INT.equals(signature)) {
      return ISignatureConstants.SIG_JAVA_LANG_INTEGER;
    }
    if (ISignatureConstants.SIG_LONG.equals(signature)) {
      return ISignatureConstants.SIG_JAVA_LANG_LONG;
    }
    if (ISignatureConstants.SIG_SHORT.equals(signature)) {
      return ISignatureConstants.SIG_JAVA_LANG_SHORT;
    }
    return signature;
  }

  /**
   * Converts the given complex signature (e.g. Ljava.lang.Long;) to the corresponding primitive signature.
   *
   * @param signature
   *          The complex input signature. Must be a valid signature (not <code>null</code>).
   * @return The primitive signature if it can be unboxed, the input signature otherwise.
   */
  @SuppressWarnings("pmd:NPathComplexity")
  public static String unboxToPrimitiveSignature(String signature) {
    if (Signature.getTypeSignatureKind(signature) == ISignatureConstants.BASE_TYPE_SIGNATURE) {
      return signature;
    }

    if (ISignatureConstants.SIG_JAVA_LANG_BOOLEAN.equals(signature)) {
      return ISignatureConstants.SIG_BOOLEAN;
    }
    if (ISignatureConstants.SIG_JAVA_LANG_BYTE.equals(signature)) {
      return ISignatureConstants.SIG_BYTE;
    }
    if (ISignatureConstants.SIG_JAVA_LANG_CHARACTER.equals(signature)) {
      return ISignatureConstants.SIG_CHAR;
    }
    if (ISignatureConstants.SIG_JAVA_LANG_DOUBLE.equals(signature)) {
      return ISignatureConstants.SIG_DOUBLE;
    }
    if (ISignatureConstants.SIG_JAVA_LANG_FLOAT.equals(signature)) {
      return ISignatureConstants.SIG_FLOAT;
    }
    if (ISignatureConstants.SIG_JAVA_LANG_INTEGER.equals(signature)) {
      return ISignatureConstants.SIG_INT;
    }
    if (ISignatureConstants.SIG_JAVA_LANG_LONG.equals(signature)) {
      return ISignatureConstants.SIG_LONG;
    }
    if (ISignatureConstants.SIG_JAVA_LANG_SHORT.equals(signature)) {
      return ISignatureConstants.SIG_SHORT;
    }
    return signature;
  }
}
