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
package org.eclipse.scout.sdk.util.signature;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.IRegEx;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.signature.internal.TypeParameterMapping;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

public final class SignatureUtility {

  /**
   * Character constant indicating an arbitrary array type in a signature.
   * Value is <code>'|'</code>.
   */
  public static final char C_ARBITRARY_ARRAY = '|';

  /**
   * Kind constant for a arbitrary array signature.
   *
   * @see #getTypeSignatureKind(String)
   * @since 3.0
   */
  public static final int ARBITRARY_ARRAY_SIGNATURE = 29;

  /**
   * Resolved type signature of the {@link Object} class.
   */
  public static final String SIG_OBJECT = SignatureCache.createTypeSignature(Object.class.getName());

  private SignatureUtility() {
  }

  public static String unboxPrimitiveSignature(String signature) {
    if (Signature.getTypeSignatureKind(signature) == Signature.BASE_TYPE_SIGNATURE) {
      if (Signature.SIG_BOOLEAN.equals(signature)) {
        signature = SignatureCache.createTypeSignature(Boolean.class.getName());
      }
      else if (Signature.SIG_BYTE.equals(signature)) {
        signature = SignatureCache.createTypeSignature(Byte.class.getName());
      }
      else if (Signature.SIG_CHAR.equals(signature)) {
        signature = SignatureCache.createTypeSignature(Character.class.getName());
      }
      else if (Signature.SIG_DOUBLE.equals(signature)) {
        signature = SignatureCache.createTypeSignature(Double.class.getName());
      }
      else if (Signature.SIG_FLOAT.equals(signature)) {
        signature = SignatureCache.createTypeSignature(Float.class.getName());
      }
      else if (Signature.SIG_INT.equals(signature)) {
        signature = SignatureCache.createTypeSignature(Integer.class.getName());
      }
      else if (Signature.SIG_LONG.equals(signature)) {
        signature = SignatureCache.createTypeSignature(Long.class.getName());
      }
      else if (Signature.SIG_SHORT.equals(signature)) {
        signature = SignatureCache.createTypeSignature(Short.class.getName());
      }
    }
    return signature;
  }

  /**
   * To get the signature kind of the given signature. If a signature starts with '|' it is a arbitrary array signature
   * otherwise see {@link Signature#getTypeSignatureKind(String)}.
   *
   * @return the signature kind.
   * @see Signature#getTypeSignatureKind(String)
   */
  public static int getTypeSignatureKind(String signature) {
    // need a minimum 1 char
    if (signature == null || signature.length() < 1) {
      throw new IllegalArgumentException("signature is null or less than 1 char.");
    }
    char c = signature.charAt(0);
    if (c == C_ARBITRARY_ARRAY) {
      return ARBITRARY_ARRAY_SIGNATURE;
    }
    else {
      return Signature.getTypeSignatureKind(signature);
    }
  }

  public static String getResolvedSignature(String signature, IType signatureOwner) throws CoreException {
    return getResolvedSignature(signature, signatureOwner, null);
  }

  public static String getResolvedSignature(String signature, IType signatureOwner, IType contextType) throws CoreException {
    Map<String, IResolvedTypeParameter> genericParameters = null;
    if (TypeUtility.exists(contextType) && TypeUtility.exists(signatureOwner)) {
      Map<String, ITypeParameterMapping> mappings = resolveTypeParameters(contextType);
      ITypeParameterMapping mapping = mappings.get(signatureOwner.getFullyQualifiedName());
      if (mapping != null) {
        genericParameters = mapping.getTypeParameters();
      }
    }
    return getResolvedSignature(signatureOwner, genericParameters, signature);
  }

