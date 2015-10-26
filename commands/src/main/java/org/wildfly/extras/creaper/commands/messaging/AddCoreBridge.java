package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
 * Creates new core bridge in messaging subsystem.
 */
public final class AddCoreBridge implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String serverName;
    private final String queueName;
    private final String forwardingAddress;
    private final Long retryInterval;
    private final BigDecimal retryIntervalMultiplier;
    private final Integer confirmationWindowSize;
    private final List<String> staticConnectors;
    private final Boolean replaceExisting;
    private final Boolean useDuplicateDetection;
    private final Boolean ha;
    private final Long checkPeriod;
    private final Long connectionTtl;
    private final String discoveryGroup;
    private final String filter;
    private final Integer initialConnectAttempts;
    private final Long maxRetryInterval;
    private final Integer minLargeMessageSize;
    private final String password;
    private final Integer reconnectAttempts;
    private final Integer reconnectAttemptsOnSameNode;
    private final String transformerClassName;
    private final String user;

    private AddCoreBridge(Builder builder) {
        this.name = builder.name;
        this.serverName = builder.serverName;
        this.queueName = builder.queueName;
        this.forwardingAddress = builder.forwardingAddress;
        this.retryInterval = builder.retryInterval;
        this.retryIntervalMultiplier = builder.retryIntervalMultiplier;
        this.confirmationWindowSize = builder.confirmationWindowSize;
        this.staticConnectors = builder.staticConnectors;
        this.replaceExisting = builder.replaceExisting;
        this.useDuplicateDetection = builder.useDuplicateDetection;
        this.ha = builder.ha;
        this.checkPeriod = builder.checkPeriod;
        this.connectionTtl = builder.connectionTtl;
        this.discoveryGroup = builder.discoveryGroup;
        this.filter = builder.filter;
        this.initialConnectAttempts = builder.initialConnectAttempts;
        this.maxRetryInterval = builder.maxRetryInterval;
        this.minLargeMessageSize = builder.minLargeMessageSize;
        this.password = builder.password;
        this.reconnectAttempts = builder.reconnectAttempts;
        this.reconnectAttemptsOnSameNode = builder.reconnectAttemptsOnSameNode;
        this.transformerClassName = builder.transformerClassName;
        this.user = builder.user;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);

        Address address = MessagingUtils.address(ctx.client, serverName)
                .and(MessagingConstants.CORE_BRIDGE, name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(address);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing core bridge " + name, e);
            }
        }

        Values values = Values.empty()
                .andOptional("queue-name", queueName)
                .andOptional("forwarding-address", forwardingAddress)
                .andOptional("retry-interval", retryInterval)
                .andOptional("retry-interval-multiplier", retryIntervalMultiplier)
                .andOptional("confirmation-window-size", confirmationWindowSize)
                .andListOptional(String.class, "static-connectors", staticConnectors)
                .andOptional("use-duplicate-detection", useDuplicateDetection)
                .andOptional("ha", ha)
                .andOptional("check-period", checkPeriod)
                .andOptional("connection-ttl", connectionTtl)
                .andOptional("discovery-group", discoveryGroup)
                .andOptional("filter", filter)
                .andOptional("initial-connect-attempts", initialConnectAttempts)
                .andOptional("max-retry-interval", maxRetryInterval)
                .andOptional("min-large-message-size", minLargeMessageSize)
                .andOptional("password", password)
                .andOptional("user", user)
                .andOptional("reconnect-attempts", reconnectAttempts)
                .andOptional("reconnect-attempts-on-same-node", reconnectAttemptsOnSameNode)
                .andOptional("transformer-class-name", transformerClassName);

        Batch batch = new Batch();
        batch.add(address, values);

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (!MessagingUtils.DEFAULT_SERVER_NAME.equals(serverName)) {
            throw new CommandFailedException("Non-default messaging server name not yet implemented in offline mode");
        }

        GroovyXmlTransform transform = GroovyXmlTransform.of(AddCoreBridge.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("name", name)
                .parameter("replaceExisting", replaceExisting)
                .parameter("queueName", queueName)
                .parameter("forwardingAddress", forwardingAddress)
                .parameter("retryInterval", retryInterval)
                .parameter("retryIntervalMultiplier", retryIntervalMultiplier)
                .parameter("confirmationWindowSize", confirmationWindowSize)
                .parameter("staticConnectors", MessagingUtils.getStringOfEntries(staticConnectors))
                .parameter("staticConnectorsList", staticConnectors)
                .parameter("useDuplicateDetection", useDuplicateDetection)
                .parameter("ha", ha)
                .parameter("checkPeriod", checkPeriod)
                .parameter("connectionTtl", connectionTtl)
                .parameter("discoveryGroup", discoveryGroup)
                .parameter("filter", filter)
                .parameter("initialConnectAttempts", initialConnectAttempts)
                .parameter("maxRetryInterval", maxRetryInterval)
                .parameter("minLargeMessageSize", minLargeMessageSize)
                .parameter("password", password)
                .parameter("user", user)
                .parameter("reconnectAttempts", reconnectAttempts)
                .parameter("reconnectAttemptsOnSameNode", reconnectAttemptsOnSameNode)
                .parameter("transformerClassName", transformerClassName)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "AddCoreBridge " + name;
    }

    public static final class Builder {

        private final String name;
        private final String serverName;
        private String queueName;
        private String forwardingAddress;
        private Long retryInterval;
        private BigDecimal retryIntervalMultiplier;
        private Integer confirmationWindowSize;
        private List<String> staticConnectors = new ArrayList<String>();
        private boolean replaceExisting;
        private Boolean useDuplicateDetection;
        private Boolean ha;
        private Long checkPeriod;
        private Long connectionTtl;
        private String discoveryGroup;
        private String filter;
        private Integer initialConnectAttempts;
        private Long maxRetryInterval;
        private Integer minLargeMessageSize;
        private String password;
        private Integer reconnectAttempts;
        private Integer reconnectAttemptsOnSameNode;
        private String transformerClassName;
        private String user;

        /**
         * Adds a core bridge to the default messaging server.
         *
         * @param name name of the core bridge
         */
        public Builder(String name) {
            this(name, MessagingUtils.DEFAULT_SERVER_NAME);
        }

        /**
         * Adds a core bridge to the specified messaging server. <b>NOT YET
         * IMPLEMENTED FOR OFFLINE!</b>
         *
         * @param name name of the core bridge
         * @param serverName name of the messaging server
         */
        public Builder(String name, String serverName) {
            if (name == null) {
                throw new IllegalArgumentException("Bridge name must be specified as non null value");
            }
            if (serverName == null) {
                throw new IllegalArgumentException("Messaging server name must be specified as non null value");
            }

            this.name = name;
            this.serverName = serverName;
        }

        /**
         * Defines the period (in milliseconds) between client failure check.
         */
        public Builder checkPeriod(long checkPeriod) {
            this.checkPeriod = checkPeriod;
            return this;
        }

        /**
         * Defines the maximum time (in milliseconds) for which the connections
         * used by the bridges are considered alive (in the absence of
         * heartbeat).
         */
        public Builder connectionTtl(long connectionTtl) {
            this.connectionTtl = connectionTtl;
            return this;
        }

        /**
         * The name of the discovery group used by this bridge. Must be
         * undefined (null) if 'static-connectors' is defined.",
         */
        public Builder discoveryGroup(String discoveryGroup) {
            this.discoveryGroup = discoveryGroup;
            return this;
        }

        /**
         * Defines the core source queue of this core bridge.
         */
        public Builder queueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        /**
         * Defines The address on the target server that the message will be
         * forwarded to. If a forwarding address is not specified then the
         * original destination of the message will be retained.
         */
        public Builder forwardingAddress(String forwardingAddress) {
            this.forwardingAddress = forwardingAddress;
            return this;
        }

        /**
         * The period in milliseconds between subsequent reconnection attempts,
         * if the connection to the target server has failed.
         */
        public Builder retryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        /**
         * The maximum interval of time used to retry connections.
         */
        public Builder maxRetryInterval(long maxRetryInterval) {
            this.maxRetryInterval = maxRetryInterval;
            return this;
        }

        /**
         * The minimum size (in bytes) for a message before it is considered as
         * a large message.
         */
        public Builder minLargeMessageSize(int minLargeMessageSize) {
            this.minLargeMessageSize = minLargeMessageSize;
            return this;
        }

        /**
         * The password to use when creating the bridge connection to the remote
         * server. If it is not specified the default cluster password specified
         * by the cluster-password attribute in the root messaging subsystem
         * resource will be used.
         *
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * The user name to use when creating the bridge connection to the
         * remote server. If it is not specified the default cluster user
         * specified by the cluster-user attribute in the root messaging
         * subsystem resource will be used.
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * The total number of reconnect attempts the bridge will make before
         * giving up and shutting down. A value of -1 signifies an unlimited
         * number of attempts.
         */
        public Builder reconnectAttempts(int reconnectAttempts) {
            this.reconnectAttempts = reconnectAttempts;
            return this;
        }

        /**
         * The total number of reconnect attempts on the same node the bridge
         * will make before giving up and shutting down. A value of -1 signifies
         * an unlimited number of attempts.
         */
        public Builder reconnectAttemptsOnSameNode(int reconnectAttemptsOnSameNode) {
            this.reconnectAttemptsOnSameNode = reconnectAttemptsOnSameNode;
            return this;
        }

        /**
         * An optional filter string . If specified then only messages which
         * match the filter expression specified will be forwarded . The filter
         * string follows the ActiveMQ filter expression syntax described in the
         * ActiveMQ documentation
         */
        public Builder filter(String filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Defines the number of attempts to connect initially with this bridge.
         */
        public Builder initialConnectAttempts(int initialConnectAttempts) {
            this.initialConnectAttempts = initialConnectAttempts;
            return this;
        }

        /**
         * A multiplier to apply to the time since the last retry to compute the
         * time to the next retry. This allows you to implement an exponential
         * backoff between retry attempts.
         */
        public Builder retryIntervalMultiplier(BigDecimal retryIntervalMultiplier) {
            this.retryIntervalMultiplier = retryIntervalMultiplier;
            return this;
        }

        /**
         * Defines the confirmation-window-size to use for the connection used
         * to forward messages to the target node.
         */
        public Builder confirmationWindowSize(int confirmationWindowSize) {
            this.confirmationWindowSize = confirmationWindowSize;
            return this;
        }

        /**
         * A list of names of statically defined connectors used by this bridge.
         * Must be undefined (null) if 'discovery-group-name' is defined.
         */
        public Builder staticConnectors(List<String> staticConnectors) {
            this.staticConnectors = staticConnectors;
            return this;
        }

        /**
         * The name of a user-defined class which implements the
         * org.apache.activemq.artemis.core.server.cluster.Transformer
         * interface.
         */
        public Builder transformerClassName(String transformerClassName) {
            this.transformerClassName = transformerClassName;
            return this;
        }

        /**
         * Whether the bridge will automatically insert a duplicate id property
         * into each message that it forwards.
         */
        public Builder useDuplicateDetection() {
            this.useDuplicateDetection = true;
            return this;
        }

        /**
         * Whether or not this bridge should support high availability. True
         * means it will connect to any available server in a cluster and
         * support failover.
         */
        public Builder ha() {
            this.ha = true;
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

        public AddCoreBridge build() {
            check();
            return new AddCoreBridge(this);
        }

        private void check() {
            if (!staticConnectors.isEmpty() && discoveryGroup != null) {
                throw new IllegalArgumentException("Only one of attributes discoveryGroup, statioConnectors can be defined");
            }
            if (queueName == null) {
                throw new IllegalArgumentException("queueName must be defined");
            }
        }
    }
}
