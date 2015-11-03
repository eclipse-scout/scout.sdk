#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.server;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;

/**
 * <h3>{@link ServerSession}</h3>
 *
 * @author ${userName}
 */
public class ServerSession extends AbstractServerSession {

  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerSession.class);

  public ServerSession() {
    super(true);
  }

  /**
   * @return The {@link ServerSession} which is associated with the current thread, or <code>null</code> if not found.
   */
  public static ServerSession get() {
    return ServerSessionProvider.currentSession(ServerSession.class);
  }

  @Override
  protected void execLoadSession() {
    LOG.info("created a new session for " + getUserId());
  }
}
