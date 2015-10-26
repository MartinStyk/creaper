package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

/**
 * Set journal directory
 *
 * @author mstyk
 */
public final class SetJournalDirectory implements OfflineCommand {

    private final String path;

    /**
     * Set the path to the journal directory on default messaging server.
     *
     * <p> NOT IMPLEMENTED FOR HORNETQ SUBSYSTEM<p>
     *
     * @param path path to the journal directory
     */
    public SetJournalDirectory(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path must be specified as non null value");
        }
        this.path = path;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform setDirectory = GroovyXmlTransform
                .of(SetJournalDirectory.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("path", path)
                .build();
        ctx.client.apply(setDirectory);
    }
}
