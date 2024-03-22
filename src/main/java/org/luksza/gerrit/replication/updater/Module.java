package org.luksza.gerrit.replication.updater;

import com.google.common.flogger.FluentLogger;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.extensions.registration.DynamicItem;
import com.google.gerrit.lifecycle.LifecycleModule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.replication.ReplicationRemotesUpdater;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.lib.Config;

public class Module extends AbstractModule {

  @Override
  protected void configure() {

    install(
        new LifecycleModule() {
          @Override
          protected void configure() {
            listener().to(DelayedReplicationConfigUpdate.class);
          }
        });
  }

  static class DelayedReplicationConfigUpdate implements LifecycleListener {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final DynamicItem<ReplicationRemotesUpdater> replicationRemotesUpdater;

    @Inject
    DelayedReplicationConfigUpdate(
        DynamicItem<ReplicationRemotesUpdater> replicationRemotesUpdater) {
      this.replicationRemotesUpdater = replicationRemotesUpdater;
    }

    @Override
    public void start() {
      new Thread(this::updateRemotesConfiguration).start();
    }

    @Override
    public void stop() {
      // no-op
    }

    private void updateRemotesConfiguration() {
      int timeout = 10;
      logger.atInfo().log("will update replication remotes configuration in %ds", timeout);
      Config updates = new Config();
      updates.setString("remote", "example", "url", "ssh://localhost:29418/example.git");

      try {
        TimeUnit.SECONDS.sleep(timeout);
        replicationRemotesUpdater.get().update(updates);
        logger.atInfo().log("replication remote configuration updated");
      } catch (IOException | InterruptedException e) {
        logger.atSevere().withCause(e).log("could not update replication remotes");
      }
    }
  }
}
