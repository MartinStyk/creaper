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
 * Creates new http connector in messaging subsystem.
 */
public final class AddHttpConnector implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String serverName;
    private String endPoint;
    private String socketBinding;
    private Map<String, String> params;
    private final boolean replaceExisting;

    private AddHttpConnector(Builder builder) {
        this.name = builder.name;
        this.serverName = builder.serverName;
        this.endPoint = builder.endPoint;
        this.socketBinding = builder.socketBinding;
        this.params = builder.params;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);

        Address connectorAddress = MessagingUtils.address(ctx.client, serverName)
                .and(MessagingConstants.HTTP_CONNECTOR, name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(connectorAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing http connector " + name, e);
            }
        }

        Values values = Values.empty()
                .andOptional("socket-binding", socketBinding)
                .andObject("params", Values.fromMap(params))
                .andOptional("endpoint", endPoint);

        Batch batch = new Batch();
        batch.add(connectorAddress, values);

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (!MessagingUtils.DEFAULT_SERVER_NAME.equals(serverName)) {
            throw new CommandFailedException("Non-default messaging server name not yet implemented in offline mode");
        }

        GroovyXmlTransform transform = GroovyXmlTransform.of(AddHttpConnector.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("name", name)
                .parameter("endpoint", endPoint)
                .parameter("socketbinding", socketBinding)
                .parameter("params", params)
                .parameter("replaceExisting", replaceExisting)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "AddHttpConnector " + name;
    }

    public static final class Builder {

        private final String name;
        private final String serverName;
        private String endPoint;
        private String socketBinding;
        private boolean replaceExisting;
        private Map<String, String> params = new HashMap<String, String>();

        /**
         * Adds a http connector to the default messaging server.
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
         * @param name name of the http connector
         * @param serverName name of the messaging server
         */
        public Builder(String name, String serverName) {
            if (name == null) {
                throw new IllegalArgumentException("Http connector name must be specified as non null value");
            }
            if (serverName == null) {
                throw new IllegalArgumentException("Messaging server name must be specified as non null value");
            }

            this.name = name;
            this.serverName = serverName;
        }

        /**
         * Defines the http-acceptor that serves as the endpoint of this
         * http-connector. !!! Set this parameter only if endpoint parameter is
         * used !!!
         */
        public Builder endPoint(String endPoint) {
            this.endPoint = endPoint;
            return this;
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
         * Specify whether to replace the existing http connector based on its
         * name. By default existing connector is not replaced and exception is
         * thrown.
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddHttpConnector build() {
            check();
            return new AddHttpConnector(this);
        }

        private void check() {
            if (socketBinding == null || socketBinding.length() == 0) {
                throw new IllegalArgumentException("Socket binding needs to be specified for http-connector");
            }
        }
    }
}
