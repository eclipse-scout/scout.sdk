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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
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
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;
import org.eclipse.scout.sdk.s2e.workspace.CompilationUnitWriteOperation;
import org.eclipse.scout.sdk.s2e.workspace.ResourceWriteOperation;

/**
 * <h3>{@link JdtUtils}</h3>
 * <p>
 * Contains utility methods for JDT objects.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class JdtUtils {
  private static final Comparator<IType> COMPARATOR = new P_TypeMatchComparator();

  private JdtUtils() {
  }

  /**
   * Converts the {@link org.eclipse.jdt.core.IType} to {@link org.eclipse.scout.sdk.core.model.api.IType}
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
   * Converts the given {@link org.eclipse.jdt.core.IType} to {@link org.eclipse.scout.sdk.core.model.api.IType}
   *
   * @param jdtType
   *          The jdt {@link IType} to convert.
   * @param env
   *          The {@link IJavaEnvironment} to use to find the matching
   *          {@link org.eclipse.scout.sdk.core.model.api.IType}.
   * @return The {@link org.eclipse.scout.sdk.core.model.api.IType} matching the given JDT {@link IType}.
   * @throws CoreException
   */
  public static org.eclipse.scout.sdk.core.model.api.IType jdtTypeToScoutType(IType jdtType, IJavaEnvironment env) throws CoreException {
    return env.findType(jdtType.getFullyQualifiedName('$'));
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
   * Searches for an {@link IType} with a specific name within the given type recursively checking all inner types. The
   * given {@link IType} itself is checked as well.
   *
   * @param type
   *          The {@link IType} to start searching. All nested inner {@link IType}s are visited recursively.
   * @param innerTypeName
   *          The simple name (case sensitive) to search for.
   * @return The first {@link IType} found in the nested {@link IType} tree below the given start type that has the
   *         given simple name or <code>null</code> if nothing could be found.
   * @throws JavaModelException
   */
  public static IType findInnerType(IType type, String innerTypeName) throws JavaModelException {
    if (!exists(type)) {
      return null;
    }
    else if (Objects.equals(type.getElementName(), innerTypeName)) {
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
   * Gets all {@link IType}s that are accessible in the current workspace having the given fully qualified name.
   *
   * @param fqn
   *          The fully qualified name of the types to search. Inner types must use the '$' enclosing type separator
   *          (e.g. <code>org.eclipse.scout.TestClass$InnerClass$NextLevelInnerClass</code>).
   * @return
   * @throws CoreException
   */
  public static Set<org.eclipse.jdt.core.IType> resolveJdtTypes(final String fqn) throws CoreException {
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = Signature.getSimpleName(fqn);
    final Set<IType> matchList = new TreeSet<>(COMPARATOR);
    new SearchEngine().search(SearchPattern.createPattern(fastPat, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH), new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
        SearchEngine.createWorkspaceScope(), new SearchRequestor() {
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

  public static IAnnotation getFirstAnnotationInSupertypeHierarchy(IType type, String... fullyQualifiedAnnotations) throws CoreException {
    if (type == null) {
      return null;
    }
    IAnnotation ann = getFirstDeclaredAnnotation(type, fullyQualifiedAnnotations);
    if (ann != null) {
      return ann;
    }
    ITypeHierarchy h = type.newSupertypeHierarchy(null);
    for (IType t : h.getAllSuperclasses(type)) {
      ann = getFirstDeclaredAnnotation(t, fullyQualifiedAnnotations);
      if (ann != null && ann.exists()) {
        return ann;
      }
    }
    for (IType t : h.getAllSuperInterfaces(type)) {
      ann = getFirstDeclaredAnnotation(t, fullyQualifiedAnnotations);
      if (ann != null && ann.exists()) {
        return ann;
      }
    }
    return null;
  }

  public static IAnnotation getFirstDeclaredAnnotation(IAnnotatable element, String... fullyQualifiedAnnotations) {
    for (String fqn : fullyQualifiedAnnotations) {
      IAnnotation ann = getAnnotation(element, fqn);
      if (ann != null) {
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
    for (IType annotationType : resolveJdtTypes(annotationName)) {
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

  public static PropertyMap propertyMap(IJavaProject p) {
    PropertyMap context = new PropertyMap();
    context.setProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, p);
    return context;
  }

  public static List<IType> writeTypesWithResult(Collection<CompilationUnitWriteOperation> ops, IProgressMonitor monitor) throws CoreException {
    writeTypes(ops, monitor, true);

    List<IType> result = new ArrayList<>(ops.size());
    for (CompilationUnitWriteOperation op : ops) {
      if (op == null) {
        result.add(null);
        continue;
      }
      result.add(op.getCompilationUnit().getTypes()[0]);
    }
    return result;
  }

  public static void writeTypes(Collection<CompilationUnitWriteOperation> ops, IProgressMonitor monitor, boolean waitUntilWritten) throws CoreException {
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
        throw new CoreException(new ScoutStatus("Unable to wait until compilation units have been written.", e));
      }
    }
  }

  public static List<IFile> writeResourcesWithResult(Collection<ResourceWriteOperation> ops, IProgressMonitor monitor) throws CoreException {
    writeResources(ops, monitor, true);

    List<IFile> result = new ArrayList<>(ops.size());
    for (ResourceWriteOperation op : ops) {
      if (op == null) {
        result.add(null);
        continue;
      }
      result.add(op.getFile());
    }
    return result;
  }

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
