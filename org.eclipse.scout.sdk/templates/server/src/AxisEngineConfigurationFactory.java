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
import java.net.URL;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.configuration.FileProvider;
import org.eclipse.core.runtime.Status;

public class AxisEngineConfigurationFactory implements EngineConfigurationFactory{

  public static EngineConfigurationFactory newFactory(Object someContext){
    return new AxisEngineConfigurationFactory();
  }

  public EngineConfiguration getClientEngineConfig(){
    return getEngineConfigInternal("client-config.wsdd");
  }

  public EngineConfiguration getServerEngineConfig(){
    return getEngineConfigInternal("server-config.wsdd");
  }

  private EngineConfiguration getEngineConfigInternal(String file){
    URL url=Activator.getDefault().getBundle().getResource(file);
    if(url!=null){
      try{
        return new FileProvider(url.openStream());
      }
      catch(IOException e){
        Activator.getDefault().getLog().log(new Status(Status.ERROR,Activator.PLUGIN_ID,"cannot read "+file));
      }
    }
    else{
      Activator.getDefault().getLog().log(new Status(Status.ERROR,Activator.PLUGIN_ID,"missing "+file));
    }
    return null;
  }

}
