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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutSdkUtilCore;
import org.eclipse.scout.sdk.util.ast.AstUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.internal.typecache.HierarchyCache;
import org.eclipse.scout.sdk.util.internal.typecache.TypeCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchyResult;
import org.eclipse.scout.sdk.util.typecache.ITypeCache;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.TypeHierarchyConstraints;

public class TypeUtility {

  public static final String DEFAULT_SOURCE_FOLDER_NAME = "src";
  public static final Pattern BEAN_METHOD_NAME = Pattern.compile("(get|set|is)([A-Z].*)");

  protected TypeUtility() {
  }

  /**
   * @see ITypeCache#getType(String)
   */
  public static IType getType(String typeName) {
    return TypeCache.getInstance().getType(typeName);
  }

  /**
   * @see ITypeCache#getTypes(String)
   */
  public static Set<IType> getTypes(String typeName) {
    return TypeCache.getInstance().getTypes(typeName);
  }

  /**
   * Gets all types of the given {@link ICompilationUnit} that match the given filter recursively.
   *
   * @param icu
   *          The {@link ICompilationUnit} for which all types should be returned.
   * @param filter
   *          The filter or null if not filtering is desired.
   * @return A {@link List} containing all types recursively within the given {@link ICompilationUnit}.
   * @throws JavaModelException
   */
  public static List<IType> getAllTypes(ICompilationUnit icu, ITypeFilter filter) throws JavaModelException {
    List<IType> result = new ArrayList<IType>();
    for (IType t : icu.getTypes()) {
      collectTypesInType(t, result, filter);
    }
    return result;
  }

  private static void collectTypesInType(IType type, Collection<IType> result, ITypeFilter filter) throws JavaModelException {
    if (filter == null || filter.accept(type)) {
      result.add(type);
    }

    for (IType t : type.getTypes()) {
      collectTypesInType(t, result, filter);
    }
  }

  public static boolean existsType(String typeName) {
    return exists(getType(typeName));
  }

  public static IPackageFragmentRoot getSrcPackageFragmentRoot(IJavaProject project) throws JavaModelException {
    return project.findPackageFragmentRoot(new Path(IPath.SEPARATOR + project.getElementName() + IPath.SEPARATOR + DEFAULT_SOURCE_FOLDER_NAME));
  }

  public static IPackageFragment getPackage(IJavaElement element) {
    IPackageFragment packageFragment = (IPackageFragment) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
    return packageFragment;
  }

  public static IPackageFragment getPackage(IJavaProject project, String packageName) throws JavaModelException {
    return getSrcPackageFragmentRoot(project).getPackageFragment(packageName);
  }

  public static Set<IType> getInnerTypes(IType type) {
    return getInnerTypes(type, null);
  }

