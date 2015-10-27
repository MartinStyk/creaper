package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

/**
 * Configure node as a replication master.
 */
public final class SetReplicationMaster implements OfflineCommand {

    private final Boolean checkForLiveServer;
    private final String clusterName;
    private final String groupName;

    private SetReplicationMaster(Builder builder) {
        this.checkForLiveServer = builder.checkForLiveServer;
        this.clusterName = builder.clusterName;
        this.groupName = builder.groupName;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {

        GroovyXmlTransform transform = GroovyXmlTransform.of(SetReplicationMaster.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("checkForLiveServer", checkForLiveServer)
                .parameter("clusterName", clusterName)
                .parameter("groupName", groupName)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "SetReplicationMaster";
    }

    public static final class Builder {

        private Boolean checkForLiveServer;
        private String clusterName;
        private String groupName;

        /**
         * Configure default messaging server as a replication master.
         * <b>NOT YET IMPLEMENTED FOR HORNETQ SUBSYSTEM</b>
         */
        public Builder() {
        }

        /**
         * Whether to check the cluster for another server using the same server
         * ID when starting up.
         */
        public Builder checkForLiveServer() {
            this.checkForLiveServer = true;
            return this;
        }

        /**
         * Name of the cluster used for replication.
         */
        public Builder clusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        /**
         * If set, backup servers will only pair with live servers with matching
         * group-name.
         */
        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public SetReplicationMaster build() {
            return new SetReplicationMaster(this);
        }

    }
}
