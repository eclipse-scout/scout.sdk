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
package org.eclipse.scout.sdk.ui.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.jdt.compile.ScoutSeverityManager;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * <h3>{@link UiUtility}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 07.03.2011
 */
@SuppressWarnings("restriction")
public final class UiUtility {

  private UiUtility() {
  }

  /**
   * Shows the given java element in the java editor.
   *
   * @param e
   *          The element to focus
   * @param createNew
   *          indicates if a new editor should be opened if not open yet. If false, the element will only be shown if
   *          there is a java editor open yet.
   */
  public static void showJavaElementInEditor(IJavaElement e, boolean createNew) {
    try {
      IEditorPart editor = null;
      if (createNew) {
        editor = JavaUI.openInEditor(e);
      }
      else {
        editor = EditorUtility.isOpenInEditor(e);
        if (editor != null) {
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(editor);
        }
      }
      if (editor != null) {
        JavaUI.revealInEditor(editor, e);
        if (editor instanceof ITextEditor) {
          ITextEditor textEditor = (ITextEditor) editor;
          IRegion reg = textEditor.getHighlightRange();
          if (reg != null) {
            textEditor.setHighlightRange(reg.getOffset(), reg.getLength(), true);
          }
        }
      }
    }
    catch (Exception ex) {
      ScoutSdkUi.logWarning(ex);
    }
  }

  /**
   * Returns the extent of the given string. No tab
   * expansion or carriage return processing will be performed.
   * <p>
   * The <em>extent</em> of a string is the width and height of the rectangular area it would cover if drawn in the
   * given font.
   * </p>
   *
   * @param text
   * @param f
   * @param context
   * @return a point containing the extent of the string
   */
  public static Point getTextBounds(String text, Font f, Drawable context) {
    GC gc = new GC(context);
    gc.setFont(f);
    return gc.stringExtent(text);
  }

  /**
   * Gets the shortened text based on the given input so that the text returned is smaller than the given bounds.
   *
   * @param origText
   *          The original text that should be shortened to match into bounds.
   * @param f
   *          The {@link Font} to use to calculate the text length.
   * @param context
   *          The context in which the calculation should be performed.
   * @param bounds
   *          The number of pixels. The returned text will be shorter than this limit if rendered in the given context
   *          with the given font.
   * @return The text shortened to given bounds including an ellipsis if it has been cut.
   */
  public static String getTextForBounds(String origText, Font f, Drawable context, int bounds) {
    return getTextForBounds(origText, f, context, bounds, "…");
  }

  /**
   * Gets the shortened text based on the given input so that the text returned is smaller than the given bounds.
   *
   * @param origText
   *          The original text that should be shortened to match into bounds.
   * @param f
   *          The {@link Font} to use to calculate the text length.
   * @param context
   *          The context in which the calculation should be performed.
   * @param bounds
   *          The number of pixels. The returned text will be shorter than this limit if rendered in the given context
   *          with the given font.
   * @param ellipsis
   *          The suffix to add if the given origText has been shortened or null if no suffix should be added.
   * @return The text shortened to given bounds including the given ellipsis if it has been cut.
   */
  public static String getTextForBounds(String origText, Font f, Drawable context, int bounds, String ellipsis) {
    if (bounds <= 0) {
      return origText;
    }

    String cleanedEllipsis = null;
    if (StringUtility.hasText(ellipsis)) {
      cleanedEllipsis = ellipsis;
    }
    else {
      cleanedEllipsis = "";
    }
    StringBuilder sb = new StringBuilder(origText);
    GC gc = new GC(context);
    gc.setFont(f);

    Point p = gc.stringExtent(sb.toString());
    while (p.x > bounds && sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
      p = gc.stringExtent(sb.toString() + cleanedEllipsis);
    }

    String ret = sb.toString();
    if (ret.equals(origText)) {
      return origText;
    }
    else {
      return sb.toString() + cleanedEllipsis;
    }
  }

  /**
   * Gets the packages suffix of the given selection.<br>
   * This method calculates the java package that surrounds the current selection and returns the suffix (the package
   * part after the symbolic name of the containing bundle).
   *
   * @param selection
   *          The selection to evaluate
   * @return A string with the package suffix or null.
   */
  public static String getPackageSuffix(IStructuredSelection selection) {
    if (selection == null) {
      return null;
    }
    return getPackageSuffix(UiUtility.adapt(selection.getFirstElement(), IJavaElement.class));
  }

