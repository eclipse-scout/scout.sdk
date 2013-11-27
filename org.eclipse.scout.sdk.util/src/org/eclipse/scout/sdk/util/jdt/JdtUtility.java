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
package org.eclipse.scout.sdk.util.jdt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.pde.LazyPluginModel;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

@SuppressWarnings("restriction")
public final class JdtUtility {
  private final static String DOUBLE_QUOTES = "\"";
  private final static Pattern LIT_ESC_1 = Pattern.compile("\\", Pattern.LITERAL);
  private final static String REP_1 = Matcher.quoteReplacement("\\\\");
  private final static Pattern LIT_ESC_2 = Pattern.compile("\"", Pattern.LITERAL);
  private final static String REP_2 = Matcher.quoteReplacement("\\\"");
  private final static Pattern LIT_ESC_3 = Pattern.compile("\n", Pattern.LITERAL);
  private final static String REP_3 = Matcher.quoteReplacement("\\n");
  private final static Pattern LIT_ESC_4 = Pattern.compile("\r", Pattern.LITERAL);
  private final static String REP_4 = Matcher.quoteReplacement("");
  private final static Pattern LIT_ESC_5 = Pattern.compile("\\\\", Pattern.LITERAL);
  private final static String REP_5 = Matcher.quoteReplacement("\\");
  private final static Pattern LIT_ESC_6 = Pattern.compile("\\\"", Pattern.LITERAL);
  private final static String REP_6 = Matcher.quoteReplacement("\"");
  private final static Pattern LIT_ESC_7 = Pattern.compile("([^\\\\]{1})\\\\n");
  private final static String REP_7 = "$1\n";

  private JdtUtility() {
  }

  public static IJavaElement findJavaElement(IFile javaFile, ITextSelection selection) throws JavaModelException {
    IJavaElement javaElement = JavaCore.create(javaFile);
    javaElement = findJavaElement(javaElement, selection.getOffset(), selection.getLength());
    return javaElement;
  }

  public static boolean hasAnnotation(IAnnotatable element, String fullyQuallifiedAnnotation) {
    return TypeUtility.exists(getAnnotation(element, fullyQuallifiedAnnotation));
  }

  public static Double getNumericAnnotationValue(IAnnotation annotation, String name) throws JavaModelException {
    if (TypeUtility.exists(annotation)) {
      IMemberValuePair[] memberValues = annotation.getMemberValuePairs();
      for (IMemberValuePair p : memberValues) {
        if (name.equals(p.getMemberName())) {
          switch (p.getValueKind()) {
            case IMemberValuePair.K_DOUBLE:
              return ((Double) p.getValue()).doubleValue();
            case IMemberValuePair.K_FLOAT:
              return ((Float) p.getValue()).doubleValue();
            case IMemberValuePair.K_INT:
              return ((Integer) p.getValue()).doubleValue();
          }
          break;
        }
      }
    }
    return null;
  }

  public static IAnnotation getAnnotation(IAnnotatable element, String fullyQuallifiedAnnotation) {
    try {
      IAnnotation annotation = element.getAnnotation(fullyQuallifiedAnnotation);
      // workaround since annotations are not cached properly from jdt
      if (TypeUtility.exists(annotation) && (annotation.getSource() == null || annotation.getSource().startsWith("@"))) {
        return annotation;
      }
      else {
        String simpleName = Signature.getSimpleName(fullyQuallifiedAnnotation);
        annotation = element.getAnnotation(simpleName);
        if (TypeUtility.exists(annotation) && (annotation.getSource() == null || annotation.getSource().startsWith("@"))) {
          return annotation;
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logError("could not get annotation '" + fullyQuallifiedAnnotation + "' of '" + element + "'", e);
    }
    return null;
  }

  /**
   * converts the given string into a string literal with leading and ending double-quotes including escaping of the
   * given string.<br>
   * this is the inverse function of {@link JdtUtility#fromStringLiteral(String)}
   * 
   * @param s
   *          the string to convert.
   * @return the literal string ready to be directly inserted into java source or null if the input string is null.
   */
  public static String toStringLiteral(String s) {
    if (s == null) return null;
    String escaped = s;
    escaped = LIT_ESC_1.matcher(escaped).replaceAll(REP_1);
    escaped = LIT_ESC_2.matcher(escaped).replaceAll(REP_2);
    escaped = LIT_ESC_3.matcher(escaped).replaceAll(REP_3);
    escaped = LIT_ESC_4.matcher(escaped).replaceAll(REP_4);
    return DOUBLE_QUOTES + escaped + DOUBLE_QUOTES;
  }

  /**
   * converts the given input string literal into the representing original string.<br>
   * this is the inverse function of {@link JdtUtility#toStringLiteral(String)}
   * 
   * @param l
   *          the literal with leading and ending double-quotes
   * @return the original (un-escaped) string. if it is no valid literal string, null is returned.
   */
  public static String fromStringLiteral(String l) {
    if (l == null || l.length() < 2 || !l.startsWith(DOUBLE_QUOTES) || !l.endsWith(DOUBLE_QUOTES)) return null;
    String inner = l.substring(1, l.length() - 1);
    String ret = inner;
    ret = LIT_ESC_7.matcher(ret).replaceAll(REP_7);
    ret = LIT_ESC_5.matcher(ret).replaceAll(REP_5);
    ret = LIT_ESC_6.matcher(ret).replaceAll(REP_6);
    return ret;
  }

  public static IJavaElement findJavaElement(IJavaElement element, int offset, int lenght) throws JavaModelException {
    if (element == null) {
      return null;
    }
    switch (element.getElementType()) {
      case IJavaElement.COMPILATION_UNIT:
        ICompilationUnit icu = (ICompilationUnit) element;
        IType[] icuTypes = icu.getTypes();
        for (IType t : icuTypes) {
          if (t.getSourceRange().getOffset() < offset && (t.getSourceRange().getOffset() + t.getSourceRange().getLength()) > (offset + lenght)) {
            // step in
            return findJavaElement(t, offset, lenght);
          }
        }
        if (icuTypes.length > 0) {
          return icuTypes[0];
        }

        break;
      case IJavaElement.TYPE:
        for (IType t : ((IType) element).getTypes()) {
          if (t.getSourceRange().getOffset() < offset && (t.getSourceRange().getOffset() + t.getSourceRange().getLength()) > (offset + lenght)) {
            // step in
            return findJavaElement(t, offset, lenght);
          }
        }
        // methods
        for (IMethod m : ((IType) element).getMethods()) {
          if (m.getSourceRange().getOffset() < offset && (m.getSourceRange().getOffset() + m.getSourceRange().getLength()) > (offset + lenght)) {
            // step in
            return findJavaElement(m, offset, lenght);
          }
        }
        break;
    }
    return element;
  }

  public static IType findDeclaringType(IJavaElement element) {
    if (element == null) {
      return null;
    }
    if (element.getElementType() == IJavaElement.TYPE) {
      return (IType) element;
    }
    return findDeclaringType(element.getParent());
  }

  public static void waitForSilentWorkspace() {
    Job worker = new Job("") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        return Status.OK_STATUS;
      }
    };
    worker.setRule(ResourcesPlugin.getWorkspace().getRoot());
    worker.schedule();
    try {
      worker.join();
    }
    catch (InterruptedException e) {
    }
    waitForRefresh();
    waitForBuild();
    waitForIndexesReady();
  }

