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
    if (startsWith(signature, ISignatureConstants.C_EXTENDS)) {
      signature = signature.substring(1);
    }
    return startsWith(signature, ISignatureConstants.C_UNRESOLVED);
  }

  private static boolean startsWith(String stringToSearchIn, char charToFind) {
    return stringToSearchIn != null && !stringToSearchIn.isEmpty() && stringToSearchIn.charAt(0) == charToFind;
  }

  /**
   * Convenience for: t != null ? {@link IType#signature()} : null
   */
  public static String getTypeSignature(IType t) {
    return t != null ? t.signature() : null;
  }

  /**
   * Returns a unique identifier of a method. The identifier looks like 'methodname(param1Signature,param2Signature)'.
   *
   * @param method
   *          The method for which the identifier should be created
   * @return The created identifier
   */
  public static String createMethodIdentifier(IMethod method) {
    List<IMethodParameter> parameters = method.parameters().list();
    List<String> signatures = new ArrayList<>(parameters.size());
    for (IMethodParameter mp : parameters) {
      signatures.add(getTypeSignature(mp.dataType()));
    }
    return createMethodIdentifier(method.elementName(), signatures);
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
  public static String createMethodIdentifier(String methodName, List<String> resolvedParamSignatures) {
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

  /**
   * Same as {@link Signature#toString()} but it preserves the dollar sign ($) for inner types.
   *
   * @param sig
   *          The signature to convert to a fully qualified name.
   * @return The fully qualified name of the given signature.
   */
  public static String toFullyQualifiedName(String sig) {
    String nameUpToPrimaryType = Signature.getSignatureSimpleName(sig).replace(ISignatureConstants.C_DOT, ISignatureConstants.C_DOLLAR); // ensure to keep $ for inner types.
    String pck = Signature.getSignatureQualifier(sig);
    StringBuilder buf = new StringBuilder(nameUpToPrimaryType.length() + 1 + pck.length());
    if (pck.length() > 0) {
      buf.append(pck);
      buf.append('.');
    }
    buf.append(nameUpToPrimaryType);
    return buf.toString();
  }

  /**
   * Converts the given base type signature (primitive types) to the corresponding wrapper class signature.
   *
   * @param signature
   *          The primitive signature.
   * @return The boxed version of the given primitive signature or the input.
   */
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
}
