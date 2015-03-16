package sample.ui.swing;

import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.ui.swing.AbstractSwingApplication;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

public class SwingApplication extends AbstractSwingApplication {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(SwingApplication.class);

  @Override
  public Object start(final IApplicationContext context) throws Exception {
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(System.getProperty("user.name")));
    return Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
      @Override
      public Object run() throws Exception {
        return startSecure(context);
      }
    });
  }

  @Override
  protected ISwingEnvironment createSwingEnvironment() {
    return new SwingEnvironment();
  }

  private Object startSecure(IApplicationContext context) throws Exception {
    return super.start(context);
  }

  @Override
  protected IClientSession getClientSession() {
    try {
      return OBJ.one(ClientSessionProvider.class).provide(ClientJobInput.empty().userAgent(initUserAgent()));
    }
    catch (ProcessingException e) {
      LOG.error("unable to load session", e);
      return null;
    }
  }
}
