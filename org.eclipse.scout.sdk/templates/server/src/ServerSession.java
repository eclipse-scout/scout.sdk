package @@BUNDLE_SERVER_NAME@@;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.AbstractServerSession;

public class ServerSession extends AbstractServerSession{
  private static IScoutLogger logger=ScoutLogManager.getLogger(ServerSession.class);

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
    logger.info("created a new session for "+getUserId());
  }
}
