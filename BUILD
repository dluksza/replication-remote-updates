load("//tools/bzl:plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "replication-remote-updates",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: replication-remote-updates",
        "Gerrit-Module: org.luksza.gerrit.replication.updater.Module",
    ],
    deps = [
        "//plugins/replication",
    ],
)