  public static String getResolvedSignature(IType contextType, Map<String /* type param name */, ? extends IResolvedTypeParameter> parameterSignatures, String unresolvedSignature) throws JavaModelException {
    StringBuilder sigBuilder = new StringBuilder();
    unresolvedSignature = ensureSourceTypeParametersAreCorrect(unresolvedSignature, contextType);
    switch (getTypeSignatureKind(unresolvedSignature)) {
      case Signature.WILDCARD_TYPE_SIGNATURE:
        sigBuilder.append(unresolvedSignature.charAt(0));
        if (unresolvedSignature.length() > 1) {
          sigBuilder.append(getResolvedSignature(contextType, parameterSignatures, unresolvedSignature.substring(1)));
        }
        break;
      case Signature.ARRAY_TYPE_SIGNATURE:
        sigBuilder.append(Signature.C_ARRAY);
        sigBuilder.append(getResolvedSignature(contextType, parameterSignatures, unresolvedSignature.substring(1)));
        break;
      case ARBITRARY_ARRAY_SIGNATURE:
        sigBuilder.append(C_ARBITRARY_ARRAY);
        sigBuilder.append(getResolvedSignature(contextType, parameterSignatures, unresolvedSignature.substring(1)));
        break;
      case Signature.BASE_TYPE_SIGNATURE:
        if (endsWith(unresolvedSignature, Signature.C_NAME_END)) {
          unresolvedSignature = unresolvedSignature.substring(0, unresolvedSignature.length() - 1);
        }
        sigBuilder.append(unresolvedSignature);
        break;
      case Signature.TYPE_VARIABLE_SIGNATURE:
        // try to resolve type
        IResolvedTypeParameter typeGeneric = null;
        if (parameterSignatures != null) {
          typeGeneric = parameterSignatures.get(Signature.getSignatureSimpleName(unresolvedSignature));
        }
        String sig = null;
        if (typeGeneric != null) {
          sig = CollectionUtility.firstElement(typeGeneric.getBoundsSignatures()); // currently only handles the first bound
        }
        if (startsWith(sig, Signature.C_UNRESOLVED) && TypeUtility.exists(contextType)) {
          String simpleName = Signature.getSignatureSimpleName(sig);
          String referencedTypeSignature = getReferencedTypeSignature(contextType, simpleName, false);
          if (referencedTypeSignature != null) {
            sig = referencedTypeSignature;
          }
        }
        if (sig != null) {
          sigBuilder.append(sig);
        }
        else {
          sigBuilder.append(SIG_OBJECT);
        }

        break;
      case Signature.CLASS_TYPE_SIGNATURE:
        String[] typeArguments = Signature.getTypeArguments(unresolvedSignature);
        unresolvedSignature = Signature.getTypeErasure(unresolvedSignature);
        unresolvedSignature = IRegEx.DOLLAR_REPLACEMENT.matcher(unresolvedSignature).replaceAll(".");
        if (startsWith(unresolvedSignature, Signature.C_UNRESOLVED)) {
          // unresolved
          if (StringUtility.hasText(Signature.getSignatureQualifier(unresolvedSignature))) {
            // kind of a qualified signature
            IType t = TypeUtility.getTypeBySignature(unresolvedSignature);
            if (TypeUtility.exists(t)) {
              unresolvedSignature = SignatureCache.createTypeSignature(t.getFullyQualifiedName().replace('$', '.'));
            }
          }
          else if (TypeUtility.exists(contextType)) {
            String simpleName = Signature.getSignatureSimpleName(unresolvedSignature);
            String referencedTypeSignature = getReferencedTypeSignature(contextType, simpleName, false);
            if (referencedTypeSignature != null) {
              unresolvedSignature = referencedTypeSignature;
            }
          }
        }
        if (endsWith(unresolvedSignature, Signature.C_NAME_END)) {
          unresolvedSignature = unresolvedSignature.substring(0, unresolvedSignature.length() - 1);
        }
        sigBuilder.append(unresolvedSignature);
        if (typeArguments != null && typeArguments.length > 0) {
          sigBuilder.append(Signature.C_GENERIC_START);
          for (int i = 0; i < typeArguments.length; i++) {
            sigBuilder.append(getResolvedSignature(contextType, parameterSignatures, typeArguments[i]));
          }
          sigBuilder.append(Signature.C_GENERIC_END);
        }
        sigBuilder.append(Signature.C_NAME_END);
        break;
      default:
        SdkUtilActivator.logWarning("unhandled signature type: '" + Signature.getTypeSignatureKind(unresolvedSignature) + "'");
        break;
    }
    return sigBuilder.toString();
  }

  private static boolean endsWith(String stringToSearchIn, char charToFind) {
    return stringToSearchIn != null && !stringToSearchIn.isEmpty() && stringToSearchIn.charAt(stringToSearchIn.length() - 1) == charToFind;
  }

  private static boolean startsWith(String stringToSearchIn, char charToFind) {
    return stringToSearchIn != null && !stringToSearchIn.isEmpty() && stringToSearchIn.charAt(0) == charToFind;
  }

