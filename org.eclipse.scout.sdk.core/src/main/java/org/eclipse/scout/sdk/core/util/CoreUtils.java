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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.TypeNames;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IPropertyBean;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;

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
   * org.eclipse.scout.commons.SecurityUtility.generateKeyPair().</b>
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

  public static File writeTempFile(String prefix, String suffix, String content) throws IOException {
    File f = File.createTempFile(prefix, suffix);
    try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f), "UTF-8")) {
      out.write(content);
    }
    return f;
  }

  public static String readTempFile(File f) throws IOException {
    try (FileInputStream in = new FileInputStream(f)) {
      return inputStreamToString(in, "UTF-8").toString();
    }
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

    String result = s.substring(1, len - 1);
    for (Entry<Character, String> a : getLiteralEscapeMap().entrySet()) {
      result = result.replace(a.getValue(), a.getKey().toString());
    }

    return result;
  }

  private static Map<Character, String> getLiteralEscapeMap() {
    Map<Character, String> escapeMap = new HashMap<>(15);
    escapeMap.put(Character.valueOf('\b'), "\\b");
    escapeMap.put(Character.valueOf('\t'), "\\t");
    escapeMap.put(Character.valueOf('\n'), "\\n");
    escapeMap.put(Character.valueOf('\f'), "\\f");
    escapeMap.put(Character.valueOf('\r'), "\\r");
    escapeMap.put(Character.valueOf('"'), "\\\"");
    escapeMap.put(Character.valueOf('\\'), "\\\\");
    escapeMap.put(Character.valueOf('\0'), "\\0");
    escapeMap.put(Character.valueOf('\1'), "\\1");
    escapeMap.put(Character.valueOf('\2'), "\\2");
    escapeMap.put(Character.valueOf('\3'), "\\3");
    escapeMap.put(Character.valueOf('\4'), "\\4");
    escapeMap.put(Character.valueOf('\5'), "\\5");
    escapeMap.put(Character.valueOf('\6'), "\\6");
    escapeMap.put(Character.valueOf('\7'), "\\7");
    return escapeMap;
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

    int len = s.length();
    Map<Character, String> literalEscapeMap = getLiteralEscapeMap();
    StringBuilder b = new StringBuilder(len * 2);
    b.append('"'); // opening delimiter
    for (int i = 0; i < len; i++) {
      Character c = Character.valueOf(s.charAt(i));
      String replacement = literalEscapeMap.get(c);
      if (replacement != null) {
        b.append(replacement);
      }
      else {
        b.append(c.charValue());
      }
    }
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
      builder.append("[" + username + "] ");
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
   * Gets the default value for the given signature data type.
   *
   * @param parameter
   *          The signature data type for which the default return value should be returned.
   * @return A {@link String} or <code>null</code> (for the void signature) holding the default value for the given data
   *         type.
   */
  public static String getDefaultValueOf(String parameter) {
    if (parameter.length() != 1) {
      // not a primitive type
      return "null";
    }

    switch (parameter.charAt(0)) {
      case ISignatureConstants.C_BOOLEAN:
        return "true";
      case ISignatureConstants.C_BYTE:
      case ISignatureConstants.C_CHAR:
      case ISignatureConstants.C_DOUBLE:
      case ISignatureConstants.C_INT:
      case ISignatureConstants.C_LONG:
      case ISignatureConstants.C_SHORT:
        return "0";
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
   * Sets the user name that should be returned by {@link ScoutUtility#getUsername()} for the current thread.
   *
   * @param newUsernameForCurrentThread
   *          the new user name
   */
  public static void setUsernameForThread(String newUsernameForCurrentThread) {
    CURRENT_USER_NAME.set(newUsernameForCurrentThread);
  }

  /**
   * Finds the first element of the given {@link Collections} for which the given {@link IFilter} evaluates to
   * <code>true</code>.
   *
   * @param collection
   *          The collection to search in.
   * @param filter
   *          The filter or <code>null</code> if the first element should be returned.
   * @return The first element that matches the given filter or <code>null</code> if the given collection is
   *         <code>null</code> or no element is accepted by the given filter.
   */
  public static <E> E findFirst(Collection<? extends E> collection, IFilter<E> filter) {//TODO imo refactor all these to use new fluent api on type and method
    if (collection == null || collection.isEmpty()) {
      return null;
    }
    if (filter == null) {
      return collection.iterator().next();
    }

    for (E element : collection) {
      if (filter.evaluate(element)) {
        return element;
      }
    }
    return null;
  }

  /**
   * Selects all items from the source {@link Collection} for which the given {@link IFilter} evaluates to
   * <code>true</code> and inserts them into the given result {@link Collection}.
   *
   * @param source
   *          The source {@link Collection}
   * @param filter
   *          The {@link IFilter} to decide which elements to select. If the filter is <code>null</code> all elements
   *          are copied into the resulting {@link Collection}.
   * @param result
   *          The {@link Collection} holding the resulting elements
   */
  public static <E> void filter(Collection<? extends E> source, IFilter<E> filter, Collection<E> result) {
    if (source == null) {
      return;
    }
    if (result == null) {
      return;
    }
    if (filter == null) {
      result.addAll(source);
      return;
    }

    for (E element : source) {
      if (filter.evaluate(element)) {
        result.add(element);
      }
    }
  }

  /**
   * Gets the first direct member {@link IType} of the given declaring type which matches the given {@link IFilter}.
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param filter
   *          The {@link IFilter} choosing the member {@link IType}.
   * @return The first inner {@link IType} or <code>null</code> if it cannot be found.
   */
  public static IType getInnerType(IType declaringType, IFilter<IType> filter) {
    if (declaringType == null) {
      return null;
    }
    return findFirst(declaringType.getTypes(), filter);
  }

  /**
   * Gets the first direct member {@link IType} with the given simple name.
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param typeName
   *          The simple name of the member {@link IType} to search.
   * @return The member {@link IType} with the given name or <code>null</code> if it cannot be found.
   */
  public static IType getInnerType(IType declaringType, String typeName) {
    return getInnerType(declaringType, TypeFilters.simpleName(typeName));
  }

  /**
   * Gets the immediate member {@link IType}s of the given {@link IType} which matches the given {@link IFilter} in the
   * order as it is defined in the source or class file.
   *
   * @param type
   *          The declaring {@link IType} holding the member {@link IType}s.
   * @param filter
   *          The {@link IFilter} to filter the member {@link IType}s.
   * @return A {@link List} holding the selected member {@link IType}.
   */
  public static List<IType> getInnerTypes(IType type, IFilter<IType> filter) {
    return getInnerTypes(type, filter, null);
  }

  /**
   * Returns the immediate member types declared by the given type. The results is filtered using the given
   * {@link IFilter} and sorted using the given {@link Comparator}.
   *
   * @param type
   *          The type whose immediate inner types should be returned.
   * @param filter
   *          the filter to apply or null
   * @param comparator
   *          the comparator to sort the result or null
   * @return the immediate inner types declared in the given type.
   */
  public static List<IType> getInnerTypes(IType type, IFilter<IType> filter, Comparator<IType> comparator) {
    List<IType> types = type.getTypes();

    List<IType> l = new ArrayList<>(types.size());
    filter(types, filter, l);
    if (comparator != null && !l.isEmpty()) {
      Collections.sort(l, comparator);
    }
    return l;
  }

  /**
   * Searches for an {@link IType} with given simple name within the given type recursively checking all inner types.
   * The given {@link IType} itself is checked as well.
   *
   * @param type
   *          The {@link IType} to start searching. All nested inner {@link IType}s are visited recursively.
   * @param innerTypeName
   *          The simple name (case sensitive) to search for.
   * @return The first {@link IType} found in the nested {@link IType} tree below the given start type that has the
   *         given simple name or <code>null</code> if nothing could be found.
   */
  public static IType findInnerType(IType type, String innerTypeName) {
    if (type == null) {
      return null;
    }
    else if (Objects.equals(type.getSimpleName(), innerTypeName)) {
      return type;
    }
    else {
      for (IType innerType : type.getTypes()) {
        IType found = findInnerType(innerType, innerTypeName);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  /**
   * Gets all interfaces implemented by the given {@link IType} (recursively checking the super hierarchy).
   *
   * @param type
   *          The {@link IType} for which all interfaces should be returned.
   * @return A {@link Set} holding all super interfaces of the given {@link IType}.
   */
  public static Set<IType> getAllSuperInterfaces(IType type) {
    Set<IType> collector = new HashSet<>();
    for (IType t : type.getSuperInterfaces()) {
      getAllSuperInterfaces(t, collector);
    }
    return collector;
  }

  private static void getAllSuperInterfaces(IType t, Set<IType> collector) {
    if (t == null) {
      return;
    }
    if (Flags.isInterface(t.getFlags())) {
      collector.add(t);
    }
    getAllSuperInterfaces(t.getSuperClass(), collector);
    for (IType superIfc : t.getSuperInterfaces()) {
      getAllSuperInterfaces(superIfc, collector);
    }
  }

  /**
   * <code>null</code> safe check for parameterized types.
   *
   * @param type
   *          The {@link IType} to check or <code>null</code>.
   * @return <code>true</code> if the given {@link IType} is not <code>null</code> and has at least one type parameter.
   *         <code>false</code> otherwise.
   */
  public static boolean isGenericType(IType type) {
    if (type == null) {
      return false;
    }
    return type.hasTypeParameters();
  }

  /**
   * Gets the first {@link IField} with given name.
   * <p>
   * The static { } section is the field with an empty name
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param fieldName
   *          The field name.
   * @return The {@link IField} with given name or <code>null</code> if it could not be found.
   */
  public static IField getField(IType declaringType, String fieldName) {
    if (declaringType == null) {
      return null;
    }
    return findFirst(declaringType.getFields(), FieldFilters.name(fieldName));
  }

  /**
   * Gets all {@link IField} of the given declaring {@link IType} that matches the given {@link IFilter} in the order as
   * they appear in the source or class file.
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param filter
   *          The {@link IFilter} selecting the {@link IField}s.
   * @return A {@link List} holding the {@link IField}s accepted by the {@link IFilter}.
   */
  public static List<IField> getFields(IType declaringType, IFilter<IField> filter) {
    return getFields(declaringType, filter, null);
  }

  /**
   * Gets all {@link IField}s of the given declaring {@link IType} that matches the given {@link IFilter} sorted by the
   * given {@link Comparator}.
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param filter
   *          The {@link IFilter} selecting the {@link IField}s.
   * @param comparator
   *          The {@link Comparator} to sort the {@link IField}s.
   * @return A {@link List} holding the {@link IField}s accepted by the {@link IFilter} sorted by the given
   *         {@link Comparator}.
   */
  public static List<IField> getFields(IType declaringType, IFilter<IField> filter, Comparator<IField> comparator) {
    List<IField> fields = declaringType.getFields();

    List<IField> l = new ArrayList<>(fields.size());
    filter(fields, filter, l);
    if (comparator != null && !l.isEmpty()) {
      Collections.sort(l, comparator);
    }
    return l;
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
   * @return A {@link List} holding all type arguments of the given type parameter or <code>null</code> if the given
   *         levelFqn could not be found in the super hierarchy.
   * @see #getResolvedTypeParamValueSignature(IType, String, int)
   */
  public static List<IType> getResolvedTypeParamValue(IType focusType, String levelFqn, int typeParamIndex) {
    IType levelType = findSuperType(focusType, levelFqn);
    if (levelType == null) {
      return null;
    }
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
   * @return A {@link List} holding all type arguments of the given type parameter.
   */
  public static List<IType> getResolvedTypeParamValue(IType focusType, IType levelType, int typeParamIndex) {
    if (levelType == null) {
      return null;
    }
    IType item = levelType.getTypeArguments().get(typeParamIndex);
    if (!item.isAnonymous()) {
      // direct bind
      return Arrays.asList(item);
    }

    IType superClassGeneric = item.getSuperClass();
    List<IType> superIfcGenerics = item.getSuperInterfaces();
    List<IType> result = null;
    if (superClassGeneric != null) {
      result = new ArrayList<>(superIfcGenerics.size() + 1);
      result.add(superClassGeneric);
    }
    else {
      result = new ArrayList<>(superIfcGenerics.size());
    }

    for (IType ifcGeneric : superIfcGenerics) {
      result.add(ifcGeneric);
    }

    return result;
  }

  /**
   * Collects inner {@link IType} matching the given {@link IFilter} checking the entire super hierarchy of the given
   * {@link IType}. Stops when {@link IFilter#evaluate(Object)} returns false.
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

    IType innerType = getInnerType(declaringType, filter);
    if (innerType != null) {
      return innerType;
    }
    return findInnerTypeInSuperHierarchy(declaringType.getSuperClass(), filter);
  }

  /**
   * Collects {@link IType} in the super hierarchy of the given {@link IType} matching the given {@link IFilter}. This
   * includes the startType. Stops when {@link IFilter#evaluate(Object)} returns false.
   *
   * @param startType
   *          The start {@link IType}.
   * @param filter
   *          The {@link IFilter} to select the {@link IType}.
   * @return The first {@link IType} on which {@link IFilter#evaluate(Object)} returns true
   */
  public static IType findTypeInSuperHierarchy(IType startType, IFilter<IType> filter) {
    if (startType == null) {
      return null;
    }

    IType type = filter.evaluate(startType) ? startType : null;
    if (type != null) {
      return type;
    }

    type = findTypeInSuperHierarchy(startType.getSuperClass(), filter);
    if (type != null) {
      return type;
    }
    return null;
  }

  /**
   * Collects {@link IMethod} in the super hierarchy of the given {@link IType} matching the given {@link IFilter}.
   * Stops when {@link IFilter#evaluate(Object)} returns false.
   *
   * @param startType
   *          The start {@link IType}.
   * @param filter
   *          The {@link IFilter} to select the {@link IMethod}.
   * @return The first {@link IMethod} on which {@link IFilter#evaluate(Object)} returns true
   */
  public static IMethod findMethodInSuperHierarchy(IType startType, IFilter<IMethod> filter) {
    if (startType == null) {
      return null;
    }

    IMethod method = getMethod(startType, filter);
    if (method != null) {
      return method;
    }

    method = findMethodInSuperHierarchy(startType.getSuperClass(), filter);
    if (method != null) {
      return method;
    }

    for (IType ifc : startType.getSuperInterfaces()) {
      method = findMethodInSuperHierarchy(ifc, filter);
      if (method != null) {
        return method;
      }
    }
    return null;
  }

  /**
   * Collects {@link IAnnotation} on type level in the super hierarchy of the given {@link IType} matching the given
   * {@link IFilter}. Stops when {@link IFilter#evaluate(Object)} returns false.
   *
   * @param startType
   *          The start {@link IType}.
   * @param filter
   *          The {@link IFilter} to select the {@link IAnnotation}.
   * @return The first {@link IAnnotation} on which {@link IFilter#evaluate(Object)} returns true
   */
  public static IAnnotation findTypeAnnotationInSuperHierarchy(IType startType, IFilter<IAnnotation> filter) {
    if (startType == null) {
      return null;
    }

    IAnnotation annotation = getAnnotation(startType, filter);
    if (annotation != null) {
      return annotation;
    }

    annotation = findTypeAnnotationInSuperHierarchy(startType.getSuperClass(), filter);
    if (annotation != null) {
      return annotation;
    }

    for (IType ifc : startType.getSuperInterfaces()) {
      annotation = findTypeAnnotationInSuperHierarchy(ifc, filter);
      if (annotation != null) {
        return annotation;
      }
    }
    return null;
  }

  /**
   * Collects {@link IAnnotation} on methods in the super hierarchy of the given {@link IType} matching the given
   * {@link IFilter}. Only methods that matche the methodFilter are visited. Stops when {@link IFilter#evaluate(Object)}
   * returns true.
   *
   * @param startType
   *          The start {@link IType}.
   * @param filter
   *          The {@link IFilter} to select the {@link IAnnotation}.
   * @return The first {@link IAnnotation} on which {@link IFilter#evaluate(Object)} returns true
   */
  public static IAnnotation findMethodAnnotationInSuperHierarchy(IType startType, IFilter<IMethod> methodFilter, IFilter<IAnnotation> annotationFilter) {
    if (startType == null) {
      return null;
    }

    IMethod m = getMethod(startType, methodFilter);

    IAnnotation annotation = getAnnotation(m, annotationFilter);
    if (annotation != null) {
      return annotation;
    }

    annotation = findMethodAnnotationInSuperHierarchy(startType.getSuperClass(), methodFilter, annotationFilter);
    if (annotation != null) {
      return annotation;
    }

    for (IType ifc : startType.getSuperInterfaces()) {
      annotation = findMethodAnnotationInSuperHierarchy(ifc, methodFilter, annotationFilter);
      if (annotation != null) {
        return annotation;
      }
    }
    return null;
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
    IFilter<IMethod> filter = Filters.and(MethodFilters.flags(Flags.AccPublic), MethodFilters.nameRegex(BEAN_METHOD_NAME));
    List<IMethod> methods = getMethods(type, filter);
    Map<String, PropertyBean> beans = new HashMap<>(methods.size());
    for (IMethod m : methods) {
      Matcher matcher = BEAN_METHOD_NAME.matcher(m.getElementName());
      if (matcher.matches()) {
        String kind = matcher.group(1);
        String name = matcher.group(2);

        List<IMethodParameter> parameterTypes = m.getParameters();
        IType returnType = m.getReturnType();
        if ("get".equals(kind) && parameterTypes.size() == 0 && !returnType.isVoid()) {
          PropertyBean desc = beans.get(name);
          if (desc == null) {
            desc = new PropertyBean(type, name);
            beans.put(name, desc);
          }
          if (desc.getReadMethod() == null) {
            desc.setReadMethod(m);
          }
        }
        else {
          boolean isBool = TypeNames.java_lang_Boolean.equals(returnType.getName()) || TypeNames._boolean.equals(returnType.getName());
          if ("is".equals(kind) && parameterTypes.size() == 0 && isBool) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.getReadMethod() == null) {
              desc.setReadMethod(m);
            }
          }
          else if ("set".equals(kind) && parameterTypes.size() == 1 && returnType.isVoid()) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.getWriteMethod() == null) {
              desc.setWriteMethod(m);
            }
          }
        }
      }
    }

    // filter
    List<IPropertyBean> l = new ArrayList<>(beans.size());
    filter(beans.values(), propertyFilter, l);

    if (comparator != null && !l.isEmpty()) {
      Collections.sort(l, comparator);
    }
    return l;
  }

  /**
   * Gets the first {@link IAnnotation} on the given {@link IAnnotatable} having the given name.
   *
   * @param annotatable
   *          The {@link IAnnotation} holder.
   * @param name
   *          Simple or fully qualified name of the annotation type.
   * @return The first {@link IAnnotation} on the given {@link IAnnotatable} having the given name or <code>null</code>
   *         if it could not be found.
   */
  public static IAnnotation getAnnotation(IAnnotatable annotatable, String name) {
    if (annotatable == null || name == null) {
      return null;
    }
    List<? extends IAnnotation> candidates = annotatable.getAnnotations();
    if (candidates.size() == 0) {
      return null;
    }
    String simpleName = Signature.getSimpleName(name);
    for (IAnnotation candidate : candidates) {
      if (name.equals(candidate.getType().getName()) || simpleName.equals(candidate.getType().getSimpleName())) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Gets the first {@link IAnnotation} which is directly in the given {@link IAnnotatable} and accepts the given
   * {@link IFilter} .
   *
   * @param element
   *          The {@link IAnnotatable} to search in.
   * @param filter
   *          The {@link IFilter} to select the {@link IAnnotation}.
   * @return The first {@link IAnnotation} or <code>null</code>.
   */
  public static IAnnotation getAnnotation(IAnnotatable element, IFilter<IAnnotation> filter) {
    if (element == null) {
      return null;
    }
    return findFirst(element.getAnnotations(), filter);
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
    return isOnClasspath(env, typeToSearch.getName());
  }

  /**
   * Finds the super {@link IType} of the given start {@link IType} having the given name.
   *
   * @param typeToCheck
   *          The start {@link IType}
   * @param queryType
   *          The fully qualified name of the super {@link IType} to find.
   * @return The {@link IType} having the given name if found in the super hierarchy of the given {@link IType} or
   *         <code>null</code> if it could not be found.
   */
  public static IType findSuperType(IType typeToCheck, String queryType) {
    if (queryType == null) {
      return null;
    }
    if (typeToCheck == null) {
      return null;
    }

    if (queryType.equals(typeToCheck.getName())) {
      return typeToCheck;
    }

    IType result = findSuperType(typeToCheck.getSuperClass(), queryType);
    if (result != null) {
      return result;
    }

    for (IType superInterface : typeToCheck.getSuperInterfaces()) {
      result = findSuperType(superInterface, queryType);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * Checks if the given {@link IType} has the given queryType in its super hierarchy.
   *
   * @param typeToCheck
   *          The {@link IType} to check.
   * @param queryType
   *          The fully qualified name of the super type to check.
   */
  public static boolean isInstanceOf(IType typeToCheck, String queryType) {
    return findSuperType(typeToCheck, queryType) != null;
  }

  private static final Pattern PRIMITIVE_TYPE_ASSIGNABLE_PAT =
      Pattern.compile("("
          + "char=(char|Character)|byte=(byte|Byte)|short=(short|Short)|int=(int|Integer)|long=(long|Long)"
          + "|float=(float|Float)|double=(double|Double)|Character=(char|Character)|Byte=(byte|Byte)"
          + "|Short=(short|Short)|Integer=(int|Integer)|Long=(long|Long)|Float=(float|Float)|Double=(double|Double)"
          + ")");

  /**
   * see {@link Class#isAssignableFrom(Class)}
   *
   * @return true if a declaration BaseClass b = (SpecificClass)s; is valid
   */
  public static boolean isAssignableFrom(IType baseClass, IType specificClass) {
    if ((baseClass.isPrimitive() || specificClass.isPrimitive())) {
      return PRIMITIVE_TYPE_ASSIGNABLE_PAT.matcher(baseClass.getSimpleName() + "=" + specificClass.getSimpleName()).matches();
    }
    if ((baseClass.isArray() || specificClass.isArray())) {
      return baseClass.getName().equals(specificClass.getName());
    }
    return findSuperType(baseClass, specificClass.getName()) != null;
  }

  /**
   * Gets the first {@link IMethod} which is directly in the given {@link IType} and accepts the given {@link IFilter} .
   *
   * @param type
   *          The {@link IType} to search in.
   * @param filter
   *          The {@link IFilter} to select the {@link IMethod}.
   * @return The first {@link IMethod} or <code>null</code>.
   */
  public static IMethod getMethod(IType type, IFilter<IMethod> filter) {
    if (type == null) {
      return null;
    }
    return findFirst(type.getMethods(), filter);
  }

  /**
   * Searches and returns the first method with the given name in the given type.<br>
   * If multiple methods with the same name exist (overloads), the returned method is undefined.
   *
   * @param type
   *          The type in which the method should be searched.
   * @param methodName
   *          The name of the method.
   * @return The first method found or null.
   */
  public static IMethod getMethod(IType type, final String methodName) {
    return getMethod(type, MethodFilters.name(methodName));
  }

  /**
   * Gets all methods in the given type.<br>
   * The methods are in no particular order.
   *
   * @param type
   *          The type to get all methods of.
   * @return A {@link Set} of all methods of the given type. Never returns null.
   */
  public static List<IMethod> getMethods(IType type) {
    return getMethods(type, null);
  }

  /**
   * Gets all methods in the given type that match the given filter.<br>
   * The methods are in no particular order.
   *
   * @param type
   *          The type to get all methods of.
   * @param filter
   *          The filter.
   * @return A {@link Set} of all methods of the given type matching the given filter. Never returns null.
   */
  public static List<IMethod> getMethods(IType type, IFilter<IMethod> filter) {
    return getMethods(type, filter, null);
  }

  /**
   * Gets all methods in the given type (no methods of inner types) that match the given filter ordered by the given
   * comparator.<br>
   * If the given comparator is null, the order of the methods is undefined.
   *
   * @param type
   *          The type to get all methods of.
   * @param filter
   *          The filter to use or null for no filtering.
   * @param comparator
   *          The comparator to use or null to get the methods in undefined order.
   * @return an {@link Set} of all methods of the given type matching the given filter. Never returns null.
   */
  public static List<IMethod> getMethods(IType type, IFilter<IMethod> filter, Comparator<IMethod> comparator) {
    List<IMethod> methods = type.getMethods();
    List<IMethod> l = new ArrayList<>(methods.size());
    filter(methods, filter, l);
    if (comparator != null && !l.isEmpty()) {
      Collections.sort(l, comparator);
    }
    return l;
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
      tmp = tmp.getDeclaringType();
    }

    return result;
  }

  /**
   * Gets the value of the given attribute in the given {@link IAnnotation} as a {@link String}.
   *
   * @param annotation
   *          The {@link IAnnotation} in which the attribute should be searched.
   * @param name
   *          The name of the attribute.
   * @return The value of the attribute with given name in the given {@link IAnnotation} (the default value is ignored!)
   *         or <code>null</code> if there is not such attribute or no value.
   */
//TODO mvi, imo maybe fix here : callers of annotation values should use {@link IAnnotationValue#isSyntheticDefaultValue()}
  public static String getAnnotationValueString(IAnnotation annotation, String name) {
    if (annotation == null) {
      return null;
    }

    IAnnotationValue value = annotation.getValue(name);
    if (value == null || value.isSyntheticDefaultValue()) {
      return null;
    }

    Object rawVal = value.getMetaValue().getObject(Object.class);
    if (rawVal == null) {
      return null;
    }
    //enum
    if (rawVal instanceof IField) {
      return ((IField) rawVal).getElementName();
    }

    return rawVal.toString();
  }

  /**
   * Gets the value of the given attribute in the given {@link IAnnotation} as a {@link BigDecimal}.
   *
   * @param annotation
   *          The {@link IAnnotation} in which the attribute should be searched.
   * @param name
   *          The name of the attribute.
   * @return The value of the attribute with given name in the given {@link IAnnotation} (the default value is ignored)
   *         or <code>null</code> if there is not such attribute or no value or it is not numeric.
   */
//TODO mvi, imo maybe fix here : callers of annotation values should use {@link IAnnotationValue#isSyntheticDefaultValue()}
  public static BigDecimal getAnnotationValueNumeric(IAnnotation annotation, String name) {
    if (annotation == null) {
      return null;
    }

    IAnnotationValue value = annotation.getValue(name);
    if (value == null || value.isSyntheticDefaultValue()) {
      return null;
    }

    Object rawVal = value.getMetaValue().getObject(Object.class);
    if (rawVal == null) {
      return null;
    }

    if (rawVal instanceof Integer || rawVal instanceof Byte || rawVal instanceof Short) {
      return new BigDecimal(((Number) rawVal).intValue());
    }
    if (rawVal instanceof Long) {
      return new BigDecimal(((Long) rawVal).longValue());
    }
    if (rawVal instanceof Float || rawVal instanceof Double) {
      return new BigDecimal(((Number) rawVal).doubleValue());
    }
    return null;
  }

  public static void exportJavaEnvironment(IJavaEnvironment env, Writer w) throws IOException {
    StringBuilder src = new StringBuilder();
    StringBuilder bin = new StringBuilder();
    for (ClasspathSpi cp : env.unwrap().getClasspath()) {
      (cp.isSource() ? src : bin).append("\n    " + cp.getPath() + ",");
    }
    Properties p = new Properties();
    p.setProperty("src", src.toString());
    p.setProperty("bin", bin.toString());
    p.store(w, "");
  }

  public static IJavaEnvironment importJavaEnvironment(InputStream in) throws IOException {
    Properties p = new Properties();
    p.load(in);
    return importJavaEnvironment(p);
  }

  public static IJavaEnvironment importJavaEnvironment(Reader r) throws IOException {
    Properties p = new Properties();
    p.load(r);
    return importJavaEnvironment(p);
  }

  /**
   * @param p
   *
   *          <pre>
   *  allowErrors=true,
   * src=path1, path2, ...
   * bin=path1, path2, ...
   *          </pre>
   *
   * @return
   */
  public static IJavaEnvironment importJavaEnvironment(Properties p) {
    JavaEnvironmentBuilder builder = new JavaEnvironmentBuilder()
        .withIncludeRunningClasspath(false);
    for (String s : p.getProperty("src").split(",")) {
      s = s.trim();
      if (!s.isEmpty()) {
        builder.withAbsoluteSourcePath(s);
      }
    }
    for (String s : p.getProperty("bin").split(",")) {
      s = s.trim();
      if (!s.isEmpty()) {
        builder.withAbsoluteBinaryPath(s);
      }
    }
    return builder.build();
  }
}
