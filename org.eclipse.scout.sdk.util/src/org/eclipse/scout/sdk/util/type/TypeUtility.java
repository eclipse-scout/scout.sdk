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
package org.eclipse.scout.sdk.util.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.jdt.SourceRange;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;

@SuppressWarnings("restriction")
public class TypeUtility {

  private static final Pattern BEAN_METHOD_NAME = Pattern.compile("(get|set|is)([A-Z].*)");

  protected TypeUtility() {
  }

  public static IType getType(String fullyQualifiedName) {
    return TypeCacheAccessor.getTypeCache().getType(fullyQualifiedName);
  }

  public static IType[] getTypes(String fullyQualifiedName) {
    return TypeCacheAccessor.getTypeCache().getTypes(fullyQualifiedName);
  }

  public static boolean existsType(String fullyQualifiedName) {
    return TypeCacheAccessor.getTypeCache().existsType(fullyQualifiedName);
  }

  public static IType[] getAllCachedTypes() {
    return TypeCacheAccessor.getTypeCache().getAllCachedTypes();
  }

  public static IJavaSearchScope newSearchScope(IJavaElement element) {
    return newSearchScope(new IJavaElement[]{element});
  }

  public static IJavaSearchScope newSearchScope(IJavaElement[] elements) {
    return SearchEngine.createJavaSearchScope(elements);
  }

  public static IPackageFragment[] getSubPackages(IPackageFragment packageFragment) {
    ArrayList<IPackageFragment> subPackages = new ArrayList<IPackageFragment>();
    String prefix = packageFragment.getElementName() + ".";
    IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) packageFragment.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    if (TypeUtility.exists(packageFragmentRoot)) {
      try {
        for (IJavaElement candidate : packageFragmentRoot.getChildren()) {
          if (TypeUtility.exists(candidate) && candidate.getElementType() == IJavaElement.PACKAGE_FRAGMENT && candidate.getElementName().startsWith(prefix)) {
            subPackages.add((IPackageFragment) candidate);
          }
        }
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logError("could not get subpackages of '" + packageFragment.getElementName() + "'.", e);
      }
    }

