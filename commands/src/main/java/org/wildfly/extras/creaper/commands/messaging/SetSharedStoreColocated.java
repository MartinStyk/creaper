package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

/**
 * Configure node for a colocated shared store.
 */
public final class SetSharedStoreColocated implements OfflineCommand {

    private final Integer backupPortOffset;
    private final Integer backupRequestRetries;
    private final Long backupRequestRetryInterval;
    private final Integer maxBackups;
    private final Boolean requestBackup;

    private SetSharedStoreColocated(Builder builder) {
        this.backupPortOffset = builder.backupPortOffset;
        this.backupRequestRetries = builder.backupRequestRetries;
        this.backupRequestRetryInterval = builder.backupRequestRetryInterval;
        this.maxBackups = builder.maxBackups;
        this.requestBackup = builder.requestBackup;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {

        GroovyXmlTransform transform = GroovyXmlTransform.of(SetSharedStoreColocated.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("backupPortOffset", backupPortOffset)
                .parameter("backupRequestRetries", backupRequestRetries)
                .parameter("backupRequestRetryInterval", backupRequestRetryInterval)
                .parameter("maxBackups", maxBackups)
                .parameter("requestBackup", requestBackup)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "SetSharedStoreColocated";
    }

    public static final class Builder {

        private Integer backupPortOffset;
        private Integer backupRequestRetries;
        private Long backupRequestRetryInterval;
        private Integer maxBackups;
        private Boolean requestBackup;

        /**
         * Configure default messaging server for colocated shared store.
         * <b>NOT YET IMPLEMENTED FOR HORNETQ SUBSYSTEM</b>
         */
        public Builder() {
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

        public SetSharedStoreColocated build() {
            return new SetSharedStoreColocated(this);
        }

    }
}
