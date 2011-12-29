/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genady Beryozkin <eclipse@genady.org> - [hovering] tooltip for constant string does not show constant value - https://bugs.eclipse.org/bugs/show_bug.cgi?id=85382
 *     BSI Business Systems Integration AG - adapted to Scout SDK
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.fields.tooltip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.javadoc.JavaDocLocations;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Renders JavaDoc of a given {@link IMember}.
 * <p>
 * This class is based on {@link JavadocHover}.
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 17.09.2010
 */
@SuppressWarnings("restriction")
public class JavadocTooltip extends AbstractTooltip {

  private IMember m_member;
  private Browser m_browser;

  private String m_javaDoc;

  public JavadocTooltip(Control sourceControl) {
    this(sourceControl, null);
  }

  public JavadocTooltip(Control sourceControl, IMember element) {
    super(sourceControl);
    m_member = element;
  }

  public void setMember(IMember member) {
    m_member = member;
    // TODO aho (uninstall tooltip if null is given)
    if (TypeUtility.exists(m_member)) {
      computJavadoc();
    }
  }

  @Override
  protected void createContent(Composite parent) {
    m_browser = new Browser(parent, SWT.NONE);
    m_browser.setJavascriptEnabled(true);
    m_browser.setText(m_javaDoc);
    GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
    layoutData.heightHint = 150;
    layoutData.widthHint = 500;
    m_browser.setLayoutData(layoutData);
    m_browser.addLocationListener(new LocationAdapter() {
      @Override
      public void changing(LocationEvent event) {
        // TODO allow navigating
        event.doit = false;
      }
    });
  }

  @Override
  protected void show(int x, int y) {
    if (!StringUtility.isNullOrEmpty(m_javaDoc)) {
      super.show(x, y);
    }
  }

