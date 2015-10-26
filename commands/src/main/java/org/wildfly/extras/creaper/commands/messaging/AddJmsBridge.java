package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
 * Creates new jms bridge in messaging subsystem.
 */
public final class AddJmsBridge implements OnlineCommand, OfflineCommand {

    private final String name;
    private final Boolean addMessageIdInHeader;
    private final String clientId;
    private final Long failureRetryInterval;
    private final Integer maxBatchSize;
    private final Long maxBatchTime;
    private final Integer maxRetries;
    private final String module;
    private final String qualityOfService;
    private final String selector;
    private final String subscriptionName;
    private final String sourceConnectionFactory;
    private final Map<String, String> sourceContext;
    private final String sourceDestination;
    private final String sourcePassword;
    private final String sourceUser;
    private final String targetConnectionFactory;
    private final Map<String, String> targetContext;
    private final String targetDestination;
    private final String targetPassword;
    private final String targetUser;
    private final boolean replaceExisting;

    private AddJmsBridge(Builder builder) {
        this.name = builder.name;
        this.addMessageIdInHeader = builder.addMessageIdInHeader;
        this.clientId = builder.clientId;
        this.failureRetryInterval = builder.failureRetryInterval;
        this.maxBatchSize = builder.maxBatchSize;
        this.maxBatchTime = builder.maxBatchTime;
        this.maxRetries = builder.maxRetries;
        this.module = builder.module;
        this.qualityOfService = builder.qualityOfService;
        this.selector = builder.selector;
        this.subscriptionName = builder.subscriptionName;
        this.sourceConnectionFactory = builder.sourceConnectionFactory;
        this.sourceContext = builder.sourceContext;
        this.sourceDestination = builder.sourceDestination;
        this.sourcePassword = builder.sourcePassword;
        this.sourceUser = builder.sourceUser;
        this.targetConnectionFactory = builder.targetConnectionFactory;
        this.targetContext = builder.targetContext;
        this.targetDestination = builder.targetDestination;
        this.targetPassword = builder.targetPassword;
        this.targetUser = builder.targetUser;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);

        Address bridgeAddress = MessagingUtils.subsystemAddress(ctx.client)
                .and(MessagingConstants.JMS_BRIDGE, name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(bridgeAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing jms bridge " + name, e);
            }
        }

        Values values = Values.empty()
                .andOptional("add-messageID-in-header", addMessageIdInHeader)
                .andOptional("client-id", clientId)
                .andOptional("failure-retry-interval", failureRetryInterval)
                .andOptional("max-batch-size", maxBatchSize)
                .andOptional("max-batch-time", maxBatchTime)
                .andOptional("max-retries", maxRetries)
                .andOptional("module", module)
                .andOptional("quality-of-service", qualityOfService)
                .andOptional("selector", selector)
                .andOptional("subscription-name", subscriptionName)
                .andOptional("source-connection-factory", sourceConnectionFactory)
                .andObjectOptional("source-context", Values.fromMap(sourceContext))
                .andOptional("source-destination", sourceDestination)
                .andOptional("source-password", sourcePassword)
                .andOptional("source-user", sourceUser)
                .andOptional("target-connection-factory", targetConnectionFactory)
                .andObjectOptional("target-context", Values.fromMap(targetContext))
                .andOptional("target-destination", targetDestination)
                .andOptional("target-password", targetPassword)
                .andOptional("target-user", targetUser);

        Batch batch = new Batch();
        batch.add(bridgeAddress, values);

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {

        Map<String, String> sourceCtx = sourceContext.isEmpty() ? null : sourceContext;
        Map<String, String> targetCtx = targetContext.isEmpty() ? null : targetContext;

        GroovyXmlTransform transform = GroovyXmlTransform.of(AddJmsBridge.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("bridgeName", name)
                .parameter("replaceExisting", replaceExisting)
                .parameter("addMessageIdInHeader", addMessageIdInHeader)
                .parameter("clientId", clientId)
                .parameter("failureRetryInterval", failureRetryInterval)
                .parameter("maxBatchSize", maxBatchSize)
                .parameter("maxBatchTime", maxBatchTime)
                .parameter("maxRetries", maxRetries)
                .parameter("module", module)
                .parameter("qualityOfService", qualityOfService)
                .parameter("selector", selector)
                .parameter("subscriptionName", subscriptionName)
                .parameter("sourceConnectionFactory", sourceConnectionFactory)
                .parameter("sourceContext", sourceCtx)
                .parameter("sourceDestination", sourceDestination)
                .parameter("sourcePassword", sourcePassword)
                .parameter("sourceUser", sourceUser)
                .parameter("targetConnectionFactory", targetConnectionFactory)
                .parameter("targetContext", targetCtx)
                .parameter("targetDestination", targetDestination)
                .parameter("targetPassword", targetPassword)
                .parameter("targetUser", targetUser)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "AddJmsBridge " + name;
    }

    public static final class Builder {

        private final String name;
        private Boolean addMessageIdInHeader;
        private String clientId;
        private Long failureRetryInterval;
        private Integer maxBatchSize;
        private Long maxBatchTime;
        private Integer maxRetries;
        private String module;
        private String qualityOfService;
        private String selector;
        private String subscriptionName;
        private String sourceConnectionFactory;
        private Map<String, String> sourceContext = new HashMap<String, String>();
        private String sourceDestination;
        private String sourcePassword;
        private String sourceUser;
        private String targetConnectionFactory;
        private Map<String, String> targetContext = new HashMap<String, String>();
        private String targetDestination;
        private String targetPassword;
        private String targetUser;
        private boolean replaceExisting;

        /**
         * Adds a jms bridge to the messaging subsystem.
         *
         * @param name name of the queue
         */
        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Bridge name must be specified as non null value");
            }
            this.name = name;
        }