  public static void waitForBuild() {
    waitForJobFamily(ResourcesPlugin.FAMILY_MANUAL_BUILD);
    waitForJobFamily(ResourcesPlugin.FAMILY_AUTO_BUILD);
  }

  public static void waitForRefresh() {
    waitForJobFamily(ResourcesPlugin.FAMILY_AUTO_REFRESH);
    waitForJobFamily(ResourcesPlugin.FAMILY_MANUAL_REFRESH);
  }

  public static void waitForIndexesReady() {
    // dummy query for waiting until the indexes are ready
    SearchEngine engine = new SearchEngine();
    IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
    try {
      engine.searchAllTypeNames(
          null,
          SearchPattern.R_EXACT_MATCH,
          "!@$#!@".toCharArray(),
          SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
          IJavaSearchConstants.CLASS,
          scope,
          new TypeNameRequestor() {
            @Override
            public void acceptType(
                int modifiers,
                char[] packageName,
                char[] simpleTypeName,
                char[][] enclosingTypeNames,
                String path) {
            }
          },
          IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
          null);
    }
    catch (CoreException e) {
    }
  }

  public static void waitForManualRefresh() {
    waitForJobFamily(ResourcesPlugin.FAMILY_MANUAL_REFRESH);
  }

  /**
   * Waits until all jobs of the given family are finished. This method will block the
   * calling thread until all such jobs have finished executing, or until this thread is
   * interrupted. If there are no jobs in the family that are currently waiting, running,
   * or sleeping, this method returns immediately.
   */
  public static void waitForJobFamily(final Object family) {
    boolean wasInterrupted = false;
    do {
      try {
        Job.getJobManager().join(family, null);
        wasInterrupted = false;
      }
      catch (OperationCanceledException e) {
        //nop
      }
      catch (InterruptedException e) {
        wasInterrupted = true;
      }
    }
    while (wasInterrupted);
  }

  /**
   * checks whether all of the given plugins are installed in the current platform
   * 
   * @param pluginIds
   *          the plugin Ids to search
   * @return true if every plugin passed exists in the given platform, false otherwise.
   */
  public static boolean areAllPluginsInstalled(String... pluginIds) {
    for (String pluginId : pluginIds) {
      Bundle b = Platform.getBundle(pluginId);
      if (b == null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the newest (highest version) bundle with the given symbolic name that can be found in the active target
   * platform.
   * 
   * @param symbolicName
   *          the symbolic name of the bundles to check.
   * @return The newest bundle of all having the given symbolic name.
   */
  public static BundleDescription getNewestBundleInActiveTargetPlatform(String symbolicName) {
    State state = LazyPluginModel.getPdeState().getState();
    BundleDescription[] allBundleVersions = state.getBundles(symbolicName);
    Version v = null;
    BundleDescription newest = null;
    for (BundleDescription d : allBundleVersions) {
      if (d != null) {
        if (v == null || (d.getVersion().compareTo(v) > 0)) {
          v = d.getVersion();
          newest = d;
        }
      }
    }
    return newest;
  }

  public static void setWorkspaceAutoBuilding(boolean autoBuild) throws CoreException {
    IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
    if (description.isAutoBuilding() != autoBuild) {
      description.setAutoBuilding(autoBuild);
      ResourcesPlugin.getWorkspace().setDescription(description);
    }
  }

  public static boolean isWorkspaceAutoBuilding() throws CoreException {
    return ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
  }

  public static boolean isBatik17OrNewer() {
    BundleDescription batikUtil = JdtUtility.getNewestBundleInActiveTargetPlatform("org.apache.batik.util");
    return batikUtil != null &&
        ((batikUtil.getVersion().getMajor() == 1 && batikUtil.getVersion().getMinor() >= 7) || batikUtil.getVersion().getMajor() > 1);
  }
}
