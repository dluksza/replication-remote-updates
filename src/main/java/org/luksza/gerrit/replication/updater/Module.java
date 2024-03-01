package org.luksza.gerrit.replication.updater;

import com.google.common.flogger.FluentLogger;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.lifecycle.LifecycleModule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.replication.ReplicationConfigModule;
import com.googlesource.gerrit.plugins.replication.ReplicationRemotesUpdater;
import org.eclipse.jgit.lib.Config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Module extends AbstractModule {
  private final ReplicationConfigModule configModule;

  @Inject
  Module(ReplicationConfigModule configModule) {
    this.configModule = configModule;
  }

  @Override
  protected void configure() {
    install(configModule);
    
    install(new LifecycleModule() {
      @Override
      protected void configure() {
        listener().to(DelayedReplicationConfigUpdate.class);
      }
    });
  }

  static class DelayedReplicationConfigUpdate implements LifecycleListener {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final ReplicationRemotesUpdater replicationRemotesUpdater;

    @Inject
    DelayedReplicationConfigUpdate(ReplicationRemotesUpdater replicationRemoteUpdater) {
      this.replicationRemotesUpdater = replicationRemoteUpdater;
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
        replicationRemotesUpdater.update(updates);
        logger.atInfo().log("replication remote configuration updated");
      } catch (IOException | InterruptedException e) {
        logger.atSevere().withCause(e).log("could not update replication remotes");
      }
    }
  }
}