    return subPackages.toArray(new IPackageFragment[subPackages.size()]);
  }

  public static boolean isSubtype(IType type, IType potentialSubtype, org.eclipse.jdt.core.ITypeHierarchy hierarchy) {
    HashSet<IType> allSubtypes = new HashSet<IType>(Arrays.asList(hierarchy.getAllSubtypes(type)));
    return allSubtypes.contains(potentialSubtype);
  }

  public static IType[] toArray(IType... types) {
    if (types == null) {
      return new IType[0];
    }
    return types;
  }

  public static IType[] getInnerTypes(IType type) {
    return getInnerTypes(type, null);
  }

  public static IType[] getInnerTypes(IType type, ITypeFilter filter) {
    return getInnerTypes(type, filter, null);
  }

  public static IType[] getInnerTypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    Collection<IType> unsortedTypes = new ArrayList<IType>();
    try {
      for (IType subType : type.getTypes()) {
        if (filter == null || filter.accept(subType)) {
          unsortedTypes.add(subType);
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get inner types of '" + type.getFullyQualifiedName() + "'", e);
    }
    if (comparator == null) {
      return unsortedTypes.toArray(new IType[unsortedTypes.size()]);
    }
    else {
      TreeSet<IType> sortedTypes = new TreeSet<IType>(comparator);
      sortedTypes.addAll(unsortedTypes);
      return sortedTypes.toArray(new IType[sortedTypes.size()]);
    }
  }

  public static IType[] getInnerTypesOrdered(IType declaringType, IType superType, Comparator<IType> comparator) {
    ITypeHierarchy combinedTypeHierarchy = getLocalTypeHierarchy(declaringType);
    IType[] allSubtypes = TypeUtility.getInnerTypes(declaringType, TypeFilters.getSubtypeFilter(superType, combinedTypeHierarchy), comparator);
    return allSubtypes;
  }

  public static IType getTypeBySignature(String signature) {
    signature = Signature.getTypeErasure(signature);
    int arrayCount = Signature.getArrayCount(signature);
    if (arrayCount > 0) {
      signature = signature.substring(arrayCount);
    }
    String fqn = Signature.toString(signature);
    return getType(fqn);
  }

  /**
   * To get a type hierarchy with the given elements as scope.
   * 
   * @param elements
   * @return
   */
  public static ITypeHierarchy getLocalTypeHierarchy(IJavaElement... elements) {
    IRegion region = JavaCore.newRegion();
    if (elements != null) {
      for (IJavaElement e : elements) {
        region.add(e);
      }
    }
    return TypeCacheAccessor.getHierarchyCache().getLocalHierarchy(region);
  }

  public static ITypeHierarchy getLocalTypeHierarchy(IRegion region) {
    return TypeCacheAccessor.getHierarchyCache().getLocalHierarchy(region);
  }

  public static ITypeHierarchy getSuperTypeHierarchy(IType type) {
    return TypeCacheAccessor.getHierarchyCache().getSuperHierarchy(type);
  }

  public static IPrimaryTypeTypeHierarchy[] getAllCachedPrimaryTypeHierarchies() throws IllegalArgumentException {
    return TypeCacheAccessor.getHierarchyCache().getAllCachedHierarchies();
  }

  /**
   * To get a type hierarchy containing no inner types and tracking changes of all primary types in the hierarchy. <br>
   * <br>
   * <b> Note: </b> the listener reference is a weak reference. If users do not keep the reference the type hierarchy
   * will be removed from cache and released.
   * 
   * @param type
   *          the type to get the primary type hierarchy for must be a primary type
   * @return the cached type hierarchy or null if type does not exist or hierarchy could not be created.
   * @throws IllegalArgumentException
   *           if the given type is not a primary type.
   */
  public static IPrimaryTypeTypeHierarchy getPrimaryTypeHierarchy(IType type) throws IllegalArgumentException {
    return TypeCacheAccessor.getHierarchyCache().getPrimaryTypeHierarchy(type);
  }

  public static boolean hasInnerType(IType declaringType, String typeName) {
    IType candidate = declaringType.getType(typeName);
    return candidate != null && candidate.exists();
  }

  public static ISourceRange getContentRange(IMethod method) throws JavaModelException {
    SourceRange contentRange = null;
    if (TypeUtility.exists(method)) {
      ISourceRange methodRange = method.getSourceRange();
      ISourceRange nameRange = method.getNameRange();
      int contentOffset = nameRange.getOffset() - methodRange.getOffset();
      contentRange = new SourceRange(contentOffset, methodRange.getLength() - contentOffset);
      String regex = "\\A" + method.getElementName() + "[^{]*\\{(.*)\\}\\s*\\z";
      CharSequence source = method.getSource().subSequence(contentRange.getOffset(), contentRange.getOffset() + contentRange.getLength());
      Matcher matcher = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE).matcher(source);
      if (matcher.find()) {
        contentRange.setOffset(methodRange.getOffset() + contentRange.getOffset() + matcher.start(1));
        contentRange.setLength(matcher.end(1) - matcher.start(1));
      }
      else {
        contentRange = null;
      }
    }
    return contentRange;
  }

  public static IMethod findMethodInHierarchy(IType type, IMethodFilter filter) {
    try {
      return findMethodInHierarchy(type, type.newSupertypeHierarchy(null), filter);
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not create super type hierarchy of '" + type.getFullyQualifiedName() + "'.", e);
      return null;
    }
  }

  public static IMethod findMethodInHierarchy(IType type, org.eclipse.jdt.core.ITypeHierarchy hierarchy, IMethodFilter filter) {
    if (exists(type)) {
      IMethod method = getFirstMethod(type, filter);
      if (TypeUtility.exists(method)) {
        return method;
      }
      else {
        findMethodInHierarchy(hierarchy.getSuperclass(type), hierarchy, filter);
      }
    }
    return null;
  }

  public static IMethod getFirstMethod(IType type, IMethodFilter filter) {
    try {
      for (IMethod method : type.getMethods()) {
        if (filter == null || filter.accept(method)) {
          return method;
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get methods of '" + type.getFullyQualifiedName() + "'.", e);
    }
    return null;
  }

  public static IMethod getMethod(IType type, final String methodName) {
    IMethod[] methods = getMethods(type, new IMethodFilter() {
      @Override
      public boolean accept(IMethod method) {
        return method.getElementName().equals(methodName);
      }
    });
    if (methods.length > 0) {
      return methods[0];
    }
    return null;
  }

  public static IMethod[] getMethods(IType type) {
    return getMethods(type, null);
  }

  public static IMethod[] getMethods(IType type, IMethodFilter filter) {
    return getMethods(type, filter, null);
  }

  public static IMethod[] getMethods(IType type, IMethodFilter filter, Comparator<IMethod> comparator) {
    Collection<IMethod> unsortedMethods = new ArrayList<IMethod>();
    try {
      for (IMethod method : type.getMethods()) {
        if (filter == null || filter.accept(method)) {
          unsortedMethods.add(method);
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get methods of '" + type.getFullyQualifiedName() + "'.", e);
    }
    if (comparator == null) {
      return unsortedMethods.toArray(new IMethod[unsortedMethods.size()]);
    }
    else {
      TreeSet<IMethod> sortedMethods = new TreeSet<IMethod>(comparator);
      sortedMethods.addAll(unsortedMethods);
      return sortedMethods.toArray(new IMethod[sortedMethods.size()]);
    }
  }

  public static IType getAncestor(IType type, ITypeFilter filter) {
    IType ancestorType = type;
    while (TypeUtility.exists(ancestorType)) {
      if (filter.accept(ancestorType)) {
        return ancestorType;
      }
      ancestorType = ancestorType.getDeclaringType();
    }
    return null;
  }

  public static IField[] getFields(IType declaringType) {
    return getFields(declaringType, null);
  }

  public static IField[] getFields(IType declaringType, IFieldFilter filter) {
    return getFields(declaringType, filter, null);
  }

  public static IField[] getFields(IType declaringType, IFieldFilter filter, Comparator<IField> comparator) {
    ArrayList<IField> unsortedFields = new ArrayList<IField>();
    try {
      for (IField candidate : declaringType.getFields()) {
        if (filter == null || filter.accept(candidate)) {
          unsortedFields.add(candidate);
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get fields of '" + declaringType.getElementName() + "'.", e);
    }
    if (comparator == null) {
      return unsortedFields.toArray(new IField[unsortedFields.size()]);
    }
    else {
      TreeSet<IField> sortedFields = new TreeSet<IField>(comparator);
      sortedFields.addAll(unsortedFields);
      return sortedFields.toArray(new IField[sortedFields.size()]);
    }
  }

  public static IField getFirstField(IType type, IFieldFilter filter) {
    try {
      for (IField method : type.getFields()) {
        if (filter == null || filter.accept(method)) {
          return method;
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get methods of '" + type.getFullyQualifiedName() + "'.", e);
    }
    return null;
  }

  public static Object getFieldConstant(IField field) throws JavaModelException {
    Object val = field.getConstant();
    if (val instanceof String) {
      String ret = (String) val;
      ret = ret.trim();
      if (ret.length() > 2 && ret.charAt(0) == '"' && ret.charAt(ret.length() - 1) == '"') {
        // when scout runtime sources are present in the workspace -> field value is returned with double quotes
        ret = ret.substring(1, ret.length() - 1);
      }
      return ret;
    }
    else {
      return val;
    }
  }

  public static boolean equalTypes(IType[] arr1, IType[] arr2) {
    if (arr1 == null && arr2 == null) {
      return true;
    }
    else if (arr1 == null) {
      return false;
    }
    else if (arr2 == null) {
      return false;
    }
    else if (arr1.length != arr2.length) {
      return false;
    }
    else {
      Arrays.sort(arr1, TypeComparators.getHashCodeComparator());
      Arrays.sort(arr2, TypeComparators.getHashCodeComparator());
      return Arrays.equals(arr1, arr2);
    }
  }

  public static IMethod getOverwrittenMethod(IMethod method, org.eclipse.jdt.core.ITypeHierarchy superTypeHierarchy) {
    IType superType = superTypeHierarchy.getSuperclass(method.getDeclaringType());
    IMethodFilter overrideFilter = MethodFilters.getSuperMethodFilter(method);
    while (superType != null) {
      IMethod superMethod = TypeUtility.getFirstMethod(superType, overrideFilter);
      if (superMethod != null) {
        return superMethod;
      }
      superType = superTypeHierarchy.getSuperclass(superType);
    }
    return null;
  }

  /**
   * <xmp>
   * class A{
   * class B{
   * class C{
   * }
   * class D{
   * }
   * }
   * }
   * // A.getTopLevelType() returns A
   * // D.getTopLevelType() returns A
   * </xmp>
   * 
   * @return the primary type of the compilation unit this type is declared in.
   */
  public static IType getToplevelType(IType type) {
    if (type == null) {
      return null;
    }
    if (type.getParent().getElementType() != IJavaElement.TYPE) {
      return type;
    }
    return getToplevelType(type.getDeclaringType());
  }

  public static IType[] getTypesInPackage(IPackageFragment pck) {
    return getTypesInPackage(pck, null);
  }

  public static IType[] getTypesInPackage(IPackageFragment pck, ITypeFilter filter) {
    return getTypesInPackage(pck, filter, null);
  }

  public static IType[] getTypesInPackage(IPackageFragment pck, ITypeFilter filter, Comparator<IType> comparator) {
    return getTypesInPackage(pck, filter, comparator, false);
  }

  public static IType[] getTypesInPackage(IPackageFragment pck, ITypeFilter filter, Comparator<IType> comparator, boolean includeSubpackages) {

    Collection<IType> unsortedTypes = new ArrayList<IType>();
    collectTypesInPackage(pck, filter, includeSubpackages, unsortedTypes);

    if (comparator == null) {
      return unsortedTypes.toArray(new IType[unsortedTypes.size()]);
    }
    else {
      TreeSet<IType> sortedTypes = new TreeSet<IType>(comparator);
      sortedTypes.addAll(unsortedTypes);
      return sortedTypes.toArray(new IType[sortedTypes.size()]);
    }
  }

  private static void collectTypesInPackage(IPackageFragment pck, ITypeFilter filter, boolean includeSubPackages, Collection<IType> collector) {
    try {
      if (pck != null && pck.exists()) {
        for (IJavaElement element : pck.getChildren()) {
          if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
            if (includeSubPackages) {
              collectTypesInPackage((IPackageFragment) element, filter, includeSubPackages, collector);
            }
          }
          else if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            ICompilationUnit icu = (ICompilationUnit) element;
            IType[] types = icu.getTypes();
            if (types != null && types.length > 0) {
              collector.add(types[0]);
            }
          }
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get types of package '" + pck.getElementName() + "'", e);
    }

  }

  public static boolean exists(IJavaElement element) {
    return element != null && element.exists();
  }

  public static IType findInnerType(IType type, String innerTypeName) {
    if (type == null) {
      return null;
    }
    else if (StringUtility.equalsIgnoreCase(type.getElementName(), innerTypeName)) {
      return type;
    }
    else {
      try {
        for (IType innerType : type.getTypes()) {
          IType found = findInnerType(innerType, innerTypeName);
          if (found != null) {
            return found;
          }
        }
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logError("could not find inner type named '" + innerTypeName + "' in type '" + type.getFullyQualifiedName() + "'.", e);
      }
    }
    return null;
  }

  public static IType getReferencedType(IType declaringType, String typeName) throws JavaModelException {
    ICompilationUnit compilationUnit = declaringType.getCompilationUnit();
    if (compilationUnit != null) {
      IImportDeclaration[] imports = compilationUnit.getImports();
      for (IImportDeclaration imp : imports) {
        if (imp.getElementName().endsWith("." + typeName)) {
          IType foundType = TypeUtility.getType(imp.getElementName());
          if (foundType != null) {
            return foundType;
          }
        }
      }
    }
    String[][] resolvedTypeName = declaringType.resolveType(typeName);
    if (resolvedTypeName != null && resolvedTypeName.length == 1) {
      String fqName = resolvedTypeName[0][0];
      if (fqName != null && fqName.length() > 0) {
        fqName = fqName + ".";
      }
      fqName = fqName + resolvedTypeName[0][1];
      IType foundType = getType(fqName);
      if (foundType != null) {
        return foundType;
      }
    }
    SdkUtilActivator.logWarning("could not find referenced type '" + typeName + "' in '" + declaringType.getFullyQualifiedName() + "'.");
    return null;
  }

  public static boolean isGenericType(IType type) {
    try {
      return exists(type) && (type.getTypeParameters().length > 0);
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get generic information of type: " + type.getFullyQualifiedName(), e);
      return false;
    }
  }

  public static boolean isOnClasspath(IMember member, IJavaProject project) {
    if (member == null) {
      return false;
    }
    if (member.isBinary()) {
      return project.isOnClasspath(member);
    }
    else if (member.getJavaProject() != null) {
      if (member.getJavaProject().equals(project)) {
        return true;
      }
      else {
        return project.isOnClasspath(member.getJavaProject());
      }
    }
    return false;
  }

  /**
   * Collects all property beans declared directly in the given type by search methods with the following naming
   * convention:
   * 
   * <pre>
   * public <em>&lt;PropertyType&gt;</em> get<em>&lt;PropertyType&gt;</em>();
   * public void set<em>&lt;PropertyType&gt;</em>(<em>&lt;PropertyType&gt;</em> a);
   * </pre>
   * 
   * If <code>PropertyType</code> is a boolean property, the following getter is expected
   * 
   * <pre>
   * public boolean is<em>&lt;PropertyType&gt;</em>();
   * </pre>
   * <p>
   * This implementation tries to determine the field by using the JDT code style settings stored in the Eclipse
   * preferences. Prefixes and suffixes used for fields must be declared. The default prefix Scout uses for fields (
   * <code>m_</code>) is added by default.
   * 
   * @param type
   *          the type within properties are searched
   * @param propertyFilter
   *          optional property bean filter used to filter the result
   * @param comparator
   *          optional property bean comparator used to sort the result
   * @return Returns an array of property bean descriptions. The array is empty if the given class does not contain any
   *         bean properties.
   * @see <a href="http://www.oracle.com/technetwork/java/javase/documentation/spec-136004.html">JavaBeans Spec</a>
   */
  public static IPropertyBean[] getPropertyBeans(IType type, IPropertyBeanFilter propertyFilter, Comparator<IPropertyBean> comparator) {
    HashMap<String, PropertyBean> beans = new HashMap<String, PropertyBean>();
    IMethodFilter filter = MethodFilters.getMultiMethodFilter(
        MethodFilters.getFlagsFilter(Flags.AccPublic),
        MethodFilters.getNameRegexFilter(BEAN_METHOD_NAME));
    IMethod[] methods = TypeUtility.getMethods(type, filter);
    for (IMethod m : methods) {
      Matcher matcher = BEAN_METHOD_NAME.matcher(m.getElementName());
      if (matcher.matches()) {
        try {
          String kind = matcher.group(1);
          String name = matcher.group(2);
          //
          String[] parameterTypes = m.getParameterTypes();
          String returnType = m.getReturnType();
          if (kind.equals("get") && parameterTypes.length == 0 && !returnType.equals(Signature.SIG_VOID)) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.getReadMethod() == null) {
              desc.setReadMethod(m);
            }
          }
          else if (kind.equals("is") && parameterTypes.length == 0 && returnType.equals(Signature.SIG_BOOLEAN)) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.getReadMethod() == null) {
              desc.setReadMethod(m);
            }
          }
          else if (kind.equals("set") && parameterTypes.length == 1 && returnType.equals(Signature.SIG_VOID)) {
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
        catch (JavaModelException e) {
          SdkUtilActivator.logError("Error while collectiong property beans of type [" + type + "]", e);
        }
      }
    }

    // filter
    if (propertyFilter == null) {
      propertyFilter = new IPropertyBeanFilter() {
        @Override
        public boolean accept(IPropertyBean property) {
          return true;
        }
      };
    }
    ArrayList<PropertyBean> filteredBeans = new ArrayList<PropertyBean>();
    for (PropertyBean bean : beans.values()) {
      if (propertyFilter.accept(bean)) {
        filteredBeans.add(bean);
      }
    }

    // fields
    IField[] fieldCandidates = TypeUtility.getFields(type, FieldFilters.getPrivateNotStaticNotFinalNotAbstract(), null);
    HashMap<String, IField> fields = new HashMap<String, IField>();
    for (IField field : fieldCandidates) {
      fields.put(field.getElementName(), field);
    }
    AssistOptions assistOptions = new AssistOptions(type.getJavaProject().getOptions(true));
    String[] fieldPrefixes = toStringArray(assistOptions.fieldPrefixes, "m_", "");
    String[] fieldSuffixes = toStringArray(assistOptions.fieldSuffixes, "");
    for (PropertyBean bean : filteredBeans) {
      IField field = findFieldForPropertyBean(bean.getBeanName(), fields, fieldPrefixes, fieldSuffixes);
      if (field != null) {
        bean.setField(field);
      }
      else {
        SdkUtilActivator.logWarning("Unable to find field for property bean [" + bean + "]");
      }
    }

    // sort
    if (comparator != null) {
      Collections.sort(filteredBeans, comparator);
    }

    return filteredBeans.toArray(new IPropertyBean[filteredBeans.size()]);
  }

  /**
   * To find out if the given child element is a ancestor of the given parent element. If parent and child is the same
   * element true is returned.
   * 
   * @param parent
   * @param chlid
   * @return
   */
  public static boolean isAncestor(IJavaElement parent, IJavaElement chlid) {
    if (parent == null || chlid == null) {
      return false;
    }
    if (parent.equals(chlid)) {
      return true;
    }
    else if (chlid.getParent() != null && chlid.getParent().getElementType() >= parent.getElementType()) {
      return isAncestor(parent, chlid.getParent().getAncestor(parent.getElementType()));
    }
    else {
      return false;
    }
  }

  /**
   * Tries to determine the field the given property is based on. All combinations of the given pre- and suffixes are
   * used to find the field. If none of them matches with a method <code>null</code> is returned.
   * 
   * @param beanName
   * @param fields
   * @param fieldPrefixes
   * @param fieldSuffixes
   * @return Returns the field the property is based on or <code>null</code>.
   */
  private static IField findFieldForPropertyBean(String beanName, HashMap<String, IField> fields, String[] fieldPrefixes, String[] fieldSuffixes) {
    for (String prefix : fieldPrefixes) {
      for (String suffix : fieldSuffixes) {
        IField field = fields.get(prefix + decapitalize(beanName) + suffix);
        if (field != null) {
          return field;
        }
      }
    }
    return null;
  }

  /**
   * Converts the given two-dimensional array of chars into an array of strings and adds the given additional values.
   * 
   * @param arrayOfChars
   * @param additionalValues
   * @return
   */
  private static String[] toStringArray(char[][] arrayOfChars, String... additionalValues) {
    HashSet<String> result = new HashSet<String>();
    if (additionalValues != null) {
      for (String s : additionalValues) {
        result.add(s);
      }
    }
    if (arrayOfChars != null) {
      for (char[] cs : arrayOfChars) {
        if (cs != null && cs.length > 0) {
          result.add(String.valueOf(cs));
        }
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Converts the given property name to its field name (according to JavaBeans spec).
   * 
   * @param s
   * @return
   */
  private static String decapitalize(String s) {
    if (s == null || s.length() == 0) return "";
    if (s.length() >= 2 && Character.isUpperCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1))) {
      return s;
    }
    return Character.toLowerCase(s.charAt(0)) + s.substring(1);
  }
}