  /**
   * Gets the packages suffix of the given element.<br>
   * This method calculates the java package that surrounds the given element and returns the suffix (the package
   * part after the symbolic name of the containing bundle).
   *
   * @param element
   *          The element
   * @return A string with the package suffix or null.
   */
  public static String getPackageSuffix(IJavaElement element) {
    if (TypeUtility.exists(element)) {
      IPackageFragment targetPackage = (IPackageFragment) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
      if (TypeUtility.exists(targetPackage)) {
        String pck = targetPackage.getElementName();
        IScoutBundle declaringBundle = ScoutTypeUtility.getScoutBundle(targetPackage);
        if (declaringBundle != null && pck.startsWith(declaringBundle.getSymbolicName()) && pck.length() > declaringBundle.getSymbolicName().length()) {
          return pck.substring(declaringBundle.getSymbolicName().length() + 1);
        }
      }
    }
    return null;
  }

  /**
   * Closes all open instances of the given editor.<br>
   * If the editors are dirty, the user is asked if they should be saved.
   *
   * @param editorId
   *          The editor id filter or null if all editors should be closed.
   * @param files
   *          The files filter or null. Only the editors of those files are closed.
   * @return true if all editors have been closed. false otherwise (e.g. when the user presses cancel in the save dirty
   *         editor confirmation message box).
   */
  public static boolean closeEditors(String editorId, Set<IFile> files) {
    for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
      for (IWorkbenchPage p : w.getPages()) {
        IEditorReference[] editorReferences = p.getEditorReferences();
        List<IEditorReference> partsToSave = new ArrayList<IEditorReference>(editorReferences.length);
        for (IEditorReference r : editorReferences) {
          if (editorId == null || editorId.equals(r.getId())) {
            if (files == null || files.isEmpty() || isEditorInputInFiles(r, files)) {
              partsToSave.add(r);
            }
          }
        }
        if (!partsToSave.isEmpty()) {
          boolean success = p.closeEditors(partsToSave.toArray(new IEditorReference[partsToSave.size()]), true);
          if (!success) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static boolean isEditorInputInFiles(IEditorReference ref, Set<IFile> files) {
    try {
      IEditorInput editorInput = ref.getEditorInput();
      if (editorInput instanceof IURIEditorInput) {
        IURIEditorInput input = (IURIEditorInput) editorInput;
        URI uriToSearch = input.getURI();
        for (IFile f : files) {
          if (URIUtil.sameURI(uriToSearch, f.getLocationURI())) {
            return true;
          }
        }
      }
    }
    catch (PartInitException e) {
      ScoutSdkUi.logError("Unable to get file input of editor.", e);
    }
    return false;
  }

  /**
   * Tries to calculate the most specific scout bundle that contains the given selection.
   *
   * @param selection
   * @return
   */
  public static IScoutBundle getScoutBundleFromSelection(IStructuredSelection selection) {
    return getScoutBundleFromSelection(selection, null);
  }

  /**
   * Tries to calculate the most specific scout bundle that contains the given selection.
   * The given filter is taken into account so that clients can influence which types of scout bundles around the
   * selected one that should be used.<br>
   *
   * @param selection
   * @param filter
   * @return
   */
  public static IScoutBundle getScoutBundleFromSelection(IStructuredSelection selection, IScoutBundleFilter filter) {
    if (selection != null) {
      IScoutBundle b = UiUtility.adapt(selection.getFirstElement(), IScoutBundle.class);
      if (b != null) {
        if (filter == null || filter.accept(b)) {
          return b;
        }
        else {
          IScoutBundle parentShared = b.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), b, true);
          if (parentShared != null) {
            IScoutBundle candidate = parentShared.getChildBundle(ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), filter), b, true);
            if (candidate != null) {
              return candidate;
            }
          }
          else {
            IScoutBundle candidate = b.getChildBundle(ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), filter), b, false);
            if (candidate != null) {
              return candidate;
            }
          }
        }
      }
    }

    // nothing in the selection. just choose one
    Set<IScoutBundle> bundles = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getFilteredRootBundlesFilter(ScoutBundleFilters.getWorkspaceBundlesFilter()));
    for (IScoutBundle root : bundles) {
      IScoutBundle candidate = root.getChildBundle(filter, true);
      if (candidate != null) {
        return candidate;
      }
    }

    return null;
  }

  public static IType getTypeFromSelection(IStructuredSelection selection) {
    return getTypeFromSelection(selection, null);
  }

  public static IType getTypeFromSelection(IStructuredSelection selection, ITypeFilter filter) {
    return getTypeFromSelection(selection.getFirstElement(), filter);
  }

  private static IType getTypeFromSelection(Object o, ITypeFilter filter) {
    IType t = UiUtility.adapt(o, IType.class);
    if (TypeUtility.exists(t) && (filter == null || filter.accept(t))) {
      return t;
    }
    return null;
  }

  public static Set<IType> getTypesFromSelection(IStructuredSelection selection, ITypeFilter filter) {
    Set<IType> result = new LinkedHashSet<IType>(selection.size());
    if (selection.isEmpty()) {
      return result;
    }

    Iterator<?> it = selection.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      IType t = getTypeFromSelection(o, filter);
      if (t != null) {
        result.add(t);
      }
    }

    return result;
  }

  /**
   * Tries to convert the given element into the given class.
   * This method respects scout IPage classes.
   *
   * @param element
   *          The element to convert.
   * @param targetClass
   *          The class in which it should be converted.
   * @return the converted element or null if the conversion could not be done.
   */
  @SuppressWarnings("unchecked")
  public static <T> T adapt(Object element, Class<T> targetClass) {
    if (element == null || targetClass == null) {
      return null;
    }
    if (targetClass.isInstance(element)) {
      return (T) element;
    }

    // get element out of the pages
    if (element instanceof IPage) {
      IPage page = (IPage) element;
      IScoutBundle sb = page.getScoutBundle();
      if (sb != null) {
        element = sb;
      }

      do {
        if (page instanceof ITypePage) {
          ITypePage astp = (ITypePage) page;
          IType t = astp.getType();
          if (TypeUtility.exists(t)) {
            element = t;
            break;
          }
        }
        page = page.getParent();
      }
      while (page != null);
    }

    // try to adapt
    if (element instanceof IAdaptable) {
      IAdaptable ad = (IAdaptable) element;
      T result = (T) ad.getAdapter(targetClass);
      if (result == null) {
        // try to get a resource
        IResource r = (IResource) ad.getAdapter(IResource.class);
        if (!ResourceUtility.exists(r)) {
          IJavaElement je = (IJavaElement) ad.getAdapter(IJavaElement.class);
          if (TypeUtility.exists(je)) {
            r = je.getResource();
            if (!ResourceUtility.exists(r) && targetClass.isAssignableFrom(IScoutBundle.class)) {
              // binary java element
              result = (T) ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(je);
            }
          }
        }

        if (result == null && ResourceUtility.exists(r)) {
          // try to convert from a resource
          result = (T) r.getAdapter(targetClass);
        }
      }
      return result;
    }
    return null;
  }

  /**
   * Gets an image descriptor for classes.<br>
   * The image descriptor includes flags like abstract, final, static, deprecated. Furthermore it shows different icons
   * for interfaces and classes and includes the severity flags of the classes (e.g. error or warning).
   *
   * @param type
   *          The type for which the icon should be returned.
   * @return The image descriptor.
   */
  public static ImageDescriptor getTypeImageDescriptor(IType type) {
    return getTypeImageDescriptor(type, true);
  }

  public static ImageDescriptor getTypeImageDescriptor(IType type, boolean showSeverity) {
    int flags = -1;
    try {
      flags = type.getFlags();
    }
    catch (JavaModelException e) {
    }

    boolean isInterface = Flags.isInterface(flags);
    boolean isInner = TypeUtility.exists(type.getDeclaringType());
    int severity = 0;
    if (showSeverity) {
      ScoutSeverityManager.getInstance().getSeverityOf(type);
    }
    ImageDescriptor desc = JavaElementImageProvider.getTypeImageDescriptor(isInner, isInterface, flags, false);

    int adornmentFlags = 0;
    if (Flags.isFinal(flags)) {
      adornmentFlags |= JavaElementImageDescriptor.FINAL;
    }
    if (Flags.isAbstract(flags) && !isInterface) {
      adornmentFlags |= JavaElementImageDescriptor.ABSTRACT;
    }
    if (Flags.isStatic(flags)) {
      adornmentFlags |= JavaElementImageDescriptor.STATIC;
    }
    if (Flags.isDeprecated(flags)) {
      adornmentFlags |= JavaElementImageDescriptor.DEPRECATED;
    }

    if (severity == IMarker.SEVERITY_ERROR) {
      adornmentFlags |= JavaElementImageDescriptor.ERROR;
    }
    else if (severity == IMarker.SEVERITY_WARNING) {
      adornmentFlags |= JavaElementImageDescriptor.WARNING;
    }

    return new JavaElementImageDescriptor(desc, adornmentFlags, JavaElementImageProvider.BIG_SIZE);
  }
}
