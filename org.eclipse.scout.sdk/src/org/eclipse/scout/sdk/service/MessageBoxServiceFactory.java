/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.service;

import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Factory for creating an {@link IMessageBoxService}.
 * 
 * @since 3.10.0-M1
 */
public class MessageBoxServiceFactory {

  /**
   * Looks up the {@link IMessageBoxService} in the OSGi service registry and returns the one with the highest priority.
   * If there is no service registered, one is returned that does not show any information to the user and that returns
   * always the given default result, if available.
   * <p/>
   * <b>Note</b>: The OSGi service reference is released immediately. Hence do not store the service returned.
   * 
   * @return Returns the {@link IMessageBoxService}
   */
  public static IMessageBoxService getMessageBoxService() {
    BundleContext bundleContext = ScoutSdk.getDefault().getBundle().getBundleContext();
    ServiceReference<IMessageBoxService> serviceReference = null;
    try {
      serviceReference = bundleContext.getServiceReference(IMessageBoxService.class);
      if (serviceReference != null) {
        IMessageBoxService service = bundleContext.getService(serviceReference);
        if (service != null) {
          return service;
        }
      }
    }
    catch (Throwable t) {
      ScoutSdk.logError("Exception while acquiring " + IMessageBoxService.class.getSimpleName(), t);
    }
    finally {
      if (serviceReference != null) {
        bundleContext.ungetService(serviceReference);
      }
    }

    return new P_NullMessageBoxService();
  }

  private static class P_NullMessageBoxService implements IMessageBoxService {
    @Override
    public YesNo showYesNoQuestion(String title, String message, YesNo defaultAnswer) {
      return defaultAnswer;
    }

    @Override
    public void showWarning(String title, String message) {
    }
  }
}
