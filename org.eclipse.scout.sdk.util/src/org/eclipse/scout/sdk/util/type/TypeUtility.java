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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.jdt.SourceRange;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;

@SuppressWarnings("restriction")
public class TypeUtility {

  public static final String DEFAULT_SOURCE_FOLDER_NAME = "src";
  public static final Pattern BEAN_METHOD_NAME = Pattern.compile("(get|set|is)([A-Z].*)");

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

  public static IPackageFragmentRoot getSrcPackageFragmentRoot(IJavaProject project) throws JavaModelException {
    return project.findPackageFragmentRoot(new Path("/" + project.getElementName() + "/" + DEFAULT_SOURCE_FOLDER_NAME)); //TODO: src constant exists in SdkProperties class. move?
  }

  public static IPackageFragment getPackage(IJavaElement element) {
    IPackageFragment packageFragment = (IPackageFragment) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
    return packageFragment;
  }

  public static IPackageFragment getPackage(IJavaProject project, String packageName) throws JavaModelException {
    return getSrcPackageFragmentRoot(project).getPackageFragment(packageName);
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

  /**
   * Returns the immediate member types declared by the given type. The results is filtered using the given filter and
   * sorted using the given comparator.
   * 
   * @param type
   *          The type whose immediate inner types should be returned.
   * @param filter
   *          the filter to apply or null
   * @param comparator
   *          the comparator to sort the result or null
   * @return the immediate inner types declared in the given type.
   */
  public static IType[] getInnerTypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    Set<IType> result = null;
    if (comparator == null) {
      result = new HashSet<IType>();
    }
    else {
      result = new TreeSet<IType>(comparator);
    }

    try {
      for (IType subType : type.getTypes()) {
        if (filter == null || filter.accept(subType)) {
          result.add(subType);
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get inner types of '" + type.getFullyQualifiedName() + "'", e);
    }
    return result.toArray(new IType[result.size()]);
  }

  /**
   * Returns the immediate member types declared by the given type which are sub-types of the given super-type. The
   * results is sorted using the given comparator.
   * 
   * @param declaringType
   *          The type whose immediate inner types should be returned.
   * @param superType
   *          The super-type for which all returned types must be a sub-type.
   * @param comparator
   *          the comparator to sort the result.
   * @return the immediate member types declared by the given type which are sub-types of the given super-type.
   */
  public static IType[] getInnerTypesOrdered(IType declaringType, IType superType, Comparator<IType> comparator) {
    ITypeHierarchy combinedTypeHierarchy = getLocalTypeHierarchy(declaringType);
    IType[] allSubtypes = TypeUtility.getInnerTypes(declaringType, TypeFilters.getSubtypeFilter(superType, combinedTypeHierarchy), comparator);
    return allSubtypes;
  }

  public static IType getTypeBySignature(String signature) {
    return getType(SignatureUtility.getFullyQuallifiedName(signature));
  }

  /**
   * To get a type hierarchy with the given elements as scope.
   * 
   * @param elements
   * @return
   * @throws JavaModelException
   */
  public static ITypeHierarchy getLocalTypeHierarchy(IJavaElement... elements) {
    IRegion region = JavaCore.newRegion();
    if (elements != null) {
      for (IJavaElement e : elements) {
        if (e.getElementType() == IJavaElement.TYPE) {
          IType t = (IType) e;
          if (t.isBinary()) {
            // binary types do not include their inner types because these inner types belong to their own class file
            // solution: add them manually
            addBinaryInnerTypesToRegionRec(t, region);
          }
        }
        region.add(e);
      }
    }
    return TypeCacheAccessor.getHierarchyCache().getLocalHierarchy(region);
  }

  private static void addBinaryInnerTypesToRegionRec(IType declaringType, IRegion region) {
    try {
      for (IType child : declaringType.getTypes()) {
        region.add(child);
        addBinaryInnerTypesToRegionRec(child, region);
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logError("Unable to get inner types of type '" + declaringType.getFullyQualifiedName() + "'.", e);
    }
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

  public static IMethod getMethod(IType declaringType, String methodName, String[] resolvedParameterSignatures) throws CoreException {
    for (IMethod m : declaringType.getMethods()) {
      if (CompareUtility.equals(m.getElementName(), methodName)) {
        // signature compare
        String[] parameterSignatures = SignatureUtility.getMethodParameterSignatureResolved(m);
        if (parameterSignatures.length == resolvedParameterSignatures.length) {
          boolean signatureEquals = true;
          for (int i = 0; i < parameterSignatures.length; i++) {
            if (!CompareUtility.equals(resolvedParameterSignatures[i], parameterSignatures[i])) {
              signatureEquals = false;
              break;
            }
          }
          if (signatureEquals) {
            return m;
          }
        }
      }
    }
    return null;
  }

  public static IMethod findMethodInSuperClassHierarchy(IType type, IMethodFilter filter) {
    try {
      return findMethodInSuperClassHierarchy(type, type.newSupertypeHierarchy(null), filter);
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not create super type hierarchy of '" + type.getFullyQualifiedName() + "'.", e);
      return null;
    }
  }

  public static IMethod findMethodInSuperClassHierarchy(IType type, org.eclipse.jdt.core.ITypeHierarchy hierarchy, IMethodFilter filter) {
    if (exists(type)) {
      IMethod method = getFirstMethod(type, filter);
      if (TypeUtility.exists(method)) {
        return method;
      }
      else {
        return findMethodInSuperClassHierarchy(hierarchy.getSuperclass(type), hierarchy, filter);
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
    catch (CoreException e) {
      SdkUtilActivator.logWarning("could not get methods of '" + type.getFullyQualifiedName() + "' with filter '" + filter + "'.", e);
    }
    return null;
  }

  /**
   * Searches and returns the first method with the given name in the given type.<br>
   * If multiple methods with the same name exist (overloads), the first is returned as they appear in the source or
   * class file.
   * 
   * @param type
   *          The type in which the method should be searched.
   * @param methodName
   *          The name of the method.
   * @return The first method found or null.
   */
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

  /**
   * Gets all methods in the given type.<br>
   * The methods are in the order in which they appear in the source or class file.
   * 
   * @param type
   *          The type to get all methods of.
   * @return an array of all methods of the given type. never returns null.
   */
  public static IMethod[] getMethods(IType type) {
    return getMethods(type, null);
  }

  /**
   * Gets all methods in the given type that match the given filter.<br>
   * The methods are in the order in which they appear in the source or class file.
   * 
   * @param type
   *          The type to get all methods of.
   * @param filter
   *          The filter.
   * @return an array of all methods of the given type matching the given filter. never returns null.
   */
  public static IMethod[] getMethods(IType type, IMethodFilter filter) {
    return getMethods(type, filter, null);
  }

  /**
   * Gets all methods in the given type that match the given filter ordered by the given comparator.<br>
   * If the given comparator is null, the methods are in the order in which they appear in the source or class file.
   * 
   * @param type
   *          The type to get all methods of.
   * @param filter
   *          The filter to use or null for no filtering.
   * @param comparator
   *          The comparator to use or null to get the methods in the order in which they appear in the source or class
   *          file.
   * @return an array of all methods of the given type matching the given filter. never returns null.
   */
  public static IMethod[] getMethods(IType type, IMethodFilter filter, Comparator<IMethod> comparator) {
    try {
      IMethod[] methods = type.getMethods();
      if (filter == null && comparator == null) {
        return methods;
      }

      Collection<IMethod> collector = null;
      if (comparator == null) {
        collector = new ArrayList<IMethod>(methods.length);
      }
      else {
        collector = new TreeSet<IMethod>(comparator);
      }

      for (IMethod method : methods) {
        if (filter == null || filter.accept(method)) {
          collector.add(method);
        }
      }
      return collector.toArray(new IMethod[collector.size()]);
    }
    catch (CoreException e) {
      SdkUtilActivator.logWarning("could not get methods of '" + type.getFullyQualifiedName() + "' with filter '" + filter + "'.", e);
      return new IMethod[]{};
    }
  }

  public static ISourceRange getContentSourceRange(IMethod method) throws JavaModelException {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setCompilerOptions(method.getJavaProject().getOptions(true));
    parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
    parser.setSource(method.getSource().toCharArray());
    parser.setResolveBindings(false);
    parser.setBindingsRecovery(false);
    ASTNode rootNode = parser.createAST(null);
    final Holder<ISourceRange> rangeHolder = new Holder<ISourceRange>(ISourceRange.class);
    rootNode.accept(new ASTVisitor() {
      boolean a = false;

      @Override
      public boolean visit(MethodDeclaration node) {
        a = true;
        return true;
      }

      @Override
      public boolean visit(Block node) {
        if (a) {
          rangeHolder.setValue(new SourceRange(node.getStartPosition(), node.getLength()));
          return false;
        }
        return true;
      }

    });
    ISourceRange methodRelativeRange = rangeHolder.getValue();
    if (methodRelativeRange != null) {
      return new SourceRange(methodRelativeRange.getOffset() + method.getSourceRange().getOffset() + 1, methodRelativeRange.getLength() - 2);
    }
    return null;
  }

  public static MethodParameter[] getMethodParameters(IMethod method, IType contextType) throws CoreException {
    String[] paramNames = method.getParameterNames();
    String[] resolvedParamSignatures = SignatureUtility.getMethodParameterSignatureResolved(method, contextType);
    if (paramNames.length != resolvedParamSignatures.length) {
      throw new IllegalArgumentException("Could not resolve method parameters of '" + method.getElementName() + "' in '" + method.getDeclaringType().getFullyQualifiedName() + "'.");
    }
    List<MethodParameter> params = new ArrayList<MethodParameter>();
    for (int i = 0; i < paramNames.length; i++) {
      params.add(new MethodParameter(paramNames[i], resolvedParamSignatures[i]));
    }
    return params.toArray(new MethodParameter[params.size()]);
  }

  public static MethodParameter[] getMethodParameters(IMethod method, Map<String, String> generics) throws CoreException {
    String[] paramNames = method.getParameterNames();
    String[] resolvedParamSignatures = SignatureUtility.getMethodParameterSignatureResolved(method, generics);
    if (paramNames.length != resolvedParamSignatures.length) {
      throw new IllegalArgumentException("Could not resolve method parameters of '" + method.getElementName() + "' in '" + method.getDeclaringType().getFullyQualifiedName() + "'.");
    }
    List<MethodParameter> params = new ArrayList<MethodParameter>();
    for (int i = 0; i < paramNames.length; i++) {
      params.add(new MethodParameter(paramNames[i], resolvedParamSignatures[i]));
    }
    return params.toArray(new MethodParameter[params.size()]);
  }

  public static String[] toSignatureArray(List<MethodParameter> parameters) {
    if (parameters == null || parameters.isEmpty()) {
      return new String[0];
    }
    String[] result = new String[parameters.size()];
    int i = 0;
    for (MethodParameter param : parameters) {
      result[i++] = param.getSignature();
    }
    return result;
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
    try {
      IField[] fields = declaringType.getFields();
      if (filter == null && comparator == null) {
        return fields;
      }

      Collection<IField> collector = null;
      if (comparator == null) {
        collector = new ArrayList<IField>(fields.length);
      }
      else {
        collector = new TreeSet<IField>(comparator);
      }

      for (IField field : fields) {
        if (filter == null || filter.accept(field)) {
          collector.add(field);
        }
      }
      return collector.toArray(new IField[collector.size()]);
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get fields of '" + declaringType.getElementName() + "'.", e);
      return new IField[]{};
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

    // some types may not be part of the compilation unit (e.g. declaringType is binary, then there is no compilation unit) and cannot be resolved in the class file.
    // this can happen when e.g. only a reference to a final static field is in the class file and there is no other reference to the class.
    // then the compiler removes this reference and directly puts the value of the field in the class file even though the reference remains in the source of the class.
    // the originating class can then not be found anymore. This happens e.g. with the AbstractIcons reference in AbstractSmartField.
    // to solve this, try to find a unique type in the workspace with the simple name. If there is only one match, we are happy.
    IType[] candidates = TypeUtility.getTypes(typeName);
    if (candidates != null && candidates.length == 1) {
      return candidates[0];
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

  /**
   * checks whether element is on the classpath of the given project
   * 
   * @param element
   *          the element to search
   * @param project
   *          the project classpath to search in
   * @return true if element was found in the classpath of project
   */
  public static boolean isOnClasspath(IJavaElement element, IJavaProject project) {
    if (!TypeUtility.exists(element)) {
      return false;
    }
    if (!TypeUtility.exists(project)) {
      return false;
    }

    if (element instanceof IMember) {
      IMember member = (IMember) element;
      if (member.isBinary()) {
        return project.isOnClasspath(member);
      }
    }

    IJavaProject elemenProject = element.getJavaProject();
    if (elemenProject != null) {
      if (project.equals(elemenProject)) {
        return true;
      }
      else {
        return project.isOnClasspath(elemenProject);
      }
    }
    return project.isOnClasspath(element);
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
    IMethodFilter filter = MethodFilters.getMultiMethodFilter(MethodFilters.getFlagsFilter(Flags.AccPublic), MethodFilters.getNameRegexFilter(BEAN_METHOD_NAME));
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
    ArrayList<PropertyBean> filteredBeans = new ArrayList<PropertyBean>(beans.size());
    for (PropertyBean bean : beans.values()) {
      if (propertyFilter == null || propertyFilter.accept(bean)) {
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

  public static int getIndent(IJavaElement element) {
    int indent = 0;
    IJavaElement visitedElement = element;
    while (visitedElement.getElementType() != IJavaElement.COMPILATION_UNIT) {
      indent++;
      visitedElement = element.getParent();
    }
    return indent;
  }

  /**
   * Tries to find a method in the given type and all super types and super interfaces.<br>
   * If multiple methods with the same name exist in a type (overloads), the first is returned as they appear in the
   * source or class file.
   * 
   * @param methodName
   *          the name of the method
   * @param type
   *          The start type in which (together with its super types and super interfaces) the given method should be
   *          searched.
   * @param superTypeHierarchy
   *          The super type hierarchy of the given type.
   * @return The first method found in the type itself, its super types or super interfaces (searched in this order). If
   *         multiple methods with the same name exist in a type (overloads), the first is returned as they appear in
   *         the source or class file.
   */
  public static IMethod findMethodInSuperHierarchy(String methodName, IType type, ITypeHierarchy superTypeHierarchy) {
    return findMethodInSuperTypeHierarchy(type, superTypeHierarchy, MethodFilters.getNameFilter(methodName));
  }

  public static IMethod findMethodInSuperTypeHierarchy(IType type, ITypeHierarchy superTypeHierarchy, IMethodFilter filter) {
    IMethod[] methods = getMethods(type, filter);
    IMethod method = null;
    if (methods.length == 1) {
      return methods[0];
    }
    else if (methods.length > 1) {
      StringBuilder sb = new StringBuilder(" [\n");
      sb.append("\t\t").append(methods[0].toString());
      for (int i = 1; i < methods.length; i++) {
        sb.append(", \n\t\t").append(methods[i].toString());
      }
      sb.append("\n\t]");
      SdkUtilActivator.logWarning("found more than one method in hierarchy" + sb.toString());
      return methods[0];
    }
    else {
      // super types
      IType superType = superTypeHierarchy.getSuperclass(type);
      if (TypeUtility.exists(superType) && !superType.getElementName().equals(Object.class.getName())) {
        method = findMethodInSuperTypeHierarchy(superType, superTypeHierarchy, filter);
      }
      if (TypeUtility.exists(method)) {
        return method;
      }
      // interfaces
      for (IType intType : superTypeHierarchy.getSuperInterfaces(type)) {
        if (TypeUtility.exists(intType) && !intType.getElementName().equals(Object.class.getName())) {
          method = findMethodInSuperTypeHierarchy(intType, superTypeHierarchy, filter);
        }
        if (TypeUtility.exists(method)) {
          return method;
        }
      }
    }
    return null;
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
