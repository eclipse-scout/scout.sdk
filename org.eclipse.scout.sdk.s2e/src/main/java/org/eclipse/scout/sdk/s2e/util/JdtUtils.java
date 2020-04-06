/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.util;

import static java.util.Collections.emptySet;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;
import static org.eclipse.scout.sdk.core.model.api.Flags.isDeprecated;
import static org.eclipse.scout.sdk.core.model.api.Flags.isInterface;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPublic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link JdtUtils}</h3>
 *
 * @since 7.0.0
 */
public final class JdtUtils {
  private static final Comparator<IType> COMPARATOR = new P_TypeMatchComparator();

  private JdtUtils() {
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
   */
  public static String getPackage(ICompilationUnit icu) {
    if (!exists(icu)) {
      return "";
    }

    try {
      IPackageDeclaration[] packageDeclarations = icu.getPackageDeclarations();
      if (packageDeclarations.length < 1) {
        return "";
      }

      return packageDeclarations[0].getElementName();
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Gets {@link IType}s for all public abstract classes existing on the classpath of the given project and
   * implementing/extending the given baseTypeFqn.
   *
   * @param sourceProject
   *          The {@link IJavaProject} defining the classpath.
   * @param baseTypeFqn
   *          The fully qualified name of the base class. The sub classes of this class are searched.
   * @param monitor
   *          The monitor or {@code null}. If the monitor becomes canceled, the search is aborted and an incomplete
   *          result may be returned. The caller of this method is responsible to react on this fact based on the
   *          monitor state ({@link IProgressMonitor#isCanceled()}).
   * @return A {@link Set} containing all {@link IType}s sorted ascending by fully qualified name.
   */
  public static Set<IType> findAbstractClassesInHierarchy(IJavaProject sourceProject, String baseTypeFqn, IProgressMonitor monitor) {
    return findTypesInStrictHierarchy(sourceProject, baseTypeFqn, monitor, new PublicAbstractPrimaryTypeFilter());
  }

  /**
   * @see #findTypesInStrictHierarchy(IJavaProject, IType, IProgressMonitor, Predicate)
   */
  public static Set<IType> findTypesInStrictHierarchy(IJavaProject sourceProject, String baseTypeFqn, IProgressMonitor monitor, Predicate<IType> filter) {
    if (!exists(sourceProject)) {
      return emptySet();
    }
    try {
      IType t = sourceProject.findType(baseTypeFqn.replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT), monitor);
      return findTypesInStrictHierarchy(sourceProject, t, monitor, filter);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Gets all {@link IType}s on the classpath of the give {@link IJavaProject} that are sub types of the given baseType
   * and fulfill the given filter. If the base type itself fulfills the given filter, it is included in the result.
   *
   * @param sourceProject
   *          The {@link IJavaProject} defining the classpath.
   * @param baseType
   *          The base class of the hierarchy.
   * @param monitor
   *          The monitor or {@code null}. If the monitor becomes canceled, the search is aborted and an incomplete
   *          result may be returned. The caller of this method is responsible to react on this fact based on the
   *          monitor state ({@link IProgressMonitor#isCanceled()}).
   * @param filter
   *          A filter to decide which matches are accepted or {@code null} if all matches should be accepted.
   * @return A {@link Set} containing the {@link IType}s sorted ascending by simple name and fully qualified name
   *         afterwards.
   */
  public static Set<IType> findTypesInStrictHierarchy(IJavaProject sourceProject, IType baseType, IProgressMonitor monitor, Predicate<IType> filter) {
    if (!exists(baseType) || !exists(baseType.getParent()) || !exists(sourceProject)) {
      return emptySet();
    }

    // do not use SearchEngine.createStrictHierarchyScope because there is a bug in Eclipse 2019-12:
    // files open in the Java Editor are not included in the result! Use the type hierarchy instead.
    try {
      ITypeHierarchy hierarchy = baseType.newTypeHierarchy(sourceProject, monitor);
      org.eclipse.jdt.core.IType[] jdtTypes = hierarchy.getAllSubtypes(baseType);
      if (jdtTypes == null || jdtTypes.length < 1) {
        return emptySet();
      }

      Set<IType> collector = new TreeSet<>(new ElementNameComparator());
      for (org.eclipse.jdt.core.IType candidate : jdtTypes) {
        if (filter == null || filter.test(candidate)) {
          collector.add(candidate);
        }
      }
      return collector;
    }
    catch (OperationCanceledException oce) {
      SdkLog.debug("Strict hierarchy search has been canceled. An incomplete result will be returned.", oce);
      return emptySet();
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Gets all {@link IType}s that are accessible in the current workspace having the given fully qualified name.
   *
   * @param fqn
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator
   *          (e.g. {@code org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass}).
   * @return A {@link Set} with all {@link IType}s
   */
  public static Set<IType> resolveJdtTypes(CharSequence fqn) {
    return resolveJdtTypes(fqn, SearchEngine.createWorkspaceScope());
  }

  /**
   * Gets all {@link IType}s that are accessible in the given scope having the given fully qualified name.
   *
   * @param fqn
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator
   *          (e.g. {@code org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass}).
   * @param scope
   *          The {@link IJavaSearchScope} that defines where to search.
   * @return A {@link Set} with all {@link IType}s
   */
  public static Set<IType> resolveJdtTypes(CharSequence fqn, IJavaSearchScope scope) {
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = JavaTypes.simpleName(fqn);
    Set<IType> matchList = new TreeSet<>(COMPARATOR);
    try {
      new SearchEngine().search(SearchPattern.createPattern(fastPat, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH),
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          scope, new SearchRequestor() {
            @Override
            public void acceptSearchMatch(SearchMatch match) {
              Object element = match.getElement();
              if (element instanceof IType) {
                IType t = (IType) element;
                if (t.getFullyQualifiedName().contains(fqn)) {
                  matchList.add(t);
                }
              }
            }
          }, null);
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
    return matchList;
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
   * Gets the {@link IAnnotation} with the given fully qualified name.
   *
   * @param element
   *          The owner for which the {@link IAnnotation} should be searched.
   * @param fullyQualifiedAnnotation
   *          The fully qualified class name of the annotation (e.g. {@code java.lang.SuppressWarnings}).
   * @return The {@link IAnnotation} on the given element or {@code null} if it could not be found.
   */
  public static IAnnotation getAnnotation(IAnnotatable element, String fullyQualifiedAnnotation) {
    if (element == null) {
      return null;
    }

    String simpleName = JavaTypes.simpleName(fullyQualifiedAnnotation);
    String startSimple = '@' + simpleName;
    String startFq = '@' + fullyQualifiedAnnotation;

    IAnnotation result = getAnnotation(element, simpleName, startSimple, startFq);
    if (result != null) {
      return result;
    }
    return getAnnotation(element, fullyQualifiedAnnotation, startSimple, startFq);
  }

  private static IAnnotation getAnnotation(IAnnotatable element, String name, String startSimple, String startFq) {
    IAnnotation annotation = element.getAnnotation(name);
    if (!exists(annotation)) {
      return null;
    }

    String annotSource = null;
    try {
      annotSource = annotation.getSource();
      if (annotSource != null) {
        annotSource = annotSource.trim();
      }
    }
    catch (JavaModelException e) {
      SdkLog.warning("Could not get source of annotation '{}' in element '{}'.", name, element, e);
    }

    if (annotSource == null || annotSource.startsWith(startSimple) || annotSource.startsWith(startFq)) {
      return annotation;
    }
    if (element instanceof IMember) {
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
   *          The monitor or {@code null}. If the monitor becomes canceled, the search is aborted and an incomplete
   *          result may be returned. The caller of this method is responsible to react on this fact based on the
   *          monitor state ({@link IProgressMonitor#isCanceled()}).
   */
  public static Set<IType> findAllTypesAnnotatedWith(String annotationName, IJavaSearchScope scope, IProgressMonitor monitor) {
    Set<IType> result = new LinkedHashSet<>();
    SearchRequestor collector = new SearchRequestor() {
      @Override
      public void acceptSearchMatch(SearchMatch match) {
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
      catch (CoreException e) {
        throw new SdkException(e);
      }
    }
    return result;
  }

  /**
   * Checks if the given {@link IJavaElement} is not {@code null} and exists.
   *
   * @param element
   *          The element to check.
   * @return {@code true} if the given element is not {@code null} and exists.
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
   * @return {@code true} if element was found in the classpath of project, {@code false} otherwise.
   */
  public static boolean isOnClasspath(IJavaElement element, IJavaProject project) {
    if (!exists(element) || !exists(project)) {
      return false;
    }
    boolean isInWorkspace = element.getResource() != null;
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
   * @return A {@link BigDecimal} with the numeric value of the given attribute or {@code null} if the attribute could
   *         not be found or is not numeric.
   */
  public static BigDecimal getAnnotationValueNumeric(IAnnotation annotation, String name) {
    if (!exists(annotation)) {
      return null;
    }

    try {
      IMemberValuePair[] memberValues = annotation.getMemberValuePairs();
      for (IMemberValuePair p : memberValues) {
        if (Objects.equals(name, p.getMemberName())) {
          switch (p.getValueKind()) {
            case IMemberValuePair.K_DOUBLE:
              Double doubleValue = (Double) p.getValue();
              return BigDecimal.valueOf(doubleValue);
            case IMemberValuePair.K_FLOAT:
              Number floatValue = (Float) p.getValue();
              return BigDecimal.valueOf(floatValue.doubleValue());
            case IMemberValuePair.K_INT:
              Number intValue = (Integer) p.getValue();
              return BigDecimal.valueOf(intValue.longValue());
            case IMemberValuePair.K_BYTE:
              Number byteValue = (Byte) p.getValue();
              return BigDecimal.valueOf(byteValue.longValue());
            case IMemberValuePair.K_LONG:
              Long longValue = (Long) p.getValue();
              return BigDecimal.valueOf(longValue);
            case IMemberValuePair.K_SHORT:
              Number shortValue = (Short) p.getValue();
              return BigDecimal.valueOf(shortValue.longValue());
            default:
              return null;
          }
        }
      }
      return null;
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Gets the value of the given annotation attribute as {@link String}.
   *
   * @param annotation
   *          The {@link IAnnotation} for which the attribute should be converted.
   * @param name
   *          The name of attribute.
   * @return A {@link String} with the value of the given attribute or {@code null} if the value is {@code null} or
   *         could not be found.
   */
  public static String getAnnotationValueString(IAnnotation annotation, String name) {
    if (!exists(annotation)) {
      return null;
    }

    try {
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
    catch (JavaModelException e) {
      throw new SdkException(e);
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
  public static IJavaSearchScope createJavaSearchScope(IJavaElement... elements) {
    return SearchEngine.createJavaSearchScope(elements, IJavaSearchScope.SOURCES);
  }

  /**
   * Creates a {@link IJavaSearchScope} based on the given {@link IResource}s.<br>
   * The search scope contains the following items depending on the resource type:
   * <ul>
   * <li>{@link IResource#PROJECT}: If the project has the java nature ({@link JavaCore#NATURE_ID}) and is open all
   * source folders of the project are added (no libraries or dependent projects).</li>
   * <li>{@link IResource#FOLDER}: The {@link IPackageFragment} or {@link IPackageFragmentRoot} is added if the folder
   * belongs to one. All sub-packages are included as well!</li>
   * <li>{@link IResource#FILE}: If the file is a java file, the corresponding {@link ICompilationUnit} is added. If the
   * resource is a class file, the corresponding {@link IClassFile} is added. If the file is a jar file, the
   * corresponding {@link IPackageFragmentRoot} is added.</li>
   * <li>{@link IResource#ROOT}: All source folders of all {@link IJavaProject}s in the workspace are added.</li>
   * <ul>
   *
   * @param resources
   *          The resources that should be added to the {@link IJavaSearchScope}.
   * @return A new {@link IJavaSearchScope} that contains all {@link IJavaElement}s that could be matched by the given
   *         resources or {@code null} if no {@link IJavaElement} is part of the given resources.
   */
  public static IJavaSearchScope createJavaSearchScope(Collection<IResource> resources) {
    if (resources == null || resources.isEmpty()) {
      return null;
    }

    Set<IJavaElement> jset = new HashSet<>(resources.size());
    for (IResource resource : resources) {
      if (resource == null || !resource.isAccessible()) {
        continue;
      }

      int type = resource.getType();
      switch (type) {
        case IResource.PROJECT:
          IProject project = (IProject) resource;
          try {
            if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
              addJavaElement(jset, JavaCore.create(project));
            }
          }
          catch (CoreException e) {
            throw new SdkException(e);
          }
          break;
        case IResource.FILE:
          addJavaElement(jset, JavaCore.create((IFile) resource));
          break;
        case IResource.FOLDER:
          try {
            resource.accept(proxy -> {
              if (proxy.getType() == IResource.FOLDER) {
                IFolder folder = (IFolder) proxy.requestResource();
                addJavaElement(jset, JavaCore.create(folder));
                return true;
              }
              return false;
            }, IResource.NONE);
          }
          catch (CoreException e) {
            throw new SdkException(e);
          }
          break;
        case IResource.ROOT:
          IJavaModel model = JavaCore.create((IWorkspaceRoot) resource);
          if (exists(model)) {
            try {
              for (IJavaProject jp : model.getJavaProjects()) {
                addJavaElement(jset, jp);
              }
            }
            catch (JavaModelException e) {
              throw new SdkException(e);
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
    return createJavaSearchScope(jset.toArray(new IJavaElement[0]));
  }

  private static void addJavaElement(Collection<IJavaElement> collector, IJavaElement elementToAdd) {
    if (exists(elementToAdd)) {
      collector.add(elementToAdd);
    }
  }

  /**
   * Checks whether the given {@link ITypeHierarchy} contains an element with the given fully qualified name.
   *
   * @param h
   *          The hierarchy to search in.
   * @param fqn
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator
   *          (e.g. {@code org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass}).
   * @return {@code true} if it is part of the given {@link ITypeHierarchy}, {@code false} otherwise.
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
   * Gets the first {@link IAnnotation} on the given {@link IType} or its super types that has one of the given fully
   * qualified names.
   *
   * @param type
   *          The {@link IType} where to start the search.
   * @param fullyQualifiedAnnotations
   *          The fully qualified names of the annotations to search.
   * @return The first of the annotations found in the super hierarchy of the given {@link IType} or {@code null} if it
   *         could not be found.
   */
  public static IAnnotation getFirstAnnotationInSupertypeHierarchy(IType type, String... fullyQualifiedAnnotations) {
    if (type == null) {
      return null;
    }
    IAnnotation ann = getFirstDeclaredAnnotation(type, fullyQualifiedAnnotations);
    if (exists(ann)) {
      return ann;
    }

    try {
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
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Gets the first {@link IAnnotation} of the given fully qualified names that exist on the given {@link IAnnotatable}.
   *
   * @param element
   *          The owner to search in.
   * @param fullyQualifiedAnnotations
   *          The annotations to search.
   * @return the first of the given annotations that was found or {@code null}.
   */
  private static IAnnotation getFirstDeclaredAnnotation(IAnnotatable element, String... fullyQualifiedAnnotations) {
    for (String fqn : fullyQualifiedAnnotations) {
      IAnnotation ann = getAnnotation(element, fqn);
      if (exists(ann)) {
        return ann;
      }
    }
    return null;
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
        return isPublic(modifiers) && !isDeprecated(modifiers);
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
        return isAbstract(modifiers) && !isInterface(modifiers);
      }
      catch (JavaModelException e) {
        throw new SdkException(e);
      }
    }
  }

  /**
   * {@link Comparator} that sorts {@link IType}s by simple name first and fully qualified name second.
   *
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
}
