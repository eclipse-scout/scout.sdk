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
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.TreeSet;

import org.apache.commons.collections4.set.ListOrderedSet;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.job.JobEx;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;

/**
 *
 */
public final class JdtUtils {
  private static final Comparator<IType> COMPARATOR = new P_TypeMatchComparator();

  private JdtUtils() {
  }

  public static org.eclipse.scout.sdk.core.model.IType jdtTypeToScoutType(org.eclipse.jdt.core.IType jdtType) throws CoreException {
    return jdtTypeToScoutType(jdtType, ScoutSdkCore.createLookupEnvironment(jdtType.getJavaProject(), true));
  }

  public static org.eclipse.scout.sdk.core.model.IType jdtTypeToScoutType(org.eclipse.jdt.core.IType jdtType, ILookupEnvironment lookupEnv) throws CoreException {
    IFile resource = (IFile) jdtType.getResource();
    String charsetName = resource.getCharset();
    if (!Charset.isSupported(charsetName)) {
      throw new CoreException(new ScoutStatus("Unsupported charset '" + charsetName + "' for resource '" + resource.getLocation().toOSString() + "'. Skipping.", new Exception("origin")));
    }
    return lookupEnv.findType(jdtType.getFullyQualifiedName('$'));
  }

  public static ListOrderedSet<IType> resolveJdtTypes(final String fqn) throws CoreException {
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = Signature.getSimpleName(fqn);
    final TreeSet<IType> matchList = new TreeSet<>(COMPARATOR);
    new SearchEngine().search(
        SearchPattern.createPattern(fastPat, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH),
        new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
        SearchEngine.createWorkspaceScope(),
        new SearchRequestor() {
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
        },
        null
        );
    return ListOrderedSet.listOrderedSet(matchList);
  }

  public static void waitForBuild() {
    JobEx.waitForJobFamily(ResourcesPlugin.FAMILY_MANUAL_BUILD);
    JobEx.waitForJobFamily(ResourcesPlugin.FAMILY_AUTO_BUILD);
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

  public static IAnnotation getAnnotation(IAnnotatable element, String fullyQualifiedAnnotation) {
    if (element == null) {
      return null;
    }

    String simpleName = Signature.getSimpleName(fullyQualifiedAnnotation);
    String startSimple = '@' + simpleName;
    String startFq = '@' + fullyQualifiedAnnotation;

    String annotSource = null;
    IAnnotation annotation = element.getAnnotation(simpleName);
    if (exists(annotation)) {
      try {
        annotSource = annotation.getSource();
        if (annotSource != null) {
          annotSource = annotSource.trim();
        }
      }
      catch (Exception e) {
        S2ESdkActivator.logWarning("Could not get source of annotation '" + fullyQualifiedAnnotation + "' in element '" + element.toString() + "'.", e);
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
    }

    annotation = element.getAnnotation(fullyQualifiedAnnotation);
    if (exists(annotation)) {
      try {
        annotSource = annotation.getSource();
        if (annotSource != null) {
          annotSource = annotSource.trim();
        }
      }
      catch (Exception e) {
        S2ESdkActivator.logWarning("Could not get source of annotation '" + fullyQualifiedAnnotation + "' in element '" + element.toString() + "'.", e);
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
    }
    return null;
  }

  private static String getAnnotationSourceFixed(IMember member, IAnnotation annotation, String startSimple) {
    try {
      ISourceRange annotSourceRange = annotation.getSourceRange();
      ISourceRange ownerSourceRange = member.getSourceRange();
      if (annotSourceRange != null && ownerSourceRange != null) {
        if (annotSourceRange.getOffset() >= 0 && ownerSourceRange.getOffset() >= 0 && ownerSourceRange.getOffset() > annotSourceRange.getOffset()) {
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
    }
    catch (JavaModelException e) {
      S2ESdkActivator.logWarning("Unable to find source for annotation '" + annotation.getElementName() + "' in '" + member.getElementName() + "'.", e);
    }
    return null;
  }

  public static boolean exists(IJavaElement element) {
    return element != null && element.exists();
  }

  public static Double getAnnotationValueNumeric(IAnnotation annotation, String name) throws JavaModelException {
    if (!exists(annotation)) {
      return null;
    }
    IMemberValuePair[] memberValues = annotation.getMemberValuePairs();
    for (IMemberValuePair p : memberValues) {
      if (name.equals(p.getMemberName())) {
        switch (p.getValueKind()) {
          case IMemberValuePair.K_DOUBLE:
            return (Double) p.getValue();
          case IMemberValuePair.K_FLOAT:
            return ((Float) p.getValue()).doubleValue();
          case IMemberValuePair.K_INT:
            return ((Integer) p.getValue()).doubleValue();
        }
        break;
      }
    }
    return null;
  }
}
