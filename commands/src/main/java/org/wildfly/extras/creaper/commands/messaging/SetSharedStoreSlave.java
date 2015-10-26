package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

/**
 * Configure node as a shared store slave.
 */
public final class SetSharedStoreSlave implements OfflineCommand {

    private final Boolean allowFailback;
    private final Long failbackDelay;
    private final Boolean failoverOnServerShutdown;
    private final Boolean restartBackup;
    private final Boolean scaleDown;
    private final String scaleDownClusterName;
    private final List<String> scaleDownConnectors;
    private final String scaleDownDiscoveryGroup;
    private final String scaleDownGroupName;

    private SetSharedStoreSlave(Builder builder) {
        this.allowFailback = builder.allowFailback;
        this.failbackDelay = builder.failbackDelay;
        this.failoverOnServerShutdown = builder.failoverOnServerShutdown;
        this.restartBackup = builder.restartBackup;
        this.scaleDown = builder.scaleDown;
        this.scaleDownClusterName = builder.scaleDownClusterName;
        this.scaleDownConnectors = builder.scaleDownConnectors;
        this.scaleDownDiscoveryGroup = builder.scaleDownDiscoveryGroup;
        this.scaleDownGroupName = builder.scaleDownGroupName;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {

        String tempConnectors = scaleDownConnectors.isEmpty() ? null
                : MessagingUtils.getStringOfEntries(scaleDownConnectors);

        GroovyXmlTransform transform = GroovyXmlTransform.of(SetSharedStoreSlave.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("allowFailback", allowFailback)
                .parameter("failbackDelay", failbackDelay)
                .parameter("failoverOnServerShutdown", failoverOnServerShutdown)
                .parameter("restartBackup", restartBackup)
                .parameter("scaleDown", scaleDown)
                .parameter("scaleDownClusterName", scaleDownClusterName)
                .parameter("scaleDownConnectorsString", tempConnectors)
                .parameter("scaleDownDiscoveryGroup", scaleDownDiscoveryGroup)
                .parameter("scaleDownGroupName", scaleDownGroupName)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "SetSharedStoreSlave ";
    }

    public static final class Builder {

        private Boolean allowFailback;
        private Long failbackDelay;
        private Boolean failoverOnServerShutdown;
        private Boolean restartBackup;
        private Boolean scaleDown;
        private String scaleDownClusterName;
        private List<String> scaleDownConnectors = new ArrayList<String>();
        private String scaleDownDiscoveryGroup;
        private String scaleDownGroupName;

        /**
         * Configure default messaging server as a shared store slave.
         * <b>NOT YET IMPLEMENTED FOR HORNETQ SUBSYSTEM</b>
         */
        public Builder() {
        }

        /**
         * Whether a server will automatically stop when a another places a
         * request to take over its place. The use case is when a regular server
         * stops and its backup takes over its duties, later the main server
         * restarts and requests the server (the former backup) to stop
         * operating.
         */
        public Builder allowFailback(boolean allowFailback) {
            this.allowFailback = allowFailback;
            return this;
        }

        /**
         * Delay to wait before failback occurs on (live's) restart.
         */
        public Builder failbackDelay(long failbackDelay) {
            this.failbackDelay = failbackDelay;
            return this;
        }

        /**
         * Whether the server must failover when it is normally shutdown.
         */
        public Builder failoverOnServerShutdown() {
            this.failoverOnServerShutdown = true;
            return this;
        }

        /**
         * Will this server, if a backup, restart once it has been stopped
         * because of failback or scaling down.
         */
        public Builder restartBackup() {
            this.restartBackup = true;
            return this;
        }

        /**
         * Configure whether this server send its messages to another live
         * server in the scale-down cluster when it is shutdown cleanly.
         */
        public Builder scaleDown() {
            this.scaleDown = true;
            return this;
        }

        /**
         * Name of the cluster used to scale down.
         */
        public Builder scaleDownClusterName(String scaleDownClusterName) {
            this.scaleDownClusterName = scaleDownClusterName;
            return this;
        }

        /**
         * List of connectors used to form the scale-down cluster.
         */
        public Builder scaleDownConnectors(List<String> scaleDownConnectors) {
            this.scaleDownConnectors = scaleDownConnectors;
            return this;
        }

        /**
         * Name of the discovery group used to build the scale-down cluster.
         */
        public Builder scaleDownDiscoveryGroup(String scaleDownDiscoveryGroup) {
            this.scaleDownDiscoveryGroup = scaleDownDiscoveryGroup;
            return this;
        }

        /**
         * Name of the group used to scale down.
         */
        public Builder scaleDownGroupName(String scaleDownGroupName) {
            this.scaleDownGroupName = scaleDownGroupName;
            return this;
        }

        public SetSharedStoreSlave build() {
            return new SetSharedStoreSlave(this);
        }

    }
}