        /**
         * If set, then the original message's message ID will be appended in
         * the message sent to the destination in the header. If the message is
         * bridged more than once, each message ID will be appended.
         */
        public Builder addMessageIdInHeader() {
            this.addMessageIdInHeader = true;
            return this;
        }

        /**
         * The JMS client ID to use when creating/looking up the subscription if
         * it is durable and the source destination is a topic.
         */
        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * The amount of time in milliseconds to wait between trying to recreate
         * connections to the source or target servers when the bridge has
         * detected they have failed.
         */
        public Builder failureRetryInterval(long failureRetryInterval) {
            this.failureRetryInterval = failureRetryInterval;
            return this;
        }

        /**
         * The maximum number of messages to consume from the source destination
         * before sending them in a batch to the target destination. Its value
         * must >= 1
         */
        public Builder maxBatchSize(int maxBatchSize) {
            this.maxBatchSize = maxBatchSize;
            return this;
        }

        /**
         * The maximum number of milliseconds to wait before sending a batch to
         * target, even if the number of messages consumed has not reached
         * max-batch-size. Its value must be -1 to represent 'wait forever', or
         * >= 1 to specify an actual time.
         */
        public Builder maxBatchTime(long maxBatchTime) {
            this.maxBatchTime = maxBatchTime;
            return this;
        }

        /**
         * The number of times to attempt to recreate connections to the source
         * or target servers when the bridge has detected they have failed. The
         * bridge will give up after trying this number of times. -1 represents
         * 'try forever'
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * The name of AS7 module containing the resources required to lookup
         * source and target JMS resources.
         */
        public Builder module(String module) {
            this.module = module;
            return this;
        }

        /**
         * The desired quality of service mode (AT_MOST_ONCE, DUPLICATES_OK or
         * ONCE_AND_ONLY_ONCE).
         */
        public Builder qualityOfService(String qualityOfService) {
            this.qualityOfService = qualityOfService;
            return this;
        }

        /**
         * A JMS selector expression used for consuming messages from the source
         * destination. Only messages that match the selector expression will be
         * bridged from the source to the target destination.
         */
        public Builder selector(String selector) {
            this.selector = selector;
            return this;
        }

        /**
         * The name of the subscription if it is durable and the source
         * destination is a topic.
         */
        public Builder subscriptionName(String subscriptionName) {
            this.subscriptionName = subscriptionName;
            return this;
        }

        /**
         * The name of the source connection factory to lookup on the source
         * messaging server.
         */
        public Builder sourceConnectionFactory(String sourceConnectionFactory) {
            this.sourceConnectionFactory = sourceConnectionFactory;
            return this;
        }

        /**
         * The properties used to configure the source JNDI initial context.
         */
        public Builder sourceContext(Map<String, String> sourceContext) {
            this.sourceContext = sourceContext;
            return this;
        }

        /**
         * The name of the source destination to lookup on the source messaging
         * server.
         */
        public Builder sourceDestination(String sourceDestination) {
            this.sourceDestination = sourceDestination;
            return this;
        }

        /**
         * The password for creating the source connection.
         */
        public Builder sourcePassword(String sourcePassword) {
            this.sourcePassword = sourcePassword;
            return this;
        }

        /**
         * The name of the user for creating the source connection.
         */
        public Builder sourceUser(String sourceUser) {
            this.sourceUser = sourceUser;
            return this;
        }

        /**
         * The name of the target connection factory to lookup on the target
         * messaging server.
         */
        public Builder targetConnectionFactory(String targetConnectionFactory) {
            this.targetConnectionFactory = targetConnectionFactory;
            return this;
        }

        /**
         * The properties used to configure the target JNDI initial context.
         */
        public Builder targetContext(Map<String, String> targetContext) {
            this.targetContext = targetContext;
            return this;
        }

        /**
         * The name of the target destination to lookup on the target messaging
         * server.
         */
        public Builder targetDestination(String targetDestination) {
            this.targetDestination = targetDestination;
            return this;
        }

        /**
         * The password for creating the target connection.
         */
        public Builder targetPassword(String targetPassword) {
            this.targetPassword = targetPassword;
            return this;
        }

        /**
         * The name of the user for creating the target connection.
         */
        public Builder targetUser(String targetUser) {
            this.targetUser = targetUser;
            return this;
        }

        /**
         * Specify whether to replace the existing bridge based on its name. By
         * default existing bridge is not replaced and exception is thrown.
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddJmsBridge build() {
            check();
            return new AddJmsBridge(this);
        }

        private void check() {
            if (targetDestination == null) {
                throw new IllegalArgumentException("targetDestination must be defined");
            }
            if (targetConnectionFactory == null) {
                throw new IllegalArgumentException("targetConnectionFactory must be defined");
            }
            if (sourceDestination == null) {
                throw new IllegalArgumentException("sourceDestination must be defined");
            }
            if (sourceConnectionFactory == null) {
                throw new IllegalArgumentException("sourceConnectionFactory must be defined");
            }
            if (qualityOfService == null) {
                throw new IllegalArgumentException("qualityOfService must be defined. Possible values : AT_MOST_ONCE, DUPLICATES_OK or ONCE_AND_ONLY_ONCE");
            }
            if (maxRetries == null) {
                throw new IllegalArgumentException("maxRetries must be defined");
            }
            if (maxBatchSize == null) {
                throw new IllegalArgumentException("maxBatchSize must be defined");
            }
            if (maxBatchTime == null) {
                throw new IllegalArgumentException("maxBatchTime must be defined");
            }
            if (failureRetryInterval == null) {
                throw new IllegalArgumentException("failureRetryInterval must be defined");
            }
        }
    }
}
