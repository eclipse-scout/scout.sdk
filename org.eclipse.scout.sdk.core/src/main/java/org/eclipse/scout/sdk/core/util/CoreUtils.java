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
package org.eclipse.scout.sdk.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.importcollector.ImportCollector;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IPropertyBean;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.internal.PropertyBean;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.w3c.dom.Document;

/**
 * <h3>{@link CoreUtils}</h3> Holds core utilities.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class CoreUtils {

  /**
   * Regular expression matching bean method names (is..., get..., set...)
   */
  public static final Pattern BEAN_METHOD_NAME = Pattern.compile("(get|set|is)([A-Z].*)");
  private static final Pattern REGEX_COMMENT_REMOVE_1 = Pattern.compile("\\/\\/.*?\\\r\\\n");
  private static final Pattern REGEX_COMMENT_REMOVE_2 = Pattern.compile("\\/\\/.*?\\\n");
  private static final Pattern REGEX_COMMENT_REMOVE_3 = Pattern.compile("(?s)\\/\\*.*?\\*\\/");

  private static final ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<>();
  private static volatile Set<String> javaKeyWords = null;

  private CoreUtils() {
  }

  /**
   * Creates a new key pair (private and public key) compatible with the Scout Runtime.<br>
   * <b>This method must behave exactly like the one implemented in
   * org.eclipse.scout.rt.platform.security.SecurityUtility.generateKeyPair().</b>
   *
   * @return A {@link String} array of length=2 containing the base64 encoded private key at index zero and the base64
   *         encoded public key at index 1.
   * @throws GeneralSecurityException
   *           When no keys could be generated
   */
  public static String[] generateKeyPair() throws GeneralSecurityException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "SunEC");
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
    ECGenParameterSpec spec = new ECGenParameterSpec("secp256k1");
    keyGen.initialize(spec, random);
    KeyPair keyPair = keyGen.generateKeyPair();

    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
    PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());

    return new String[]{DatatypeConverter.printBase64Binary(pkcs8EncodedKeySpec.getEncoded()) /*private key*/, DatatypeConverter.printBase64Binary(x509EncodedKeySpec.getEncoded()) /* public key*/};
  }

  /**
   * Deletes the given file or folder.<br>
   * In case the given {@link File} is a folder the contents of the folder are deleted recursively.
   *
   * @param dirToDelete
   *          The file or folder to delete.
   * @throws IOException
   */
  public static void deleteFolder(File dirToDelete) throws IOException {
    Files.walkFileTree(Paths.get(dirToDelete.toURI()), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Removes all comments in the given java source.
   *
   * @param methodBody
   *          The java source
   * @return The source with all comments (single line & multi line) removed.
   */
  public static String removeComments(String methodBody) {
    if (methodBody == null) {
      return null;
    }
    String retVal = methodBody;
    retVal = REGEX_COMMENT_REMOVE_1.matcher(retVal).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_2.matcher(retVal).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_3.matcher(retVal).replaceAll("");
    return retVal;
  }

  /**
   * Reads all bytes from the given {@link InputStream} and converts them into a {@link StringBuilder} using the given
   * charset name.<br>
   *
   * @param is
   *          The data source. Must not be <code>null</code>.
   * @param charsetName
   *          The name of the {@link Charset} to use. Must be supported by the platform.
   * @return A {@link StringBuilder} holding the contents.
   * @throws IOException
   *           While reading data from the stream or if the given charsetName does not exist on this platform.
   * @see Charset#isSupported(String)
   */
  public static StringBuilder inputStreamToString(InputStream is, String charsetName) throws IOException {
    if (!Charset.isSupported(charsetName)) {
      throw new IOException("Charset '" + charsetName + "' is not supported.");
    }
    return inputStreamToString(is, Charset.forName(charsetName));
  }

  /**
   * Gets the method prefix to use for getter methods having the given return type.
   *
   * @param returnTypeSignature
   *          The return type signature of the method this prefix belongs to
   * @return "is" if the return type is <code>boolean</code>. "get" otherwise. Note: <code>java.lang.Boolean</code> does
   *         also return "get" to ensure bean specification compliance.
   */
  public static String getGetterMethodPrefix(String returnTypeSignature) {
    if (ISignatureConstants.SIG_BOOLEAN.equals(returnTypeSignature)) {
      return "is";
    }
    return "get";
  }

  /**
   * Reads all bytes from the given {@link InputStream} and converts them into a {@link StringBuilder} using the given
   * {@link Charset}.<br>
   *
   * @param is
   *          The data source. Must not be <code>null</code>.
   * @param charset
   *          The {@link Charset} to use for the byte-to-char conversion.
   * @return A {@link StringBuilder} holding the contents.
   * @throws IOException
   *           While reading data from the stream.
   */
  public static StringBuilder inputStreamToString(InputStream is, Charset charset) throws IOException {
    final char[] buffer = new char[8192];
    final StringBuilder out = new StringBuilder();
    int length = 0;
    Reader in = new InputStreamReader(is, charset);
    while ((length = in.read(buffer)) != -1) {
      out.append(buffer, 0, length);
    }
    return out;
  }

  /**
   * Converts the given input string literal into the representing original string.<br>
   * This is the inverse function of {@link #toStringLiteral(String)}.
   *
   * @param s
   *          The literal with leading and ending double-quotes
   * @return the original (un-escaped) string. if it is no valid literal string, <code>null</code> is returned.
   */
  public static String fromStringLiteral(String s) {
    if (s == null) {
      return null;
    }

    int len = s.length();
    if (len < 2 || s.charAt(0) != '"' || s.charAt(len - 1) != '"') {
      return null;
    }

    return replaceLiterals(s.substring(1, len - 1), true);
  }

  private static String replaceLiterals(String result, boolean fromLiteral) {
    String[] a = new String[]{"\b", "\t", "\n", "\f", "\r", "\"", "\\", "\0", "\1", "\2", "\3", "\4", "\5", "\6", "\7"};
    String[] b = new String[]{"\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\\\", "\\0", "\\1", "\\2", "\\3", "\\4", "\\5", "\\6", "\\7"};

    if (fromLiteral) {
      return StringUtils.replaceEach(result, b, a);
    }
    return StringUtils.replaceEach(result, a, b);
  }

  /**
   * Converts the given string into a string literal with leading and ending double-quotes including escaping of the
   * given string.<br>
   * This is the inverse function of {@link #fromStringLiteral(String)}.
   *
   * @param s
   *          the string to convert.
   * @return the literal string ready to be directly inserted into java source or null if the input string is null.
   */
  public static String toStringLiteral(String s) {
    if (s == null) {
      return null;
    }

    StringBuilder b = new StringBuilder(s.length() * 2);
    b.append('"'); // opening delimiter
    b.append(replaceLiterals(s, false));
    b.append('"'); // closing delimiter
    return b.toString();
  }

  /**
   * ensures the given java name starts with a lower case character.
   *
   * @param name
   *          The name to handle.
   * @return null if the input is null, an empty string if the given string is empty or only contains white spaces.
   *         Otherwise the input string is returned with the first character modified to lower case.
   */
  public static String ensureStartWithLowerCase(String name) {
    if (StringUtils.isBlank(name)) {
      return name;
    }

    char firstChar = name.charAt(0);
    if (Character.isLowerCase(firstChar)) {
      return name;
    }

    StringBuilder sb = new StringBuilder(name.length());
    sb.append(Character.toLowerCase(firstChar));
    if (name.length() > 1) {
      sb.append(name.substring(1));
    }
    return sb.toString();
  }

  /**
   * ensures the given java name starts with an upper case character.
   *
   * @param name
   *          The name to handle.
   * @return null if the input is null, an empty string if the given string is empty or only contains white spaces.
   *         Otherwise the input string is returned with the first character modified to upper case.
   */
  public static String ensureStartWithUpperCase(String name) {
    if (StringUtils.isBlank(name)) {
      return name;
    }
    char firstChar = name.charAt(0);
    if (Character.isUpperCase(firstChar)) {
      return name;
    }

    StringBuilder sb = new StringBuilder(name.length());
    sb.append(Character.toUpperCase(firstChar));
    if (name.length() > 1) {
      sb.append(name.substring(1));
    }
    return sb.toString();
  }

  /**
   * Gets a one line comment block with given text
   *
   * @param content
   *          The text content
   * @return The comment line.
   */
  public static String getCommentBlock(String content) {
    StringBuilder builder = new StringBuilder();
    builder.append("// TODO ");
    String username = getUsername();
    if (StringUtils.isNotBlank(username)) {
      builder.append('[').append(username).append("] ");
    }
    builder.append(content);
    return builder.toString();
  }

  public static String getCommentAutoGeneratedMethodStub() {
    return getCommentBlock("Auto-generated method stub.");
  }

  /**
   * Returns the user name of the current thread. If the current thread has no user name set, the system property is
   * returned.<br>
   * Use {@link ScoutUtility#setUsernameForThread(String)} to define the user name for the current thread.
   *
   * @return The user name of the thread or the system if no user name is defined on the thread.
   */
  public static String getUsername() {
    String name = CURRENT_USER_NAME.get();
    if (name == null) {
      name = System.getProperty("user.name");
    }
    return name;
  }

  /**
   * Sets the user name that should be returned by {@link ScoutUtility#getUsername()} for the current thread.
   *
   * @param newUsernameForCurrentThread
   *          the new user name
   */
  public static void setUsernameForThread(String newUsernameForCurrentThread) {
    CURRENT_USER_NAME.set(newUsernameForCurrentThread);
  }

  /**
   * Gets the default value for the given signature data type.
   *
   * @param signature
   *          The signature data type for which the default return value should be returned.
   * @return A {@link String} holding the default value for the given signature. Returns <code>null</code> if the given
   *         signature is the void type or <code>null</code>.
   */
  public static String getDefaultValueOf(String signature) {
    if (signature == null) {
      return null;
    }
    if (signature.length() != 1) {
      // not a primitive type
      return "null";
    }
    switch (signature.charAt(0)) {
      case ISignatureConstants.C_BOOLEAN:
        return Boolean.FALSE.toString();
      case ISignatureConstants.C_BYTE:
      case ISignatureConstants.C_CHAR:
      case ISignatureConstants.C_INT:
      case ISignatureConstants.C_SHORT:
        return "0";
      case ISignatureConstants.C_DOUBLE:
        return "0.0";
      case ISignatureConstants.C_LONG:
        return "0L";
      case ISignatureConstants.C_FLOAT:
        return "0.0f";
      default: // e.g. void
        return null;
    }
  }

  /**
   * If the given name is a reserved java keyword a suffix is added to ensure it is a valid name to use e.g. for
   * variables or parameters.
   *
   * @param parameterName
   *          The original name.
   * @return The new value which probably has a suffix appended.
   */
  public static String ensureValidParameterName(String parameterName) {
    if (isReservedJavaKeyword(parameterName)) {
      return parameterName + "Value";
    }
    return parameterName;
  }

  /**
   * @return <code>true</code> if the given word is a reserved java keyword. Otherwise <code>false</code>.
   * @since 3.8.3
   */
  public static boolean isReservedJavaKeyword(String word) {
    if (word == null) {
      return false;
    }
    return getJavaKeyWords().contains(word.toLowerCase());
  }

  /**
   * Gets all reserved java keywords.
   *
   * @return An unmodifiable {@link Set} holding all reserved java keywords.
   */
  public static Set<String> getJavaKeyWords() {
    if (javaKeyWords == null) {
      synchronized (CoreUtils.class) {
        if (javaKeyWords == null) {
          String[] keyWords = new String[]{"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for",
              "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
              "throw", "throws", "transient", "try", "void", "volatile", "while", "false", "null", "true"};
          Set<String> tmp = new HashSet<>(keyWords.length);
          for (String s : keyWords) {
            tmp.add(s);
          }
          javaKeyWords = Collections.unmodifiableSet(tmp);
        }
      }
    }
    return javaKeyWords;
  }

  /**
   * Gets all type parameter arguments as resolved type signatures.
   *
   * @param focusType
   *          The origin focus type that defines the type argument.
   * @param levelFqn
   *          The level on which the value of a type parameter should be extracted.
   * @param typeParamIndex
   *          The index of the type parameter on the given level type whose value should be extracted.
   * @return A {@link List} holding all type argument signatures of the given type parameter.
   * @see #getResolvedTypeParamValue(IType, String, int)
   */
  public static List<String> getResolvedTypeParamValueSignature(IType focusType, String levelFqn, int typeParamIndex) {
    List<IType> typeParamsValue = getResolvedTypeParamValue(focusType, levelFqn, typeParamIndex);
    List<String> result = new ArrayList<>(typeParamsValue.size());
    for (IType t : typeParamsValue) {
      result.add(SignatureUtils.getTypeSignature(t));
    }
    return result;
  }

  /**
   * Gets all type parameter arguments.
   *
   * @param focusType
   *          The origin focus type that defines the type argument.
   * @param levelFqn
   *          The fully qualified name of the class on which the value of a type parameter should be extracted.
   * @param typeParamIndex
   *          The index of the type parameter on the given level type whose value should be extracted.
   * @return A {@link List} holding all type arguments of the given type parameter or an empty {@link List} if the given
   *         levelFqn could not be found in the super hierarchy.
   * @see #getResolvedTypeParamValueSignature(IType, String, int)
   */
  public static List<IType> getResolvedTypeParamValue(IType focusType, String levelFqn, int typeParamIndex) {
    IType levelType = focusType.superTypes().withName(levelFqn).first();
    return getResolvedTypeParamValue(focusType, levelType, typeParamIndex);
  }

  /**
   * Gets all type parameter arguments.
   *
   * @param focusType
   *          The origin focus type that defines the type argument.
   * @param levelType
   *          The {@link IType} on which the value of a type parameter should be extracted.
   * @param typeParamIndex
   *          The index of the type parameter on the given level type whose value should be extracted.
   * @return A {@link List} holding all type arguments of the given type parameter. Returns an empty {@link List} if the
   *         given levelType is <code>null</code>.
   */
  public static List<IType> getResolvedTypeParamValue(IType focusType, IType levelType, int typeParamIndex) {
    if (levelType == null) {
      return Collections.emptyList();
    }
    IType item = levelType.typeArguments().get(typeParamIndex);
    if (!item.isParameterType()) {
      // direct bind
      return Arrays.asList(item);
    }

    IType superClassGeneric = item.superClass();
    List<IType> superIfcGenerics = item.superInterfaces();
    List<IType> result = null;
    if (superClassGeneric != null) {
      result = new ArrayList<>(superIfcGenerics.size() + 1);
      result.add(superClassGeneric);
    }
    else {
      result = new ArrayList<>(superIfcGenerics.size());
    }
    result.addAll(superIfcGenerics);
    return result;
  }

  /**
   * Searches the first direct inner {@link IType} matching the given {@link IFilter} checking the entire super
   * hierarchy of the given {@link IType}. Only direct member {@link IType}s of the given type and its super classes are
   * searched (no recursive inner types).
   *
   * @param declaringType
   *          The {@link IType} to start searching
   * @param filter
   *          The {@link IFilter} to select the member {@link IType}.
   * @return The first member {@link IType} on which {@link IFilter#evaluate(Object)} returns true
   */
  public static IType findInnerTypeInSuperHierarchy(IType declaringType, IFilter<IType> filter) {
    if (declaringType == null) {
      return null;
    }

    IType innerType = declaringType.innerTypes().withFilter(filter).first();
    if (innerType != null) {
      return innerType;
    }
    return findInnerTypeInSuperHierarchy(declaringType.superClass(), filter);
  }

  /**
   * Collects all property beans declared directly in the given type by search methods with the following naming
   * convention:
   *
   * <pre>
   * public <em>&lt;PropertyType&gt;</em> get<em>&lt;PropertyName&gt;</em>();
   * public void set<em>&lt;PropertyName&gt;</em>(<em>&lt;PropertyType&gt;</em> a);
   * </pre>
   *
   * If <code>PropertyType</code> is a boolean property, the following getter is expected
   *
   * <pre>
   * public boolean is<em>&lt;PropertyName&gt;</em>();
   * </pre>
   *
   * @param type
   *          the type within properties are searched
   * @param propertyFilter
   *          optional property bean {@link IFilter} used to filter the result
   * @param comparator
   *          optional property bean {@link Comparator} used to sort the result
   * @return Returns a {@link Set} of property bean descriptions.
   * @see <a href="http://www.oracle.com/technetwork/java/javase/documentation/spec-136004.html">JavaBeans Spec</a>
   */
  public static List<IPropertyBean> getPropertyBeans(IType type, IFilter<IPropertyBean> propertyFilter, Comparator<IPropertyBean> comparator) {
    List<IMethod> methods = type.methods().withFlags(Flags.AccPublic).withName(BEAN_METHOD_NAME).list();
    Map<String, PropertyBean> beans = new HashMap<>(methods.size());
    for (IMethod m : methods) {
      Matcher matcher = BEAN_METHOD_NAME.matcher(m.elementName());
      if (matcher.matches()) {
        String kind = matcher.group(1);
        String name = matcher.group(2);

        List<IMethodParameter> parameterTypes = m.parameters().list();
        IType returnType = m.returnType();
        if ("get".equals(kind) && parameterTypes.size() == 0 && !returnType.isVoid()) {
          PropertyBean desc = beans.get(name);
          if (desc == null) {
            desc = new PropertyBean(type, name);
            beans.put(name, desc);
          }
          if (desc.readMethod() == null) {
            desc.setReadMethod(m);
          }
        }
        else {
          boolean isBool = IJavaRuntimeTypes.java_lang_Boolean.equals(returnType.name()) || IJavaRuntimeTypes._boolean.equals(returnType.name());
          if ("is".equals(kind) && parameterTypes.size() == 0 && isBool) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.readMethod() == null) {
              desc.setReadMethod(m);
            }
          }
          else if ("set".equals(kind) && parameterTypes.size() == 1 && returnType.isVoid()) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.writeMethod() == null) {
              desc.setWriteMethod(m);
            }
          }
        }
      }
    }

    // filter
    List<IPropertyBean> l = new ArrayList<>(beans.size());
    if (propertyFilter == null) {
      l.addAll(beans.values());
    }
    else {
      for (PropertyBean bean : beans.values()) {
        if (propertyFilter.evaluate(bean)) {
          l.add(bean);
        }
      }
    }

    if (comparator != null && !l.isEmpty()) {
      Collections.sort(l, comparator);
    }
    return l;
  }

  /**
   * Checks if a type with given name exists in the given {@link IJavaEnvironment} (classpath).
   *
   * @param env
   *          The context to search in.
   * @param typeToSearchFqn
   *          The fully qualified name to search. See {@link IJavaEnvironment#existsType(String)} for detailed
   *          constraints on the name.
   * @return <code>true</code> if the given type exists, <code>false</code> otherwise.
   */
  public static boolean isOnClasspath(IJavaEnvironment env, String typeToSearchFqn) {
    if (StringUtils.isBlank(typeToSearchFqn)) {
      return false;
    }
    return env.findType(typeToSearchFqn) != null;

  }

  /**
   * Checks if the given {@link IType} exists in the given {@link IJavaEnvironment} (classpath).
   *
   * @param env
   *          The context to search in.
   * @param typeToSearch
   *          The {@link IType} to search
   * @return <code>true</code> if the given type exists, <code>false</code> otherwise.
   */
  public static boolean isOnClasspath(IJavaEnvironment env, IType typeToSearch) {
    if (typeToSearch == null) {
      return false;
    }
    return isOnClasspath(env, typeToSearch.name());
  }

  /**
   * Gets the primary {@link IType} of the given {@link IType}.
   *
   * @param t
   *          The {@link IType} for which the primary {@link IType} should be returned.
   * @return The primary {@link IType} of t or <code>null</code>.
   */
  public static IType getPrimaryType(IType t) {
    IType result = null;
    IType tmp = t;
    while (tmp != null) {
      result = tmp;
      tmp = tmp.declaringType();
    }

    return result;
  }

  /**
   * Tries to box a primitive type to its corresponding complex type.<br>
   * If the given name is already a boxed type (e.g. java.lang.Long), this method returns the input name.
   *
   * @param name
   *          The primitive name (e.g. 'int' or 'boolean')
   * @return The corresponding fully qualified complex type name (e.g. java.lang.Long) or <code>null</code> if the given
   *         input could not be boxed.
   */
  public static String boxPrimitive(String name) {
    switch (name) {
      case IJavaRuntimeTypes._boolean:
        return IJavaRuntimeTypes.java_lang_Boolean;
      case IJavaRuntimeTypes._char:
        return IJavaRuntimeTypes.java_lang_Character;
      case IJavaRuntimeTypes._byte:
        return IJavaRuntimeTypes.java_lang_Byte;
      case IJavaRuntimeTypes._short:
        return IJavaRuntimeTypes.java_lang_Short;
      case IJavaRuntimeTypes._int:
        return IJavaRuntimeTypes.java_lang_Integer;
      case IJavaRuntimeTypes._long:
        return IJavaRuntimeTypes.java_lang_Long;
      case IJavaRuntimeTypes._float:
        return IJavaRuntimeTypes.java_lang_Float;
      case IJavaRuntimeTypes._double:
        return IJavaRuntimeTypes.java_lang_Double;
      case IJavaRuntimeTypes._void:
        return IJavaRuntimeTypes.java_lang_Void;
      case IJavaRuntimeTypes.java_lang_Boolean:
        return name;
      case IJavaRuntimeTypes.java_lang_Character:
        return name;
      case IJavaRuntimeTypes.java_lang_Byte:
        return name;
      case IJavaRuntimeTypes.java_lang_Short:
        return name;
      case IJavaRuntimeTypes.java_lang_Integer:
        return name;
      case IJavaRuntimeTypes.java_lang_Long:
        return name;
      case IJavaRuntimeTypes.java_lang_Float:
        return name;
      case IJavaRuntimeTypes.java_lang_Double:
        return name;
      case IJavaRuntimeTypes.java_lang_Void:
        return name;
      default:
        return null;
    }
  }

  /**
   * Tries to unbox the given name to its corresponding primitive.<br>
   * If the given fqn is already a primitive (e.g. "boolean"), this method returns the input fqn.
   *
   * @param fqn
   *          The fully qualified complex type name (e.g. java.lang.Long)
   * @return The primitive type name or <code>null</code> if no primitive exists.
   */
  public static String unboxToPrimitive(String fqn) {
    switch (fqn) {
      case IJavaRuntimeTypes.java_lang_Boolean:
        return IJavaRuntimeTypes._boolean;
      case IJavaRuntimeTypes.java_lang_Character:
        return IJavaRuntimeTypes._char;
      case IJavaRuntimeTypes.java_lang_Byte:
        return IJavaRuntimeTypes._byte;
      case IJavaRuntimeTypes.java_lang_Short:
        return IJavaRuntimeTypes._short;
      case IJavaRuntimeTypes.java_lang_Integer:
        return IJavaRuntimeTypes._int;
      case IJavaRuntimeTypes.java_lang_Long:
        return IJavaRuntimeTypes._long;
      case IJavaRuntimeTypes.java_lang_Float:
        return IJavaRuntimeTypes._float;
      case IJavaRuntimeTypes.java_lang_Double:
        return IJavaRuntimeTypes._double;
      case IJavaRuntimeTypes.java_lang_Void:
        return IJavaRuntimeTypes._void;
      case IJavaRuntimeTypes._boolean:
        return fqn;
      case IJavaRuntimeTypes._char:
        return fqn;
      case IJavaRuntimeTypes._byte:
        return fqn;
      case IJavaRuntimeTypes._short:
        return fqn;
      case IJavaRuntimeTypes._int:
        return fqn;
      case IJavaRuntimeTypes._long:
        return fqn;
      case IJavaRuntimeTypes._float:
        return fqn;
      case IJavaRuntimeTypes._double:
        return fqn;
      case IJavaRuntimeTypes._void:
        return fqn;
      default:
        return null;
    }
  }

  /**
   * Converts the stack trace of the given {@link Throwable} into a {@link String}. The resulting string includes a
   * leading new line.
   *
   * @param t
   *          The {@link Throwable} from which the stack trace should be extracted. Must not be <code>null</code>.
   * @return The {@link String} containing the stack trace.
   */
  public static String getStackTrace(Throwable t) {
    try (StringWriter w = new StringWriter(); PrintWriter p = new PrintWriter(w)) {
      p.println();
      t.printStackTrace(p);
      return w.toString();
    }
    catch (IOException e) {
      return '[' + e.toString() + ']' + t.toString();
    }
  }

  /**
   * Creates the java source code for the given {@link ISourceBuilder} and returns the resulting source.
   *
   * @param srcBuilder
   *          The {@link ISourceBuilder} that should create the source.
   * @param env
   *          The {@link IJavaEnvironment} to be used to resolve fully qualified names vs. simple names.
   * @param lineSeparator
   *          The line separator to use.
   * @param context
   *          Optional context information.
   * @return The created java source code as {@link String}. Returns <code>null</code> if the {@link ISourceBuilder} or
   *         the {@link IJavaEnvironment} is <code>null</code>.
   */
  public static String createJavaCode(ISourceBuilder srcBuilder, IJavaEnvironment env, String lineSeparator, PropertyMap context) {
    if (srcBuilder == null || env == null) {
      return null;
    }
    if (lineSeparator == null) {
      lineSeparator = "\n";
    }

    IImportValidator validator = new ImportValidator(new ImportCollector(env));
    StringBuilder sourceBuilder = new StringBuilder();
    srcBuilder.createSource(sourceBuilder, lineSeparator, context, validator);
    return sourceBuilder.toString();
  }

  /**
   * Returns the given input HTML with all characters escaped.
   *
   * @param html
   *          The input HTML.
   * @return The escaped version.
   */
  public static String escapeHtml(String html) {
    if (StringUtils.isBlank(html)) {
      return html;
    }
    return StringUtils.replaceEach(html, new String[]{
        "&", "<", ">", "\"", "/", "'"
    }, new String[]{
        "&amp;", "&lt;", "&gt;", "&quot;", "&#47;", "&#39;"
    });
  }

  /**
   * Creates a new {@link DocumentBuilder} to create a DOM of an XML file.<br>
   * Use {@link DocumentBuilder#parse()} to create a new {@link Document}.
   *
   * @return The created builder. All external entities are disabled to prevent XXE.
   * @throws ParserConfigurationException
   *           if a {@link DocumentBuilder} cannot be created which satisfies the configuration requested.
   */
  public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Map<String, Boolean> features = new HashMap<>(5);
    features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
    features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
    features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
    features.put("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
    features.put(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
    dbf.setXIncludeAware(false);
    dbf.setExpandEntityReferences(false);

    for (Entry<String, Boolean> a : features.entrySet()) {
      String feature = a.getKey();
      boolean enabled = a.getValue().booleanValue();
      try {
        dbf.setFeature(feature, enabled);
      }
      catch (ParserConfigurationException e) {
        SdkLog.debug("Feature '{}' is not supported in the current XML parser. Skipping.", feature, e);
      }
    }
    return dbf.newDocumentBuilder();
  }
}
