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
package org.eclipse.scout.sdk.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.pde.PdeUtility;
import org.eclipse.scout.sdk.pde.ProductXml;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * Create a client development product file and config.ini (for localhost name) based on a production product /
 * config.ini
 */
public class CreateClientDevelopmentProductOperation implements IOperation {
  private IFile m_templateProductFile;
  private IFile m_devProduct;
  private final IScoutBundle m_clientBundle;

  /**
   * @param configIni
   *          when null is passed, a config.ini is searched in the same folder as the product file
   */
  public CreateClientDevelopmentProductOperation(IScoutBundle clientBundle, IFile templateProductFile, IFile devProduct) {
    m_clientBundle = clientBundle;
    m_templateProductFile = templateProductFile;
    m_devProduct = devProduct;
  }

  public String getOperationName() {
    return "Create personalized product '" + m_devProduct.getParent().getName() + "' based on '" + m_templateProductFile.getParent().getName() + "'";
  }

  @Override
  public void validate() throws IllegalArgumentException {

  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    if (!m_templateProductFile.exists()) return;
    IFile productionConfigIni = m_templateProductFile.getParent().getFile(new Path("config.ini"));
    IFile devConfigIni = m_devProduct.getParent().getFile(new Path("config.ini"));
    try {
      PdeUtility.createFile(m_devProduct, m_templateProductFile.getContents(), monitor);
      if (productionConfigIni.exists()) {
        PdeUtility.createFile(devConfigIni, productionConfigIni.getContents(), monitor);
      }
      //
      ProductXml xml = new ProductXml(m_devProduct);
      SimpleXmlElement configIniXml = xml.getConfigIniXml(false);
      if (configIniXml != null) {
        SimpleXmlElement win32Xml = configIniXml.getChild("win32");
        if (win32Xml != null) {
          String s = win32Xml.getContent();
          if (s != null) {
            win32Xml.setContent(s.replace("/products/development", "/products/" + m_devProduct.getParent().getName() + "/"));
          }
        }
      }
      xml.store(monitor);
      //
      if (devConfigIni.exists()) {
        ArrayList<String> configIniContent = new ArrayList<String>(Arrays.asList(IOUtility.getContent(new InputStreamReader(devConfigIni.getContents(), devConfigIni.getCharset(true))).split("[\\n]")));
        ArrayList<String> newContent = new ArrayList<String>();
        // add lines at beginning
        newContent.add("#Development Settings");
        newContent.add("osgi.console=");
        newContent.add("eclipse.consoleLog=true");
        newContent.add("");
        for (String s : configIniContent) {
          String[] a = s.split("[=]", 2);
          if (!s.startsWith("#") && a.length == 2) {
            String key = a[0].trim();
            // default add
            if (1 == 0) {
              // ...
            }
            else {
              newContent.add(s);
            }
          }
          else {
            newContent.add(s);
          }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(bos, devConfigIni.getCharset(true)));
        for (String s : newContent) {
          out.println(s);
        }
        out.close();
        devConfigIni.setContents(new ByteArrayInputStream(bos.toByteArray()), true, false, monitor);
      }
    }
    catch (CoreException e) {
      throw e;
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

}
