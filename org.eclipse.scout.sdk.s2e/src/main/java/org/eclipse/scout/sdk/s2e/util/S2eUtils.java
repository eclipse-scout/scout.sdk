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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.project.ProjectConfigurationManager;
import org.eclipse.m2e.core.project.MavenUpdateRequest;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.CompilationUnitWriteOperation;
import org.eclipse.scout.sdk.s2e.operation.IFileWriteOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.operation.ResourceWriteOperation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
   */
  public static org.eclipse.scout.sdk.core.model.api.IType jdtTypeToScoutType(IType jdtType) {
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
    if (!exists(icu)) {
      return "";
    }

    IPackageDeclaration[] packageDeclarations = icu.getPackageDeclarations();
    if (packageDeclarations.length < 1) {
      return "";
    }

    return packageDeclarations[0].getElementName();
  }

  /**
   * Converts the given {@link IType} to {@link org.eclipse.scout.sdk.core.model.api.IType}
   *
   * @param jdtType
   *          The jdt {@link IType} to convert.
   * @param env
   *          The {@link IJavaEnvironment} to use to find the matching {@link org.eclipse.scout.sdk.core.model.api.IType}.
   * @return The {@link org.eclipse.scout.sdk.core.model.api.IType} matching the given JDT {@link IType}.
   */
  public static org.eclipse.scout.sdk.core.model.api.IType jdtTypeToScoutType(IType jdtType, IJavaEnvironment env) {
    return env.findType(jdtType.getFullyQualifiedName());
  }

  /**
   * Gets {@link IType}s for all public abstract classes existing on the classpath of the given project and implementing
   * the given baseTypeFqn.
   *
   * @param sourceProject
   *          The {@link IJavaProject} defining the classpath.
   * @param baseTypeFqn
   *          The fully qualified name of the base class. The sub classes of this class are searched.
   * @param monitor
   *          The monitor or <code>null</code>. If the monitor becomes canceled, the search is aborted and an incomplete
   *          result may be returned. The caller of this method is responsible to react on this fact based on the monitor
   *          state ({@link IProgressMonitor#isCanceled()}).
   * @return A {@link Set} containing all {@link IType}s sorted ascending by fully qualified name.
   * @throws CoreException
   */
  public static Set<IType> findAbstractClassesInHierarchy(final IJavaProject sourceProject, String baseTypeFqn, IProgressMonitor monitor) throws CoreException {
    return findClassesInStrictHierarchy(sourceProject, baseTypeFqn, monitor, new PublicAbstractPrimaryTypeFilter());
  }

  /**
   * @see #findClassesInStrictHierarchy(IJavaProject, IType, IProgressMonitor, Predicate)
   */
  public static Set<IType> findClassesInStrictHierarchy(IJavaProject sourceProject, String baseTypeFqn, IProgressMonitor monitor, Predicate<IType> filter) throws CoreException {
    if (!exists(sourceProject)) {
      return Collections.emptySet();
    }
    return findClassesInStrictHierarchy(sourceProject, sourceProject.findType(baseTypeFqn.replace('$', '.')), monitor, filter);
  }

  /**
   * Gets all {@link IType}s on the classpath of the give {@link IJavaProject} that are sub classes of the given baseType
   * and fulfill the given filter. If the base type itself fulfills the given filter, it is included in the result.
   *
   * @param sourceProject
   *          The {@link IJavaProject} defining the classpath.
   * @param baseType
   *          The base class of the hierarchy.
   * @param monitor
   *          The monitor or <code>null</code>. If the monitor becomes canceled, the search is aborted and an incomplete
   *          result may be returned. The caller of this method is responsible to react on this fact based on the monitor
   *          state ({@link IProgressMonitor#isCanceled()}).
   * @param filter
   *          A filter to decide which matches are accepted or <code>null</code> if all matches should be accepted.
   * @return A {@link Set} containing the {@link IType}s sorted ascending by simple name and fully qualified name
   *         afterwards.
   * @throws CoreException
   */
  public static Set<IType> findClassesInStrictHierarchy(final IJavaProject sourceProject, IType baseType, final IProgressMonitor monitor, final Predicate<IType> filter) throws CoreException {
    if (!exists(baseType) || !exists(baseType.getParent()) || !exists(sourceProject)) {
      return Collections.emptySet();
    }

    final Set<IType> collector = new TreeSet<>(new ElementNameComparator());
    TypeNameMatchRequestor nameMatchRequestor = new TypeNameMatchRequestor() {
      @Override
      public void acceptTypeNameMatch(TypeNameMatch match) {
        if (monitor != null && monitor.isCanceled()) {
          throw new OperationCanceledException("strict hierarchy search canceled by monitor.");
        }
        if (filter == null || filter.test(match.getType())) {
          collector.add(match.getType());
        }
      }
    };
    IJavaSearchScope strictHierarchyScope = SearchEngine.createStrictHierarchyScope(sourceProject, baseType, true, true, null);

    try {
      new SearchEngine().searchAllTypeNames(null, SearchPattern.R_EXACT_MATCH, null, SearchPattern.R_EXACT_MATCH, IJavaSearchConstants.CLASS, strictHierarchyScope,
          nameMatchRequestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
    }
    catch (OperationCanceledException oce) {
      SdkLog.debug("Strict hierarchy search has been canceled. An incomplete result will be returned.", oce);
    }
    return collector;
  }

  /**
   * Gets the first {@link IMethod} for which the given {@link Predicate} evaluates to <code>true</code>.<br>
   * <b>Note:</b> Inner Types are not searched.
   *
   * @param type
   *          The {@link IType} in which the {@link IMethod} should be searched.
   * @param filter
   *          The {@link Predicate}.
   * @return The first {@link IMethod} found in the given type or <code>null</code> if it could not be found.
   * @throws JavaModelException
   */
  public static IMethod getFirstMethod(IType type, Predicate<IMethod> filter) throws JavaModelException {
    for (IMethod method : type.getMethods()) {
      if (filter == null || filter.test(method)) {
        return method;
      }
    }
    return null;
  }

  /**
   * Gets all {@link IType}s that are accessible in the current workspace having the given fully qualified name.
   *
   * @param fqn
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator (e.g.
   *          <code>org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass</code>).
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
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator (e.g.
   *          <code>org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass</code>).
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
              if (t.getFullyQualifiedName().indexOf(fqn) >= 0) {
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
   * Gets the first {@link IAnnotation} on the given {@link IType} or its super types that has one of the given fully
   * qualified names.
   *
   * @param type
   *          The {@link IType} where to start the search.
   * @param fullyQualifiedAnnotations
   *          The fully qualified names of the annotations to search.
   * @return The first of the annotations found in the super hierarchy of the given {@link IType} or <code>null</code> if
   *         it could not be found.
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
      SdkLog.warning("Could not get source of annotation '{}' in element '{}'.", name, element, e);
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

      if (SourceRange.isAvailable(ownerSourceRange)
          && SourceRange.isAvailable(annotSourceRange)
          && ownerSourceRange.getOffset() > annotSourceRange.getOffset()) {
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
      SdkLog.warning("Unable to find source for annotation '{}' in '{}'.", annotation.getElementName(), member.getElementName(), e);
    }
    return null;
  }

  /**
   * Find all types annotated with the given annotation.
   *
   * @param annotationName
   *          fully qualified name of annotation
   * @param scope
   *          A search scope. Use {@link #createJavaSearchScope(Collection)} or
   *          {@link SearchEngine#createJavaSearchScope(IJavaElement[])}.
   * @param monitor
   *          The monitor or <code>null</code>. If the monitor becomes canceled, the search is aborted and an incomplete
   *          result may be returned. The caller of this method is responsible to react on this fact based on the monitor
   *          state ({@link IProgressMonitor#isCanceled()}).
   * @throws CoreException
   */
  public static Set<IType> findAllTypesAnnotatedWith(String annotationName, IJavaSearchScope scope, final IProgressMonitor monitor) throws CoreException {
    final Set<IType> result = new LinkedHashSet<>();
    SearchRequestor collector = new SearchRequestor() {
      @Override
      public void acceptSearchMatch(SearchMatch match) throws CoreException {
        if (monitor != null && monitor.isCanceled()) {
          throw new OperationCanceledException("annotated types search canceled.");
        }
        if (match.getElement() instanceof IType) {
          IType t = (IType) match.getElement();
          result.add(t);
        }
      }
    };
    for (IType annotationType : resolveJdtTypes(annotationName, SearchEngine.createWorkspaceScope())) {
      if (monitor != null && monitor.isCanceled()) {
        return result;
      }
      SearchPattern pattern = SearchPattern.createPattern(annotationType, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE);
      try {
        new SearchEngine().search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, scope, collector, monitor);
      }
      catch (OperationCanceledException oce) {
        SdkLog.debug("Search for all types annotated with '{}' has been canceled.", annotationName, oce);
      }
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
    if (!exists(element) || !exists(project)) {
      return false;
    }
    final boolean isInWorkspace = element.getResource() != null;
    if (isInWorkspace) {
      // if the element is in the workspace (not binary in a library): check on the project level. Otherwise the results of IJavaProject#isOnClasspath() may be wrong!
      // do not calculate the classpath visibility based on the project for binary types because their project may be any project having this dependency which may be wrong.
      IJavaProject javaProjectOfElement = element.getJavaProject();
      return project.equals(javaProjectOfElement) || project.isOnClasspath(javaProjectOfElement);
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
            Double doubleValue = (Double) p.getValue();
            return BigDecimal.valueOf(doubleValue.doubleValue());
          case IMemberValuePair.K_FLOAT:
            Float floatValue = (Float) p.getValue();
            return BigDecimal.valueOf(floatValue.doubleValue());
          case IMemberValuePair.K_INT:
            Integer intValue = (Integer) p.getValue();
            return BigDecimal.valueOf(intValue.longValue());
          case IMemberValuePair.K_BYTE:
            Byte byteValue = (Byte) p.getValue();
            return BigDecimal.valueOf(byteValue.longValue());
          case IMemberValuePair.K_LONG:
            Long longValue = (Long) p.getValue();
            return BigDecimal.valueOf(longValue.longValue());
          case IMemberValuePair.K_SHORT:
            Short shortValue = (Short) p.getValue();
            return BigDecimal.valueOf(shortValue.longValue());
          default:
            return null;
        }
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
   * @return A {@link String} with the value of the given attribute or <code>null</code> if the value is <code>null</code>
   *         or could not be found.
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
   * Returns the line separator for the given project.<br>
   * If the project is <code>null</code>, returns the line separator as specified in the current workspace.<br>
   * If there is nothing specified in the project or the workspace, the system line separator is returned:
   * <code>System.getProperty("line.separator")</code>.
   *
   * @param p
   *          The {@link IJavaProject} for which the line separator should be returned or <code>null</code> if the
   *          workspace preference should be returned.
   * @return The preferred line separator. Is never <code>null</code>.
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
   *         files and reasons why this is not possible. This may be the case if the file is still read-only or because it
   *         changed value in the mean time.
   */
  @SuppressWarnings("pmd:NPathComplexity")
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
   * Executes the given {@link CompilationUnitWriteOperation}s in a new {@link ResourceBlockingOperationJob}, waits until
   * all have finished and returns the resulting first {@link IType}s of each created or updated
   * {@link ICompilationUnit}.<br>
   * If you don't need the created or updated {@link IType}s it is faster to call
   * {@link #writeFiles(Collection, IProgressMonitor, boolean)}.<br>
   * <br>
   * <b>Important:</b> If the {@link Job} invoking this method uses an {@link ISchedulingRule} that conflicts with one of
   * the {@link IFile}s to be written, this will result in a deadlock! In that case use
   * {@link #writeType(IPackageFragmentRoot, ICompilationUnitSourceBuilder, IJavaEnvironment, IProgressMonitor, IWorkingCopyManager)}
   * instead or write the types sync using
   * {@link CompilationUnitWriteOperation#run(IProgressMonitor, IWorkingCopyManager)}.
   *
   * @param ops
   *          The {@link CompilationUnitWriteOperation}s to execute.
   * @param monitor
   *          The {@link IProgressMonitor}
   * @return The first {@link IType} of each created or updated {@link ICompilationUnit}. The {@link List} contains the
   *         results in the same order as the given {@link Collection} returns {@link CompilationUnitWriteOperation}s.
   *         Therefore the first {@link IType} in the resulting {@link List} belongs to the first
   *         {@link CompilationUnitWriteOperation} in ops.
   * @throws CoreException
   */
  public static List<IType> writeTypes(Collection<CompilationUnitWriteOperation> ops, IProgressMonitor monitor) throws CoreException {
    if (ops == null || ops.isEmpty()) {
      return Collections.emptyList();
    }

    writeFiles(ops, monitor);

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

    ICompilationUnit compilationUnit = op.getCreatedCompilationUnit();
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
   * Executes the given {@link IFileWriteOperation}s in a new {@link ResourceBlockingOperationJob}, waits until all have
   * finished and returns the created or updated {@link IFile}s.<br>
   * If you don't need the created or updated {@link IFile}s it is faster to call
   * {@link #writeFiles(Collection, IProgressMonitor, boolean)}.<br>
   * <br>
   * <b>Important:</b> If the {@link Job} invoking this method uses an {@link ISchedulingRule} that conflicts with one of
   * the {@link IFile}s to be written, this will result in a deadlock! In that case use
   * {@link #writeFiles(Collection, IProgressMonitor, boolean)} without waiting instead or write the files sync using
   * {@link IFileWriteOperation#run(IProgressMonitor, IWorkingCopyManager)} if you need the resulting {@link IFile} after
   * the invocation of this method.
   *
   * @param ops
   *          The {@link IFileWriteOperation}s to execute
   * @param monitor
   *          The {@link IProgressMonitor}
   * @return A {@link List} containing all created or updated {@link IFile}s in the same order as the given
   *         {@link Collection} returns {@link IFileWriteOperation}s. Therefore the first {@link IFile} in the resulting
   *         {@link List} belongs to the first {@link IFileWriteOperation} in ops.
   */
  public static List<IFile> writeFiles(Collection<? extends IFileWriteOperation> ops, IProgressMonitor monitor) {
    if (ops == null || ops.isEmpty()) {
      return Collections.emptyList();
    }

    writeFiles(ops, monitor, true);

    List<IFile> result = new ArrayList<>(ops.size());
    for (IFileWriteOperation op : ops) {
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
   * Executes the given {@link IFileWriteOperation}s in a new {@link ResourceBlockingOperationJob}.<br>
   *
   * @param ops
   *          The {@link IFileWriteOperation}s to execute.
   * @param monitor
   *          The {@link IProgressMonitor}
   * @param waitUntilWritten
   *          <code>true</code> if this method should block until all operations have been executed. <code>false</code> if
   *          this method should directly return after the {@link ResourceWriteOperation}s have been scheduled.<br>
   *          Important: If this parameter is <code>true</code> and the {@link Job} invoking this method uses an
   *          {@link ISchedulingRule} that conflicts with one of the {@link IFile}s to be written, this will result in a
   *          deadlock! In that case consider writing the files sync using
   *          {@link IFileWriteOperation#run(IProgressMonitor, IWorkingCopyManager)} if you need the resulting
   *          {@link IFile} after the invocation of this method.
   */
  public static void writeFiles(Collection<? extends IFileWriteOperation> ops, IProgressMonitor monitor, boolean waitUntilWritten) {
    if (ops == null || ops.isEmpty()) {
      return;
    }

    Set<IResource> lockedResources = new HashSet<>(ops.size());
    for (IFileWriteOperation op : ops) {
      if (op != null) {
        IResource r = op.getAffectedResource();
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
        SdkLog.info("Unable to wait until resources have been written.", e);
      }
    }
  }

  /**
   * Returns a Java search scope limited to the given Java elements. The Java elements resulting from a search with this
   * scope will be children of the given elements.
   * <p>
   * If an element is an {@link IJavaProject}, then only project's source folders will be included.
   * </p>
   *
   * @see SearchEngine#createJavaSearchScope(IJavaElement[], int)
   */
  public static IJavaSearchScope createJavaSearchScope(IJavaElement[] elements) {
    return SearchEngine.createJavaSearchScope(elements, IJavaSearchScope.SOURCES);
  }

  /**
   * Creates a {@link IJavaSearchScope} based on the given {@link IResource}s.<br>
   * The search scope contains the following items depending on the resource type:
   * <ul>
   * <li>{@link IResource#PROJECT}: If the project has the java nature ({@link JavaCore#NATURE_ID}) and is open all source
   * folders of the project are added (no libraries or dependent projects).</li>
   * <li>{@link IResource#FOLDER}: The {@link IPackageFragment} or {@link IPackageFragmentRoot} is added if the folder
   * belongs to one. All sub-packages are included as well!</li>
   * <li>{@link IResource#FILE}: If the file is a java file, the corresponding {@link ICompilationUnit} is added. If the
   * resource is a class file, the corresponding {@link IClassFile} is added. If the file is a jar file, the corresponding
   * {@link IPackageFragmentRoot} is added.</li>
   * <li>{@link IResource#ROOT}: All source folders of all {@link IJavaProject}s in the workspace are added.</li>
   * <ul>
   *
   * @param resources
   *          The resources that should be added to the {@link IJavaSearchScope}.
   * @return A new {@link IJavaSearchScope} that contains all {@link IJavaElement}s that could be matched by the given
   *         resources or <code>null</code> if no {@link IJavaElement} is part of the given resources.
   * @throws CoreException
   */
  public static IJavaSearchScope createJavaSearchScope(Collection<IResource> resources) throws CoreException {
    if (resources == null || resources.isEmpty()) {
      return null;
    }

    final Set<IJavaElement> jset = new HashSet<>(resources.size());
    for (IResource resource : resources) {
      if (resource == null || !resource.isAccessible()) {
        continue;
      }

      int type = resource.getType();
      switch (type) {
        case IResource.PROJECT:
          IProject project = (IProject) resource;
          if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
            addJavaElement(jset, JavaCore.create(project));
          }
          break;
        case IResource.FILE:
          addJavaElement(jset, JavaCore.create((IFile) resource));
          break;
        case IResource.FOLDER:
          resource.accept(new IResourceProxyVisitor() {
            @Override
            public boolean visit(IResourceProxy proxy) throws CoreException {
              if (proxy.getType() == IResource.FOLDER) {
                IFolder folder = (IFolder) proxy.requestResource();
                addJavaElement(jset, JavaCore.create(folder));
                return true;
              }
              return false;
            }
          }, IResource.NONE);
          break;
        case IResource.ROOT:
          IJavaModel model = JavaCore.create((IWorkspaceRoot) resource);
          if (exists(model)) {
            for (IJavaProject jp : model.getJavaProjects()) {
              addJavaElement(jset, jp);
            }
          }
          break;
        default:
          throw new UnsupportedOperationException("Unknown resource type: " + type);
      }
    }
    if (jset.isEmpty()) {
      return null;
    }
    return createJavaSearchScope(jset.toArray(new IJavaElement[jset.size()]));
  }

  private static void addJavaElement(Collection<IJavaElement> collector, IJavaElement elementToAdd) {
    if (exists(elementToAdd)) {
      collector.add(elementToAdd);
    }
  }

  /**
   * Gets the mostly used {@link IPackageFragmentRoot} of the given {@link IJavaProject}.
   *
   * @param project
   *          The {@link IJavaProject} for which the source folder should be returned.
   * @return The primary {@link IPackageFragmentRoot} (which will always be of kind
   *         {@link IPackageFragmentRoot#K_SOURCE}).
   * @throws JavaModelException
   */
  public static IPackageFragmentRoot getPrimarySourceFolder(IJavaProject project) throws JavaModelException {
    Collection<IPackageFragmentRoot> sourceFolders = getSourceFolders(project);
    if (sourceFolders.isEmpty()) {
      return null;
    }
    return sourceFolders.iterator().next();
  }

  /**
   * Gets all source folders of the given {@link IJavaProject}.
   *
   * @param project
   *          The {@link IJavaProject} for which the source folders should be returned.
   * @return All {@link IPackageFragmentRoot}s of kind {@link IPackageFragmentRoot#K_SOURCE} in the given
   *         {@link IJavaProject} sorted by relevance.
   * @throws JavaModelException
   */
  public static Set<IPackageFragmentRoot> getSourceFolders(IJavaProject project) throws JavaModelException {
    return getSourceFolders(Collections.singletonList(project), null);
  }

  /**
   * Gets all source folders of the given {@link IJavaProject}s.
   *
   * @param projects
   *          The {@link IJavaProject}s in which the source folders should be searched.
   * @param monitor
   *          The {@link IProgressMonitor} to use. The search aborts if the given {@link IProgressMonitor} is canceled. In
   *          this case an empty {@link Set} is returned.
   * @return A {@link Set} with all source folders ({@link IPackageFragmentRoot}s of kind
   *         {@link IPackageFragmentRoot#K_SOURCE}) of the given {@link IJavaProject}s ordered by relevance and project.
   * @throws JavaModelException
   */
  public static Set<IPackageFragmentRoot> getSourceFolders(Collection<IJavaProject> projects, IProgressMonitor monitor) throws JavaModelException {
    return getSourceFolders(projects, null, monitor);
  }

  /**
   * Gets all source folders of the given {@link IJavaProject}s that fulfill the given {@link Predicate}.
   *
   * @param projects
   *          The {@link IJavaProject}s in which the source folders should be searched.
   * @param filter
   *          The {@link Predicate} the {@link IPackageFragmentRoot} candidates must fulfill.
   * @param monitor
   *          The {@link IProgressMonitor} to use. The search aborts if the given {@link IProgressMonitor} is canceled. In
   *          this case an empty {@link Set} is returned.
   * @return A {@link Set} with all source folders ({@link IPackageFragmentRoot}s of kind
   *         {@link IPackageFragmentRoot#K_SOURCE}) of the given {@link IJavaProject}s that accept the given
   *         {@link Predicate} ordered by relevance and project.
   * @throws JavaModelException
   */
  public static Set<IPackageFragmentRoot> getSourceFolders(Collection<IJavaProject> projects, Predicate<IPackageFragmentRoot> filter, IProgressMonitor monitor) throws JavaModelException {
    if (projects == null || projects.isEmpty()) {
      return Collections.emptySet();
    }

    NavigableMap<CompositeObject, IPackageFragmentRoot> prioMap = new TreeMap<>();
    for (IJavaProject project : projects) {
      if (!exists(project)) {
        continue;
      }

      for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
        if (monitor != null && monitor.isCanceled()) {
          return Collections.emptySet();
        }
        if (!isJavaSourceFolder(root, filter)) {
          continue;
        }

        String s = root.getPath().removeFirstSegments(1).toString().toLowerCase();
        if ("src/main/java".equals(s)) {
          prioMap.put(new CompositeObject(1, project, s), root);
        }
        else if ("src".equals(s)) {
          prioMap.put(new CompositeObject(11, project, s), root);
        }
        else if (s.startsWith("src/main/")) {
          prioMap.put(new CompositeObject(12, project, s), root);
        }
        else if ("src/test/java".equals(s)) {
          prioMap.put(new CompositeObject(20, project, s), root);
        }
        else if (s.startsWith("src/test/")) {
          prioMap.put(new CompositeObject(21, project, s), root);
        }
        else {
          prioMap.put(new CompositeObject(30, project, s), root);
        }
      }
    }
    return new LinkedHashSet<>(prioMap.values());
  }

  @SuppressWarnings("pmd:NPathComplexity")
  private static boolean isJavaSourceFolder(IPackageFragmentRoot root, Predicate<IPackageFragmentRoot> filter) throws JavaModelException {
    if (!exists(root)) {
      return false;
    }
    if (root.getKind() != IPackageFragmentRoot.K_SOURCE || root.isArchive() || root.isExternal()) {
      return false;
    }
    if (filter != null && !filter.test(root)) {
      return false;
    }
    IResource resource = root.getResource();
    if (resource == null || !resource.exists() || resource.isDerived()) {
      return false;
    }

    IClasspathEntry rawClasspathEntry = root.getRawClasspathEntry();
    if (rawClasspathEntry == null) {
      return false;
    }
    IPath[] exclusionPatterns = rawClasspathEntry.getExclusionPatterns();
    if (exclusionPatterns != null && exclusionPatterns.length > 0) {
      char[] javaSample = ("Whatever" + SuffixConstants.SUFFIX_STRING_java).toCharArray();
      for (IPath excludedPath : exclusionPatterns) {
        char[] pattern = excludedPath.toString().toCharArray();
        boolean javaFilesExcluded = CharOperation.pathMatch(pattern, javaSample, true, '/');
        if (javaFilesExcluded) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Creates the source for the given {@link ISourceBuilder} created in the given {@link IJavaProject}.
   *
   * @param srcBuilder
   *          The {@link ISourceBuilder} that should create the code.
   * @param targetProject
   *          The {@link IJavaProject} in which the source should be created.
   * @return The created source as {@link String} or <code>null</code> if the {@link ISourceBuilder} or the target
   *         {@link IJavaProject} is <code>null</code>.
   */
  public static String createJavaCode(ISourceBuilder srcBuilder, IJavaProject targetProject) {
    return createJavaCode(srcBuilder, targetProject, null);
  }

  /**
   * Creates the source for the given {@link ISourceBuilder} created in the given {@link IJavaProject}.
   *
   * @param srcBuilder
   *          The {@link ISourceBuilder} that should create the code.
   * @param targetProject
   *          The {@link IJavaProject} in which the source should be created.
   * @param env
   *          The optional {@link IJavaEnvironment} to use.
   * @return The created source as {@link String} or <code>null</code> if the {@link ISourceBuilder} or the target
   *         {@link IJavaProject} is <code>null</code>.
   */
  public static String createJavaCode(ISourceBuilder srcBuilder, IJavaProject targetProject, IJavaEnvironment env) {
    if (srcBuilder == null) {
      return null;
    }
    if (!exists(targetProject)) {
      return null;
    }
    if (env == null) {
      env = ScoutSdkCore.createJavaEnvironment(targetProject);
    }
    return CoreUtils.createJavaCode(srcBuilder, env, S2eUtils.lineSeparator(targetProject), S2eUtils.propertyMap(targetProject));
  }

  /**
   * Checks whether the given {@link ITypeHierarchy} contains an element with the given fully qualified name.
   *
   * @param h
   *          The hierarchy to search in.
   * @param fqn
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator (e.g.
   *          <code>org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass</code>).
   * @return <code>true</code> if it is part of the given {@link ITypeHierarchy}, <code>false</code> otherwise.
   */
  public static boolean hierarchyContains(ITypeHierarchy h, String fqn) {
    for (IType t : h.getAllTypes()) {
      if (fqn.equals(t.getFullyQualifiedName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Writes the given {@link ICompilationUnitSourceBuilder} into the given source folder.<br>
   * <br>
   * <b>Important Note: </b>The write operation is directly executed in the current thread! This means the current
   * {@link Job} must already hold an {@link ISchedulingRule} that contains the target file.
   *
   * @param srcFolder
   *          The source folder that will hold the new compilation unit. Must be of type
   *          {@link IPackageFragmentRoot#K_SOURCE}.
   * @param sb
   *          The source builder that creates the compilation unit contents.
   * @param env
   *          The {@link IJavaEnvironment} of the {@link IJavaProject} that belongs to the given
   *          {@link IPackageFragmentRoot} or <code>null</code> if a new {@link IJavaEnvironment} should be created.
   * @param monitor
   *          The {@link IProgressMonitor} of the surrounding job. Must not be <code>null</code>.
   * @param workingCopyManager
   *          The {@link IWorkingCopyManager} of the surrounding job. Must not be <code>null</code>.
   * @return The primary {@link IType} of the compilation unit written.
   */
  public static IType writeType(IPackageFragmentRoot srcFolder, ICompilationUnitSourceBuilder sb, IJavaEnvironment env, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    CompilationUnitWriteOperation writeIcu = new CompilationUnitWriteOperation(srcFolder, sb, env);
    writeIcu.validate();
    writeIcu.run(Validate.notNull(monitor), Validate.notNull(workingCopyManager));
    ICompilationUnit compilationUnit = writeIcu.getCreatedCompilationUnit();
    return compilationUnit.getType(sb.getMainType().getElementName());
  }

  /**
   * {@link Predicate} that only accepts public primary {@link IType}. Furthermore enum types and deprecated types are
   * excluded.
   */
  public static class PublicPrimaryTypeFilter implements Predicate<IType> {
    @Override
    public boolean test(IType candidate) {
      try {
        if (candidate.isMember() || candidate.isAnonymous() || candidate.isEnum() || candidate.isLocal()) {
          return false;
        }
        int modifiers = candidate.getFlags();
        return Flags.isPublic(modifiers) && !Flags.isDeprecated(modifiers);
      }
      catch (JavaModelException e) {
        throw new SdkException(e);
      }
    }
  }

  /**
   * {@link Predicate} that only accepts public abstract primary types that are no enums, not deprecated and no
   * interfaces.
   */
  public static class PublicAbstractPrimaryTypeFilter extends PublicPrimaryTypeFilter {
    @Override
    public boolean test(IType candidate) {
      boolean accept = super.test(candidate);
      if (!accept) {
        return false;
      }

      try {
        int modifiers = candidate.getFlags();
        return Flags.isAbstract(modifiers) && !Flags.isInterface(modifiers);
      }
      catch (JavaModelException e) {
        throw new SdkException(e);
      }
    }
  }

  /**
   * {@link Comparator} that sorts {@link IType}s by simple name first and fully qualified name second.
   *
   * @author Matthias Villiger
   * @since 5.2.0
   */
  public static final class ElementNameComparator implements Comparator<IType>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(IType o1, IType o2) {
      if (o1 == o2) {
        return 0;
      }

      int res = o1.getElementName().compareTo(o2.getElementName());
      if (res != 0) {
        return res;
      }
      res = o1.getFullyQualifiedName().compareTo(o2.getFullyQualifiedName());
      if (res != 0) {
        return res;
      }
      return o1.toString().compareTo(o2.toString());
    }
  }

  /**
   * Tries to find a test {@link IPackageFragmentRoot} in the given {@link IJavaProject} or an associated test
   * {@link IJavaProject}.
   *
   * @param orig
   *          The {@link IJavaProject} for which the primary test source folder should be found.
   * @param fqnOfRequiredType
   *          Fully qualified name of a type that must be accessible in the resulting {@link IPackageFragmentRoot} to be
   *          returned. May be <code>null</code> which means every {@link IPackageFragmentRoot} is accepted.
   * @return The test source folder having the given fqnOfRequiredType on the classpath or <code>null</code> if no such
   *         source folder could be found.
   * @throws JavaModelException
   */
  public static IPackageFragmentRoot getTestSourceFolder(IJavaProject orig, String fqnOfRequiredType) throws JavaModelException {
    IPackageFragmentRoot sourceFolder = getTestSourceFolderInProject(orig, fqnOfRequiredType);
    if (exists(sourceFolder)) {
      return sourceFolder;
    }

    // search for a test project
    String[] testProjectSuffixes = new String[]{".test", ".tests", ".testing"};
    IJavaModel javaModel = orig.getJavaModel(); // NOSONAR
    for (String suffix : testProjectSuffixes) {
      IJavaProject testProject = javaModel.getJavaProject(orig.getElementName() + suffix);
      sourceFolder = getTestSourceFolderInProject(testProject, fqnOfRequiredType);
      if (exists(sourceFolder)) {
        return sourceFolder;
      }
    }
    return null;
  }

  private static IPackageFragmentRoot getTestSourceFolderInProject(IJavaProject project, String fqnOfRequiredType) throws JavaModelException {
    if (!exists(project)) {
      return null;
    }

    if (StringUtils.isNotBlank(fqnOfRequiredType) && !exists(project.findType(fqnOfRequiredType))) {
      // it is not a test project (no dependency to required class)
      return null;
    }

    // search for a source folder in same project
    Predicate<IPackageFragmentRoot> filter = new Predicate<IPackageFragmentRoot>() {
      @Override
      public boolean test(IPackageFragmentRoot element) {
        String s = element.getPath().removeFirstSegments(1).toString().toLowerCase();
        return s.contains("test");
      }
    };
    Set<IPackageFragmentRoot> testSourceFolders = getSourceFolders(Collections.singletonList(project), filter, null);
    if (!testSourceFolders.isEmpty()) {
      return testSourceFolders.iterator().next();
    }
    return null;
  }

  /**
   * Gets the preferred source folder for DTOs created in the {@link IJavaProject} of the given source folder.
   *
   * @param selectedSourceFolder
   *          The default source folder.
   * @return The given selectedSourceFolder or the src/generated/java folder within the same {@link IJavaProject} if it
   *         exists.
   */
  public static IPackageFragmentRoot getDtoSourceFolder(IPackageFragmentRoot selectedSourceFolder) {
    if (!exists(selectedSourceFolder)) {
      return selectedSourceFolder;
    }
    IJavaProject targetProject = selectedSourceFolder.getJavaProject();
    if (!exists(targetProject)) {
      return selectedSourceFolder;
    }
    IFolder generatedFolder = targetProject.getProject().getFolder(ISdkProperties.GENERATED_SOURCE_FOLDER_NAME);
    if (generatedFolder == null || !generatedFolder.exists()) {
      return selectedSourceFolder;
    }
    IPackageFragmentRoot generatedSourceFolder = targetProject.getPackageFragmentRoot(generatedFolder);
    if (!exists(generatedSourceFolder)) {
      return selectedSourceFolder;
    }
    return generatedSourceFolder;
  }

  /**
   * Gets the best scout session type on the classpath of the given {@link IJavaProject}.
   *
   * @param project
   *          The {@link IJavaProject} for which the accessible session should be searched.
   * @param tier
   *          The type of session.
   * @param monitor
   * @return The session {@link IType} or <code>null</code> if no session could be found.
   * @throws CoreException
   */
  @SuppressWarnings("squid:S1067")
  public static IType getSession(IJavaProject project, ScoutTier tier, IProgressMonitor monitor) throws CoreException {
    Predicate<IType> filter = new PublicPrimaryTypeFilter() {
      @Override
      public boolean test(IType candidate) {
        if (!super.test(candidate)) {
          return false;
        }
        try {
          return candidate.isClass() && !Flags.isAbstract(candidate.getFlags());
        }
        catch (JavaModelException e) {
          throw new SdkException("Unable to check for flags in type '" + candidate.getFullyQualifiedName() + "'.", e);
        }
      }
    };

    String sessionToFind = null;
    switch (tier) {
      case Server:
        sessionToFind = IScoutRuntimeTypes.IServerSession;
        break;
      case Client:
      case HtmlUi:
        sessionToFind = IScoutRuntimeTypes.IClientSession;
        break;
      default:
        sessionToFind = IScoutRuntimeTypes.ISession;
        break;
    }
    Set<IType> sessions = S2eUtils.findClassesInStrictHierarchy(project, sessionToFind, monitor, filter);
    if (sessions.isEmpty()) {
      return null;
    }
    else if (sessions.size() == 1) {
      return sessions.iterator().next();
    }
    else {
      return findMostSpecific(sessions);
    }
  }

  private static IType findMostSpecific(Collection<IType> candidates) throws JavaModelException {
    ITypeHierarchy superHierarchy = null;
    for (IType t : candidates) {
      if (superHierarchy == null || !superHierarchy.contains(t)) {
        superHierarchy = t.newSupertypeHierarchy(null);
      }
    }
    if (superHierarchy == null) {
      return null;
    }
    return superHierarchy.getType();
  }

  /**
   * Gets the content of the given {@link IFile} as {@link String}.
   *
   * @param file
   *          The {@link IFile} whose content should be returned.
   * @return The content of the given {@link IFile}.
   * @throws CoreException
   *           If there is an error reading the file content. Reasons include:
   *           <ul>
   *           <li>This resource does not exist.</li>
   *           <li>This resource is not local.</li>
   *           <li>The file-system resource is not a file.</li>
   *           <li>The workspace is not in sync with the corresponding location in the local file system (and
   *           {@link ResourcesPlugin#PREF_LIGHTWEIGHT_AUTO_REFRESH} is disabled).</li>
   *           </ul>
   */
  public static String getContentOfFile(IFile file) throws CoreException {
    if (file == null || !file.exists()) {
      return null;
    }
    String charsetName = file.getCharset();
    try (InputStream contents = file.getContents()) {
      return CoreUtils.inputStreamToString(contents, charsetName).toString();
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus("Unable to read file '" + file.getFullPath().toOSString() + "'.", e));
    }
  }

  /**
   * Gets the content of the pom.xml file of the given {@link IProject} as {@link Document}.
   *
   * @param p
   *          The {@link IProject} for which the pom should be returned.
   * @return The {@link Document} holding the pom.xml contents.
   * @throws CoreException
   */
  public static Document getPomDocument(IProject p) throws CoreException {
    IFile pom = p.getFile(IMavenConstants.POM);
    return readXmlDocument(pom);
  }

  /**
   * Reads the given {@link IFile} into an XML {@link Document}.
   *
   * @param file
   *          The {@link IFile} that should be loaded. Must be an XML file!
   * @return a {@link Document} holding the contents of the {@link IFile} or <code>null</code> if the given {@link IFile}
   *         does not exist.
   * @throws CoreException
   */
  public static Document readXmlDocument(IFile file) throws CoreException {
    if (!file.exists()) {
      return null;
    }

    try {
      DocumentBuilder docBuilder = CoreUtils.createDocumentBuilder();
      try (InputStream in = file.getContents()) {
        return docBuilder.parse(in);
      }
    }
    catch (IOException | ParserConfigurationException | CoreException | SAXException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  /**
   * Writes the given {@link Document} to the given {@link IFile}.
   *
   * @param document
   *          The {@link Document} holding the new content of the given {@link IFile}. Must not be <code>null</code>.
   * @param file
   *          The {@link IFile} where the contents should be written to. The file must not exist yet. Must not be
   *          <code>null</code>.
   * @param monitor
   *          The progress monitor
   * @param workingCopyManager
   *          The working copy manager
   * @throws CoreException
   */
  public static void writeXmlDocument(Document document, IFile file, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      ResourceWriteOperation writeOp = new ResourceWriteOperation(file, CoreUtils.xmlDocumentToString(document, true));
      writeOp.validate();
      writeOp.run(monitor, workingCopyManager);
    }
    catch (TransformerException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  /**
   * Execute a Maven update on the given {@link IProject}s.
   *
   * @param projects
   *          The projects for which a Maven update should be performed.
   * @param updateSnapshots
   *          Specifies if an update of snapshot dependencies should be enforced.
   * @param updateConfig
   *          Specifies if the Eclipse project configuration should be updated based on the pom.xml
   * @param cleanProject
   *          Specifies if the project should be cleaned.
   * @param refreshFromDisk
   *          Specifies if the project should be refreshed from disk.
   * @param monitor
   *          The {@link IProgressMonitor} for the update operation.
   * @return A {@link Map} containing the project name as key and an {@link IStatus} describing the update result for the
   *         corresponding project.
   */
  public static Map<String, IStatus> mavenUpdate(Set<IProject> projects, boolean updateSnapshots, boolean updateConfig, boolean cleanProject, boolean refreshFromDisk, IProgressMonitor monitor) {
    if (projects == null || projects.isEmpty()) {
      return Collections.emptyMap();
    }
    MavenPluginActivator mavenPlugin = MavenPluginActivator.getDefault();
    if (mavenPlugin == null) {
      return Collections.emptyMap();
    }
    ProjectConfigurationManager configurationManager = (ProjectConfigurationManager) mavenPlugin.getProjectConfigurationManager();
    if (configurationManager == null) {
      return Collections.emptyMap();
    }
    MavenUpdateRequest request = new MavenUpdateRequest(projects.toArray(new IProject[projects.size()]), false, updateSnapshots);
    if (monitor != null && monitor.isCanceled()) {
      return Collections.emptyMap();
    }
    Map<String, IStatus> result = configurationManager.updateProjectConfiguration(request, updateConfig, cleanProject, refreshFromDisk, monitor);
    return result;
  }
}