  public static boolean isEqualSignature(String signature1, String signature2) {
    if (signature1 == null && signature2 == null) {
      return true;
    }
    else if (signature1 == null || signature2 == null) {
      return false;
    }
    signature1 = IRegEx.DOLLAR_REPLACEMENT.matcher(signature1).replaceAll(".");
    signature2 = IRegEx.DOLLAR_REPLACEMENT.matcher(signature2).replaceAll(".");
    return signature1.equals(signature2);
  }

  /**
   * @throws CoreException
   * @see {@link #getTypeReference(String, IType, IImportValidator)}
   */
  public static String getTypeReference(String signature, IImportValidator importValidator) throws CoreException {
    return getTypeReference(signature, null, importValidator);
  }

  /**
   * @param signature
   *          fully parameterized signature
   * @param signatureOwner
   *          the owner of the signature used to lookup unresolved types.
   * @param validator
   *          an import validator to decide simple name vs. fully qualified name.
   * @return the type reference
   * @throws CoreException
   * @see {@link IImportValidator}, {@link ImportValidator}, {@link CompilationUnitImportValidator}
   */
  public static String getTypeReference(String signature, IType signatureOwner, IImportValidator validator) throws CoreException {
    StringBuilder sigBuilder = new StringBuilder();
    int arrayCount = 0;
    boolean isArbitraryArray = false;
    switch (getTypeSignatureKind(signature)) {
      case Signature.WILDCARD_TYPE_SIGNATURE:
        sigBuilder.append("?");
        if (signature.length() > 1) {
          sigBuilder.append(" extends ");
          sigBuilder.append(getTypeReference(signature.substring(1), signatureOwner, validator));
        }
        break;
      case Signature.ARRAY_TYPE_SIGNATURE:
        arrayCount = Signature.getArrayCount(signature);
        sigBuilder.append(getTypeReference(signature.substring(arrayCount), signatureOwner, validator));
        break;
      case ARBITRARY_ARRAY_SIGNATURE:
        isArbitraryArray = true;
        sigBuilder.append(getTypeReference(signature.substring(1), signatureOwner, validator));
        break;
      case Signature.BASE_TYPE_SIGNATURE:
        sigBuilder.append(Signature.getSignatureSimpleName(signature));
        break;
      case Signature.TYPE_VARIABLE_SIGNATURE:
        sigBuilder.append(Signature.toString(signature));
        break;
      default:
        String[] typeArguments = Signature.getTypeArguments(signature);
        signature = Signature.getTypeErasure(signature);
        signature = IRegEx.DOLLAR_REPLACEMENT.matcher(signature).replaceAll(".");
        if (startsWith(signature, Signature.C_UNRESOLVED)) {
          // unresolved
          if (signatureOwner != null) {
            String simpleName = Signature.getSignatureSimpleName(signature);
            String referencedTypeSignature = getReferencedTypeSignature(signatureOwner, simpleName, false);
            if (referencedTypeSignature != null) {
              sigBuilder.append(validator.getTypeName(referencedTypeSignature));
            }
          }
          else {
            sigBuilder.append(Signature.toString(signature));
          }
        }
        else {
          // resolved
          sigBuilder.append(validator.getTypeName(signature));
        }
        if (typeArguments != null && typeArguments.length > 0) {
          sigBuilder.append(Signature.C_GENERIC_START);
          for (int i = 0; i < typeArguments.length; i++) {
            if (i > 0) {
              sigBuilder.append(", ");
            }
            sigBuilder.append(getTypeReference(typeArguments[i], signatureOwner, validator));
          }
          sigBuilder.append(Signature.C_GENERIC_END);
        }
        break;
    }
    for (int i = 0; i < arrayCount; i++) {
      sigBuilder.append("[]");
    }
    if (isArbitraryArray) {
      sigBuilder.append("...");
    }
    return sigBuilder.toString();
  }

  /**
   * To get resolved and substituted generic parameter signatures of the method. The signature starts with
   * {@link ScoutSignature#C_ARBITRARY_ARRAY} if the parameter is a arbitrary array.
   *
   * @param method
   *          a scout method
   * @return an array of the parameter signatures
   * @throws CoreException
   */
  public static List<String> getMethodParameterSignatureResolved(IMethod method) throws CoreException {
    return getMethodParameterSignatureResolved(method, method.getDeclaringType());
  }