  public static Set<IType> getInnerTypes(IType type, ITypeFilter filter) {
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
  public static Set<IType> getInnerTypes(IType type, ITypeFilter filter, Comparator<IType> comparator) {
    IType[] types = null;
    try {
      types = type.getTypes();
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get inner types of '" + type.getFullyQualifiedName() + "'", e);
      return CollectionUtility.hashSet();
    }

    Set<IType> result = null;
    if (comparator == null) {
      result = new HashSet<IType>(types.length);
    }
    else {
      result = new TreeSet<IType>(comparator);
    }

    for (IType subtype : types) {
      if (filter == null || filter.accept(subtype)) {
        result.add(subtype);
      }
    }
    return result;
  }

  /**
   * Returns the immediate member types declared by the given type which are subtypes of the given supertype. The
   * results is sorted using the given comparator.
   *
   * @param declaringType
   *          The type whose immediate inner types should be returned.
   * @param supertype
   *          The supertype for which all returned types must be a subtype.
   * @param comparator
   *          the comparator to sort the result.
   * @return the immediate member types declared by the given type which are subtypes of the given supertype.
   */
  public static Set<IType> getInnerTypesOrdered(IType declaringType, IType supertype, Comparator<IType> comparator) {
    ITypeHierarchy localTypeHierarchy = getLocalTypeHierarchy(declaringType);
    Set<IType> allSubtypes = getInnerTypes(declaringType, TypeFilters.getSubtypeFilter(supertype, localTypeHierarchy), comparator);
    return allSubtypes;
  }

  public static IType getTypeBySignature(String signature) {
    if (signature == null) {
      return null;
    }
    return getType(SignatureUtility.getFullyQualifiedName(signature));
  }

  /**
   * To get a type hierarchy with the given elements as scope.
   *
   * @param elements
   * @return
   * @throws JavaModelException
   */
  public static ITypeHierarchy getLocalTypeHierarchy(Collection<? extends IJavaElement> elements) {
    IRegion region = JavaCore.newRegion();
    if (elements != null) {
      for (IJavaElement e : elements) {
        if (exists(e)) {
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
    }
    return getLocalTypeHierarchy(region);
  }

  /**
   * To get a type hierarchy with the given elements as scope.
   *
   * @param elements
   * @return
   * @throws JavaModelException
   */
  public static ITypeHierarchy getLocalTypeHierarchy(IJavaElement... elements) {
    return getLocalTypeHierarchy(CollectionUtility.hashSet(elements));
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
    return HierarchyCache.getInstance().getLocalTypeHierarchy(region);
  }

  public static ITypeHierarchy getSupertypeHierarchy(IType type) {
    return HierarchyCache.getInstance().getSupertypeHierarchy(type);
  }

  /**
   * To get a type hierarchy containing this type, all of its supertypes, and all its subtypes.<br>
   *
   * @param type
   *          the type to get the hierarchy for
   * @return the cached type hierarchy or null if type does not exist or hierarchy could not be created.
   */
  public static ICachedTypeHierarchy getTypeHierarchy(IType type) {
    return HierarchyCache.getInstance().getTypeHierarchy(type);
  }

  /**
   * Creates a primary type hierarchy only containing primary {@link IType}s.<br>
   * Primary types are all except nested types. Or more formally: {@link IType}s for which
   * <code>{@link IType#getDeclaringType()} == null</code>.
   *
   * @param type
   *          The base type of the primary type hierarchy.
   * @return The primary type hierarchy. The hierarchy will only be initialized with values on first use and will be
   *         cached for later re-use.
   * @throws IllegalArgumentException
   *           if the given type is not a primary type.
   */
  public static ICachedTypeHierarchy getPrimaryTypeHierarchy(IType type) throws IllegalArgumentException {
    return HierarchyCache.getInstance().getPrimaryTypeHierarchy(type);
  }

  public static boolean hasInnerType(IType declaringType, String typeName) {
    IType candidate = declaringType.getType(typeName);
    return candidate != null && candidate.exists();
  }

  public static IMethod getMethod(IType declaringType, String methodName, List<String> resolvedParameterSignatures) throws CoreException {
    for (IMethod m : declaringType.getMethods()) {
      if (CompareUtility.equals(m.getElementName(), methodName)) {
        // signature compare
        List<String> parameterSignatures = SignatureUtility.getMethodParameterSignatureResolved(m);
        if (parameterSignatures.size() == resolvedParameterSignatures.size()) {
          boolean signatureEquals = true;
          for (int i = 0; i < parameterSignatures.size(); i++) {
            if (!CompareUtility.equals(resolvedParameterSignatures.get(i), parameterSignatures.get(i))) {
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

  public static IMethod findMethodInSuperclassHierarchy(IType type, IMethodFilter filter) {
    return findMethodInSuperclassHierarchy(type, ScoutSdkUtilCore.getHierarchyCache().getSupertypeHierarchy(type), filter);
  }

  public static IMethod findMethodInSuperclassHierarchy(IType type, ITypeHierarchy hierarchy, IMethodFilter filter) {
    if (exists(type)) {
      IMethod method = getFirstMethod(type, filter);
      if (exists(method)) {
        return method;
      }
      else {
        return findMethodInSuperclassHierarchy(hierarchy.getSuperclass(type), hierarchy, filter);
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
   * If multiple methods with the same name exist (overloads), the returned method is undefined.
   *
   * @param type
   *          The type in which the method should be searched.
   * @param methodName
   *          The name of the method.
   * @return The first method found or null.
   */
  public static IMethod getMethod(IType type, final String methodName) {
    Set<IMethod> methods = getMethods(type, MethodFilters.getNameFilter(methodName));
    if (CollectionUtility.hasElements(methods)) {
      return CollectionUtility.firstElement(methods);
    }
    return null;
  }

  /**
   * Gets all methods in the given type.<br>
   * The methods are in no particular order.
   *
   * @param type
   *          The type to get all methods of.
   * @return A {@link Set} of all methods of the given type. Never returns null.
   */
  public static Set<IMethod> getMethods(IType type) {
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
  public static Set<IMethod> getMethods(IType type, IMethodFilter filter) {
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
  public static Set<IMethod> getMethods(IType type, IMethodFilter filter, Comparator<IMethod> comparator) {
    try {
      IMethod[] methods = type.getMethods();
      if (filter == null && comparator == null) {
        return CollectionUtility.hashSet(methods);
      }

      Set<IMethod> collector = null;
      if (comparator == null) {
        collector = new HashSet<IMethod>(methods.length);
      }
      else {
        collector = new TreeSet<IMethod>(comparator);
      }

      for (IMethod method : methods) {
        if (filter == null || filter.accept(method)) {
          collector.add(method);
        }
      }
      return collector;
    }
    catch (CoreException e) {
      SdkUtilActivator.logWarning("could not get methods of '" + type.getFullyQualifiedName() + "' with filter '" + filter + "'.", e);
      return CollectionUtility.hashSet();
    }
  }

  public static ISourceRange getContentSourceRange(IMethod method) throws JavaModelException {
    ASTParser parser = AstUtility.newParser();
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

  public static List<MethodParameter> getMethodParameters(IMethod method, IType contextType) throws CoreException {
    String[] paramNames = method.getParameterNames();
    List<String> resolvedParamSignatures = SignatureUtility.getMethodParameterSignatureResolved(method, contextType);
    if (paramNames.length != resolvedParamSignatures.size()) {
      throw new IllegalArgumentException("Could not resolve method parameters of '" + method.getElementName() + "' in '" + method.getDeclaringType().getFullyQualifiedName() + "'.");
    }
    List<MethodParameter> params = new ArrayList<MethodParameter>(paramNames.length);
    for (int i = 0; i < paramNames.length; i++) {
      params.add(new MethodParameter(paramNames[i], resolvedParamSignatures.get(i)));
    }
    return params;
  }

  public static List<MethodParameter> getMethodParameters(IMethod method, Map<String, String> generics) throws CoreException {
    String[] paramNames = method.getParameterNames();
    List<String> resolvedParamSignatures = SignatureUtility.getMethodParameterSignatureResolved(method, generics);
    if (paramNames.length != resolvedParamSignatures.size()) {
      throw new IllegalArgumentException("Could not resolve method parameters of '" + method.getElementName() + "' in '" + method.getDeclaringType().getFullyQualifiedName() + "'.");
    }
    List<MethodParameter> params = new ArrayList<MethodParameter>(paramNames.length);
    for (int i = 0; i < paramNames.length; i++) {
      params.add(new MethodParameter(paramNames[i], resolvedParamSignatures.get(i)));
    }
    return params;
  }

  public static IType getAncestor(IType type, ITypeFilter filter) {
    IType ancestorType = type;
    while (exists(ancestorType)) {
      if (filter.accept(ancestorType)) {
        return ancestorType;
      }
      ancestorType = ancestorType.getDeclaringType();
    }
    return null;
  }

  public static Set<IField> getFields(IType declaringType) {
    return getFields(declaringType, null);
  }

  public static Set<IField> getFields(IType declaringType, IFieldFilter filter) {
    return getFields(declaringType, filter, null);
  }

  public static Set<IField> getFields(IType declaringType, IFieldFilter filter, Comparator<IField> comparator) {
    try {
      IField[] fields = declaringType.getFields();
      if (filter == null && comparator == null) {
        return CollectionUtility.hashSet(fields);
      }

      Set<IField> collector = null;
      if (comparator == null) {
        collector = new HashSet<IField>(fields.length);
      }
      else {
        collector = new TreeSet<IField>(comparator);
      }

      for (IField field : fields) {
        if (filter == null || filter.accept(field)) {
          collector.add(field);
        }
      }
      return collector;
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logWarning("could not get fields of '" + declaringType.getElementName() + "'.", e);
      return CollectionUtility.hashSet();
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

  public static IMethod getOverwrittenMethod(IMethod method, ITypeHierarchy supertypeHierarchy) {
    IType supertype = supertypeHierarchy.getSuperclass(method.getDeclaringType());
    IMethodFilter overrideFilter = MethodFilters.getSuperMethodFilter(method);
    while (supertype != null) {
      IMethod superMethod = getFirstMethod(supertype, overrideFilter);
      if (superMethod != null) {
        return superMethod;
      }
      supertype = supertypeHierarchy.getSuperclass(supertype);
    }
    return null;
  }

  /**
   * <code>
   * class A{<br>
   * &nbsp;&nbsp;class B{<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;class C{<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;}<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;class D{<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;}<br>
   * &nbsp;&nbsp;}<br>
   * }<br>
   * // A.getTopLevelType() returns A<br>
   * // D.getTopLevelType() returns A
   * </code>
   *
   * @return the primary type of the compilation unit this type is declared in.
   */
  public static IType getToplevelType(IJavaElement e) {
    if (e == null) {
      return null;
    }
    IType surroundingType = (IType) e.getAncestor(IJavaElement.TYPE);
    if (!exists(surroundingType)) {
      return null; // element is not within a type.
    }

    IType result = null;
    IType tmp = surroundingType;
    while (exists(tmp)) {
      result = tmp;
      tmp = tmp.getDeclaringType();
    }

    return result;
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

  /**
   * Tries to find the fully qualified class name of the given simple name based on the context of the given type.
   *
   * @param declaringType
   *          The context type for which the simple name should be resolved.
   * @param typeName
   *          The type name to resolve.
   * @param searchOnClassPath
   *          specifies if the classpath of the {@link IJavaProject} of the declaringType should be searched as well.
   * @return The fully qualified name of the given simple name for the given context or null.
   * @throws JavaModelException
   */
  public static String getReferencedTypeFqn(IType declaringType, String typeName, boolean searchOnClassPath) throws JavaModelException {
    // 1. check the imports (performance improvement)
    ICompilationUnit compilationUnit = declaringType.getCompilationUnit();
    if (compilationUnit != null) {
      String searchString = '.' + typeName;
      IImportDeclaration[] imports = compilationUnit.getImports();
      for (IImportDeclaration imp : imports) {
        if (imp.getElementName().endsWith(searchString)) {
          return imp.getElementName();
        }
      }
    }

    // 2. try to resolve
    String[][] resolvedTypeName = declaringType.resolveType(typeName);
    if (resolvedTypeName != null && resolvedTypeName.length == 1) {
      String pck = resolvedTypeName[0][0];
      StringBuilder fqName = new StringBuilder();
      if (pck != null && pck.length() > 0) {
        fqName.append(pck);
        fqName.append('.');
      }
      fqName.append(resolvedTypeName[0][1]); // class simple name
      return fqName.toString();
    }

    if (searchOnClassPath) {
      // 3. try to find a matching type on the classpath
      // some types may not be part of the compilation unit (e.g. declaringType is binary, then there is no compilation unit) and cannot be resolved in the class file.
      // this can happen when e.g. only a reference to a final static field is in the class file and there is no other reference to the class.
      // then the compiler removes this reference and directly puts the value of the field in the class file even though the reference remains in the source of the class.
      // the originating class can then not be found anymore. This happens e.g. with the AbstractIcons reference in AbstractSmartField.
      // to solve this, try to find a unique type in the workspace with the simple name. If there is only one match, we are happy.
      IJavaProject javaProject = declaringType.getJavaProject();
      if (exists(javaProject)) {
        Set<IType> candidates = getTypes(typeName);

        // remove all that are not on the requested classpath
        Iterator<IType> iterator = candidates.iterator();
        while (iterator.hasNext()) {
          IType cur = iterator.next();
          if (!isOnClasspath(cur, javaProject)) {
            iterator.remove();
          }
        }

        if (candidates.size() == 1) {
          return CollectionUtility.firstElement(candidates).getFullyQualifiedName();
        }
      }
    }
    return null;
  }

  /**
   * @return The {@link IType} of the fully qualified name found or null.
   * @see TypeUtility#getReferencedTypeFqn(IType, String, boolean)
   */
  public static IType getReferencedType(IType declaringType, String typeName, boolean searchOnClassPath) throws JavaModelException {
    String referencedTypeFqn = getReferencedTypeFqn(declaringType, typeName, searchOnClassPath);
    if (referencedTypeFqn != null) {
      return getType(referencedTypeFqn);
    }
    return null;
  }

  public static boolean isGenericType(IType type) {
    return getTypeParameters(type).size() > 0;
  }

  /**
   * Gets the type that is more specific. This means:<br>
   * If a is a subtype of b or b is null: a is returned.<br>
   * If b is a subtype of a or a is null: b is returned.<br>
   * If both are null or they have no common supertype: null is returned.
   *
   * @param a
   *          The first {@link IType}
   * @param b
   *          The second {@link IType}
   * @return The more specific type or null according to the rule described above.
   * @throws JavaModelException
   *           Occurred during supertype creation.
   */
  public static IType getMoreSpecificType(IType a, IType b) throws JavaModelException {
    if (!exists(a) && !exists(b)) {
      return null;
    }
    if (!exists(a)) {
      return b;
    }
    if (!exists(b)) {
      return a;
    }
    if (ScoutSdkUtilCore.getHierarchyCache().getSupertypeHierarchy(a).contains(b)) {
      return a;
    }
    else if (ScoutSdkUtilCore.getHierarchyCache().getSupertypeHierarchy(b).contains(a)) {
      return b;
    }
    else {
      return null;
    }
  }

  public static List<ITypeParameter> getTypeParameters(IType type) {
    if (TypeUtility.exists(type)) {
      try {
        return CollectionUtility.arrayList(type.getTypeParameters());
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logWarning("could not get generic information of type: " + type.getFullyQualifiedName(), e);
      }
    }
    return CollectionUtility.arrayList();
  }

  public static ICachedTypeHierarchyResult getAbstractTypesOnClasspathHierarchy(IType hierarchyBaseType, IJavaProject project) {
    TypeHierarchyConstraints constraints = new TypeHierarchyConstraints(hierarchyBaseType, project);
    constraints.modifiersNotSet(Flags.AccInterface, Flags.AccDeprecated).modifiersSet(Flags.AccAbstract, Flags.AccPublic);
    ICachedTypeHierarchyResult h = HierarchyCache.getInstance().getProjectContextTypeHierarchy(constraints);
    return h;
  }

  public static Set<IType> getAbstractTypesOnClasspath(IType hierarchyBaseType, IJavaProject project, ITypeFilter filter) {
    ICachedTypeHierarchyResult h = getAbstractTypesOnClasspathHierarchy(hierarchyBaseType, project);
    return h.getAllTypes(filter, TypeComparators.getTypeNameComparator());
  }

  public static Set<IType> getClassesOnClasspath(IType supertype, IJavaProject project, ITypeFilter filter) {
    TypeHierarchyConstraints constraints = new TypeHierarchyConstraints(supertype, project);
    constraints.modifiersNotSet(Flags.AccAbstract, Flags.AccDeprecated, Flags.AccInterface);
    ICachedTypeHierarchyResult h = HierarchyCache.getInstance().getProjectContextTypeHierarchy(constraints);
    return h.getAllTypes(filter, TypeComparators.getTypeNameComparator());
  }

  public static Set<IType> getInterfacesOnClasspath(IType supertype, IJavaProject project, ITypeFilter filter) {
    TypeHierarchyConstraints constraints = new TypeHierarchyConstraints(supertype, project);
    constraints.modifiersSet(Flags.AccInterface);
    ICachedTypeHierarchyResult h = HierarchyCache.getInstance().getProjectContextTypeHierarchy(constraints);
    return h.getAllTypes(filter, TypeComparators.getTypeNameComparator());
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
    if (!exists(element)) {
      return false;
    }
    if (!exists(project)) {
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
  public static Set<? extends IPropertyBean> getPropertyBeans(IType type, IPropertyBeanFilter propertyFilter, Comparator<IPropertyBean> comparator) {
    IMethodFilter filter = MethodFilters.getMultiMethodFilter(MethodFilters.getFlagsFilter(Flags.AccPublic), MethodFilters.getNameRegexFilter(BEAN_METHOD_NAME));
    Set<IMethod> methods = getMethods(type, filter);
    Map<String, PropertyBean> beans = new HashMap<String, PropertyBean>(methods.size());
    for (IMethod m : methods) {
      Matcher matcher = BEAN_METHOD_NAME.matcher(m.getElementName());
      if (matcher.matches()) {
        try {
          String kind = matcher.group(1);
          String name = matcher.group(2);
          //
          String[] parameterTypes = m.getParameterTypes();
          String returnType = m.getReturnType();
          if ("get".equals(kind) && parameterTypes.length == 0 && !returnType.equals(Signature.SIG_VOID)) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.getReadMethod() == null) {
              desc.setReadMethod(m);
            }
          }
          else if ("is".equals(kind) && parameterTypes.length == 0 && returnType.equals(Signature.SIG_BOOLEAN)) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.getReadMethod() == null) {
              desc.setReadMethod(m);
            }
          }
          else if ("set".equals(kind) && parameterTypes.length == 1 && returnType.equals(Signature.SIG_VOID)) {
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
    Set<PropertyBean> filteredBeans = null;
    if (comparator == null) {
      filteredBeans = new HashSet<PropertyBean>(beans.size());
    }
    else {
      filteredBeans = new TreeSet<PropertyBean>(comparator);
    }

    for (PropertyBean bean : beans.values()) {
      if (propertyFilter == null || propertyFilter.accept(bean)) {
        filteredBeans.add(bean);
      }
    }

    // fields
    Set<IField> fieldCandidates = getFields(type, FieldFilters.getPrivateNotStaticNotFinalNotAbstract(), null);
    HashMap<String, IField> fields = new HashMap<String, IField>(fieldCandidates.size());
    for (IField field : fieldCandidates) {
      fields.put(field.getElementName(), field);
    }

    // get field pre- and suffixes
    @SuppressWarnings("restriction")
    org.eclipse.jdt.internal.codeassist.impl.AssistOptions assistOptions = new org.eclipse.jdt.internal.codeassist.impl.AssistOptions(type.getJavaProject().getOptions(true));
    @SuppressWarnings("restriction")
    Set<String> fieldPrefixes = toStringSet(assistOptions.fieldPrefixes, "m_", "");
    @SuppressWarnings("restriction")
    Set<String> fieldSuffixes = toStringSet(assistOptions.fieldSuffixes, "");

    for (PropertyBean bean : filteredBeans) {
      IField field = findFieldForPropertyBean(bean.getBeanName(), fields, fieldPrefixes, fieldSuffixes);
      if (field != null) {
        bean.setField(field);
      }
      else {
        SdkUtilActivator.logWarning("Unable to find field for property bean [" + bean + "]");
      }
    }

    return filteredBeans;
  }

  /**
   * To find out if the given child element is an ancestor of the given parent element. If parent and child is the same
   * element true is returned.
   *
   * @param parent
   * @param child
   * @return
   */
  public static boolean isAncestor(IJavaElement parent, IJavaElement child) {
    if (parent == null || child == null) {
      return false;
    }
    if (parent.equals(child)) {
      return true;
    }
    else if (child.getParent() != null && child.getParent().getElementType() >= parent.getElementType()) {
      return isAncestor(parent, child.getParent().getAncestor(parent.getElementType()));
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
   * Tries to find a method in the given type and all supertypes and super interfaces.<br>
   * If multiple methods with the same name exist in a type (overloads), the first is returned as they appear in the
   * source or class file.
   *
   * @param methodName
   *          the name of the method
   * @param type
   *          The start type in which (together with its supertypes and super interfaces) the given method should be
   *          searched.
   * @param supertypeHierarchy
   *          The supertype hierarchy of the given type.
   * @return The first method found in the type itself, its supertypes or super interfaces (searched in this order). If
   *         multiple methods with the same name exist in a type (overloads), the first is returned as they appear in
   *         the source or class file.
   */
  public static IMethod findMethodInSupertypeHierarchy(String methodName, IType type, ITypeHierarchy supertypeHierarchy) {
    return findMethodInSupertypeHierarchy(type, supertypeHierarchy, MethodFilters.getNameFilter(methodName));
  }

  public static IMethod findMethodInSupertypeHierarchy(IType type, ITypeHierarchy supertypeHierarchy, IMethodFilter filter) {
    Set<IMethod> methods = getMethods(type, filter);
    IMethod method = null;
    IMethod first = CollectionUtility.firstElement(methods);
    if (methods.size() == 1) {
      return first;
    }
    else if (methods.size() > 1) {
      StringBuilder sb = new StringBuilder(" [\n");
      for (IMethod m : methods) {
        if (m != first) {
          sb.append(",\n");
        }
        sb.append("\t\t").append(m.toString());
      }
      sb.append("\n\t]");
      SdkUtilActivator.logWarning("found more than one method in hierarchy" + sb.toString());
      return first;
    }
    else {
      // supertypes
      IType supertype = supertypeHierarchy.getSuperclass(type);
      if (exists(supertype) && !supertype.getElementName().equals(Object.class.getName())) {
        method = findMethodInSupertypeHierarchy(supertype, supertypeHierarchy, filter);
      }
      if (exists(method)) {
        return method;
      }
      // interfaces
      for (IType intType : supertypeHierarchy.getSuperInterfaces(type)) {
        if (exists(intType) && !intType.getElementName().equals(Object.class.getName())) {
          method = findMethodInSupertypeHierarchy(intType, supertypeHierarchy, filter);
        }
        if (exists(method)) {
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
  private static IField findFieldForPropertyBean(String beanName, HashMap<String, IField> fields, Set<String> fieldPrefixes, Set<String> fieldSuffixes) {
    for (String prefix : fieldPrefixes) {
      for (String suffix : fieldSuffixes) {
        IField field = fields.get(prefix + NamingUtility.ensureStartWithLowerCase(beanName) + suffix);
        if (TypeUtility.exists(field)) {
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
  private static Set<String> toStringSet(char[][] arrayOfChars, String... additionalValues) {
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
    return result;
  }
}
