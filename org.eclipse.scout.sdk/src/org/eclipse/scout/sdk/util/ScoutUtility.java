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
package org.eclipse.scout.sdk.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.method.IMethodReturnValueParser;
import org.eclipse.scout.sdk.util.method.MethodReturnExpression;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraphVisitor;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>ScoutUtility</h3>Contains scout specific utility methods.
 */
public final class ScoutUtility {

  public final static String JAVA_MARKER = "java ";
  private final static Pattern REGEX_LINE_SEP_CLEAN = Pattern.compile("(\\n)\\r");
  private final static Pattern REGEX_LINE_SEP_CLEAN2 = Pattern.compile("\\r(\\n)");
  private final static Pattern REGEX_PACKAGE_NAME = Pattern.compile("^[0-9a-zA-Z\\.\\_]*$");
  private final static Pattern REGEX_PACKAGE_NAME_START = Pattern.compile("[a-zA-Z]{1}.*$");
  private final static Pattern REGEX_PACKAGE_NAME_END = Pattern.compile("^.*[a-zA-Z]{1}$");
  private final static Pattern REGEX_CONTAINS_UPPER_CASE = Pattern.compile(".*[A-Z].*");
  private final static Object LOCK = new Object();
  private final static ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<String>();

  private static Set<String> javaKeyWords = null;

  private ScoutUtility() {
  }