  /**
   * To get resolved and substituted generic parameter signatures of the method. The signature starts with
   * {@link ScoutSignature#C_ARBITRARY_ARRAY} if the parameter is a arbitrary array.
   *
   * @param jdtMethod
   * @param contextType
   *          the type in what context the method appears, used for generic bindings.
   * @return an array of the parameter signatures
   * @throws CoreException
   */
  public static List<String> getMethodParameterSignatureResolved(IMethod jdtMethod, IType contextType) throws CoreException {
    Map<String, IResolvedTypeParameter> parameters = null;
    if (jdtMethod.getParameterTypes().length > 0) {
      // only resolve type parameters if the method has parameters.
      Map<String, ITypeParameterMapping> mappings = resolveTypeParameters(contextType);
      ITypeParameterMapping mapping = mappings.get(jdtMethod.getDeclaringType().getFullyQualifiedName());
      if (mapping != null) {
        parameters = mapping.getTypeParameters();
      }
    }
    return getMethodParameterSignatureResolved(jdtMethod, parameters);
  }

  public static List<String> getMethodParameterSignatureResolved(IMethod jdtMethod, Map<String, IResolvedTypeParameter> generics) throws CoreException {
    List<String> methodParameterSignature = getMethodParameterSignature(jdtMethod);
    IType methodOwnerType = jdtMethod.getDeclaringType();
    for (int i = 0; i < methodParameterSignature.size(); i++) {
      methodParameterSignature.set(i, getResolvedSignature(methodOwnerType, generics, methodParameterSignature.get(i)));
    }
    return methodParameterSignature;
  }

  /**
   * The get parameter signatures of the given method. The signature starts with
   * {@link ScoutSignature#C_ARBITRARY_ARRAY} if the parameter is an arbitrary array. <h5>NOTE:</h5> <b>generic types
   * are
   * not resolved use {@link ScoutSignature#getMethodParameterSignatureResolved(IMethod)} to get resolved and
   * generic substituted parameter signature</b><br>
   * <br>
   *
   * @param method
   * @return
   * @throws JavaModelException
   */
  public static List<String> getMethodParameterSignature(IMethod method) throws JavaModelException {
    String[] paramNames = method.getParameterNames();
    List<String> paramSignatures = CollectionUtility.arrayList(method.getParameterTypes());

    // check for ... array on last parameter
    if (paramSignatures.size() > 0) {
      String lastSig = paramSignatures.get(paramSignatures.size() - 1);
      String lastParamName = paramNames[paramNames.length - 1];
      if (Signature.getTypeSignatureKind(lastSig) == Signature.ARRAY_TYPE_SIGNATURE) {
        String source = method.getSource();
        if (source != null) {
          String regex = method.getElementName() + "\\s*\\(.*([\\.]{3})\\s*" + lastParamName + "\\s*\\)";
          if (Pattern.compile(regex, Pattern.MULTILINE).matcher(source).find()) {
            paramSignatures.set(paramSignatures.size() - 1, lastSig.replaceFirst("^\\[", "|"));
          }
        }
      }
    }
    return paramSignatures;
  }

  /**
   * To get resolved return type signature of the given method. Generic types are substituted within the method context.
   *
   * @param method
   *          a scout method
   * @return an array of the parameter signatures
   * @throws CoreException
   */
  public static String getReturnTypeSignatureResolved(IMethod method, IType contextType) throws CoreException {
    String returnTypeSignature = method.getReturnType();
    IType methodDeclaringType = method.getDeclaringType();
    returnTypeSignature = getResolvedSignature(returnTypeSignature, methodDeclaringType, contextType);
    return returnTypeSignature;
  }

  /**
   * Checks if the given signature is actually a type parameter signature. If this is the case it is corrected to really
   * declare a type parameter.
   *
   * @param signature
   *          The signature to check.
   * @param signatureOwner
   *          The owner {@link IType} of the signature to compare with.
   * @return The corrected signature.
   * @throws JavaModelException
   */
  public static String ensureSourceTypeParametersAreCorrect(String signature, IType signatureOwner) throws JavaModelException {
    if (!TypeUtility.exists(signatureOwner) || signatureOwner.isBinary()) {
      return signature;
    }
    else if (Signature.getTypeSignatureKind(signature) == Signature.TYPE_VARIABLE_SIGNATURE) {
      // already a type signature
      return signature;
    }
    else {
      ITypeParameter[] typeParameters = signatureOwner.getTypeParameters();
      for (ITypeParameter tp : typeParameters) {
        String typeParamName = tp.getElementName();
        if (CompareUtility.equals(typeParamName, Signature.getSignatureSimpleName(signature))) {
          return new StringBuilder(typeParamName.length() + 2).append(Signature.C_TYPE_VARIABLE).append(typeParamName).append(Signature.C_SEMICOLON).toString();
        }
      }
      return signature;
    }
  }

