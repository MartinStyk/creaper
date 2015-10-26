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
 * Removes an existing jms bridge connector.
 */
public final class RemoveJmsBridge implements OnlineCommand, OfflineCommand {

    private final String name;

    /**
     * Removes a existing jms bridge from the default messaging server.
     */
    public RemoveJmsBridge(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name of the jms bridge must be specified as non null value");
        }
        this.name = name;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);
        ops.remove(MessagingUtils.subsystemAddress(ctx.client).and(MessagingConstants.JMS_BRIDGE, name));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException {
        GroovyXmlTransform transform = GroovyXmlTransform.of(RemoveJmsBridge.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("name", name)
                .build();
        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "RemoveJmsBridge " + name;
    }
}
