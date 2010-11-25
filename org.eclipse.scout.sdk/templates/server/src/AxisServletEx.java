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
package @@BUNDLE_SERVER_NAME@@;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.axis.transport.http.AxisServlet;
import org.eclipse.scout.http.servletfilter.ServletFilterRegistry;


/**
 * Inside Equinox use this class instead of {@link AxisServlet} in order to support
 * servlet filters inside Equinox Serverside.
* See extension point org.eclipse.scout.http.servletfilter.filters in plugin org.eclipse.scout.http.servletfilter
 */
public class AxisServletEx extends AxisServlet{
  private static final long serialVersionUID = 1L;

  @Override
  public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException{
    ServletFilterRegistry.getInstance().delegateService(req,res,new ServletFilterRegistry.ISuperCallRunnable(){
      public ServletContext getServletContext(){
        return AxisServletEx.this.getServletContext();
      }
      public void run(ServletRequest reqDlg, ServletResponse resDlg) throws ServletException, IOException{
        AxisServletEx.super.service(reqDlg,resDlg);
      }
    });
  }

}
