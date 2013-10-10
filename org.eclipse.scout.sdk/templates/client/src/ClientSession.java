package @@BUNDLE_CLIENT_NAME@@;

import @@BUNDLE_CLIENT_NAME@@.ui.desktop.Desktop;

import org.eclipse.scout.commons.UriUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.servicetunnel.http.ClientHttpServiceTunnel;
import org.eclipse.scout.rt.shared.services.common.code.CODES;

public class ClientSession extends AbstractClientSession{
  private static IScoutLogger logger = ScoutLogManager.getLogger(ClientSession.class);

  public ClientSession(){
    super(true);
  }

  /**
   * @return session in current ThreadContext
   */
  public static ClientSession get(){
    return ClientJob.getCurrentSession(ClientSession.class);
  }

  @Override
  public void execLoadSession() throws ProcessingException{
    setServiceTunnel(new ClientHttpServiceTunnel(this, UriUtility.toUrl(getBundle().getBundleContext().getProperty("server.url"))));

    //pre-load all known code types
    CODES.getAllCodeTypes(@@BUNDLE_SHARED_NAME@@.Activator.PLUGIN_ID);

    setDesktop(new Desktop());

    // turn client notification polling on
    // getServiceTunnel().setClientNotificationPollInterval(2000L);
  }

  @Override
  public void execStoreSession() throws ProcessingException{
  }
}
