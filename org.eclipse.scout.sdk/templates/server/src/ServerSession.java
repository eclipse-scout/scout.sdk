package @@BUNDLE_SERVER_NAME@@;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.AbstractServerSession;

public class ServerSession extends AbstractServerSession{
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerSession.class);

  public ServerSession(){
    super(true);
  }

  /**
   * @return session in current ThreadContext
   */
  public static ServerSession get(){
    return ServerJob.getCurrentSession(ServerSession.class);
  }

  @Override
  protected void execLoadSession() throws ProcessingException{
    LOG.info("created a new session for " + getUserId());
  }
}