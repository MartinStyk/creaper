package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

/**
 * Set paging directory
 *
 * @author mstyk
 */
public final class SetPagingDirectory implements OfflineCommand {

    private final String path;

    /**
     * Set the path to the paging directory on default messaging server.
     *
     * <p>NOT IMPLEMENTED FOR HORNETQ SUBSYSTEM<p>
     *
     * @param path path to the paging directory
     */
    public SetPagingDirectory(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path must be specified as non null value");
        }
        this.path = path;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform setDirectory = GroovyXmlTransform
                .of(SetPagingDirectory.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("path", path)
                .build();
        ctx.client.apply(setDirectory);
    }
}
