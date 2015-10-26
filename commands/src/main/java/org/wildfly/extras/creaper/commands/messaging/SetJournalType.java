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
 * Set journal type
 *
 * @author mstyk
 */
public final class SetJournalType implements OfflineCommand, OnlineCommand {

    private final String serverName;
    private final String journalType;

    /**
     * Set the journal type in default messaging server.
     *
     * @param journalFileSize size of each journal file
     */
    public SetJournalType(String journalType) {
        this(journalType, MessagingUtils.DEFAULT_SERVER_NAME);
    }

    /**
     * Set the journal type in the specified messaging
     * server. <b>NOT YET IMPLEMENTED FOR OFFLINE!</b>
     *
     * @param journalFileSize size of each journal file
     * @param serverName name of the messaging server
     */
    public SetJournalType(String journalType, String serverName) {
        if (serverName == null) {
            throw new IllegalArgumentException("Messaging server name must be specified as non null value");
        }
        this.serverName = serverName;
        this.journalType = journalType;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform setType = GroovyXmlTransform
                .of(SetJournalType.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("journalType", journalType)
                .build();
        ctx.client.apply(setType);
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);

        Address address = MessagingUtils.address(ctx.client, serverName);

        ops.writeAttribute(address, MessagingConstants.JOURNAL_TYPE, journalType);
    }
}
