package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

/**
 * Configure node as a shared store master.
 */
public final class SetSharedStoreMaster implements OfflineCommand {

    private final Boolean failoverOnServerShutdown;
    private final Long failbackDelay;

    private SetSharedStoreMaster(Builder builder) {
        this.failoverOnServerShutdown = builder.failoverOnServerShutdown;
        this.failbackDelay = builder.failbackDelay;
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {

        GroovyXmlTransform transform = GroovyXmlTransform.of(SetSharedStoreMaster.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("failoverOnServerShutdown", failoverOnServerShutdown)
                .parameter("failbackDelay", failbackDelay)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "SetSharedStoreMaster";
    }

    public static final class Builder {

        private Boolean failoverOnServerShutdown;
        private Long failbackDelay;

        /**
         * Configure default messaging server as a shared store master.
         * <b>NOT YET IMPLEMENTED FOR HORNETQ SUBSYSTEM</b>
         */
        public Builder() {
        }

        /**
         * Whether the server must failover when it is normally shutdown.
         */
        public Builder failoverOnServerShutdown() {
            this.failoverOnServerShutdown = true;
            return this;
        }

        /**
         * Delay to wait before failback occurs on (live's) restart.
         */
        public Builder failbackDelay(long failbackDelay) {
            this.failbackDelay = failbackDelay;
            return this;
        }

        public SetSharedStoreMaster build() {
            return new SetSharedStoreMaster(this);
        }
    }
}
