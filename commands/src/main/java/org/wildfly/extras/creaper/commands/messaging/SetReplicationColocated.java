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
 * Configure node for a colocated replication.
 */
public final class SetReplicationColocated implements OfflineCommand {

    private final Integer backupPortOffset;
    private final Integer backupRequestRetries;
    private final Long backupRequestRetryInterval;
    private final Integer maxBackups;
    private final Boolean requestBackup;
    private final List<String> excludedConnectors;

    private SetReplicationColocated(Builder builder) {
        this.backupPortOffset = builder.backupPortOffset;
        this.backupRequestRetries = builder.backupRequestRetries;
        this.backupRequestRetryInterval = builder.backupRequestRetryInterval;
        this.maxBackups = builder.maxBackups;
        this.requestBackup = builder.requestBackup;
        this.excludedConnectors = builder.excludedConnectors;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {

        String connectors = excludedConnectors.isEmpty() ? null
                : MessagingUtils.getStringOfEntries(excludedConnectors);

        GroovyXmlTransform transform = GroovyXmlTransform.of(SetReplicationColocated.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("backupPortOffset", backupPortOffset)
                .parameter("backupRequestRetries", backupRequestRetries)
                .parameter("backupRequestRetryInterval", backupRequestRetryInterval)
                .parameter("maxBackups", maxBackups)
                .parameter("requestBackup", requestBackup)
                .parameter("excludedConnectors", connectors)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "SetReplicationColocated";
    }

    public static final class Builder {

        private Integer backupPortOffset;
        private Integer backupRequestRetries;
        private Long backupRequestRetryInterval;
        private Integer maxBackups;
        private Boolean requestBackup;
        private List<String> excludedConnectors = new ArrayList<String>();

        /**
         * Configure default messaging server for colocated replication.
         * <b>NOT YET IMPLEMENTED FOR HORNETQ SUBSYSTEM</b>
         */
        public Builder() {
        }

        /**
         * The connectors that must not have their port offset.
         */
        public Builder excludedConnectors(List<String> excludedConnectors) {
            this.excludedConnectors = excludedConnectors;
            return this;
        }

        /**
         * The offset to use for the Connectors and Acceptors when creating a
         * new backup server.
         */
        public Builder backupPortOffset(int backupPortOffset) {
            this.backupPortOffset = backupPortOffset;
            return this;
        }

        /**
         * How many times the live server will try to request a backup, -1 means
         * for ever.
         */
        public Builder backupRequestRetries(int backupRequestRetries) {
            this.backupRequestRetries = backupRequestRetries;
            return this;
        }

        /**
         * How long (in ms) to wait for retries between attempts to request a
         * backup server.
         */
        public Builder backupRequestRetryInterval(long backupRequestRetryInterval) {
            this.backupRequestRetryInterval = backupRequestRetryInterval;
            return this;
        }

        /**
         * Whether or not this live server will accept backup requests from
         * other live servers.
         */
        public Builder maxBackups(int maxBackups) {
            this.maxBackups = maxBackups;
            return this;
        }

        /**
         * If true then the server will request a backup on another node.
         */
        public Builder requestBackup() {
            this.requestBackup = true;
            return this;
        }

        public SetReplicationColocated build() {
            return new SetReplicationColocated(this);
        }

    }
}