  public static String getCommentBlock(String content) {
    StringBuilder builder = new StringBuilder();
    builder.append("//TODO ");
    String username = getUsername();
    if (!StringUtility.isNullOrEmpty(username)) {
      builder.append("[" + username + "] ");
    }
    builder.append(content);
    return builder.toString();
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

  public static String getCommentAutoGeneratedMethodStub() {
    return getCommentBlock("Auto-generated method stub.");
  }

  /**
   * strips a (IMethod) method body from its comments
   * this is needed in order to avoid wrong method property
   * assignments.
   * 
   * @param methodBody
   * @return
   */
  public static String removeComments(String methodBody) {
    if (methodBody == null) {
      return null;
    }
    String retVal = methodBody;
    try {
      retVal = methodBody.replaceAll("\\/\\/.*?\\\r\\\n", "");
      retVal = retVal.replaceAll("\\/\\/.*?\\\n", "");
      retVal = retVal.replaceAll("(?s)\\/\\*.*?\\*\\/", "");
    }
    catch (Throwable t) {
      // nop
    }
    return retVal;
  }

  public static String cleanLineSeparator(String buffer, ICompilationUnit icu) {
    return cleanLineSeparator(buffer, ResourceUtility.getLineSeparator(icu));
  }

  public static String cleanLineSeparator(String buffer, Document doc) {
    return cleanLineSeparator(buffer, ResourceUtility.getLineSeparator(doc));
  }

  public static String cleanLineSeparator(String buffer, String separator) {
    buffer = REGEX_LINE_SEP_CLEAN.matcher(buffer).replaceAll("$1");
    buffer = REGEX_LINE_SEP_CLEAN2.matcher(buffer).replaceAll("$1");
    // max 1 newline at the end
    buffer = buffer.replaceAll("\\n+$", "\n");
    return buffer.replaceAll("\\n", separator);
  }

  public static String getIndent(IType type) {
    StringBuilder indent = new StringBuilder("");
    if (type.getDeclaringType() != null) {
      IType decType = type.getDeclaringType();
      while (decType != null) {
        decType = decType.getDeclaringType();
        indent.append(SdkProperties.TAB);
      }
    }
    return indent.toString();
  }

  public static void registerServiceClass(String extensionPoint, String elemType, String serviceClass, ServiceRegistrationDescription desc) throws CoreException {
    PluginModelHelper h = new PluginModelHelper(desc.targetProject.getProject());
    HashMap<String, String> attributes = new HashMap<String, String>(3);
    attributes.put("class", serviceClass);
    if (desc.session != null) {
      attributes.put("session", desc.session);
    }
    if (desc.serviceFactory != null) {
      attributes.put("factory", desc.serviceFactory);
    }
    if (!h.PluginXml.existsSimpleExtension(extensionPoint, elemType, attributes)) {
      h.PluginXml.addSimpleExtension(extensionPoint, elemType, attributes);
      h.save();
    }
  }

  public static void unregisterServiceProxy(IType interfaceType) throws CoreException {
    IScoutBundle interfaceBundle = ScoutTypeUtility.getScoutBundle(interfaceType);
    for (IScoutBundle clientBundle : interfaceBundle.getChildBundles(
        ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT)), true)) {
      unregisterServiceClass(clientBundle.getProject(), IRuntimeClasses.EXTENSION_POINT_CLIENT_SERVICE_PROXIES, IRuntimeClasses.EXTENSION_ELEMENT_CLIENT_SERVICE_PROXY, interfaceType.getFullyQualifiedName());
    }
  }

  public static void unregisterServiceImplementation(IType serviceType) throws CoreException {
    IScoutBundle implementationBundle = ScoutTypeUtility.getScoutBundle(serviceType);
    for (IScoutBundle serverBundle : implementationBundle.getChildBundles(
        ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER)), true)) {
      unregisterServiceClass(serverBundle.getProject(), IRuntimeClasses.EXTENSION_POINT_SERVICES, IRuntimeClasses.EXTENSION_ELEMENT_SERVICE, serviceType.getFullyQualifiedName());
    }
  }

  public static void unregisterServiceClass(IProject project, String extensionPoint, String elemType, String className) throws CoreException {
    PluginModelHelper h = new PluginModelHelper(project);
    HashMap<String, String> attributes = new HashMap<String, String>(1);
    attributes.put("class", className);
    h.PluginXml.removeSimpleExtension(extensionPoint, elemType, attributes);
    h.save();
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

  public static String getDefaultValueOf(String parameter) {
    if (parameter.length() == 1) {
      switch (parameter.charAt(0)) {
        case Signature.C_BOOLEAN:
          return "true";
        case Signature.C_BYTE:
          return "0";
        case Signature.C_CHAR:
          return "0";
        case Signature.C_DOUBLE:
          return "0";
        case Signature.C_FLOAT:
          return "0.0f";
        case Signature.C_INT:
          return "0";
        case Signature.C_LONG:
          return "0";
        case Signature.C_SHORT:
          return "0";
        case Signature.C_VOID:
          return null;
      }
    }
    return "null";
  }

  /**
   * Returns true if the package fragment has class files in it.
   * 
   * @param packageFragment
   * @param includeSubpackages
   *          to include all sub packages
   * @return true is there are existing class files found.
   * @throws JavaModelException
   */
  public static boolean hasExistingChildren(IPackageFragment packageFragment, boolean includeSubpackages) throws JavaModelException {
    for (IJavaElement element : packageFragment.getChildren()) {
      if (element instanceof IPackageFragment && includeSubpackages) {
        return hasExistingChildren((IPackageFragment) element, includeSubpackages);
      }
      else if (element.exists()) {
        return true;
      }
    }
    return false;
  }

  /**
   * <xmp>
   * import xx.yy.B;
   * class A{
   * }
   * // getReferencedType(A, "B") returns the type B
   * </xmp>
   * 
   * @param declaringType
   * @param typeName
   * @return
   * @throws JavaModelException
   */
  public static IType getReferencedType(IType declaringType, String typeName) throws JavaModelException {
    String[][] resolvedTypeName = declaringType.resolveType(typeName);
    if (resolvedTypeName != null && resolvedTypeName.length == 1) {
      String fqName = resolvedTypeName[0][0];
      if (fqName != null && fqName.length() > 0) {
        fqName = fqName + ".";
      }
      fqName = fqName + resolvedTypeName[0][1];
      IType foundType = TypeUtility.getType(fqName);
      if (foundType != null) {
        return foundType;
      }
    }
    ScoutSdk.logWarning("could not find referenced type '" + typeName + "' in '" + declaringType.getFullyQualifiedName() + "'.");
    return null;
  }

  public static String getReferencedTypeSignature(IType declaringType, String simpleTypeName) throws JavaModelException {
    String[][] resolvedTypeName = declaringType.resolveType(simpleTypeName);
    if (resolvedTypeName == null) {
      return Signature.createTypeSignature(simpleTypeName, false);
    }
    else if (resolvedTypeName.length > 0) {
      StringBuilder fqnBuilder = new StringBuilder();
      if (!StringUtility.isNullOrEmpty(resolvedTypeName[0][0])) {
        fqnBuilder.append(resolvedTypeName[0][0] + ".");
      }
      fqnBuilder.append(resolvedTypeName[0][1]);
      return SignatureCache.createTypeSignature(fqnBuilder.toString());
    }
    return null;
  }

  public static String sourceCodeToSql(String source) {
    StringBuilder buf = new StringBuilder();
    StringBuilder outsideSqlCode = new StringBuilder();
    // meta levels
    boolean incomment1 = false;// /*...*/
    boolean incomment0 = false;// //...
    boolean instring = false;// "..."
    for (int i = 0; i < source.length(); i++) {
      char ch = source.charAt(i);
      if (ch == '\\') {
        buf.append(ch);
        buf.append(source.charAt(i + 1));
        i++;
      }
      else if ((!incomment1) && (ch == '/' && source.charAt(i + 1) == '*' && source.charAt(i + 2) != '+')) {
        // go into comment 1
        incomment1 = true;
        i++;
        buf.append("/**");
      }
      else if (incomment1 && (ch == '*' && source.charAt(i + 1) == '/')) {
        // go out of comment 1
        i++;
        incomment1 = false;
        buf.append("**/");
      }
      else if ((!incomment1) && (!incomment0) && (ch == '/' && source.charAt(i + 1) == '/')) {
        // go into comment 0
        incomment0 = true;
        i++;
        buf.append("/**");
        if (i + 1 >= source.length()) {
          incomment0 = false;// eot
          buf.append("**/");
        }
      }
      else if ((!incomment1) && (incomment0) && (ch == '\n' || ch == '\r' || i + 1 >= source.length())) {
        // go out of comment 0
        incomment0 = false;
        buf.append("**/");
        buf.append(ch);
      }
      else if ((!incomment1) && (!incomment0) && (!instring) && (ch == '"')) {
        // go into string
        instring = true;
      }
      else if ((!incomment1) && (!incomment0) && (instring) && (ch == '"')) {
        // go out of string
        instring = false;
      }
      else if (incomment1 || incomment0 || instring) {
        // inside meta
        buf.append(ch);
      }
      else if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
        // out of meta: white space
        buf.append(ch);
      }
      else if (ch == '+') {
        // out of meta: concatenation
        if (outsideSqlCode.length() > 0) {
          buf.append("*/");
          outsideSqlCode.setLength(0);
        }
      }
      else {
        // out of string/comment: java code
        if (outsideSqlCode.length() == 0) {
          buf.append("/* " + JAVA_MARKER);
        }
        outsideSqlCode.append(ch);
        buf.append(ch);
      }
    }
    if (outsideSqlCode.length() > 0) {
      buf.append("*/");
      outsideSqlCode.setLength(0);
    }
    return buf.toString();
  }

  public static String sqlToSourceCode(String sql) {
    // ignore empty lines
    sql = sql.replace("[\\n\\r]+", "\\n");
    // meta levels
    boolean incomment = false;// /**...**/
    StringBuilder buf = new StringBuilder();
    StringBuilder currentSqlLine = new StringBuilder();
    for (int i = 0; i < sql.length(); i++) {
      char ch = sql.charAt(i);
      if (ch == '\\') {
        if (incomment) {
          buf.append(ch);
          buf.append(sql.charAt(i + 1));
        }
        else {
          if (currentSqlLine.length() == 0) currentSqlLine.append("\"");
          currentSqlLine.append(ch);
          currentSqlLine.append(sql.charAt(i + 1));
        }
        i++;
      }
      else if ((!incomment) && (ch == '/' && sql.charAt(i + 1) == '*' && sql.charAt(i + 2) == '*')) {
        // go into comment
        incomment = true;
        i = i + 2;
        if (currentSqlLine.length() > 0) {
          String line = currentSqlLine.toString();
          buf.append(line);
          if (!line.endsWith(" ")) {
            buf.append(" ");
          }
          buf.append("\"+");
          currentSqlLine.setLength(0);
        }
        buf.append("/+++");
      }
      else if (incomment && (ch == '*' && sql.charAt(i + 1) == '*' && sql.charAt(i + 2) == '/')) {
        // go out of comment
        i = i + 2;
        incomment = false;
        buf.append("+++/");
      }
      else if (incomment) {
        // inside meta
        buf.append(ch);
      }
      else if (ch == '\r' || ch == '\n') {
        // out of meta: newline
        if (currentSqlLine.length() > 0) {
          String line = currentSqlLine.toString();
          buf.append(line);
          if (!line.endsWith(" ")) {
            buf.append(" ");
          }
          buf.append("\"+");
          currentSqlLine.setLength(0);
        }
        buf.append("\n");
      }
      else {
        // out of string/comment: sql code
        if (currentSqlLine.length() == 0) currentSqlLine.append("\"");
        currentSqlLine.append(ch);
      }
    }
    if (currentSqlLine.length() > 0) {
      String line = currentSqlLine.toString();
      buf.append(line);
      buf.append("\"");
      currentSqlLine.setLength(0);
    }
    String s = buf.toString();
    s = s.replaceAll("/\\*([^+*].*[^*])\\*/", "\"+$1+\"");
    s = s.replaceAll("/\\+\\+\\+", "/*");
    s = s.replaceAll("\\+\\+\\+/", "*/");
    return s;
  }

  public static String[] getSourceCodeLines(String source) {
    if (source == null) return new String[0];
    if (source.indexOf('\n') >= 0) {
      return source.replace("\r", "").split("[\\n]");
    }
    else {
      return source.replace("\n", "").split("[\\r]");
    }
  }

  public static int getSourceCodeIndent(String source, boolean includeFirstLine) {
    String[] a = getSourceCodeLines(source);
    int min = 102400;
    int count = 0;
    for (int i = 0; i < a.length; i++) {
      if (i > 0 || includeFirstLine) {
        String s = a[i];
        if (s.trim().length() > 0) {
          int index = 0;
          while (index < s.length() && (s.charAt(index) == ' ' || s.charAt(index) == '\t')) {
            index++;
          }
          min = Math.min(min, index);
          count++;
        }
      }
    }
    return (count > 0 ? min : 0);
  }

  public static String addSourceCodeIndent(String source, int indent, boolean includeFirstLine) {
    StringBuffer buf = new StringBuffer();
    String[] a = getSourceCodeLines(source);
    char[] prefix = new char[indent];
    Arrays.fill(prefix, ' ');
    for (int i = 0; i < a.length; i++) {
      if (i > 0 || includeFirstLine) {
        buf.append(prefix);
      }
      buf.append(a[i]);
      if (i + 1 < a.length) {
        buf.append('\n');
      }
    }
    return buf.toString();
  }

  /**
   * Gets the bundle type for the given product file. This is the type of first type-defining-bundle that is found in
   * the dependencies of the given project.
   * 
   * @param productFile
   *          The product file.
   * @return The type of the type-defining bundle with the lowest (first) order number. If no type-defining-bundle is in
   *         the dependencies of the given product, this method returns null. Types can be added using the
   *         <code>org.eclipse.scout.sdk.runtimeBundles</code> extension point. The predefined types are available in
   *         the {@link IScoutBundle}.TYPE* constants.
   * @throws CoreException
   */
  public static String getProductFileType(IFile productFile) throws CoreException {
    ProductFileModelHelper pfmh = new ProductFileModelHelper(productFile);
    String[] symbolicNames = pfmh.ProductFile.getPluginSymbolicNames();
    return RuntimeBundles.getBundleType(symbolicNames);
  }

  public static String removeSourceCodeIndent(String source, int indent) {
    StringBuffer buf = new StringBuffer();
    String[] a = getSourceCodeLines(source);
    for (int i = 0; i < a.length; i++) {
      String s = a[i];
      int index = 0;
      while (index < indent && index < s.length() && (s.charAt(index) == ' ' || s.charAt(index) == '\t')) {
        index++;
      }
      buf.append(s.substring(index));
      if (i + 1 < a.length) {
        buf.append('\n');
      }
    }
    return buf.toString();
  }

  public static IJavaProject getJavaProject(IScoutBundle bundle) {
    if (bundle != null) {
      return bundle.getJavaProject();
    }
    return null;
  }

  public static String[] getEntities(IScoutBundle p) throws JavaModelException {
    TreeSet<String> ret = new TreeSet<String>();
    IScoutBundle[] roots = p.getParentBundles(ScoutBundleFilters.getRootBundlesFilter(), true);
    IScoutBundleFilter workspaceClientSharedServerFilter = ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(),
        ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SERVER, IScoutBundle.TYPE_SHARED));
    for (IScoutBundle root : roots) {
      for (IScoutBundle b : root.getChildBundles(workspaceClientSharedServerFilter, true)) {
        for (IPackageFragmentRoot r : b.getJavaProject().getPackageFragmentRoots()) {
          String bundleName = null;
          if (r.isExternal()) {
            bundleName = r.getElementName();
            int versionPos = bundleName.indexOf('_');
            if (versionPos > 0) {
              bundleName = bundleName.substring(0, versionPos);
            }
            if (ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(bundleName) == null) {
              // not a scout bundle. we are not interested
              bundleName = null;
            }
          }
          else {
            // its me
            bundleName = b.getSymbolicName();
          }
          if (bundleName != null) {
            int bundleNameMin = bundleName.length() + 1;
            for (IJavaElement je : r.getChildren()) {
              if (je instanceof IPackageFragment) {
                String pckName = je.getElementName();
                if (pckName.startsWith(bundleName) && pckName.length() > bundleNameMin) {
                  ret.add(pckName.substring(bundleNameMin));
                }
              }
            }
          }
        }
      }
    }
    return ret.toArray(new String[ret.size()]);
  }

  public static String ensureStartWithUpperCase(String name) {
    StringBuilder builder = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(name)) {
      builder.append(Character.toUpperCase(name.charAt(0)));
      if (name.length() > 1) {
        builder.append(name.substring(1));
      }
    }
    return builder.toString();
  }

  public static String ensureStartWithLowerCase(String name) {
    StringBuilder builder = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(name)) {
      builder.append(Character.toLowerCase(name.charAt(0)));
      if (name.length() > 1) {
        builder.append(name.substring(1));
      }
    }
    return builder.toString();
  }

  public static String removeFieldSuffix(String fieldName) {
    if (fieldName.endsWith(SdkProperties.SUFFIX_FORM_FIELD)) {
      fieldName = fieldName.replaceAll(SdkProperties.SUFFIX_FORM_FIELD + "$", "");
    }
    else if (fieldName.endsWith(SdkProperties.SUFFIX_BUTTON)) {
      fieldName = fieldName.replaceAll(SdkProperties.SUFFIX_BUTTON + "$", "");
    }
    else if (fieldName.endsWith(SdkProperties.SUFFIX_TABLE_COLUMN)) {
      fieldName = fieldName.replaceAll(SdkProperties.SUFFIX_TABLE_COLUMN + "$", "");
    }
    else if (fieldName.endsWith(SdkProperties.SUFFIX_OUTLINE_PAGE)) {
      fieldName = fieldName.replaceAll(SdkProperties.SUFFIX_OUTLINE_PAGE + "$", "");
    }
    return fieldName;
  }

  public static String ensureValidParameterName(String parameterName) {
    if (isReservedJavaKeyword(parameterName)) {
      return parameterName + "Value";
    }
    return parameterName;
  }

  public static Set<String> getJavaKeyWords() {
    if (javaKeyWords == null) {
      synchronized (LOCK) {
        if (javaKeyWords == null) {
          String[] keyWords = new String[]{"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum",
              "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected",
              "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "false", "null", "true"};
          HashSet<String> tmp = new HashSet<String>(keyWords.length);
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
   * @return Returns <code>true</code> if the given word is a reserved java keyword. Otherwise <code>false</code>.
   * @throws NullPointerException
   *           if the given word is <code>null</code>.
   * @since 3.8.3
   */
  public static boolean isReservedJavaKeyword(String word) {
    return getJavaKeyWords().contains(word.toLowerCase());
  }

  public static IStatus validatePackageName(String pckName) {
    if (StringUtility.isNullOrEmpty(pckName)) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("DefaultPackageIsDiscouraged"));
    }
    // no double points
    if (pckName.contains("..")) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("PackageNameNotValid"));
    }
    // invalid characters
    if (!REGEX_PACKAGE_NAME.matcher(pckName).matches()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("PackageNameNotValid"));
    }
    // no start and end with number or special characters
    if (!REGEX_PACKAGE_NAME_START.matcher(pckName).matches() || !REGEX_PACKAGE_NAME_END.matcher(pckName).matches()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("PackageNameNotValid"));
    }
    // reserved java keywords
    String jkw = getContainingJavaKeyWord(pckName);
    if (jkw != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("PackageNotContainJavaKeyword", jkw));
    }
    // warn containing upper case characters
    if (REGEX_CONTAINS_UPPER_CASE.matcher(pckName).matches()) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("PackageOnlyLowerCase"));
    }
    return Status.OK_STATUS;
  }

  private static String getContainingJavaKeyWord(String s) {
    for (String keyWord : getJavaKeyWords()) {
      if (s.startsWith(keyWord + ".") || s.endsWith("." + keyWord) || s.contains("." + keyWord + ".")) {
        return keyWord;
      }
    }
    return null;
  }

  public static IStatus validateNewBundleName(String bundleName) {
    // validate name
    if (StringUtility.isNullOrEmpty(bundleName)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("ProjectNameMissing"));
    }
    // no double points
    if (bundleName.contains("..")) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("ProjectNameIsNotValid"));
    }
    // invalid characters
    if (!REGEX_PACKAGE_NAME.matcher(bundleName).matches()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("TheBundleNameContainsInvalidCharacters"));
    }
    // no start and end with number or special characters
    if (!REGEX_PACKAGE_NAME_START.matcher(bundleName).matches() || !REGEX_PACKAGE_NAME_END.matcher(bundleName).matches()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("BundleNameCanNotStartOrEndWithSpecialCharactersOrDigits"));
    }
    // reserved java keywords
    String jkw = getContainingJavaKeyWord(bundleName);
    if (jkw != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("TheProjectNameMayNotContainAReservedJavaKeyword", jkw));
    }
    // already existing bundle name
    if (Platform.getBundle(bundleName) != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("BundleAlreadyExists", bundleName));
    }
    if (ResourcesPlugin.getWorkspace().getRoot().getProject(bundleName).exists()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("BundleAlreadyExists", bundleName));
    }
    // warn containing upper case characters
    if (REGEX_CONTAINS_UPPER_CASE.matcher(bundleName).matches()) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("ProjectNameShouldContainOnlyLowerCaseCharacters"));
    }
    return Status.OK_STATUS;
  }

  public static String getMethodReturnValue(IMethod method, IImportValidator validator) {
    return getMethodReturnExpression(method).getReturnStatement(validator);
  }

  public static String getMethodReturnValue(IMethod method) {
    return getMethodReturnExpression(method).getReturnStatement();
  }

  public static MethodReturnExpression getMethodReturnExpression(IMethod method) {
    for (IMethodReturnValueParser parser : IMethodReturnValueParser.INSTANCES) {
      MethodReturnExpression returnExpression = parser.parse(method);
      if (returnExpression != null) {
        return returnExpression;
      }
    }
    return null; // no parser was able to calculate a value
  }

  public static INlsEntry getReturnNlsEntry(IMethod method) throws CoreException {
    String value = getMethodReturnValue(method);
    return getReturnNlsEntry(value, method);
  }

  private static INlsEntry getReturnNlsEntry(String value, IMethod method) throws CoreException {
    String key = PropertyMethodSourceUtility.parseReturnParameterNlsKey(value);
    if (!StringUtility.isNullOrEmpty(key)) {
      INlsProject nlsProject = ScoutTypeUtility.findNlsProject(method);
      if (nlsProject == null) {
        ScoutSdk.logWarning("could not find nls project for method '" + method.getElementName() + "' in type '" + method.getDeclaringType().getFullyQualifiedName() + "'");
      }
      else {
        return nlsProject.getEntry(key);
      }
    }
    return null;
  }

  /**
   * Gets the status of the given java name. Checks if a name that differs from the suffix is entered and that the name
   * is a valid java name.
   * 
   * @param name
   *          The name to check
   * @param suffix
   *          The suffix to compare against.
   * @return A status that describes the state of the given name
   */
  public static IStatus getJavaNameStatus(String name, String suffix) {
    if (!StringUtility.hasText(name) || name.equals(suffix)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_className"));
    }
    if (Regex.REGEX_WELLFORMD_JAVAFIELD.matcher(name).matches()) {
      return Status.OK_STATUS;
    }
    else if (Regex.REGEX_JAVAFIELD.matcher(name).matches()) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("NameNotValid"));
    }
  }

  /**
   * Gets the validation status for a potential new class in the given bundle, below the given package suffix with given
   * name.<br>
   * The method does no check for java classes, but checks if there exists a resource with the target name already.
   * 
   * @param container
   *          The bundle in which the type would be created.
   * @param packageSuffix
   *          The suffix under which the type would be created.
   * @param typeName
   *          the name of the potential type.
   * @return the ok status if no file exists at the target location with the given name. An error status otherwise.
   */
  public static IStatus getTypeExistingStatus(IScoutBundle container, String packageSuffix, String typeName) {
    if (typeName == null) {
      return Status.OK_STATUS;
    }
    String pck = container.getPackageName(packageSuffix);
    StringBuilder pathBuilder = new StringBuilder(TypeUtility.DEFAULT_SOURCE_FOLDER_NAME);
    pathBuilder.append(IPath.SEPARATOR).append(pck.replace('.', IPath.SEPARATOR));

    final BooleanHolder elementFound = new BooleanHolder(false);
    final String typeNameComplete = typeName + ".java";
    IFolder folder = container.getProject().getFolder(pathBuilder.toString());

    if (folder.exists()) {
      try {
        folder.accept(new IResourceProxyVisitor() {
          @Override
          public boolean visit(IResourceProxy proxy) throws CoreException {
            if (proxy.getType() == IResource.FILE && typeNameComplete.equalsIgnoreCase(proxy.getName())) {
              elementFound.setValue(true);
            }
            return true;
          }
        }, IResource.DEPTH_ONE, IResource.NONE);
      }
      catch (CoreException e) {
        return new ScoutStatus("Unable to check if the type '" + typeName + "' already exists.", e);
      }
    }

    if (elementFound.getValue()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    return Status.OK_STATUS;
  }

  /**
   * Gets the one type from the given candidate types that is contained in the closest (nearest) parent bundle
   * (=dependencies) of the given reference bundle.<br>
   * <br>
   * Types that are not found in any of the parent bundles (=dependencies) are ignored.
   * 
   * @param candidates
   *          The list of types.
   * @param reference
   *          The reference bundle. Must not be null.
   * @return The one (from the given candidates) whose surrounding scout bundle has the lowest distance to the reference
   *         bundle. Or null if the candidates do not contain any types or all of them cannot be found in the
   *         dependencies of the reference bundle.
   */
  public static IType getNearestType(IType[] candidates, IScoutBundle reference) {
    if (candidates == null || candidates.length < 1) {
      return null;
    }
    final Holder<IType> result = new Holder<IType>(IType.class, null);
    final Holder<Integer> minDistance = new Holder<Integer>(Integer.class, Integer.valueOf(Integer.MAX_VALUE));
    for (IType c : candidates) {
      final IType candidate = c;
      reference.visit(new IScoutBundleGraphVisitor() {
        @Override
        public boolean visit(IScoutBundle bundle, int traversalLevel) {
          if (bundle.contains(candidate)) {
            if (traversalLevel < minDistance.getValue().intValue()) {
              minDistance.setValue(Integer.valueOf(traversalLevel));
              result.setValue(candidate);
            }
            return false;
          }
          return true;
        }
      }, true, true);
    }
    return result.getValue();
  }
}
