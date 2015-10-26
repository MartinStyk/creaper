package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

/**
 *
 * @author mstyk
 */
public class AddDiscoveryGroup implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String serverName;
    private final String socketBinding;
    private final boolean replaceExisting;
    private final String jGroupsStack;
    private final String jGroupsChannel;
    private final Long refreshTimeout;
    private final Long initialWaitTimeout;

    private AddDiscoveryGroup(Builder builder) {
        this.name = builder.name;
        this.serverName = builder.serverName;
        this.socketBinding = builder.socketBinding;
        this.replaceExisting = builder.replaceExisting;
        this.jGroupsChannel = builder.jGroupsChannel;
        this.jGroupsStack = builder.jGroupsStack;
        this.refreshTimeout = builder.refreshTimeout;
        this.initialWaitTimeout = builder.initialWaitTimeout;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);

        Address connectorAddress = MessagingUtils.address(ctx.client, serverName)
                .and(MessagingConstants.DISCOVERY_GROUP, name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(connectorAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing discovery group " + name, e);
            }
        }

        Values values = Values.empty()
                .andOptional("socket-binding", socketBinding)
                .andOptional("jgroups-stack", jGroupsStack)
                .andOptional("jgroups-channel", jGroupsChannel)
                .andOptional("initial-wait-timeout", initialWaitTimeout)
                .andOptional("refresh-timeout", refreshTimeout);

        Batch batch = new Batch();
        batch.add(connectorAddress, values);

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (!MessagingUtils.DEFAULT_SERVER_NAME.equals(serverName)) {
            throw new CommandFailedException("Non-default messaging server name not yet implemented in offline mode");
        }

        GroovyXmlTransform transform = GroovyXmlTransform.of(AddDiscoveryGroup.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("name", name)
                .parameter("socketBinding", socketBinding)
                .parameter("replaceExisting", replaceExisting)
                .parameter("jGroupsStack", jGroupsStack)
                .parameter("jGroupsChannel", jGroupsChannel)
                .parameter("initialWaitTimeout", initialWaitTimeout)
                .parameter("refreshTimeout", refreshTimeout)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "AddDiscoveryGroup " + name;
    }

    public static final class Builder {

        private final String name;
        private final String serverName;
        private String socketBinding;
        private boolean replaceExisting;
        private String jGroupsStack;
        private String jGroupsChannel;
        private Long initialWaitTimeout;
        private Long refreshTimeout;

        /**
         * Adds a broadcast group to the default messaging server.
         *
         * @param name name of the group
         */
        public Builder(String name) {
            this(name, MessagingUtils.DEFAULT_SERVER_NAME);
        }

        /**
         * Adds a broadcast group to the specified messaging server. <b>NOT YET
         * IMPLEMENTED FOR OFFLINE!</b>
         *
         * @param name name of the group
         * @param serverName name of the messaging server
         */
        public Builder(String name, String serverName) {
            if (name == null) {
                throw new IllegalArgumentException("Group name must be specified as non null value");
            }
            if (serverName == null) {
                throw new IllegalArgumentException("Messaging server name must be specified as non null value");
            }

            this.name = name;
            this.serverName = serverName;
        }

        /**
         * Defines the socket binding that the connector will use to create
         * connections.
         */
        public Builder socketBinding(String socketBinding) {
            this.socketBinding = socketBinding;
            return this;
        }

        /**
         * The name of a stack defined in the org.jboss.as.clustering.jgroups
         * subsystem that is used to form a cluster.
         */
        public Builder jGroupsStack(String jGroupsStack) {
            this.jGroupsStack = jGroupsStack;
            return this;
        }

        /**
         * The name used by a JGroups channel to join a cluster.
         */
        public Builder jGroupsChannel(String jGroupsChannel) {
            this.jGroupsChannel = jGroupsChannel;
            return this;
        }

        /**
         * Period the discovery group waits after receiving the last broadcast
         * from a particular server before removing that server's connector pair
         * entry from its list.
         *
         */
        public Builder refreshTimeout(long refreshTimeout) {
            this.refreshTimeout = refreshTimeout;
            return this;
        }

        /**
         * Period, in ms, to wait for an initial broadcast to give us at least
         * one node in the cluster.
         */
        public Builder initialWaitTimeout(long initialWaitTimeout) {
            this.initialWaitTimeout = initialWaitTimeout;
            return this;
        }

        /**
         * Specify whether to replace the existing group based on its name. By
         * default existing connector is not replaced and exception is thrown.
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddDiscoveryGroup build() {
            return new AddDiscoveryGroup(this);
        }
    }
}
