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

import java.util.HashMap;
import java.util.List;
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
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.method.AstMethodReturnValueParser;
import org.eclipse.scout.sdk.util.method.IMethodReturnValueParser;
import org.eclipse.scout.sdk.util.method.MethodReturnExpression;
import org.eclipse.scout.sdk.util.method.SimpleMethodReturnValueParser;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeFilters;
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

  private static final Pattern REGEX_LINE_SEP_CLEAN = Pattern.compile("(\\n)\\r");
  private static final Pattern REGEX_LINE_SEP_CLEAN2 = Pattern.compile("\\r(\\n)");
  private static final Pattern REGEX_LINE_SET_CLEAN3 = Pattern.compile("\\n+$");
  private static final Pattern REGEX_LINE_SET_CLEAN4 = Pattern.compile("\\n");
  private static final Pattern REGEX_PACKAGE_NAME = Pattern.compile("^[0-9a-zA-Z\\.\\_]*$");
  private static final Pattern REGEX_PACKAGE_NAME_START = Pattern.compile("[a-zA-Z]{1}.*$");
  private static final Pattern REGEX_PACKAGE_NAME_END = Pattern.compile("^.*[a-zA-Z]{1}$");
  private static final Pattern REGEX_CONTAINS_UPPER_CASE = Pattern.compile(".*[A-Z].*");
  private static final Pattern REGEX_COMMENT_REMOVE_1 = Pattern.compile("\\/\\/.*?\\\r\\\n");
  private static final Pattern REGEX_COMMENT_REMOVE_2 = Pattern.compile("\\/\\/.*?\\\n");
  private static final Pattern REGEX_COMMENT_REMOVE_3 = Pattern.compile("(?s)\\/\\*.*?\\*\\/");

  private static final ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<String>();

  private static final IMethodReturnValueParser[] METHOD_RETURN_VALUE_PARSERS = new IMethodReturnValueParser[]{
    SimpleMethodReturnValueParser.INSTANCE,
    AstMethodReturnValueParser.INSTANCE
  };

  private ScoutUtility() {
  }

  public static String getCommentBlock(String content) {
    StringBuilder builder = new StringBuilder();
    builder.append("// TODO ");
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
    retVal = REGEX_COMMENT_REMOVE_1.matcher(retVal).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_2.matcher(retVal).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_3.matcher(retVal).replaceAll("");
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
    buffer = REGEX_LINE_SET_CLEAN3.matcher(buffer).replaceAll("\n");
    return REGEX_LINE_SET_CLEAN4.matcher(buffer).replaceAll(separator);
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
    // do not limit filter to a scout-bundle-type because every scout-bundle type can hold service implementations (e.g. text services in the shared bundle).
    for (IScoutBundle bundle : implementationBundle.getChildBundles(ScoutBundleFilters.getWorkspaceBundlesFilter(), true)) {
      unregisterServiceClass(bundle.getProject(), IRuntimeClasses.EXTENSION_POINT_SERVICES, IRuntimeClasses.EXTENSION_ELEMENT_SERVICE, serviceType.getFullyQualifiedName());
    }
  }

  public static void unregisterServiceClass(IProject project, String extensionPoint, String elemType, String className) throws CoreException {
    PluginModelHelper h = new PluginModelHelper(project);
    HashMap<String, String> attributes = new HashMap<String, String>(1);
    attributes.put("class", className);
    h.PluginXml.removeSimpleExtension(extensionPoint, elemType, attributes);
    h.save();
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
    List<String> symbolicNames = pfmh.ProductFile.getPluginSymbolicNames();
    return RuntimeBundles.getBundleType(symbolicNames);
  }

  public static IJavaProject getJavaProject(IScoutBundle bundle) {
    if (bundle != null) {
      return bundle.getJavaProject();
    }
    return null;
  }

  public static Set<String> getEntities(IScoutBundle p) throws JavaModelException {
    Set<String> ret = new TreeSet<String>();
    Set<? extends IScoutBundle> roots = p.getParentBundles(ScoutBundleFilters.getRootBundlesFilter(), true);
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
    return ret;
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

  private static String getContainingJavaKeyWord(String s) {
    for (String keyWord : NamingUtility.getJavaKeyWords()) {
      if (s.startsWith(keyWord + ".") || s.endsWith("." + keyWord) || s.contains("." + keyWord + ".")) {
        return keyWord;
      }
    }
    return null;
  }

  public static String getMethodReturnValue(IMethod method, IImportValidator validator) {
    return getMethodReturnExpression(method).getReturnStatement(validator);
  }

  public static String getMethodReturnValue(IMethod method) {
    return getMethodReturnExpression(method).getReturnStatement();
  }

  public static MethodReturnExpression getMethodReturnExpression(IMethod method) {
    for (IMethodReturnValueParser parser : METHOD_RETURN_VALUE_PARSERS) {
      MethodReturnExpression returnExpression = parser.parse(method);
      if (returnExpression != null) {
        return returnExpression;
      }
    }
    return null; // no parser was able to calculate a value
  }

  public static INlsEntry getReturnNlsEntry(IMethod method) throws CoreException {
    String key = PropertyMethodSourceUtility.parseReturnParameterNlsKey(getMethodReturnValue(method));
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
  public static IStatus validateJavaName(String name, String suffix) {
    if (!StringUtility.hasText(name) || name.equals(suffix)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_className"));
    }
    if (IRegEx.WELLFORMD_JAVAFIELD.matcher(name).matches()) {
      return Status.OK_STATUS;
    }
    else if (IRegEx.JAVAFIELD.matcher(name).matches()) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("NameNotValid"));
    }
  }

  /**
   * Gets the status of a form field to be created.
   *
   * @param name
   *          The name of the field
   * @param suffix
   *          The suffix of the name to be used.
   * @param declaringType
   *          The type in which the field should be created.
   * @return A status that describes the state of such a form field in the given context.
   */
  public static IStatus validateFormFieldName(String name, String suffix, IType declaringType) {
    IStatus javaFieldNameStatus = ScoutUtility.validateJavaName(name, suffix);
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }

    try {
      List<IType> allTypesWithSameSimpleName = TypeUtility.getAllTypes(declaringType.getCompilationUnit(), TypeFilters.getElementNameFilter(name));
      if (allTypesWithSameSimpleName.size() > 0) {
        return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("unable to get all types in '" + declaringType.getCompilationUnit().getElementName() + "'.", e);
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Fatal: " + e.getMessage());
    }
    IType formType = TypeUtility.getToplevelType(declaringType);
    String plainName = removeFieldSuffix(name);
    String pat = "(?:get|is)(?:" + name + "|" + plainName + ")";
    Set<IMethod> existingMethods = TypeUtility.getMethods(formType, MethodFilters.getNameRegexFilter(Pattern.compile(pat)));
    if (existingMethods.size() > 0) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    return javaFieldNameStatus;
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
  public static IStatus validateTypeNotExisting(IScoutBundle container, String packageSuffix, String typeName) {
    if (typeName == null) {
      return Status.OK_STATUS;
    }
    String pck = container.getPackageName(packageSuffix);
    StringBuilder pathBuilder = new StringBuilder(TypeUtility.DEFAULT_SOURCE_FOLDER_NAME);
    pathBuilder.append(IPath.SEPARATOR).append(pck.replace('.', IPath.SEPARATOR));

    final BooleanHolder elementFound = new BooleanHolder(false);
    final String typeNameComplete = typeName + ".java";
    final IFolder folder = container.getProject().getFolder(pathBuilder.toString());

    if (folder.exists()) {
      try {
        folder.accept(new IResourceProxyVisitor() {
          boolean selfVisited = false;

          @Override
          public boolean visit(IResourceProxy proxy) throws CoreException {
            if (proxy.getType() == IResource.FOLDER) {
              if (!selfVisited) {
                selfVisited = true;
                return true;
              }
              return false;
            }
            else if (proxy.getType() == IResource.FILE && typeNameComplete.equalsIgnoreCase(proxy.getName())) {
              elementFound.setValue(true);
            }
            return false;
          }
        }, IResource.NONE);
      }
      catch (CoreException e) {
        return new ScoutStatus("Unable to check if the type '" + typeName + "' already exists.", e);
      }
    }

    if (elementFound.getValue()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("NameXAlreadyUsed", typeName));
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
  public static IType getNearestType(Set<IType> candidates, IScoutBundle reference) {
    if (candidates == null || candidates.size() < 1) {
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
