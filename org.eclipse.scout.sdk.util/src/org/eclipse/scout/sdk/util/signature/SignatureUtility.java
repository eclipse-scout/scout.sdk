package org.eclipse.scout.sdk.util.signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.internal.TypeGenericMapping;
import org.eclipse.scout.sdk.util.type.TypeUtility;

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

  public static String getResolvedSignature(String signature, IType signatureOwner) throws CoreException {
    return getResolvedSignature(signature, signatureOwner, null);
  }

  private static boolean endsWith(String stringToSearchIn, char charToFind) {
    return stringToSearchIn != null && !stringToSearchIn.isEmpty() && stringToSearchIn.charAt(stringToSearchIn.length() - 1) == charToFind;
  }

  private static boolean startsWith(String stringToSearchIn, char charToFind) {
    return stringToSearchIn != null && !stringToSearchIn.isEmpty() && stringToSearchIn.charAt(0) == charToFind;
  }

  public static String getResolvedSignature(String signature, IType signatureOwner, IType contextType) throws CoreException {
    final Map<String, String> genericParameters;
    if (TypeUtility.exists(contextType)) {
      Map<String, ITypeGenericMapping> collector = new HashMap<String, ITypeGenericMapping>();
      resolveGenericParametersInSuperHierarchy(contextType, new String[0], contextType.newSupertypeHierarchy(new NullProgressMonitor()), collector);
      genericParameters = collector.get(signatureOwner.getFullyQualifiedName()).getParameters();
    }
    else {
      genericParameters = new HashMap<String, String>();
    }
    return getResolvedSignature(signatureOwner, genericParameters, signature);

    /*

    StringBuilder sigBuilder = new StringBuilder();
    signature = ensureSourceTypeParametersAreCorrect(signature, signatureOwner);
    switch (getTypeSignatureKind(signature)) {
      case Signature.WILDCARD_TYPE_SIGNATURE:
        sigBuilder.append(signature.charAt(0));
        if (signature.length() > 1) {
          sigBuilder.append(getResolvedSignature(signature.substring(1), signatureOwner, contextType));
        }
        break;
      case Signature.ARRAY_TYPE_SIGNATURE:
        sigBuilder.append(Signature.C_ARRAY);
        sigBuilder.append(getResolvedSignature(signature.substring(1), signatureOwner, contextType));
        break;
      case ARBITRARY_ARRAY_SIGNATURE:
        sigBuilder.append(C_ARBITRARY_ARRAY);
        sigBuilder.append(getResolvedSignature(signature.substring(1), signatureOwner, contextType));
        break;
      case Signature.BASE_TYPE_SIGNATURE:
        if (endsWith(signature, Signature.C_NAME_END)) {
          signature = signature.substring(0, signature.length() - 1);
        }
        sigBuilder.append(signature);
        break;
      case Signature.TYPE_VARIABLE_SIGNATURE:
        // try to resolve type
        String sig = findTypeParameterSignature(signature, signatureOwner, contextType);
        if (CompareUtility.equals(sig, signature)) {
          sigBuilder.append(sig);
          break;
        }
        sigBuilder.append(getResolvedSignature(sig, signatureOwner, contextType));
        break;
      case Signature.CLASS_TYPE_SIGNATURE:

        String[] typeArguments = Signature.getTypeArguments(signature);
        signature = Signature.getTypeErasure(signature);
        signature = SIG_REPLACEMENT_REGEX.matcher(signature).replaceAll(".");
        if (startsWith(signature, Signature.C_UNRESOLVED)) {
          // unresolved
          if (StringUtility.hasText(Signature.getSignatureQualifier(signature))) {
            // kind of a qualified signature
            IType t = TypeUtility.getTypeBySignature(signature);
            if (TypeUtility.exists(t)) {
              signature = SignatureCache.createTypeSignature(t.getFullyQualifiedName().replace('$', '.'));
            }
          }
          else {
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
        }
        if (endsWith(signature, Signature.C_NAME_END)) {
          signature = signature.substring(0, signature.length() - 1);
        }
        sigBuilder.append(signature);
        if (typeArguments != null && typeArguments.length > 0) {
          sigBuilder.append(Signature.C_GENERIC_START);
          for (int i = 0; i < typeArguments.length; i++) {
            sigBuilder.append(getResolvedSignature(typeArguments[i], signatureOwner, contextType));
          }
          sigBuilder.append(Signature.C_GENERIC_END);
        }
        sigBuilder.append(Signature.C_NAME_END);
        break;
      default:
        SdkUtilActivator.logWarning("unhandled signature type: '" + Signature.getTypeSignatureKind(signature) + "'");
        break;
    }
    return sigBuilder.toString();

    */
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
   * @throws CoreException
   * @see ScoutSdkUtility#getSimpleTypeRefName(String, IImportValidator)
   */
  public static String getTypeReferenceFromFqn(String fullyQualifiedTypeName, IImportValidator importValidator) throws CoreException {
    return getTypeReference(SignatureCache.createTypeSignature(fullyQualifiedTypeName), importValidator);
  }

  /**
   * @throws CoreException
   * @see {@link ScoutSignature#getTypeReference(String, IType, IType, IImportValidator)}
   */
  public static String getTypeReference(String signature) throws CoreException {
    return getTypeReference(signature, null, null, new FullyQuallifiedValidator());
  }

  /**
   * @throws CoreException
   * @see {@link ScoutSignature#getTypeReference(String, IType, IType, IImportValidator)}
   */
  public static String getTypeReference(String signature, IImportValidator importValidator) throws CoreException {
    return getTypeReference(signature, null, null, importValidator);
  }

  /**
   * @see {@link ScoutSignature#getTypeReference(String, IType, IType, IImportValidator)}
   */
  /*public static String getTypeReference(String signature, IType signatureOwner) throws JavaModelException {
    return getTypeReference(signature, signatureOwner, null, new FullyQuallifiedValidator());
  }*/

  /**
   * @throws CoreException
   * @see {@link ScoutSignature#getTypeReference(String, IType, IType, IImportValidator)}
   */
  public static String getTypeReference(String signature, IType signatureOwner, IImportValidator validator) throws CoreException {
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
   * @throws CoreException
   * @see {@link FullyQuallifiedValidator}, {@link IImportValidator}, {@link SimpleImportValidator}
   */
  private static String getTypeReference(String signature, IType signatureOwner, IType contextType, IImportValidator validator) throws CoreException {
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
        if (CompareUtility.equals(sig, signature)) {
          sigBuilder.append(sig);
        }
        else {
          sigBuilder.append(getTypeReference(sig, signatureOwner, contextType, validator));
        }
        break;
      default:
        String[] typeArguments = Signature.getTypeArguments(signature);
        signature = Signature.getTypeErasure(signature);
        signature = SIG_REPLACEMENT_REGEX.matcher(signature).replaceAll(".");
        if (startsWith(signature, Signature.C_UNRESOLVED)) {
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
          sigBuilder.append(Signature.C_GENERIC_START);
          for (int i = 0; i < typeArguments.length; i++) {
            if (i > 0) {
              sigBuilder.append(", ");
            }
            sigBuilder.append(getTypeReference(typeArguments[i], signatureOwner, contextType, validator));
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
  public static String[] getMethodParameterSignatureResolved(IMethod method) throws CoreException {
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
  public static String[] getMethodParameterSignatureResolved(IMethod jdtMethod, IType contextType) throws CoreException {
    Map<String, ITypeGenericMapping> genericMapperCollector = new HashMap<String, ITypeGenericMapping>();
    resolveGenericParametersInSuperHierarchy(contextType, new String[0], contextType.newSupertypeHierarchy(new NullProgressMonitor()), genericMapperCollector);
    return getMethodParameterSignatureResolved(jdtMethod, genericMapperCollector.get(jdtMethod.getDeclaringType().getFullyQualifiedName()).getParameters());
//    String[] methodParameterSignature = getMethodParameterSignature(jdtMethod);
//    IType methodOwnerType = jdtMethod.getDeclaringType();
//    for (int i = 0; i < methodParameterSignature.length; i++) {
//      methodParameterSignature[i] = getResolvedSignature(methodParameterSignature[i], methodOwnerType, contextType);
//    }
//    return methodParameterSignature;
  }

  public static String[] getMethodParameterSignatureResolved(IMethod jdtMethod, Map<String, String> generics) throws CoreException {
    String[] methodParameterSignature = getMethodParameterSignature(jdtMethod);
    IType methodOwnerType = jdtMethod.getDeclaringType();
    for (int i = 0; i < methodParameterSignature.length; i++) {
      methodParameterSignature[i] = getResolvedSignature(methodOwnerType, generics, methodParameterSignature[i]); // TODOmethodParameterSignature[i], methodOwnerType, contextType);
    }
    return methodParameterSignature;
  }

  /**
   * The get parameter signatures of the given method. The signature starts with
   * {@link ScoutSignature#C_ARBITRARY_ARRAY} if the parameter is a arbitrary array. <h5>NOTE:</h5> <b>generic types are
   * not resolved use {@link ScoutSignature#getMethodParameterSignatureResolved(IMethod)} to get resolved and
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
   * @throws CoreException
   */
  public static String getReturnTypeSignatureResolved(IMethod method, IType contextType) throws CoreException {
    String returnTypeSignature = method.getReturnType();
    IType methodDeclaringType = method.getDeclaringType();
    returnTypeSignature = getResolvedSignature(returnTypeSignature, methodDeclaringType, contextType);
    return returnTypeSignature;
  }

  public static String ensureSourceTypeParametersAreCorrect(String signature, IType signatureOwner) throws JavaModelException {
    if (!TypeUtility.exists(signatureOwner) || signatureOwner.isBinary()) {
      return signature;
    }
    else {

      ITypeParameter[] typeParameters = signatureOwner.getTypeParameters();
      if (typeParameters != null && typeParameters.length > 0) {
        for (ITypeParameter tp : typeParameters) {
          if (CompareUtility.equals(tp.getElementName(), Signature.getSignatureSimpleName(signature))) {
            return new StringBuilder().append(Signature.C_TYPE_VARIABLE).append(tp.getElementName()).append(Signature.C_SEMICOLON).toString();
          }
        }
      }
      return signature;
    }
  }

  /**
   * returns a unique identifier of a scout method. The identifier looks like
   * 'methodname(param1Signature,param2Signature)'.
   * 
   * @param method
   * @return an unique method identifier of the given method
   * @throws CoreException
   */
  public static String getMethodIdentifier(IMethod method) throws CoreException {
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
   * @throws CoreException
   */
  public static String getMethodIdentifier(IMethod method, IType contextType) throws CoreException {
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

  private static String findTypeParameterSignature(String typeParameterSignature, IType signatureOwner, IType contextType) throws CoreException {
    if (!TypeUtility.exists(contextType) || !TypeUtility.exists(signatureOwner)) {
      return typeParameterSignature;
    }
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
        String signature = getResolvedSignature(paramSig, signatureOwner, null);
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
    return typeParameterSignature;
  }

  public static String getFullyQuallifiedName(String signature) {
    signature = Signature.getTypeErasure(signature);
    int arrayCount = Signature.getArrayCount(signature);
    if (arrayCount > 0) {
      signature = signature.substring(arrayCount);
    }
    String fqn = Signature.toString(signature);
    return fqn;
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

  public static void resolveGenericParametersInSuperHierarchy(String signature, String[] parameterSignatures, String superTypeSignature, String[] interfaceSignatures, Map<String/*fullyQuallifiedName*/, ITypeGenericMapping> collector) throws CoreException {
    TypeGenericMapping typeDesc = new TypeGenericMapping(Signature.getSignatureQualifier(signature) + "." + Signature.getSignatureSimpleName(signature));
    String[] localParameterSignatures = Signature.getTypeParameters(signature);
    if (localParameterSignatures.length > 0) {
      for (int i = 0; i < localParameterSignatures.length; i++) {
        typeDesc.addParameter(Signature.getSignatureSimpleName(localParameterSignatures[i]), parameterSignatures[i]);
      }
    }
    collector.put(typeDesc.getFullyQuallifiedName(), typeDesc);

    // super type
    String[] superTypeParameterSignatures = new String[0];
    IType superType = TypeUtility.getTypeBySignature(superTypeSignature);
    if (TypeUtility.exists(superType)) {
      String[] typeParameters = Signature.getTypeArguments(superTypeSignature);
      superTypeParameterSignatures = new String[typeParameters.length];
      for (int i = 0; i < typeParameters.length; i++) {//String typeParameter: typeParameters){
        if (Signature.getTypeSignatureKind(typeParameters[i]) == Signature.TYPE_VARIABLE_SIGNATURE) {
          superTypeParameterSignatures[i] = typeDesc.getParameterSignature(Signature.getSignatureSimpleName(typeParameters[i]));
        }
        else {
          superTypeParameterSignatures[i] = typeParameters[i];
        }
      }
      resolveGenericParametersInSuperHierarchy(superType, superTypeParameterSignatures, superType.newSupertypeHierarchy(new NullProgressMonitor()), collector);
    }
    // interfaces
    if (interfaceSignatures != null) {
      for (String interfaceSignature : interfaceSignatures) {
        String[] intefaceTypeParameterSignatures = new String[0];
        IType interfaceType = TypeUtility.getTypeBySignature(interfaceSignature);
        if (TypeUtility.exists(interfaceType)) {
          String[] typeParameters = Signature.getTypeParameters(interfaceSignature);
          intefaceTypeParameterSignatures = new String[typeParameters.length];
          for (int i = 0; i < typeParameters.length; i++) {//String typeParameter: typeParameters){
            if (Signature.getTypeSignatureKind(typeParameters[i]) == Signature.TYPE_VARIABLE_SIGNATURE) {
              intefaceTypeParameterSignatures[i] = typeDesc.getParameterSignature(Signature.getSignatureSimpleName(typeParameters[i]));
            }
          }
          resolveGenericParametersInSuperHierarchy(interfaceType, intefaceTypeParameterSignatures, interfaceType.newSupertypeHierarchy(new NullProgressMonitor()), collector);
        }
      }
    }

  }

  public static void resolveGenericParametersInSuperHierarchy(IType type, String[] parameterSignatures, ITypeHierarchy hierarchy, Map<String/*fullyQuallifiedName*/, ITypeGenericMapping> collector) throws CoreException {
    TypeGenericMapping typeDesc = new TypeGenericMapping(type.getFullyQualifiedName());
    ITypeParameter[] typeParameters = type.getTypeParameters();
    for (int i = 0; i < typeParameters.length; i++) {
      if (parameterSignatures.length > i) {
        typeDesc.addParameter(typeParameters[i].getElementName(), parameterSignatures[i]);
      }
      else {
        typeDesc.addParameter(typeParameters[i].getElementName(), SignatureCache.createTypeSignature(Object.class.getName()));
      }
    }
    collector.put(typeDesc.getFullyQuallifiedName(), typeDesc);
    // super class
    if (!Flags.isInterface(type.getFlags())) {
      String superclassTypeSignature = type.getSuperclassTypeSignature();
      if (StringUtility.hasText(superclassTypeSignature)) {
        String[] superParameterSigs = Signature.getTypeArguments(superclassTypeSignature);
        String[] superclassParameterSignatures = new String[superParameterSigs.length];
        for (int i = 0; i < superclassParameterSignatures.length; i++) {
          String resolvedSignature = getResolvedSignature(type, typeDesc.getParameters(), superParameterSigs[i]);
          String signatureQualifier = Signature.getSignatureQualifier(resolvedSignature);
          String signatureSimpleName = Signature.getSignatureSimpleName(resolvedSignature);
          if (StringUtility.isNullOrEmpty(signatureQualifier) && typeDesc.getParameterSignature(signatureSimpleName) != null) {
            // resolve parameter
            resolvedSignature = typeDesc.getParameterSignature(signatureSimpleName);
          }
          superclassParameterSignatures[i] = resolvedSignature;
        }
        resolveGenericParametersInSuperHierarchy(hierarchy.getSuperclass(type), superclassParameterSignatures, hierarchy, collector);
//        resolveGenericParametersInSuperHierarchy(TypeUtility.getTypeBySignature(superclassTypeSignature), superclassParameterSignatures, hierarchy, collector);
      }
    }
    // interfaces
    String[] superInterfaceTypeSignatures = type.getSuperInterfaceTypeSignatures();
    for (String superInterfaceTypeSignature : superInterfaceTypeSignatures) {
      String[] interfaceParameterSigs = Signature.getTypeArguments(superInterfaceTypeSignature);
      String[] interfaceParameterSignatures = new String[interfaceParameterSigs.length];

      for (int i = 0; i < interfaceParameterSignatures.length; i++) {
        String resolvedSignature = getResolvedSignature(type, typeDesc.getParameters(), interfaceParameterSigs[i]);
        String signatureQualifier = Signature.getSignatureQualifier(resolvedSignature);
        String signatureSimpleName = Signature.getSignatureSimpleName(resolvedSignature);
        if (StringUtility.isNullOrEmpty(signatureQualifier) && typeDesc.getParameterSignature(signatureSimpleName) != null) {
          // resolve parameter
          resolvedSignature = typeDesc.getParameterSignature(signatureSimpleName);
        }
        interfaceParameterSignatures[i] = resolvedSignature;
      }
      resolveGenericParametersInSuperHierarchy(TypeUtility.getTypeBySignature(superInterfaceTypeSignature), interfaceParameterSignatures, hierarchy, collector);
    }

  }

  public static String getResolvedSignature(IType contextType, Map<String, String> parameterSignatures, String unresolvedSignature) throws JavaModelException {
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
        String sig = parameterSignatures.get(Signature.getSignatureSimpleName(unresolvedSignature));
        sigBuilder.append(sig);
        break;
      case Signature.CLASS_TYPE_SIGNATURE:

        String[] typeArguments = Signature.getTypeArguments(unresolvedSignature);
        unresolvedSignature = Signature.getTypeErasure(unresolvedSignature);
        unresolvedSignature = SIG_REPLACEMENT_REGEX.matcher(unresolvedSignature).replaceAll(".");
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
            String[][] resolvedTypeName = contextType.resolveType(Signature.getSignatureSimpleName(unresolvedSignature));
            if (resolvedTypeName != null && resolvedTypeName.length == 1) {
              String fqName = resolvedTypeName[0][0];
              if (fqName != null && fqName.length() > 0) {
                fqName = fqName + ".";
              }
              fqName = fqName + resolvedTypeName[0][1];
              unresolvedSignature = SignatureCache.createTypeSignature(fqName);
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
}
