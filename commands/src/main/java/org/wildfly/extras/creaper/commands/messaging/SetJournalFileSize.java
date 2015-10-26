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
import org.wildfly.extras.creaper.core.online.operations.Operations;

/**
 * Set journal file size
 *
 * @author mstyk
 */
public final class SetJournalFileSize implements OfflineCommand, OnlineCommand {

    private final String serverName;
    private final long journalFileSize;

    /**
     * Set the size (in bytes) of each journal file in default messaging server.
     *
     * @param journalFileSize size of each journal file
     */
    public SetJournalFileSize(long journalFileSize) {
        this(journalFileSize, MessagingUtils.DEFAULT_SERVER_NAME);
    }

    /**
     * Set the size (in bytes) of each journal file in the specified messaging
     * server. <b>NOT YET IMPLEMENTED FOR OFFLINE!</b>
     *
     * @param journalFileSize size of each journal file
     * @param serverName name of the messaging server
     */
    public SetJournalFileSize(long journalFileSize, String serverName) {
        if (serverName == null) {
            throw new IllegalArgumentException("Messaging server name must be specified as non null value");
        }
        this.serverName = serverName;
        this.journalFileSize = journalFileSize;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform enableSecurity = GroovyXmlTransform
                .of(SetJournalFileSize.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("journalFileSize", Long.valueOf(journalFileSize).toString())
                .build();
        ctx.client.apply(enableSecurity);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address address = MessagingUtils.address(ctx.client, serverName);

        ops.writeAttribute(address, MessagingConstants.JOURNAL_FILE_SIZE, journalFileSize);
    }
}
