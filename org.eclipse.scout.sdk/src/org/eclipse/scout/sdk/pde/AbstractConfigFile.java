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
package org.eclipse.scout.sdk.pde;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.sdk.ScoutStatus;

/**
 * representation of a configuration file
 * when storing the file, a check is done whether the file was changed inbetween
 */
public abstract class AbstractConfigFile {
  private IFile m_file;
  private long m_modificationTime;

  public AbstractConfigFile(IProject p, String path) throws CoreException {
    this(p.getFile(path));
  }

  public AbstractConfigFile(IFile file) throws CoreException {
    m_file = file;
    if (m_file.exists()) {
      m_file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
      m_modificationTime = m_file.getModificationStamp();
    }
  }

  public IProject getProject() {
    return m_file.getProject();
  }

  public IFile getFile() {
    return m_file;
  }

  /**
   * @return true if file was stored, false if file was not stored
   */
  public abstract boolean store(IProgressMonitor p) throws CoreException;

  protected void loadXmlInternal(SimpleXmlElement root) throws CoreException {
    IFile f = getFile();
    if (f.exists()) {
      try {
        root.parseStream(f.getContents());
      }
      catch (Exception e) {
        throw new CoreException(new ScoutStatus(e));
      }
    }
  }

  protected String loadTextInternal(String encoding) throws CoreException {
    IFile f = getFile();
    if (f.exists()) {
      try {
        byte[] b = IOUtility.getContent(f.getContents());
        return new String(b, encoding);
      }
      catch (Exception e) {
        throw new CoreException(new ScoutStatus(e));
      }
    }
    return "";
  }

  /**
   * @return true if file was stored, false if file was not stored
   */
  protected boolean storeTextInternal(String content, IProgressMonitor p) throws CoreException {
    // if(!acceptStoreInternal()){
    // return false;
    // }
    try {
      if (content == null) content = "";
      ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
      if (getFile().exists()) {
        getFile().setContents(in, true, true, p);
      }
      else {
        getFile().create(in, true, p);
      }
      return true;
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  /**
   * @return true if file was stored, false if file was not stored
   */
  protected boolean storeXmlInternal(SimpleXmlElement root, IProgressMonitor p) throws CoreException {
    // if(!acceptStoreInternal()){
    // return false;
    // }
    try {
      StringWriter w = new StringWriter();
      root.writeDocument(w, null, "UTF-8");
      w.close();
      ByteArrayInputStream in = new ByteArrayInputStream(w.toString().getBytes("UTF-8"));
      if (getFile().exists()) {
        getFile().setContents(in, true, true, p);
      }
      else {
        getFile().create(in, true, p);
      }
      return true;
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  // protected boolean acceptStoreInternal(){
  // long oldModificationTime=m_modificationTime;
  // long newModificationTime=0;
  // IFile f=getFile();
  // if(f.exists()){
  // newModificationTime=f.getModificationStamp();
  // }
  // if(oldModificationTime!=newModificationTime){
  // MessageBox mbox=new MessageBox(SDE.getShell(),SWT.YES | SWT.NO);
  // mbox.setText("Local file not sync with filesystem");
  // StringBuilder b=new StringBuilder();
  // b.append(m_file.getName()+" is not sync with filesystem\n");
  // b.append("\n");
  // b.append("Please confirm to overwrite.");
  // mbox.setMessage(b.toString());
  // int response=mbox.open();
  // if(response!=SWT.YES){
  // return false;
  // }
  // }
  // return true;
  // }

}
