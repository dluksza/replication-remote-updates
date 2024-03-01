## About

Simple Gerrit plugin that will exercise new `replication` plugin API to
update remote configurations programmatically.

It will just call `ReplicationRemotesUpdater.update(Config)` 10s after
it's loaded. This way changes in `$site_path/etc/replication.config` or
`$site_path/etc/replication/` (or other configuration overrides) can be
observed.

## Installataion

Clone the repository and create a symbolic link in
`$gerrit_workspace/plugins`, then build with:

```
$ bazel build plugins/replication-remote-updates
```

Finally deploy to Gerrit test site:

```
$ cp bazel-bin/plugins/replication-remote-updates/replication-remote-updates.jar $site_path/plugins
```
