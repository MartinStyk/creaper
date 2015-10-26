package org.wildfly.extras.creaper.commands.messaging;

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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates new remote connector in messaging subsystem.
 */
public final class AddRemoteConnector implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String serverName;
    private String socketBinding;
    private Map<String, String> params;
    private final boolean replaceExisting;

    private AddRemoteConnector(Builder builder) {
        this.name = builder.name;
        this.serverName = builder.serverName;
        this.socketBinding = builder.socketBinding;
        this.params = builder.params;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);

        Address connectorAddress = MessagingUtils.address(ctx.client, serverName)
                .and(MessagingConstants.REMOTE_CONNECTOR, name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(connectorAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing remote connector " + name, e);
            }
        }

        Values values = Values.empty()
                .andOptional("socket-binding", socketBinding)
                .andObject("params", Values.fromMap(params));

        Batch batch = new Batch();
        batch.add(connectorAddress, values);

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (!MessagingUtils.DEFAULT_SERVER_NAME.equals(serverName)) {
            throw new CommandFailedException("Non-default messaging server name not yet implemented in offline mode");
        }

        GroovyXmlTransform transform = GroovyXmlTransform.of(AddRemoteConnector.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("name", name)
                .parameter("socketbinding", socketBinding)
                .parameter("params", params)
                .parameter("replaceExisting", replaceExisting)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "AddRemoteConnector " + name;
    }

    public static final class Builder {

        private final String name;
        private final String serverName;
        private String socketBinding;
        private boolean replaceExisting;
        private Map<String, String> params = new HashMap<String, String>();

        /**
         * Adds a remote connector to the default messaging server.
         *
         * @param name name of the connector
         */
        public Builder(String name) {
            this(name, MessagingUtils.DEFAULT_SERVER_NAME);
        }

        /**
         * Adds a http connector to the specified messaging server. <b>NOT YET
         * IMPLEMENTED FOR OFFLINE!</b>
         *
         * @param name name of the connector
         * @param serverName name of the messaging server
         */
        public Builder(String name, String serverName) {
            if (name == null) {
                throw new IllegalArgumentException("Connector name must be specified as non null value");
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
         * Defines key-value pair understood by the connector factory-class and
         * used to configure it.
         */
        public Builder params(Map<String, String> params) {
            this.params = params;
            return this;
        }

        /**
         * Specify whether to replace the existing connector based on its name.
         * By default existing connector is not replaced and exception is
         * thrown.
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddRemoteConnector build() {
            check();
            return new AddRemoteConnector(this);
        }

        private void check() {
            if (socketBinding == null || socketBinding.length() == 0) {
                throw new IllegalArgumentException("Socket binding needs to be specified for remtoe-connector");
            }
        }
    }
}
