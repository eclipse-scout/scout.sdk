/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.fields.javadoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.javadoc.JavaDocLocations;
import org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link JavaDocBrowser}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class JavaDocBrowser {

  private JavaDocBrowser() {
  }

  public static String getJavaDoc(IJavaElement element) {
    if (!S2eUtils.exists(element)) {
      return null;
    }

    return getJavaDocHtml(element);
  }

  public static Browser create(Composite parent, String javaDocHtml) {

    if (!BrowserInformationControl.isAvailable(parent)) {
      return null;
    }

    Browser browser = new Browser(parent, SWT.NONE);
    browser.setJavascriptEnabled(false);
    browser.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    browser.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    browser.addOpenWindowListener(new OpenWindowListener() {
      @Override
      public void open(WindowEvent event) {
        event.required = true; // Cancel opening of new windows
      }
    });
    browser.addLocationListener(new LocationListener() {
      @Override
      public void changing(LocationEvent event) {
        event.doit = "about:blank".equals(event.location);// prevent loading of links
      }

      @Override
      public void changed(LocationEvent event) {
        // nop
      }
    });
    browser.setText(javaDocHtml);

    return browser;
  }

  @SuppressWarnings("squid:S1149") // StringBuffer required by API
  static String getJavaDocHtml(IJavaElement element) {
    try {
      String javaDoc = JavadocContentAccess2.getHTMLContent(element, true);
      if (StringUtils.isBlank(javaDoc)) {
        return null;
      }

      StringBuffer buffer = new StringBuffer();

      // header
      HTMLPrinter.insertPageProlog(buffer, 0, getCssStyles());

      // content
      buffer.append(javaDoc);

      // base URL
      insertBaseUrl(javaDoc, element, buffer);

      // footer
      HTMLPrinter.addPageEpilog(buffer);

      return buffer.toString();
    }
    catch (CoreException e) {
      SdkLog.info("Unable to get javadoc for element '{}'.", element.getElementName(), e);
    }
    return null;
  }

  @SuppressWarnings("squid:S1149") // StringBuffer required by API
  static void insertBaseUrl(String javaDoc, IJavaElement element, StringBuffer buffer) {
    try {
      String base = JavadocContentAccess2.extractBaseURL(javaDoc);
      if (base == null) {
        boolean isBinary = element instanceof IMember && ((IMember) element).isBinary();
        base = JavaDocLocations.getBaseURL(element, isBinary);
      }
      if (base != null) {
        int endHeadIdx = buffer.indexOf("</head>");
        buffer.insert(endHeadIdx, "\n<base href='" + base + "'>\n");
      }
    }
    catch (JavaModelException e) {
      SdkLog.info("Unable to get base URL of javadoc for element '{}'.", element.getElementName(), e);
    }
  }

  static String getCssStyles() {
    Bundle bundle = Platform.getBundle(JavaUI.ID_PLUGIN);
    if (bundle == null) {
      return null;
    }
    URL url = bundle.getEntry("/JavadocHoverStyleSheet.css");
    if (url == null) {
      return null;
    }

    String style = null;
    try {
      url = FileLocator.toFileURL(url);
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
        StringBuilder builder = new StringBuilder(256);
        String line = reader.readLine();
        while (line != null) {
          builder.append(line);
          builder.append('\n');
          line = reader.readLine();
        }
        style = builder.toString();
      }
    }
    catch (IOException ex) {
      SdkLog.info("Unable to read CSS Style for JavaDoc", ex);
    }

    if (style != null) {
      FontData fontData = JFaceResources.getFontRegistry().getFontData(PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
      style = HTMLPrinter.convertTopLevelFont(style, fontData);
    }
    return style;
  }
}
