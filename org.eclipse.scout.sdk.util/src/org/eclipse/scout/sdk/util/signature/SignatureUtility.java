package org.eclipse.scout.sdk.util.signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;

public final class SignatureUtility {

  private final static Pattern QUALIFIED_SIG_REGEX = Pattern.compile("^([\\+\\[]*)([^\\<\\(\\;]*)(.*)$");
  private final static Pattern SIG_REPLACEMENT_REGEX = Pattern.compile("[\\.\\$]{1}");
  private final static Pattern DOL_REPLACEMENT_REGEX = Pattern.compile("\\$");
  private final static Pattern PARAM_SIG_REPLACEMENT_REGEX = Pattern.compile("^([^\\:]*)\\:(.*)$");

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

  private SignatureUtility() {
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
      //private final static Pattern SIMPLE_NAME_REGEX = Pattern.compile("^[\\[]*([^\\<]*).*(\\;)$");
      // signature = SIMPLE_NAME_REGEX.matcher(signature).replaceAll("$1$2");
      return Signature.getTypeSignatureKind(signature);
    }
  }

  private static String quoteRegexSpecialCharacters(String input) {
    input = input.replace("\\", "\\\\");
    input = input.replace(".", "\\.");
    input = input.replace("+", "\\+");
    input = input.replace("?", "\\?");
    input = input.replace("^", "\\^");
    input = input.replace("$", "\\$");
    input = input.replace("[", "\\[");
    input = input.replace("]", "\\]");
    input = input.replace("(", "\\(");
    input = input.replace(")", "\\)");
    input = input.replace("{", "\\{");
    input = input.replace("}", "\\}");
    input = input.replace("*", "\\*");
    input = input.replace("|", "\\|");
    return input;
  }

  public static String getResolvedSignature(String signature, IType signatureOwner) throws JavaModelException {
    return getResolvedSignature(signature, signatureOwner, null);
  }

  public static String getResolvedSignature(String signature, IType signatureOwner, IType contextType) throws JavaModelException {
    StringBuilder sigBuilder = new StringBuilder();
    switch (getTypeSignatureKind(signature)) {
      case Signature.WILDCARD_TYPE_SIGNATURE:
        sigBuilder.append(signature.charAt(0));
        if (signature.length() > 1) {
          sigBuilder.append(getResolvedSignature(signature.substring(1), signatureOwner, contextType));
        }
        break;
      case Signature.ARRAY_TYPE_SIGNATURE:
        sigBuilder.append("[");
        sigBuilder.append(getResolvedSignature(signature.substring(1), signatureOwner, contextType));
        break;
      case ARBITRARY_ARRAY_SIGNATURE:
        sigBuilder.append(C_ARBITRARY_ARRAY);
        sigBuilder.append(getResolvedSignature(signature.substring(1), signatureOwner, contextType));
        break;
      case Signature.BASE_TYPE_SIGNATURE:
        if (signature.endsWith(";")) {
          signature = signature.substring(0, signature.length() - 1);
        }
        sigBuilder.append(signature);
        break;
      case Signature.TYPE_VARIABLE_SIGNATURE:
        // try to resolve type
        String sig = findTypeParameterSignature(signature, signatureOwner, contextType);
        sigBuilder.append(getResolvedSignature(sig, signatureOwner, contextType));
        break;
      case Signature.CLASS_TYPE_SIGNATURE:

        String[] typeArguments = Signature.getTypeArguments(signature);
        signature = Signature.getTypeErasure(signature);
        signature = SIG_REPLACEMENT_REGEX.matcher(signature).replaceAll(".");
        if (signature.startsWith("Q")) {
          // unresolved
          String[][] resolvedTypeName = signatureOwner.resolveType(Signature.getSignatureSimpleName(signature));
          if (resolvedTypeName != null && resolvedTypeName.length == 1) {
            String fqName = resolvedTypeName[0][0];
            if (fqName != null && fqName.length() > 0) {
              fqName = fqName + ".";
            }
            fqName = fqName + resolvedTypeName[0][1];
            signature = SignatureCache.createTypeSignature(fqName);
          }
        }
        if (signature.endsWith(";")) {
          signature = signature.substring(0, signature.length() - 1);
        }
        sigBuilder.append(signature);
        if (typeArguments != null && typeArguments.length > 0) {
          sigBuilder.append("<");
          for (int i = 0; i < typeArguments.length; i++) {
            sigBuilder.append(getResolvedSignature(typeArguments[i], signatureOwner, contextType));
          }
          sigBuilder.append(">");
        }
        sigBuilder.append(";");
        break;
      default:
        SdkUtilActivator.logWarning("unhandled signature type: '" + Signature.getTypeSignatureKind(signature) + "'");
        break;
    }
    return sigBuilder.toString();
  }

  public static boolean isEqualSignature(String signature1, String signature2) {
    if (signature1 == null && signature2 == null) {
      return true;
    }
    else if (signature1 == null || signature2 == null) {
      return false;
    }
    signature1 = DOL_REPLACEMENT_REGEX.matcher(signature1).replaceAll(".");
    signature2 = DOL_REPLACEMENT_REGEX.matcher(signature2).replaceAll(".");
    return signature1.equals(signature2);
  }

  /**
   * To get the simple type reference name within a context represented by the given importValidator. Every fully
   * qualified type name will be passed to the importValidator to decide if the import is already in use.
   * 
   * @param fullyQuallifiedTypeName
   *          e.g. java.lang.String (not a signature).
   * @param importValidator
   *          to evaluate all fully qualified names for create an import and use simple names.
   * @return the simple reference type name in the given validator scope.
   * @see ScoutSdkUtility#getSimpleTypeRefName(String, IImportValidator)
   */
  public static String getTypeReferenceFromFqn(String fullyQualifiedTypeName, IImportValidator importValidator) throws JavaModelException {
    return getTypeReference(SignatureCache.createTypeSignature(fullyQualifiedTypeName), importValidator);
  }

  /**
   * @see {@link ScoutSignature#getTypeReference(String, IType, IType, IImportValidator)}
   */
  public static String getTypeReference(String signature) throws JavaModelException {
    return getTypeReference(signature, null, null, new FullyQuallifiedValidator());
  }

  /**
   * @see {@link ScoutSignature#getTypeReference(String, IType, IType, IImportValidator)}
   */
  public static String getTypeReference(String signature, IImportValidator importValidator) throws JavaModelException {
    return getTypeReference(signature, null, null, importValidator);
  }

  /**
   * @see {@link ScoutSignature#getTypeReference(String, IType, IType, IImportValidator)}
   */
  /*public static String getTypeReference(String signature, IType signatureOwner) throws JavaModelException {
    return getTypeReference(signature, signatureOwner, null, new FullyQuallifiedValidator());
  }*/

  /**
   * @see {@link ScoutSignature#getTypeReference(String, IType, IType, IImportValidator)}
   */
  public static String getTypeReference(String signature, IType signatureOwner, IImportValidator validator) throws JavaModelException {
    return getTypeReference(signature, signatureOwner, null, validator);
  }

  /**
   * @see {@link ScoutSignature#getTypeReference(String, IType, IType, IImportValidator)}
   */
  /*public static String getTypeReference(String signature, IType signatureOwner, IType contextType) throws JavaModelException {
    return getTypeReference(signature, signatureOwner, contextType, new FullyQuallifiedValidator());
  }*/

  /**
   * <h4>Examples</h4> <xmp>
   * getTypeReferenceImpl("Ljava.lang.String;", typeA, typeA, fullyQuallifiedImpValidator)
   * -> java.lang.String
   * getTypeReferenceImpl("QList<?QString>;", typeA, typeA, fullyQuallifiedImpValidator)
   * -> java.util.List<? extends java.lang.String>
   * </xmp>
   * 
   * @param signature
   *          fully parameterized signature
   * @param signatureOwner
   *          the owner of the signature used to lookup unresolved types.
   * @param contextType
   *          must be a subtype of signature owner or the owner itself. Used to find generic variables as T. If null and
   *          signature contains generic types the supertype closest to java.lang.Object with the given type parameter
   *          is calculated.
   * @param validator
   *          an import validator to decide simple name vs. fully qualified name.
   * @return the type reference
   * @see {@link FullyQuallifiedValidator}, {@link IImportValidator}, {@link SimpleImportValidator}
   * @throws JavaModelException
   */
  private static String getTypeReference(String signature, IType signatureOwner, IType contextType, IImportValidator validator) throws JavaModelException {
    StringBuilder sigBuilder = new StringBuilder();
    int arrayCount = 0;
    boolean isArbitraryArray = false;
    switch (getTypeSignatureKind(signature)) {
      case Signature.WILDCARD_TYPE_SIGNATURE:
        sigBuilder.append("?");
        if (signature.length() > 1) {
          sigBuilder.append(" extends ");
          sigBuilder.append(getTypeReference(signature.substring(1), signatureOwner, contextType, validator));
        }
        break;
      case Signature.ARRAY_TYPE_SIGNATURE:
        arrayCount = Signature.getArrayCount(signature);
        sigBuilder.append(getTypeReference(signature.substring(arrayCount), signatureOwner, contextType, validator));
        break;
      case ARBITRARY_ARRAY_SIGNATURE:
        isArbitraryArray = true;
        sigBuilder.append(getTypeReference(signature.substring(1), signatureOwner, contextType, validator));
        break;
      case Signature.BASE_TYPE_SIGNATURE:
        sigBuilder.append(Signature.getSignatureSimpleName(signature));
        break;
      case Signature.TYPE_VARIABLE_SIGNATURE:
        // try to resolve type
        String sig = findTypeParameterSignature(signature, signatureOwner, contextType);
        sigBuilder.append(getTypeReference(sig, signatureOwner, contextType, validator));
        break;
      default:
        String[] typeArguments = Signature.getTypeArguments(signature);
        signature = Signature.getTypeErasure(signature);
        signature = SIG_REPLACEMENT_REGEX.matcher(signature).replaceAll(".");
        if (signature.startsWith("Q")) {
          if (signatureOwner != null) {
            // unresolved
            String[][] resolvedTypeName = signatureOwner.resolveType(Signature.getSignatureSimpleName(signature));
            if (resolvedTypeName != null && resolvedTypeName.length == 1) {
              String fqName = resolvedTypeName[0][0];
              if (fqName != null && fqName.length() > 0) {
                fqName = fqName + ".";
              }
              fqName = fqName + resolvedTypeName[0][1];
              sigBuilder.append(validator.getTypeName(SignatureCache.createTypeSignature(fqName)));
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
          sigBuilder.append("<");
          for (int i = 0; i < typeArguments.length; i++) {
            if (i > 0) {
              sigBuilder.append(", ");
            }
            sigBuilder.append(getTypeReference(typeArguments[i], signatureOwner, contextType, validator));
          }
          sigBuilder.append(">");
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
   * @throws JavaModelException
   */
  public static String[] getMethodParameterSignatureResolved(IMethod method) throws JavaModelException {
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
   * @throws JavaModelException
   */
  public static String[] getMethodParameterSignatureResolved(IMethod jdtMethod, IType contextType) throws JavaModelException {
    String[] methodParameterSignature = getMethodParameterSignature(jdtMethod);
    IType methodOwnerType = jdtMethod.getDeclaringType();
    for (int i = 0; i < methodParameterSignature.length; i++) {
      methodParameterSignature[i] = getResolvedSignature(methodParameterSignature[i], methodOwnerType, contextType);
    }
    return methodParameterSignature;
  }

  /**
   * The get parameter signatures of the given method. The signature starts with
   * {@link ScoutSignature#C_ARBITRARY_ARRAY} if the parameter is a arbitrary array. <h5>NOTE:</h5> <b>generic types are
   * not resolved use {@link ScoutSignature#getMethodParameterSignatureResolved(IScoutMethod)} to get resolved and
   * generic substituted parameter signature</b><br>
   * <br>
   * 
   * @param method
   * @return
   * @throws JavaModelException
   */
  public static String[] getMethodParameterSignature(IMethod method) throws JavaModelException {
    String[] paramNames = method.getParameterNames();
    String[] paramSignatures = Arrays.copyOf(method.getParameterTypes(), method.getParameterTypes().length);
    // check for ... array on last parameter
    if (paramSignatures != null && paramSignatures.length > 0) {
      String lastSig = paramSignatures[paramSignatures.length - 1];
      String lastParamName = paramNames[paramNames.length - 1];
      if (Signature.getTypeSignatureKind(lastSig) == Signature.ARRAY_TYPE_SIGNATURE) {
        String source = method.getSource();
        if (source != null) {
          String regex = method.getElementName() + "\\s*\\(.*([\\.]{3})\\s*" + lastParamName + "\\s*\\)";
          if (Pattern.compile(regex, Pattern.MULTILINE).matcher(source).find()) {
            paramSignatures[paramSignatures.length - 1] = lastSig.replaceFirst("^\\[", "|");
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
   * @throws JavaModelException
   */
  public static String getReturnTypeSignatureResolved(IMethod method, IType contextType) throws JavaModelException {
    String returnTypeSignature = method.getReturnType();
    IType methodDeclaringType = method.getDeclaringType();
    returnTypeSignature = getResolvedSignature(returnTypeSignature, methodDeclaringType, contextType);
    return returnTypeSignature;
  }

  /**
   * returns a unique identifier of a scout method. The identifier looks like
   * 'methodname(param1Signature,param2Signature)'.
   * 
   * @param method
   * @return an unique method identifier of the given method
   * @throws JavaModelException
   */
  public static String getMethodIdentifier(IMethod method) throws JavaModelException {
    StringBuilder methodIdBuilder = new StringBuilder(method.getElementName());
    methodIdBuilder.append("(");
    String[] resolvedParamSignatures = getMethodParameterSignatureResolved(method);
    for (int i = 0; i < resolvedParamSignatures.length; i++) {
      methodIdBuilder.append(resolvedParamSignatures[i]);
      if (i + 1 < resolvedParamSignatures.length) {
        methodIdBuilder.append(",");
      }
    }
    methodIdBuilder.append(")");
    return methodIdBuilder.toString();
  }

  /**
   * returns a unique identifier of a scout method. The identifier looks like
   * 'methodname(param1Signature,param2Signature)'.
   * 
   * @param method
   * @param contextType
   *          the type in what context the method appears, used for generic bindings.
   * @return
   * @throws JavaModelException
   */
  public static String getMethodIdentifier(IMethod method, IType contextType) throws JavaModelException {
    StringBuilder methodIdBuilder = new StringBuilder();
    methodIdBuilder.append(method.getElementName());
    methodIdBuilder.append("(");
    String[] resolvedParamSignatures = getMethodParameterSignatureResolved(method, contextType);
    for (int i = 0; i < resolvedParamSignatures.length; i++) {
      methodIdBuilder.append(resolvedParamSignatures[i]);
      if (i + 1 < resolvedParamSignatures.length) {
        methodIdBuilder.append(",");
      }
    }
    methodIdBuilder.append(")");
    return methodIdBuilder.toString();
  }

  private static String findTypeParameterSignature(String typeParameterSignature, IType signatureOwner, IType contextType) throws JavaModelException {
    String paramTypeName = Signature.getSignatureSimpleName(typeParameterSignature);

    List<IType> hierarchyList = new ArrayList<IType>();
    if (contextType != null) {
      ITypeHierarchy superTypeHierarchy = contextType.newSupertypeHierarchy(null);
      IType visitorType = contextType;
      while (visitorType != null && !visitorType.equals(signatureOwner)) {
        hierarchyList.add(0, visitorType);
        visitorType = superTypeHierarchy.getSuperclass(visitorType);
      }
    }

    // check requested Parameter
    String[] ownerParameterSignatures = signatureOwner.getTypeParameterSignatures();
    int parameterIndex = -1;
    for (int i = 0; i < ownerParameterSignatures.length; i++) {
      String paramSig = ownerParameterSignatures[i];
      String paramName = PARAM_SIG_REPLACEMENT_REGEX.matcher(paramSig).replaceAll("$1");
      paramSig = PARAM_SIG_REPLACEMENT_REGEX.matcher(paramSig).replaceAll("$2");
      if (contextType == null) {
        String signature = getResolvedSignature(paramSig, signatureOwner, contextType);
        return signature;
      }
      else if (paramTypeName.equals(paramName)) {
        parameterIndex = i;
        break;
      }
    }
    if (parameterIndex < 0) {
      return SignatureCache.createTypeSignature(Object.class.getName());
    }
    for (IType hType : hierarchyList) {
      String superClassSignature = hType.getSuperclassTypeSignature();
      if (StringUtility.isNullOrEmpty(superClassSignature)) {
        return SignatureCache.createTypeSignature(Object.class.getName());
      }
      String[] superClassParameterSignatures = Signature.getTypeArguments(superClassSignature);
      if (superClassParameterSignatures.length < parameterIndex + 1) {
        return SignatureCache.createTypeSignature(Object.class.getName());
        // throw new IllegalArgumentException("Lost parameter '" + typeParameterSignature + "' in hierarchy at '" + hType.getFullyQualifiedName() + "'.");
      }
      else {
        // translate
        String signature = getResolvedSignature(superClassParameterSignatures[parameterIndex], hType, contextType);
        return signature;
      }
    }
    return null;
  }

  public static boolean isGenericSignature(String sig) {
    String[] params = Signature.getTypeArguments(sig);
    return params != null && params.length > 0;
  }

  public static String getQuallifiedSignature(String signature, IType jdtType) throws JavaModelException {
    if (getTypeSignatureKind(signature) == Signature.BASE_TYPE_SIGNATURE) {
      return signature;
    }
    else {
      Matcher m = QUALIFIED_SIG_REGEX.matcher(signature);
      if (m.find()) {
        String prefix = m.group(1);
        String simpleSignature = m.group(2);
        String postfix = m.group(3);
        if (simpleSignature.startsWith("Q")) {
          String[][] resolvedTypeName = jdtType.resolveType(Signature.getSignatureSimpleName(simpleSignature + ";"));
          if (resolvedTypeName != null && resolvedTypeName.length == 1) {
            String fqName = resolvedTypeName[0][0];
            if (fqName != null && fqName.length() > 0) {
              fqName = fqName + ".";
            }
            fqName = fqName + resolvedTypeName[0][1];
            simpleSignature = SignatureCache.createTypeSignature(fqName).replaceAll("(^.*)\\;$", "$1");
            signature = prefix + simpleSignature + postfix;
          }
        }
        String[] typeArguments = Signature.getTypeArguments(signature);

        for (String typeArg : typeArguments) {
          signature.replaceFirst("^([^<]*\\<.*)(" + quoteRegexSpecialCharacters(typeArg) + ")(.*)$", "$1" + getQuallifiedSignature(typeArg, jdtType) + "$3");
        }
      }
      else {
        SdkUtilActivator.logWarning("could not quallify types of signature '" + signature + "'");
      }
      return signature;
    }
  }
}
