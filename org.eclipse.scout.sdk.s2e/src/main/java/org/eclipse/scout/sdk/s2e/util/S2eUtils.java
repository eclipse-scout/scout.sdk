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
package org.eclipse.scout.sdk.s2e.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;
import org.eclipse.scout.sdk.s2e.workspace.CompilationUnitWriteOperation;
import org.eclipse.scout.sdk.s2e.workspace.ResourceWriteOperation;

/**
 * <h3>{@link S2eUtils}</h3>
 * <p>
 * Contains utility methods for the Scout SDK Eclipse integration.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class S2eUtils {
  private static final Comparator<IType> COMPARATOR = new P_TypeMatchComparator();

  private S2eUtils() {
  }

  /**
   * Converts the {@link IType} to {@link org.eclipse.scout.sdk.core.model.api.IType}
   *
   * @param jdtType
   *          The input jdt {@link IType}
   * @return The resulting {@link org.eclipse.scout.sdk.core.model.api.IType}
   * @throws CoreException
   */
  public static org.eclipse.scout.sdk.core.model.api.IType jdtTypeToScoutType(IType jdtType) throws CoreException {
    return jdtTypeToScoutType(jdtType, ScoutSdkCore.createJavaEnvironment(jdtType.getJavaProject()));
  }

  /**
   * Gets the {@link IPackageFragmentRoot} of the given {@link IJavaElement}.
   *
   * @param e
   *          The {@link IJavaElement} for which the source folder should be returned.
   * @return The {@link IPackageFragmentRoot} of the given element.
   */
  public static IPackageFragmentRoot getSourceFolder(IJavaElement e) {
    if (e == null) {
      return null;
    }
    return (IPackageFragmentRoot) e.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
  }

  /**
   * Gets the package name of the given {@link ICompilationUnit}.
   *
   * @param icu
   *          The compilation unit for which the package should be returned.
   * @return The package or empty {@link String} if the compilation unit declares no package
   * @throws JavaModelException
   */
  public static String getPackage(ICompilationUnit icu) throws JavaModelException {
    String pck = "";
    if (icu != null) {
      IPackageDeclaration[] packageDeclarations = icu.getPackageDeclarations();
      if (packageDeclarations.length > 0) {
        pck = packageDeclarations[0].getElementName();
      }
    }
    return pck;
  }

  /**
   * Converts the given {@link IType} to {@link org.eclipse.scout.sdk.core.model.api.IType}
   *
   * @param jdtType
   *          The jdt {@link IType} to convert.
   * @param env
   *          The {@link IJavaEnvironment} to use to find the matching
   *          {@link org.eclipse.scout.sdk.core.model.api.IType}.
   * @return The {@link org.eclipse.scout.sdk.core.model.api.IType} matching the given JDT {@link IType}.
   * @throws CoreException
   */
  public static org.eclipse.scout.sdk.core.model.api.IType jdtTypeToScoutType(IType jdtType, IJavaEnvironment env) {
    return env.findType(jdtType.getFullyQualifiedName('$'));
  }

  /**
   * Gets all fully qualified type names of abstract public classes existing on the classpath of the given project and
   * implementing the given baseTypeFqn.
   *
   * @param sourceProject
   *          The {@link IJavaProject} defining the classpath.
   * @param baseTypeFqn
   *          The fully qualified name of the base class. The sub classes of this class are searched.
   * @param monitor
   *          The monitor or <code>null</code>.
   * @return A {@link Set} sorted ascending holding the fully qualified names.
   * @throws CoreException
   */
  public static Set<String> findAbstractClassesInHierarchy(final IJavaProject sourceProject, String baseTypeFqn, IProgressMonitor monitor) throws CoreException {
    IFilter<TypeNameMatch> filter = new IFilter<TypeNameMatch>() {
      @Override
      public boolean evaluate(TypeNameMatch match) {
        int modifiers = match.getModifiers();
        if (Flags.isAbstract(modifiers) && Flags.isPublic(modifiers) && !Flags.isDeprecated(modifiers) && !Flags.isInterface(modifiers)) {
          boolean isPrimaryType = match.getPackageName().equals(match.getTypeContainerName());
          return isPrimaryType;
        }
        return false;
      }
    };
    return findClassesInStrictHierarchy(sourceProject, baseTypeFqn, monitor, filter);
  }

  /**
   * Gets all fully qualified type names of sub classes of the given baseTypeFqn that are on the classpath of the given
   * project and fulfill the given filter.
   *
   * @param sourceProject
   *          The {@link IJavaProject} defining the classpath.
   * @param baseTypeFqn
   *          The fully qualified name of the base class of the hierarchy.
   * @param monitor
   *          The monitor or <code>null</code>.
   * @param filter
   *          A filter to decide which matches are accepted or <code>null</code> if all matches should be accepted.
   * @return A {@link Set} sorted ascending holding the fully qualified names.
   * @throws CoreException
   */
  public static Set<String> findClassesInStrictHierarchy(final IJavaProject sourceProject, String baseTypeFqn, IProgressMonitor monitor, final IFilter<TypeNameMatch> filter) throws CoreException {
    final Set<String> collector = new TreeSet<>();
    TypeNameMatchRequestor nameMatchRequestor = new TypeNameMatchRequestor() {
      @Override
      public void acceptTypeNameMatch(TypeNameMatch match) {
        if (filter == null || filter.evaluate(match)) {
          collector.add(match.getFullyQualifiedName());
        }
      }
    };

    SearchEngine e = new SearchEngine(JavaCore.getWorkingCopies(null));
    IType baseType = sourceProject.findType(baseTypeFqn.replace('$', '.'));
    if (!exists(baseType)) {
      return collector;
    }

    IJavaSearchScope strictHierarchyScope = SearchEngine.createStrictHierarchyScope(sourceProject, baseType, true, false, null);
    e.searchAllTypeNames(null, SearchPattern.R_EXACT_MATCH, null, SearchPattern.R_EXACT_MATCH, IJavaSearchConstants.CLASS, strictHierarchyScope,
        nameMatchRequestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
    return collector;
  }

  /**
   * Gets the first {@link IMethod} for which the given {@link IFilter} evaluates to <code>true</code>.<br>
   * <b>Note:</b> Inner Types are not searched.
   *
   * @param type
   *          The {@link IType} in which the {@link IMethod} should be searched.
   * @param filter
   *          The {@link IFilter}.
   * @return The first {@link IMethod} found in the given type or <code>null</code> if it could not be found.
   * @throws JavaModelException
   */
  public static IMethod getFirstMethod(IType type, IFilter<IMethod> filter) throws JavaModelException {
    for (IMethod method : type.getMethods()) {
      if (filter == null || filter.evaluate(method)) {
        return method;
      }
    }
    return null;
  }

  /**
   * Gets all {@link IType}s that are accessible in the current workspace having the given fully qualified name.
   *
   * @param fqn
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator
   *          (e.g. <code>org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass</code>).
   * @return
   * @throws CoreException
   */
  public static Set<IType> resolveJdtTypes(final String fqn) throws CoreException {
    return resolveJdtTypes(fqn, SearchEngine.createWorkspaceScope());
  }

  /**
   * Gets all {@link IType}s that are accessible in the given scope having the given fully qualified name.
   *
   * @param fqn
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator
   *          (e.g. <code>org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass</code>).
   * @param scope
   *          The {@link IJavaSearchScope} that defines where to search.
   * @return A {@link Set} with all {@link IType}s
   * @throws CoreException
   */
  public static Set<IType> resolveJdtTypes(final String fqn, IJavaSearchScope scope) throws CoreException {
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = Signature.getSimpleName(fqn);
    final Set<IType> matchList = new TreeSet<>(COMPARATOR);
    new SearchEngine().search(SearchPattern.createPattern(fastPat, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH), new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
        scope, new SearchRequestor() {
          @Override
          public void acceptSearchMatch(SearchMatch match) throws CoreException {
            Object element = match.getElement();
            if (element instanceof IType) {
              IType t = (IType) element;
              if (t.getFullyQualifiedName('$').indexOf(fqn) >= 0) {
                matchList.add(t);
              }
            }
          }
        }, null);
    return matchList;
  }

  /**
   * Blocks the calling thread until all Eclipse builds have been completed.
   */
  public static void waitForBuild() {
    AbstractJob.waitForJobFamily(ResourcesPlugin.FAMILY_MANUAL_BUILD);
    AbstractJob.waitForJobFamily(ResourcesPlugin.FAMILY_AUTO_BUILD);
  }

  /**
   * Waits until all JDT initializations have been executed.
   *
   * @see org.eclipse.jdt.internal.ui.InitializeAfterLoadJob.RealJob
   */
  public static void waitForJdt() {
    AbstractJob.waitForJobFamily("org.eclipse.jdt.ui");
  }

  private static final class P_TypeMatchComparator implements Comparator<IType>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(IType o1, IType o2) {
      if (o1 == o2) {
        return 0;
      }

      // favor types in the workspace
      boolean b1 = o1.isBinary();
      boolean b2 = o2.isBinary();
      if (b1 != b2) {
        if (b1) {
          return 1;
        }
        return -1;
      }

      String path1 = buildPath(o1);
      String path2 = buildPath(o2);

      // descending (newest first)
      return path2.compareTo(path1);
    }

    private static String buildPath(IType t) {
      String fqn = t.getFullyQualifiedName();
      String portableString = t.getPath().toPortableString();

      StringBuilder sb = new StringBuilder(fqn.length() + portableString.length());
      sb.append(fqn);
      sb.append(portableString);
      return sb.toString();
    }
  }

  /**
   * Checks whether the given {@link ITypeHierarchy} contains an element with the given fully qualified name.
   *
   * @param h
   *          The hierarchy to search in.
   * @param fqn
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator
   *          (e.g. <code>org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass</code>).
   * @return <code>true</code> if it is part of the given {@link ITypeHierarchy}, <code>false</code> otherwise.
   */
  public static boolean contains(ITypeHierarchy h, String fqn) {
    for (IType t : h.getAllTypes()) {
      if (fqn.equals(t.getFullyQualifiedName('$'))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the first {@link IAnnotation} on the given {@link IType} or its super types that has one of the given fully
   * qualified names.
   *
   * @param type
   *          The {@link IType} where to start the search.
   * @param fullyQualifiedAnnotations
   *          The fully qualified names of the annotations to search.
   * @return The first of the annotations found in the super hierarchy of the given {@link IType} or <code>null</code>
   *         if it could not be found.
   * @throws CoreException
   */
  public static IAnnotation getFirstAnnotationInSupertypeHierarchy(IType type, String... fullyQualifiedAnnotations) throws CoreException {
    if (type == null) {
      return null;
    }
    IAnnotation ann = getFirstDeclaredAnnotation(type, fullyQualifiedAnnotations);
    if (exists(ann)) {
      return ann;
    }
    ITypeHierarchy h = type.newSupertypeHierarchy(null);
    for (IType t : h.getAllSuperclasses(type)) {
      ann = getFirstDeclaredAnnotation(t, fullyQualifiedAnnotations);
      if (exists(ann)) {
        return ann;
      }
    }
    for (IType t : h.getAllSuperInterfaces(type)) {
      ann = getFirstDeclaredAnnotation(t, fullyQualifiedAnnotations);
      if (exists(ann)) {
        return ann;
      }
    }
    return null;
  }

  /**
   * Gets the first {@link IAnnotation} of the given fully qualified names that exist on the given {@link IAnnotatable}.
   * 
   * @param element
   *          The owner to search in.
   * @param fullyQualifiedAnnotations
   *          The annotations to search.
   * @return the first of the given annotations that was found or <code>null</code>.
   */
  public static IAnnotation getFirstDeclaredAnnotation(IAnnotatable element, String... fullyQualifiedAnnotations) {
    for (String fqn : fullyQualifiedAnnotations) {
      IAnnotation ann = getAnnotation(element, fqn);
      if (exists(ann)) {
        return ann;
      }
    }
    return null;
  }

  /**
   * Gets the {@link IAnnotation} with the given fully qualified name.
   *
   * @param element
   *          The owner for which the {@link IAnnotation} should be searched.
   * @param fullyQualifiedAnnotation
   *          The fully qualified class name of the annotation (e.g. <code>java.lang.SuppressWarnings</code>).
   * @return The {@link IAnnotation} on the given element or <code>null</code> if it could not be found.
   */
  public static IAnnotation getAnnotation(IAnnotatable element, String fullyQualifiedAnnotation) {
    if (element == null) {
      return null;
    }

    String simpleName = Signature.getSimpleName(fullyQualifiedAnnotation);
    String startSimple = '@' + simpleName;
    String startFq = '@' + fullyQualifiedAnnotation;

    IAnnotation result = getAnnotation(element, simpleName, startSimple, startFq);
    if (result != null) {
      return result;
    }
    return getAnnotation(element, fullyQualifiedAnnotation, startSimple, startFq);
  }

  private static IAnnotation getAnnotation(IAnnotatable element, String name, String startSimple, String startFq) {
    String annotSource = null;
    IAnnotation annotation = element.getAnnotation(name);
    if (!exists(annotation)) {
      return null;
    }

    try {
      annotSource = annotation.getSource();
      if (annotSource != null) {
        annotSource = annotSource.trim();
      }
    }
    catch (Exception e) {
      SdkLog.warning("Could not get source of annotation '" + name + "' in element '" + element.toString() + "'.", e);
    }

    if (annotSource == null || annotSource.startsWith(startSimple) || annotSource.startsWith(startFq)) {
      return annotation;
    }
    else if (element instanceof IMember) {
      annotSource = getAnnotationSourceFixed((IMember) element, annotation, startSimple);
      if (annotSource != null && (annotSource.startsWith(startSimple) || annotSource.startsWith(startFq))) {
        return annotation;
      }
    }
    return null;
  }

  private static String getAnnotationSourceFixed(IMember member, IAnnotation annotation, String startSimple) {
    try {
      ISourceRange annotSourceRange = annotation.getSourceRange();
      ISourceRange ownerSourceRange = member.getSourceRange();
      if (annotSourceRange != null && ownerSourceRange != null && annotSourceRange.getOffset() >= 0 && ownerSourceRange.getOffset() >= 0 && ownerSourceRange.getOffset() > annotSourceRange.getOffset()) {
        String icuSource = member.getCompilationUnit().getSource();
        if (icuSource != null && icuSource.length() >= ownerSourceRange.getOffset()) {
          String diff = icuSource.substring(annotSourceRange.getOffset(), ownerSourceRange.getOffset());
          int offset = diff.lastIndexOf(startSimple);
          if (offset >= 0) {
            offset += annotSourceRange.getOffset();
            int end = offset + annotSourceRange.getLength();
            if (icuSource.length() >= end) {
              return icuSource.substring(offset, end);
            }
          }
        }
      }
    }
    catch (JavaModelException e) {
      SdkLog.warning("Unable to find source for annotation '" + annotation.getElementName() + "' in '" + member.getElementName() + "'.", e);
    }
    return null;
  }

  /**
   * Find all types annotated with this annotation
   *
   * @param annotationName
   *          fully qualified name of annotation
   * @param scope
   *          SearchEngine.createJavaSearchScope
   * @param monitor
   * @throws CoreException
   */
  public static Set<IType> findAllTypesAnnotatedWith(String annotationName, IJavaSearchScope scope, IProgressMonitor monitor) throws CoreException {
    final Set<IType> result = new LinkedHashSet<>();
    SearchRequestor collector = new SearchRequestor() {
      @Override
      public void acceptSearchMatch(SearchMatch match) throws CoreException {
        if (match.getElement() instanceof IType) {
          IType t = (IType) match.getElement();
          result.add(t);
        }
      }
    };
    for (IType annotationType : resolveJdtTypes(annotationName, scope)) {
      SearchPattern pattern = SearchPattern.createPattern(annotationType, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE);
      new SearchEngine().search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, scope, collector, monitor);
    }
    return result;
  }

  /**
   * Checks if the given {@link IJavaElement} is not <code>null</code> and exists.
   *
   * @param element
   *          The element to check.
   * @return <code>true</code> if the given element is not <code>null</code> and exists.
   */
  public static boolean exists(IJavaElement element) {
    return element != null && element.exists();
  }

  /**
   * checks whether the given {@link IJavaElement} is on the classpath of the given {@link IJavaProject}.
   *
   * @param element
   *          the {@link IJavaElement} to search
   * @param project
   *          the project classpath to search in
   * @return <code>true</code> if element was found in the classpath of project, <code>false</code> otherwise.
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
      return project.isOnClasspath(elemenProject);
    }
    return project.isOnClasspath(element);
  }

  /**
   * Gets the value of the given annotation attribute as {@link BigDecimal}.
   *
   * @param annotation
   *          The {@link IAnnotation} for which the attribute should be converted.
   * @param name
   *          The name of attribute.
   * @return A {@link BigDecimal} with the numeric value of the given attribute or <code>null</code> if the attribute
   *         could not be found or is not numeric.
   * @throws JavaModelException
   */
  public static BigDecimal getAnnotationValueNumeric(IAnnotation annotation, String name) throws JavaModelException {
    if (!exists(annotation)) {
      return null;
    }
    IMemberValuePair[] memberValues = annotation.getMemberValuePairs();
    for (IMemberValuePair p : memberValues) {
      if (Objects.equals(name, p.getMemberName())) {
        switch (p.getValueKind()) {
          case IMemberValuePair.K_DOUBLE:
            return new BigDecimal(((Double) p.getValue()).doubleValue());
          case IMemberValuePair.K_FLOAT:
            return new BigDecimal(((Float) p.getValue()).doubleValue());
          case IMemberValuePair.K_INT:
            return new BigDecimal(((Integer) p.getValue()).intValue());
          case IMemberValuePair.K_BYTE:
            return new BigDecimal(((Byte) p.getValue()).intValue());
          case IMemberValuePair.K_LONG:
            return new BigDecimal(((Long) p.getValue()).longValue());
          case IMemberValuePair.K_SHORT:
            return new BigDecimal(((Short) p.getValue()).intValue());
        }
        break;
      }
    }
    return null;
  }

  /**
   * Gets teh value of the given annotation attribute as {@link String}.
   *
   * @param annotation
   *          The {@link IAnnotation} for which the attribute should be converted.
   * @param name
   *          The name of attribute.
   * @return A {@link String} with the value of the given attribute or <code>null</code> if the value is
   *         <code>null</code> or could not be found.
   * @throws JavaModelException
   */
  public static String getAnnotationValueString(IAnnotation annotation, String name) throws JavaModelException {
    if (!exists(annotation)) {
      return null;
    }

    IMemberValuePair[] memberValues = annotation.getMemberValuePairs();
    for (IMemberValuePair p : memberValues) {
      if (Objects.equals(name, p.getMemberName())) {
        Object val = p.getValue();
        if (val != null) {
          return val.toString();
        }
      }
    }
    return null;
  }

  /**
   * To get a type hierarchy with the given elements as scope.
   *
   * @param elements
   * @return
   * @throws JavaModelException
   */
  public static ITypeHierarchy getLocalTypeHierarchy(Collection<? extends IJavaElement> elements) throws JavaModelException {
    IRegion region = JavaCore.newRegion();
    if (elements != null && !elements.isEmpty()) {
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
    return JavaCore.newTypeHierarchy(region, null, null);
  }

  private static void addBinaryInnerTypesToRegionRec(IType declaringType, IRegion region) throws JavaModelException {
    for (IType child : declaringType.getTypes()) {
      region.add(child);
      addBinaryInnerTypesToRegionRec(child, region);
    }
  }

  /**
   * To get a type hierarchy with the given elements as scope.
   *
   * @param elements
   * @return
   * @throws JavaModelException
   */
  public static ITypeHierarchy getLocalTypeHierarchy(IJavaElement... elements) throws JavaModelException {
    if (elements == null || elements.length < 1) {
      return null;
    }
    Set<IJavaElement> el = new HashSet<>(elements.length);
    for (IJavaElement e : elements) {
      el.add(e);
    }
    return getLocalTypeHierarchy(el);
  }

  /**
   * Returns the line separator for the given project.<br>
   * If the project is null, returns the line separator for the workspace.<br>
   * If still null, return the system line separator.
   *
   * @param p
   *          The {@link IJavaProject} for which the line separator should be returned or <code>null</code> if the
   *          workspace preference should be returned.
   * @return The preferred line separator.
   */
  public static String lineSeparator(IJavaProject p) {
    return Util.getLineSeparator(null, p);
  }

  /**
   * Creates a new {@link PropertyMap} containing the given {@link IJavaProject}.
   *
   * @param p
   *          The {@link IJavaProject} the map should contain or <code>null</code>.
   * @return The created {@link PropertyMap}. Never returns <code>null</code>.
   */
  public static PropertyMap propertyMap(IJavaProject p) {
    PropertyMap context = new PropertyMap();
    context.setProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, p);
    return context;
  }

  /**
   * Tries to ensure that the given {@link IResource}s can be written (not read-only).<br>
   *
   * @param resources
   *          The resources that should be written
   * @return An {@link IStatus} describing if the given resources can be written now. If {@link IStatus#isOK()} returns
   *         <code>true</code>, it is safe to continue the write operation. Otherwise the {@link IStatus} contains the
   *         files and reasons why this is not possible. This may be the case if the file is still read-only or because
   *         it changed value in the mean time.
   */
  public static IStatus makeCommittable(Collection<IResource> resources) {
    if (resources == null || resources.isEmpty()) {
      return Status.OK_STATUS;
    }

    Set<IFile> existingReadOnlyFiles = new HashSet<>(resources.size());
    for (IResource r : resources) {
      if (r != null && r.exists() && r.getType() == IResource.FILE && isReadOnly(r)) {
        existingReadOnlyFiles.add((IFile) r);
      }
    }
    if (existingReadOnlyFiles.isEmpty()) {
      return Status.OK_STATUS;
    }

    Map<IFile, Long> oldTimeStamps = createModificationStampMap(existingReadOnlyFiles);
    IStatus status = ResourcesPlugin.getWorkspace().validateEdit(existingReadOnlyFiles.toArray(new IFile[existingReadOnlyFiles.size()]), IWorkspace.VALIDATE_PROMPT);
    if (!status.isOK()) {
      return status;
    }

    IStatus modified = null;
    // check if the resources can be written now
    for (IFile f : existingReadOnlyFiles) {
      if (isReadOnly(f)) {
        String message = "File '" + f.getFullPath() + "' is read only.";
        modified = addStatus(modified, message);
      }
    }
    // check for in between modifications
    Map<IFile, Long> newTimeStamps = createModificationStampMap(existingReadOnlyFiles);
    for (Entry<IFile, Long> e : oldTimeStamps.entrySet()) {
      IFile file = e.getKey();
      if (!e.getValue().equals(newTimeStamps.get(file))) {
        String message = "File '" + file.getFullPath() + "' has been modified since the beginning of the operation.";
        modified = addStatus(modified, message);
      }
    }
    if (modified != null) {
      return modified;
    }
    return Status.OK_STATUS;
  }

  private static IStatus addStatus(IStatus status, String msg) {
    IStatus entry = new ScoutStatus(IStatus.ERROR, msg, null);
    if (status == null) {
      return entry;
    }
    else if (status.isMultiStatus()) {
      ((MultiStatus) status).add(entry);
      return status;
    }
    else {
      MultiStatus result = new MultiStatus(S2ESdkActivator.PLUGIN_ID, 0, msg, null);
      result.add(status);
      result.add(entry);
      return result;
    }
  }

  private static Map<IFile, Long> createModificationStampMap(Collection<IFile> files) {
    Map<IFile, Long> map = new HashMap<>(files.size());
    for (IFile f : files) {
      map.put(f, Long.valueOf(f.getModificationStamp()));
    }
    return map;
  }

  /**
   * Checks if the given {@link IResource} is read-only.
   *
   * @param resource
   *          The {@link IResource} to check. Must not be <code>null</code>.
   * @return <code>true</code> if the resource is marked as read-only. <code>false</code> otherwise.
   */
  public static boolean isReadOnly(IResource resource) {
    ResourceAttributes resourceAttributes = resource.getResourceAttributes();
    if (resourceAttributes == null) {
      // not supported on this platform for this resource
      return false;
    }
    return resourceAttributes.isReadOnly();
  }

  /**
   * Executes the given {@link CompilationUnitWriteOperation}s in a new {@link ResourceBlockingOperationJob}, waits
   * until all have finished and returns the resulting first {@link IType}s of each created {@link ICompilationUnit}.
   *
   * @param ops
   *          The {@link CompilationUnitWriteOperation}s to execute.
   * @param monitor
   *          The {@link IProgressMonitor}
   * @return The first {@link IType} of each created {@link ICompilationUnit}. The {@link List} contains the results in
   *         the same order as the given {@link Collection} returns {@link CompilationUnitWriteOperation}s. Therefore
   *         the first {@link IType} in the resulting {@link List} belongs to the first
   *         {@link CompilationUnitWriteOperation} in ops.
   * @throws JavaModelException
   */
  public static List<IType> writeTypesWithResult(Collection<CompilationUnitWriteOperation> ops, IProgressMonitor monitor) throws JavaModelException {
    writeTypes(ops, monitor, true);

    List<IType> result = new ArrayList<>(ops.size());
    for (CompilationUnitWriteOperation op : ops) {
      result.add(getFirstType(op));
    }
    return result;
  }

  private static IType getFirstType(CompilationUnitWriteOperation op) throws JavaModelException {
    if (op == null) {
      return null;
    }

    ICompilationUnit compilationUnit = op.getCompilationUnit();
    if (compilationUnit == null) {
      return null;
    }

    IType[] types = compilationUnit.getTypes();
    if (types.length < 1) {
      return null;
    }
    IType candidate = types[0];
    if (!exists(candidate)) {
      return null;
    }

    return candidate;
  }

  /**
   * Executes the given {@link CompilationUnitWriteOperation}s in a new {@link ResourceBlockingOperationJob}.
   *
   * @param ops
   *          The {@link CompilationUnitWriteOperation}s to execute.
   * @param monitor
   *          The {@link IProgressMonitor}
   * @param waitUntilWritten
   *          <code>true</code> if this method should block until all operations have been executed. <code>false</code>
   *          if this method should directly return after the {@link CompilationUnitWriteOperation}s have been
   *          scheduled.
   */
  public static void writeTypes(Collection<CompilationUnitWriteOperation> ops, IProgressMonitor monitor, boolean waitUntilWritten) {
    if (ops == null || ops.isEmpty()) {
      return;
    }

    Set<IResource> lockedResources = new HashSet<>(ops.size());
    for (CompilationUnitWriteOperation op : ops) {
      if (op != null) {
        ICompilationUnit icu = op.getCompilationUnit();
        if (icu != null) {
          IResource r = getExistingParent(icu.getResource());
          if (r != null) {
            lockedResources.add(r);
          }
        }
      }
    }

    ResourceBlockingOperationJob job = new ResourceBlockingOperationJob(ops, lockedResources.toArray(new IResource[lockedResources.size()]));
    job.schedule();
    if (waitUntilWritten) {
      try {
        job.join(0L, monitor);
      }
      catch (OperationCanceledException | InterruptedException e) {
        SdkLog.info("Unable to wait until compilation units have been written.", e);
      }
    }
  }

  /**
   * Executes the given {@link ResourceWriteOperation}s in a new {@link ResourceBlockingOperationJob}, waits until all
   * have finished and returns the created {@link IFile}s.
   *
   * @param ops
   *          The {@link ResourceWriteOperation}s to execute
   * @param monitor
   *          The {@link IProgressMonitor}
   * @return A {@link List} containing all created {@link IFile}s in the same order as the given {@link Collection}
   *         returns {@link ResourceWriteOperation}s. Therefore the first {@link IFile} in the resulting {@link List}
   *         belongs to the first {@link ResourceWriteOperation} in ops.
   * @throws CoreException
   */
  public static List<IFile> writeResourcesWithResult(Collection<ResourceWriteOperation> ops, IProgressMonitor monitor) throws CoreException {
    writeResources(ops, monitor, true);

    List<IFile> result = new ArrayList<>(ops.size());
    for (ResourceWriteOperation op : ops) {
      if (op == null) {
        result.add(null);
      }
      else {
        result.add(op.getFile());
      }
    }
    return result;
  }

  /**
   * Executes the given {@link ResourceWriteOperation}s in a new {@link ResourceBlockingOperationJob}.
   *
   * @param ops
   *          The {@link ResourceWriteOperation}s to execute.
   * @param monitor
   *          The {@link IProgressMonitor}
   * @param waitUntilWritten
   *          <code>true</code> if this method should block until all operations have been executed. <code>false</code>
   *          if this method should directly return after the {@link ResourceWriteOperation}s have been scheduled.
   * @throws CoreException
   */
  public static void writeResources(Collection<ResourceWriteOperation> ops, IProgressMonitor monitor, boolean waitUntilWritten) throws CoreException {
    if (ops == null || ops.isEmpty()) {
      return;
    }

    Set<IResource> lockedResources = new HashSet<>(ops.size());
    for (ResourceWriteOperation op : ops) {
      if (op != null) {
        IResource r = getExistingParent(op.getFile());
        if (r != null) {
          lockedResources.add(r);
        }
      }
    }

    ResourceBlockingOperationJob job = new ResourceBlockingOperationJob(ops, lockedResources.toArray(new IResource[lockedResources.size()]));
    job.schedule();
    if (waitUntilWritten) {
      try {
        job.join(0L, monitor);
      }
      catch (OperationCanceledException | InterruptedException e) {
        throw new CoreException(new ScoutStatus(e));
      }
    }
  }

  private static IResource getExistingParent(IResource startResource) {
    IResource curResource = startResource;
    while (curResource != null && !curResource.exists()) {
      curResource = curResource.getParent();
    }
    return curResource;
  }
}