  /**
   * Returns a unique identifier of a method. The identifier looks like 'methodname(param1Signature,param2Signature)'.
   *
   * @param method
   *          The method for which the identifier should be created
   * @return The created identifier
   * @throws CoreException
   */
  public static String getMethodIdentifier(IMethod method) throws CoreException {
    return getMethodIdentifier(method.getElementName(), getMethodParameterSignatureResolved(method, method.getDeclaringType()));
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

  /**
   * Gets the fully qualified name of the given signature.
   *
   * @param signature
   * @return The fully qualified name of the given signature.
   */
  public static String getFullyQualifiedName(String signature) {
    signature = Signature.getTypeErasure(signature);
    int arrayCount = Signature.getArrayCount(signature);
    if (arrayCount > 0) {
      signature = signature.substring(arrayCount);
    }
    String fqn = Signature.toString(signature);
    return fqn;
  }

  /**
   * Resolves all type parameters on the given focus signature and all existing super types according to the given super
   * signatures. The type parameter signatures are narrowed using the bounds existing on the focus type (therefore the
   * bounds are valid on the focus type only!).
   *
   * @param signature
   *          The signature that describes the focus type. Must not exist yet in the workspace.
   * @param superClassSignature
   *          The signature describing the super class type. May be null. If this parameter is provided the
   *          {@link IType} the signature refers to must exist to be part of the type parameter calculation
   * @param interfaceSignatures
   *          The signatures describing the super interfaces of the given signature. May be null. If signatures are
   *          provided the {@link IType}s the signatures refer to must exist to be part of the type parameter
   *          calculation.
   * @return A {@link Map} containing the fully qualified type name of the super hierarchy of the focus type and the
   *         corresponding {@link ITypeParameterMapping} narrowed according to the given focus type.
   * @throws CoreException
   */
  public static Map<String, ITypeParameterMapping> resolveTypeParameters(String signature, String superClassSignature, List<String> interfaceSignatures) throws CoreException {
    if (!StringUtility.hasText(signature)) {
      return null;
    }

    Map<String, TypeParameterMapping> collector = new HashMap<String, TypeParameterMapping>();
    TypeParameterMapping first = new TypeParameterMapping(signature, superClassSignature, interfaceSignatures);
    collector.put(Signature.toString(signature), first);

    if (StringUtility.hasText(superClassSignature)) {
      IType superType = TypeUtility.getTypeBySignature(superClassSignature);
      if (TypeUtility.exists(superType)) {
        TypeParameterMapping.buildMappingRec(superType, TypeUtility.getSupertypeHierarchy(superType), collector, first);
      }
    }

    if (CollectionUtility.hasElements(interfaceSignatures)) {
      for (String ifcSig : interfaceSignatures) {
        IType interfaceType = TypeUtility.getTypeBySignature(ifcSig);
        if (TypeUtility.exists(interfaceType)) {
          TypeParameterMapping.buildMappingRec(interfaceType, TypeUtility.getSupertypeHierarchy(interfaceType), collector, first);
        }
      }
    }

    return new HashMap<String, ITypeParameterMapping>(collector);
  }

  /**
   * Calculates the resolved type parameter signature of the given type parameter in the context of the given focus
   * type.
   *
   * @param type
   *          The focus {@link IType} that defines the bounds of the type parameter calculation.
   * @param paramDefiningSuperTypeFqn
   *          The fully qualified type name of the {@link IType} which defines the given type parameter.
   * @param paramIndex
   *          The index of the type parameter on the given paramDefiningSuperTypeFqn whose resolved signature should be
   *          returned.
   * @return The signature of the given type parameter as narrowed by the given focus type and its super types or
   *         <code>null</code> if the type parameter could not be found.
   * @throws CoreException
   */
  public static String resolveTypeParameter(IType type, String paramDefiningSuperTypeFqn, int paramIndex) throws CoreException {
    if (!TypeUtility.exists(type) || !StringUtility.hasText(paramDefiningSuperTypeFqn)) {
      return null;
    }
    return resolveTypeParameter(type, TypeUtility.getSupertypeHierarchy(type), paramDefiningSuperTypeFqn, paramIndex);
  }

  /**
   * Calculates the resolved type parameter signature of the given type parameter in the context of the given focus
   * type.
   *
   * @param type
   *          The focus {@link IType} that defines the bounds of the type parameter calculation.
   * @param supertypeHierarchy
   *          The super type hierarchy of the focus type.
   * @param paramDefiningSuperTypeFqn
   *          The fully qualified type name of the {@link IType} which defines the given type parameter.
   * @param paramIndex
   *          The index of the type parameter on the given paramDefiningSuperTypeFqn whose resolved signature should be
   *          returned.
   * @return The signature of the given type parameter as narrowed by the given focus type and its super types or
   *         <code>null</code> if the type parameter could not be found.
   * @throws CoreException
   */
  public static String resolveTypeParameter(IType type, ITypeHierarchy supertypeHierarchy, String paramDefiningSuperTypeFqn, int paramIndex) throws CoreException {
    if (!TypeUtility.exists(type) || supertypeHierarchy == null || !StringUtility.hasText(paramDefiningSuperTypeFqn)) {
      return null;
    }
    Map<String, TypeParameterMapping> collector = new HashMap<String, TypeParameterMapping>();
    TypeParameterMapping.buildMappingRec(type, supertypeHierarchy, collector, null);
    ITypeParameterMapping mapping = collector.get(paramDefiningSuperTypeFqn);
    if (mapping == null) {
      return null;
    }
    Set<String> bounds = mapping.getTypeParameterBounds(paramIndex);
    return CollectionUtility.firstElement(bounds);
  }

  /**
   * Resolves all type parameters on the given focus type and all its super types. The type parameters are narrowed
   * against the bounds defined on the focus type and are valid on the focus type only.
   *
   * @param type
   *          The focus type for which the type parameter hierarchy should be calculated.
   * @return A {@link Map} containing the fully qualified type name of the super hierarchy of the given focus type and
   *         the corresponding {@link ITypeParameterMapping} for each type. May return null if the given type does not
   *         exist.
   * @throws CoreException
   */
  public static Map<String, ITypeParameterMapping> resolveTypeParameters(IType type) throws CoreException {
    if (!TypeUtility.exists(type)) {
      return null;
    }
    return resolveTypeParameters(type, TypeUtility.getSupertypeHierarchy(type));
  }

  /**
   * Resolves all type parameters on the given focus type and all its super types. The type parameters are narrowed
   * against the bounds defined on the focus type and are valid on the focus type only.
   *
   * @param type
   *          The focus type for which the type parameter hierarchy should be calculated.
   * @param supertypeHierarchy
   *          The super hierarchy of the given focus type.
   * @return A {@link Map} containing the fully qualified type name of the super hierarchy of the given focus type and
   *         the corresponding {@link ITypeParameterMapping} for each type. May return null if the given type of
   *         hierarchy are <code>null</code>.
   * @throws CoreException
   */
  public static Map<String, ITypeParameterMapping> resolveTypeParameters(IType type, ITypeHierarchy supertypeHierarchy) throws CoreException {
    if (!TypeUtility.exists(type) || supertypeHierarchy == null) {
      return null;
    }

    Map<String, TypeParameterMapping> collector = new HashMap<String, TypeParameterMapping>();
    TypeParameterMapping.buildMappingRec(type, supertypeHierarchy, collector, null);
    return new HashMap<String, ITypeParameterMapping>(collector);
  }

  /**
   * @return The resolved signature of the given simple type name. The simple name is resolved against the given
   *         declaring type and transformed into a resolved signature.
   * @see TypeUtility#getReferencedTypeFqn(IType, String, boolean)
   */
  public static String getReferencedTypeSignature(IType declaringType, String typeName, boolean searchOnClassPath) throws JavaModelException {
    String referencedTypeFqn = TypeUtility.getReferencedTypeFqn(declaringType, typeName, searchOnClassPath);
    if (referencedTypeFqn != null) {
      return SignatureCache.createTypeSignature(referencedTypeFqn);
    }
    return null;
  }
}
