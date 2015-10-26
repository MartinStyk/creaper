package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Removes an existing cluster connection
 */
public final class RemoveClusterConnection implements OnlineCommand, OfflineCommand {
    private final String name;
    private final String serverName;

    /**
     * Removes a core bridge from the default messaging server.
     */
    public RemoveClusterConnection(String name) {
        this(name, MessagingUtils.DEFAULT_SERVER_NAME);
    }

    /**
     * Removes a cluster connection from the specified messaging server. <b>NOT YET IMPLEMENTED FOR OFFLINE!</b>
     */
    public RemoveClusterConnection(String name, String serverName) {
        if (name == null) {
            throw new IllegalArgumentException("Name of the cluster connection must be specified as non null value");
        }
        if (serverName == null) {
            throw new IllegalArgumentException("Server name must be specified as non null value");
        }
        this.name = name;
        this.serverName = serverName;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);
        ops.remove(MessagingUtils.address(ctx.client, serverName).and(MessagingConstants.CLUSTER_CONNECTION, name));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        if (!MessagingUtils.DEFAULT_SERVER_NAME.equals(serverName)) {
            throw new CommandFailedException("Non-default messaging server name not yet implemented in offline mode");
        }

        GroovyXmlTransform transform = GroovyXmlTransform.of(RemoveClusterConnection.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("name", name)
                .build();
        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "RemoveClusterConnection " + name;
    }
}