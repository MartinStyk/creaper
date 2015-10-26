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
 * Creates new cluster connection in default messaging server.
 */
public final class AddClusterConnection implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String serverName;
    private final Boolean allowDirectConnectionsOnly;
    private final Long callFailoverTimeout;
    private final Long callTimeout;
    private final String clusterConnectionAddress;
    private final Integer confirmationWindowSize;
    private final Long connectionTtl;
    private final Long checkPeriod;
    private final boolean replaceExisting;
    private final String connectorName;
    private final String discoveryGroup;
    private final Integer initialConnectAttempts;
    private final Integer maxHops;
    private final Long maxRetryInterval;
    private final String messageLoadBalancingType;
    private final Integer minLargeMessageSize;
    private final Integer notificationAttempts;
    private final Long notificationInterval;
    private final Integer reconnectAttempts;
    private final Long retryInterval;
    private final BigDecimal retryIntervalMultiplier;
    private final List<String> staticConnectors;
    private final Boolean useDuplicateDetection;

    private AddClusterConnection(Builder builder) {
        this.name = builder.name;
        this.serverName = builder.serverName;
        this.allowDirectConnectionsOnly = builder.allowDirectConnectionsOnly;
        this.callFailoverTimeout = builder.callFailoverTimeout;
        this.callTimeout = builder.callTimeout;
        this.clusterConnectionAddress = builder.clusterConnectionAddress;
        this.confirmationWindowSize = builder.confirmationWindowSize;
        this.connectionTtl = builder.connectionTtl;
        this.checkPeriod = builder.checkPeriod;
        this.replaceExisting = builder.replaceExisting;
        this.connectorName = builder.connectorName;
        this.discoveryGroup = builder.discoveryGroup;
        this.initialConnectAttempts = builder.initialConnectAttempts;
        this.maxHops = builder.maxHops;
        this.maxRetryInterval = builder.maxRetryInterval;
        this.minLargeMessageSize = builder.minLargeMessageSize;
        this.messageLoadBalancingType = builder.messageLoadBalancingType;
        this.notificationAttempts = builder.notificationAttempts;
        this.notificationInterval = builder.notificationInterval;
        this.reconnectAttempts = builder.reconnectAttempts;
        this.retryInterval = builder.retryInterval;
        this.retryIntervalMultiplier = builder.retryIntervalMultiplier;
        this.staticConnectors = builder.staticConnectors;
        this.useDuplicateDetection = builder.useDuplicateDetection;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);

        Address address = MessagingUtils.address(ctx.client, serverName)
                .and(MessagingConstants.CLUSTER_CONNECTION, name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(address);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing cluster connection " + name, e);
            }
        }

        Values values = Values.empty()
                .andOptional("allow-direct-connections-only", allowDirectConnectionsOnly)
                .andOptional("call-failover-timeout", callFailoverTimeout)
                .andOptional("call-timeout", callTimeout)
                .andOptional("check-period", checkPeriod)
                .andOptional("cluster-connection-address", clusterConnectionAddress)
                .andListOptional(String.class, "static-connectors", staticConnectors)
                .andOptional("confirmation-window-size", confirmationWindowSize)
                .andOptional("connection-ttl", connectionTtl)
                .andOptional("connector-name", connectorName)
                .andOptional("connector-ref", connectorName)
                .andOptional("discovery-group", discoveryGroup)
                .andOptional("initial-connect-attempts", initialConnectAttempts)
                .andOptional("max-hops", maxHops)
                .andOptional("max-retry-interval", maxRetryInterval)
                .andOptional("message-load-balancing-type", messageLoadBalancingType)
                .andOptional("min-large-message-size", minLargeMessageSize)
                .andOptional("notification-attempts", notificationAttempts)
                .andOptional("notification-interval", notificationInterval)
                .andOptional("reconnect-attempts", reconnectAttempts)
                .andOptional("retry-interval", retryInterval)
                .andOptional("retry-interval-multiplier", retryIntervalMultiplier)
                .andOptional("use-duplicate-detection", useDuplicateDetection);

        Batch batch = new Batch();
        batch.add(address, values);

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (!MessagingUtils.DEFAULT_SERVER_NAME.equals(serverName)) {
            throw new CommandFailedException("Non-default messaging server name not yet implemented in offline mode");
        }

        List<String> staticConnectorsLocal = staticConnectors.isEmpty() ? null : staticConnectors;

        GroovyXmlTransform transform = GroovyXmlTransform.of(AddClusterConnection.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("name", name)
                .parameter("allowDirectConnectionsOnly", allowDirectConnectionsOnly)
                .parameter("callFailoverTimeout", callFailoverTimeout)
                .parameter("callTimeout", callTimeout)
                .parameter("checkPeriod", checkPeriod)
                .parameter("clusterConnectionAddress", clusterConnectionAddress)
                .parameter("staticConnectors", staticConnectorsLocal)
                .parameter("staticConnectorsString", MessagingUtils.getStringOfEntries(staticConnectors))
                .parameter("confirmationWindowSize", confirmationWindowSize)
                .parameter("connectionTtl", connectionTtl)
                .parameter("connectorName", connectorName)
                .parameter("discoveryGroup", discoveryGroup)
                .parameter("initialConnectAttempts", initialConnectAttempts)
                .parameter("maxHops", maxHops)
                .parameter("maxRetryInterval", maxRetryInterval)
                .parameter("messageLoadBalancingType", messageLoadBalancingType)
                .parameter("minLargeMessageSize", minLargeMessageSize)
                .parameter("notificationAttempts", notificationAttempts)
                .parameter("notificationInterval", notificationInterval)
                .parameter("reconnectAttempts", reconnectAttempts)
                .parameter("retryInterval", retryInterval)
                .parameter("retryIntervalMultiplier", retryIntervalMultiplier)
                .parameter("useDuplicateDetection", useDuplicateDetection)
                .parameter("replaceExisting", replaceExisting)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "AddClusterConnection " + name;
    }

    public static final class Builder {

        private final String name;
        private final String serverName;
        private Boolean allowDirectConnectionsOnly;
        private Long callFailoverTimeout;
        private Long callTimeout;
        private String clusterConnectionAddress;
        private Integer confirmationWindowSize;
        private Long connectionTtl;
        private Long checkPeriod;
        private boolean replaceExisting;
        private String connectorName;
        private String discoveryGroup;
        private Integer initialConnectAttempts;
        private Integer maxHops;
        private Long maxRetryInterval;
        private String messageLoadBalancingType;
        private Integer minLargeMessageSize;
        private Integer notificationAttempts;
        private Long notificationInterval;
        private Integer reconnectAttempts;
        private Long retryInterval;
        private BigDecimal retryIntervalMultiplier;
        private List<String> staticConnectors = new ArrayList<String>();
        private Boolean useDuplicateDetection;

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
         * Whether, if a node learns of the existence of a node that is more
         * than 1 hop away, we do not create a bridge for direct cluster
         * connection. Only relevant if 'static-connectors' is defined.
         */
        public Builder allowDirectConnectionsOnly() {
            this.allowDirectConnectionsOnly = true;
            return this;
        }

        /**
         * The timeout to use when fail over is in process (in ms) for remote
         * calls made by the cluster connection.
         */
        public Builder callFailoverTimeout(long callFailoverTimeout) {
            this.callFailoverTimeout = callFailoverTimeout;
            return this;
        }

        /**
         * The timeout (in ms) for remote calls made by the cluster connection.
         */
        public Builder callTimeout(long callTimeout) {
            this.callTimeout = callTimeout;
            return this;
        }

        /**
         * The period (in milliseconds) between client failure check.
         */
        public Builder checkPeriod(long checkPeriod) {
            this.checkPeriod = checkPeriod;
            return this;
        }

        /**
         * Each cluster connection only applies to messages sent to an address
         * that starts with this value.
         */
        public Builder clusterConnectionAddress(String clusterConnectionAddress) {
            this.clusterConnectionAddress = clusterConnectionAddress;
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
         * The maximum time (in milliseconds) for which the connections used by
         * the cluster connections are considered alive (in the absence of
         * heartbeat).
         */
        public Builder connectionTtl(long connectionTtl) {
            this.connectionTtl = connectionTtl;
            return this;
        }

        /**
         * The name of connector to use for live connection.
         */
        public Builder connectorName(String connectorName) {
            this.connectorName = connectorName;
            return this;
        }

        /**
         * The discovery group used to obtain the list of other servers in the
         * cluster to which this cluster connection will make connections. Must
         * be undefined (null) if 'static-connectors' is defined.
         */
        public Builder discoveryGroup(String discoveryGroup) {
            this.discoveryGroup = discoveryGroup;
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
         * The maximum number of times a message can be forwarded. ActiveMQ can
         * be configured to also load balance messages to nodes which might be
         * connected to it only indirectly with other ActiveMQ servers as
         * intermediates in a chain.
         */
        public Builder maxHops(int maxHops) {
            this.maxHops = maxHops;
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
         * The type of message load balancing provided by the cluster
         * connection. "OFF", "STRICT", "ON_DEMAND"
         *
         */
        public Builder messageLoadBalancingType(String messageLoadBalancingType) {
            this.messageLoadBalancingType = messageLoadBalancingType;
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
         * The total number of reconnect attempts the bridge will make before
         * giving up and shutting down. A value of -1 signifies an unlimited
         * number of attempts.
         */
        public Builder reconnectAttempts(int reconnectAttempts) {
            this.reconnectAttempts = reconnectAttempts;
            return this;
        }

        /**
         * How many times the cluster connection will broadcast itself.
         */
        public Builder notificationAttempts(int notificationAttempts) {
            this.notificationAttempts = notificationAttempts;
            return this;
        }

        /**
         * How often the cluster connection will broadcast itself.
         */
        public Builder notificationInterval(long notificationInterval) {
            this.notificationInterval = notificationInterval;
            return this;
        }

        /**
         * The period in milliseconds between subsequent attempts to reconnect
         * to a target server, if the connection to the target server has
         * failed.
         */
        public Builder retryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
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
         * A list of names of statically defined connectors used by this bridge.
         * Must be undefined (null) if 'discovery-group-name' is defined.
         */
        public Builder staticConnectors(List<String> staticConnectors) {
            this.staticConnectors = staticConnectors;
            return this;
        }

        /**
         * Whether automatically insert a duplicate id property into each
         * message that it forwards.
         */
        public Builder useDuplicateDetection(boolean useDuplicateDetection) {
            this.useDuplicateDetection = useDuplicateDetection;
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

        public AddClusterConnection build() {
            check();
            return new AddClusterConnection(this);
        }

        private void check() {
            if (!staticConnectors.isEmpty() && discoveryGroup != null) {
                throw new IllegalArgumentException("Only one of attributes discoveryGroup, statioConnectors can be defined");
            }
            if (connectorName == null) {
                throw new IllegalArgumentException("connectorName must be defined");
            }
            if (clusterConnectionAddress == null) {
                throw new IllegalArgumentException("clusterConnectionAddress must be defined");
            }
        }
    }
}