  private void computJavadoc() {
    Job j = new Job("calculating java doc of '" + m_member.getElementName() + "'") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        Reader contentReader = null;
        try {
          if (TypeUtility.exists(m_member)) {
            contentReader = JavadocContentAccess.getHTMLContentReader(m_member, true, true);
            if (contentReader != null) {
              m_javaDoc = getJavadocHtml(new IJavaElement[]{m_member});
              if (getSourceControl() != null && !getSourceControl().isDisposed()) {
                getSourceControl().getDisplay().asyncExec(new Runnable() {

                  @Override
                  public void run() {
                    if (m_browser != null && !m_browser.isDisposed()) {
                      m_browser.setText(m_javaDoc);
                    }
                  }
                });
              }
            }
          }
        }
        catch (Exception e) {
          ScoutSdkUi.logWarning(e);
        }
        finally {
          if (contentReader != null) {
            try {
              contentReader.close();
            }
            catch (Exception e) {
            }
          }
        }
        return Status.OK_STATUS;
      }
    };
    j.setPriority(Job.DECORATE);
    j.setSystem(true);
    j.setUser(false);
    j.schedule();
  }

  /**
   * Returns the Javadoc in HTML format.
   * 
   * @param result
   *          the Java elements for which to get the Javadoc
   * @param activePart
   *          the active part if any
   * @param selection
   *          the selection of the active site if any
   * @param monitor
   *          a monitor to report progress to
   * @return a string with the Javadoc in HTML format.
   */
  private String getJavadocHtml(IJavaElement[] result) {
    StringBuffer buffer = new StringBuffer();
    int nResults = result.length;

    if (nResults == 0) return null;

    String base = null;
    if (nResults > 1) {

      for (int i = 0; i < result.length; i++) {
        HTMLPrinter.startBulletList(buffer);
        IJavaElement curr = result[i];
        if (curr instanceof IMember || curr.getElementType() == IJavaElement.LOCAL_VARIABLE) HTMLPrinter.addBullet(buffer, getInfoText(curr, null, false));
        HTMLPrinter.endBulletList(buffer);
      }

    }
    else {

      IJavaElement curr = result[0];
      if (curr instanceof IMember) {
        final IMember member = (IMember) curr;

        String constantValue = null;

        HTMLPrinter.addSmallHeader(buffer, getInfoText(member, constantValue, true));
        Reader reader = null;
        try {
          String content = JavadocContentAccess2.getHTMLContent(member, true);
          reader = content == null ? null : new StringReader(content);

          // Provide hint why there's no Javadoc
          if (reader == null && member.isBinary()) {
            boolean hasAttachedJavadoc = JavaDocLocations.getJavadocBaseLocation(member) != null;
            IPackageFragmentRoot root = (IPackageFragmentRoot) member.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
            boolean hasAttachedSource = root != null && root.getSourceAttachmentPath() != null;
            IOpenable openable = member.getOpenable();
            boolean hasSource = openable.getBuffer() != null;

            if (!hasAttachedSource && !hasAttachedJavadoc) reader = new StringReader("");
            else if (!hasAttachedJavadoc && !hasSource) reader = new StringReader("");
            else if (!hasAttachedSource) reader = new StringReader("");
            else if (!hasSource) reader = new StringReader("");

          }
          else {
            base = JavaDocLocations.getBaseURL(member);
          }

        }
        catch (JavaModelException ex) {
//       reader = new StringReader(InfoViewMessages.JavadocView_error_gettingJavadoc);
          JavaPlugin.log(ex.getStatus());
        }
        if (reader != null) {
          HTMLPrinter.addParagraph(buffer, reader);
        }
      }
      else if (curr.getElementType() == IJavaElement.LOCAL_VARIABLE || curr.getElementType() == IJavaElement.TYPE_PARAMETER) {
        HTMLPrinter.addSmallHeader(buffer, getInfoText(curr, null, true));
      }
    }

    boolean flushContent = true;
    if (buffer.length() > 0 || flushContent) {
      HTMLPrinter.insertPageProlog(buffer, 0, null, null, loadStyleSheet());
      if (base != null) {
        int endHeadIdx = buffer.indexOf("</head>"); //$NON-NLS-1$
        buffer.insert(endHeadIdx, "\n<base href='" + base + "'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      HTMLPrinter.addPageEpilog(buffer);
      return buffer.toString();
    }

    return null;
  }

  /**
   * Gets the label for the given member.
   * 
   * @param member
   *          the Java member
   * @param constantValue
   *          the constant value if any
   * @param allowImage
   *          true if the java element image should be shown
   * @return a string containing the member's label
   */
  private String getInfoText(IJavaElement member, String constantValue, boolean allowImage) {
    StringBuffer label = new StringBuffer(JavaElementLinks.getElementLabel(member, LABEL_FLAGS));
    if (member.getElementType() == IJavaElement.FIELD && constantValue != null) {
      label.append(constantValue);
    }

    String imageName = null;
    try {
      if (allowImage) {
        URL imageUrl = JavaPlugin.getDefault().getImagesOnFSRegistry().getImageURL(member);
        if (imageUrl != null) {
          imageName = imageUrl.toExternalForm();
        }
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("could not load image for '" + member.getElementName() + "'.");
    }
    return addImageAndLabel(member, imageName, label.toString());
  }

  private String addImageAndLabel(IJavaElement member, String imageName, String label) {
    // workaround to make internal 3.X code accessible with API changes.
    StringBuffer buffer = new StringBuffer();
    Version frameworkVersion = new Version(ScoutSdkUi.getDefault().getBundle().getBundleContext().getProperty("osgi.framework.version"));
    if (frameworkVersion.getMajor() == 3) {
      if (frameworkVersion.getMinor() <= 5) {
        try {
          Method method = JavadocHover.class.getMethod("addImageAndLabel", StringBuffer.class, String.class, int.class, int.class, int.class, int.class, String.class, int.class, int.class);
          method.invoke(null, buffer, imageName, 16, 16, 8, 5, label, 22, 0);
        }
        catch (Exception e) {
          ScoutSdkUi.logError("could not access method 'addImageAndLabel' on 'JavaHoover'.", e);
        }
      }
      else if (frameworkVersion.getMinor() <= 6) {
        try {
          Method method = JavadocHover.class.getMethod("addImageAndLabel", StringBuffer.class, String.class, int.class, int.class, String.class, int.class, int.class);
          method.invoke(null, buffer, imageName, 16, 16, label, 22, 0);
        }
        catch (Exception e) {
          ScoutSdkUi.logError("could not access method 'addImageAndLabel' on 'JavaHoover'.", e);
        }
      }
      else {
        try {
          Method method = JavadocHover.class.getMethod("addImageAndLabel", StringBuffer.class, IJavaElement.class, String.class, int.class, int.class, String.class, int.class, int.class);
          method.invoke(null, buffer, member, imageName, 16, 16, label, 22, 0);
        }
        catch (Exception e) {
          ScoutSdkUi.logError("could not access method 'addImageAndLabel' on 'JavaHoover'.", e);
        }
      }
    }

    return buffer.toString();
  }

  private static String loadStyleSheet() {
    Bundle bundle = Platform.getBundle(JavaPlugin.getPluginId());
    URL styleSheetURL = bundle.getEntry("/JavadocViewStyleSheet.css"); //$NON-NLS-1$
    if (styleSheetURL == null) return null;

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(styleSheetURL.openStream()));
      StringBuffer buffer = new StringBuffer(1500);
      String line = reader.readLine();
      while (line != null) {
        buffer.append(line);
        buffer.append('\n');
        line = reader.readLine();
      }

      FontData fontData = JFaceResources.getFontRegistry().getFontData(PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
      return HTMLPrinter.convertTopLevelFont(buffer.toString(), fontData);
    }
    catch (IOException ex) {
      JavaPlugin.log(ex);
      return null;
    }
    finally {
      try {
        if (reader != null) reader.close();
      }
      catch (IOException e) {
      }
    }
  }

  /** Flags used to render a label in the text widget. */
  private static final long LABEL_FLAGS = JavaElementLabels.ALL_FULLY_QUALIFIED
      | JavaElementLabels.M_PRE_RETURNTYPE | JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES | JavaElementLabels.M_EXCEPTIONS
      | JavaElementLabels.F_PRE_TYPE_SIGNATURE | JavaElementLabels.T_TYPE_PARAMETERS;

}
