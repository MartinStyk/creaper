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
 *
 * @author mstyk
 */
public class AddConnectionFactory implements OfflineCommand, OnlineCommand {

    private final String name;
    private final String serverName;
    private final boolean replaceExisting;
    private final Boolean autoGroup;
    private final Boolean blockOnAcknowledge;
    private final Boolean blockOnDurableSend;
    private final Boolean blockOnNonDurableSend;
    private final Boolean cacheLargeMessageClient;
    private final Long callFailoverTimeout;
    private final Long callTimeout;
    private final Long clientFailureCheckPeriod;
    private final String clientId;
    private final Boolean compressLargeMessages;
    private final Integer confirmationWindowSize;
    private final String connectionLoadBalancingPolicyClassName;
    private final Long connectonTtl;
    private final List<String> connectors;
    private final Integer consumerMaxRate;
    private final Integer consumerWindowSize;
    private final String discoveryGroup;
    private final Integer dupsOkBatchSize;
    private final List<String> entries;
    private final String factoryType;
    private final Boolean failoverOnInitialConnection;
    private final String groupId;
    private final Boolean ha;
    private final Long maxRetryInterval;
    private final Integer minLargeMessageSize;
    private final Boolean preAcknowledge;
    private final Integer producerMaxRate;
    private final Integer producerWindowSize;
    private final Integer reconnectAttempts;
    private final Long retryInterval;
    private final BigDecimal retryIntervalMultiplier;
    private final Integer scheduledThreadPoolMaxSize;
    private final Integer threadPoolMaxSize;
    private final Integer transactionBatchSize;
    private final Boolean useGlobalPools;

    private AddConnectionFactory(Builder builder) {
        this.name = builder.name;
        this.serverName = builder.serverName;
        this.replaceExisting = builder.replaceExisting;
        this.autoGroup = builder.autoGroup;
        this.blockOnAcknowledge = builder.blockOnAcknowledge;
        this.blockOnDurableSend = builder.blockOnDurableSend;
        this.blockOnNonDurableSend = builder.blockOnNonDurableSend;
        this.cacheLargeMessageClient = builder.cacheLargeMessageClient;
        this.callFailoverTimeout = builder.callFailoverTimeout;
        this.callTimeout = builder.callTimeout;
        this.clientFailureCheckPeriod = builder.clientFailureCheckPeriod;
        this.clientId = builder.clientId;
        this.compressLargeMessages = builder.compressLargeMessages;
        this.confirmationWindowSize = builder.confirmationWindowSize;
        this.connectionLoadBalancingPolicyClassName = builder.connectionLoadBalancingPolicyClassName;
        this.connectonTtl = builder.connectonTtl;
        this.connectors = builder.connectors;
        this.consumerMaxRate = builder.consumerMaxRate;
        this.consumerWindowSize = builder.consumerWindowSize;
        this.discoveryGroup = builder.discoveryGroup;
        this.dupsOkBatchSize = builder.dupsOkBatchSize;
        this.entries = builder.entries;
        this.factoryType = builder.factoryType;
        this.failoverOnInitialConnection = builder.failoverOnInitialConnection;
        this.groupId = builder.groupId;
        this.ha = builder.ha;
        this.maxRetryInterval = builder.maxRetryInterval;
        this.minLargeMessageSize = builder.minLargeMessageSize;
        this.preAcknowledge = builder.preAcknowledge;
        this.producerMaxRate = builder.producerMaxRate;
        this.producerWindowSize = builder.producerWindowSize;
        this.reconnectAttempts = builder.reconnectAttempts;
        this.retryInterval = builder.retryInterval;
        this.retryIntervalMultiplier = builder.retryIntervalMultiplier;
        this.scheduledThreadPoolMaxSize = builder.scheduledThreadPoolMaxSize;
        this.threadPoolMaxSize = builder.threadPoolMaxSize;
        this.transactionBatchSize = builder.transactionBatchSize;
        this.useGlobalPools = builder.useGlobalPools;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException, CommandFailedException {
        Operations ops = new Operations(ctx.client);

        Address connectorAddress = MessagingUtils.address(ctx.client, serverName)
                .and(MessagingConstants.CONNECTION_FACTORY, name);

        if (replaceExisting) {
            try {
                ops.removeIfExists(connectorAddress);
            } catch (OperationException e) {
                throw new CommandFailedException("Failed to remove existing connection factory " + name, e);
            }
        }

        Values values = Values.empty()
                .andOptional("auto-group", autoGroup)
                .andOptional("block-on-acknowledge", blockOnAcknowledge)
                .andOptional("block-on-durable-send", blockOnDurableSend)
                .andOptional("block-on-non-durable-send", blockOnNonDurableSend)
                .andOptional("cache-large-message-client", cacheLargeMessageClient)
                .andOptional("call-failover-timeout", callFailoverTimeout)
                .andOptional("call-timeout", callTimeout)
                .andOptional("client-failure-check-period", clientFailureCheckPeriod)
                .andOptional("client-id", clientId)
                .andOptional("compress-large-messages", compressLargeMessages)
                .andOptional("confirmation-window-size", confirmationWindowSize)
                .andOptional("connection-load-balancing-policy-class-name", connectionLoadBalancingPolicyClassName)
                .andOptional("connection-ttl", connectonTtl)
                .andListOptional(String.class, "connectors", connectors)
                .andOptional("consumer-max-rate", consumerMaxRate)
                .andOptional("consumer-window-size", consumerWindowSize)
                .andOptional("discovery-group", discoveryGroup)
                .andOptional("dups-ok-batch-size", dupsOkBatchSize)
                .andListOptional(String.class, "entries", entries)
                .andOptional("factory-type", factoryType)
                .andOptional("failover-on-initial-connection", failoverOnInitialConnection)
                .andOptional("group-id", groupId)
                .andOptional("ha", ha)
                .andOptional("max-retry-interval", maxRetryInterval)
                .andOptional("min-large-message-size", minLargeMessageSize)
                .andOptional("pre-acknowledge", preAcknowledge)
                .andOptional("producer-max-rate", producerMaxRate)
                .andOptional("producer-window-size", producerWindowSize)
                .andOptional("reconnect-attempts", reconnectAttempts)
                .andOptional("retry-interval", retryInterval)
                .andOptional("retry-interval-multiplier", retryIntervalMultiplier)
                .andOptional("scheduled-thread-pool-max-size", scheduledThreadPoolMaxSize)
                .andOptional("thread-pool-max-size", threadPoolMaxSize)
                .andOptional("transaction-batch-size", transactionBatchSize)
                .andOptional("use-global-pools", useGlobalPools);

        Batch batch = new Batch();
        batch.add(connectorAddress, values);

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        if (!MessagingUtils.DEFAULT_SERVER_NAME.equals(serverName)) {
            throw new CommandFailedException("Non-default messaging server name not yet implemented in offline mode");
        }

        GroovyXmlTransform transform = GroovyXmlTransform.of(AddConnectionFactory.class)
                .subtree("messagingHornetq", Subtree.subsystem("messaging"))
                .subtree("messagingActivemq", Subtree.subsystem("messaging-activemq"))
                .parameter("name", name)
                .parameter("replaceExisting", replaceExisting)
                .parameter("autoGroup", autoGroup)
                .parameter("blockOnAcknowledge", blockOnAcknowledge)
                .parameter("blockOnDurableSend", blockOnDurableSend)
                .parameter("blockOnNonDurableSend", blockOnNonDurableSend)
                .parameter("cacheLargeMessageClient", cacheLargeMessageClient)
                .parameter("callFailoverTimeout", callFailoverTimeout)
                .parameter("callTimeout", callTimeout)
                .parameter("clientFailureCheckPeriod", clientFailureCheckPeriod)
                .parameter("clientId", clientId)
                .parameter("compressLargeMessages", compressLargeMessages)
                .parameter("confirmationWindowSize", confirmationWindowSize)
                .parameter("connectionLoadBalancingPolicyClassName", connectionLoadBalancingPolicyClassName)
                .parameter("connectonTtl", connectonTtl)
                .parameter("connectorsString", MessagingUtils.getStringOfEntries(connectors))
                .parameter("consumerMaxRate", consumerMaxRate)
                .parameter("consumerWindowSize", consumerWindowSize)
                .parameter("discoveryGroup", discoveryGroup)
                .parameter("dupsOkBatchSize", dupsOkBatchSize)
                .parameter("entriesString", MessagingUtils.getStringOfEntries(entries))
                .parameter("factoryType", factoryType)
                .parameter("failoverOnInitialConnection", failoverOnInitialConnection)
                .parameter("groupId", groupId)
                .parameter("ha", ha)
                .parameter("maxRetryInterval", maxRetryInterval)
                .parameter("minLargeMessageSize", minLargeMessageSize)
                .parameter("preAcknowledge", preAcknowledge)
                .parameter("producerMaxRate", producerMaxRate)
                .parameter("producerWindowSize", producerWindowSize)
                .parameter("reconnectAttempts", reconnectAttempts)
                .parameter("retryInterval", retryInterval)
                .parameter("retryIntervalMultiplier", retryIntervalMultiplier)
                .parameter("scheduledThreadPoolMaxSize", scheduledThreadPoolMaxSize)
                .parameter("threadPoolMaxSize", threadPoolMaxSize)
                .parameter("transactionBatchSize", transactionBatchSize)
                .parameter("useGlobalPools", useGlobalPools)
                .build();

        ctx.client.apply(transform);
    }

    @Override
    public String toString() {
        return "AddConnectionFactory " + name;
    }

    public static final class Builder {

        private final String name;
        private final String serverName;
        private boolean replaceExisting;
        private Boolean autoGroup;
        private Boolean blockOnAcknowledge;
        private Boolean blockOnDurableSend;
        private Boolean blockOnNonDurableSend;
        private Boolean cacheLargeMessageClient;
        private Long callFailoverTimeout;
        private Long callTimeout;
        private Long clientFailureCheckPeriod;
        private String clientId;
        private Boolean compressLargeMessages;
        private Integer confirmationWindowSize;
        private String connectionLoadBalancingPolicyClassName;
        private Long connectonTtl;
        private List<String> connectors = new ArrayList<String>();
        private Integer consumerMaxRate;
        private Integer consumerWindowSize;
        private String discoveryGroup;
        private Integer dupsOkBatchSize;
        private List<String> entries = new ArrayList<String>();
        private String factoryType;
        private Boolean failoverOnInitialConnection;
        private String groupId;
        private Boolean ha;
        private Long maxRetryInterval;
        private Integer minLargeMessageSize;
        private Boolean preAcknowledge;
        private Integer producerMaxRate;
        private Integer producerWindowSize;
        private Integer reconnectAttempts;
        private Long retryInterval;
        private BigDecimal retryIntervalMultiplier;
        private Integer scheduledThreadPoolMaxSize;
        private Integer threadPoolMaxSize;
        private Integer transactionBatchSize;
        private Boolean useGlobalPools;

        /**
         * Adds a connection factory to the default messaging server.
         * <b>NOT YET IMPLEMENTED FOR HORNETQ!</b>
         *
         * @param name name of the factory
         */
        public Builder(String name) {
            this(name, MessagingUtils.DEFAULT_SERVER_NAME);
        }

        /**
         * Adds a connection factory to the specified messaging server. <b>NOT
         * YET IMPLEMENTED FOR OFFLINE!</b>
         * <b>NOT YET IMPLEMENTED FOR HORNETQ!</b>
         *
         * @param name name of the conenciton factory
         * @param serverName name of the messaging server
         */
        public Builder(String name, String serverName) {
            if (name == null) {
                throw new IllegalArgumentException("Conneciton factory name must be specified as non null value");
            }
            if (serverName == null) {
                throw new IllegalArgumentException("Messaging server name must be specified as non null value");
            }

            this.name = name;
            this.serverName = serverName;
        }

        /**
         * Whether or not message grouping is automatically used.
         */
        public Builder autoGroup() {
            this.autoGroup = true;
            return this;
        }

        /**
         * True to set block on acknowledge.
         */
        public Builder blockOnAcknowledge() {
            this.blockOnAcknowledge = true;
            return this;
        }

        /**
         * True to set block on durable send.
         */
        public Builder blockOnDurableSend(boolean blockOnDurableSend) {
            this.blockOnDurableSend = blockOnDurableSend;
            return this;
        }

        /**
         * True to set block on non durable send.
         */
        public Builder blockOnNonDurableSend() {
            this.blockOnNonDurableSend = true;
            return this;
        }

        /**
         * True to cache large messages.
         */
        public Builder cacheLargeMessageClient() {
            this.cacheLargeMessageClient = true;
            return this;
        }

        /**
         * The timeout to use when fail over is in process (in ms).
         */
        public Builder callFailoverTimeout(long callFailoverTimeout) {
            this.callFailoverTimeout = callFailoverTimeout;
            return this;
        }

        /**
         * The call timeout.
         */
        public Builder callTimeout(long callTimeout) {
            this.callTimeout = callTimeout;
            return this;
        }

        /**
         * The client failure check period.
         */
        public Builder clientFailureCheckPeriod(long clientFailureCheckPeriod) {
            this.clientFailureCheckPeriod = clientFailureCheckPeriod;
            return this;
        }

        /**
         * The client id.
         */
        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Whether large messages should be compressed.
         */
        public Builder compressLargeMessages() {
            this.compressLargeMessages = true;
            return this;
        }

        /**
         * The confirmation window size.
         */
        public Builder confirmationWindowSize(int confirmationWindowSize) {
            this.confirmationWindowSize = confirmationWindowSize;
            return this;
        }

        /**
         * Name of a class implementing a client-side load balancing policy that
         * a client can use to load balance sessions across different nodes in a
         * cluster.
         */
        public Builder connectionLoadBalancingPolicyClassName(String connectionLoadBalancingPolicyClassName) {
            this.connectionLoadBalancingPolicyClassName = connectionLoadBalancingPolicyClassName;
            return this;
        }

        /**
         * The connection ttl.
         */
        public Builder connectonTtl(long connectonTtl) {
            this.connectonTtl = connectonTtl;
            return this;
        }

        /**
         * Defines the connectors. These are stored in a map by connector name
         * (with an undefined value). It is possible to pass a list of connector
         * names when writing this attribute.
         */
        public Builder connectors(List<String> connectors) {
            this.connectors = connectors;
            return this;
        }

        /**
         * The consumer max rate.
         */
        public Builder consumerMaxRate(int consumerMaxRate) {
            this.consumerMaxRate = consumerMaxRate;
            return this;
        }

        /**
         * The consumer window rate.
         */
        public Builder consumerWindowSize(int consumerWindowSize) {
            this.consumerWindowSize = consumerWindowSize;
            return this;
        }

        /**
         * The discovery group name.
         */
        public Builder discoveryGroup(String discoveryGroup) {
            this.discoveryGroup = discoveryGroup;
            return this;
        }

        /**
         * The dups ok batch size.
         */
        public Builder dupsOkBatchSize(int dupsOkBatchSize) {
            this.dupsOkBatchSize = dupsOkBatchSize;
            return this;
        }

        /**
         * The jndi names the connection factory should be bound to.
         */
        public Builder entries(List<String> entries) {
            this.entries = entries;
            return this;
        }

        /**
         * The type of connection factory. Allowed : "GENERIC", "TOPIC",
         * "QUEUE", "XA_GENERIC", "XA_QUEUE", "XA_TOPIC"
         */
        public Builder factoryType(String factoryType) {
            this.factoryType = factoryType;
            return this;
        }

        /**
         * True to fail over on initial connection.
         */
        public Builder failoverOnInitialConnection() {
            this.failoverOnInitialConnection = true;
            return this;
        }

        /**
         * The group id.
         */
        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        /**
         * Whether the connection factory supports High Availability.
         */
        public Builder ha() {
            this.ha = true;
            return this;
        }

        /**
         * The max retry interval.
         */
        public Builder maxRetryInterval(long maxRetryInterval) {
            this.maxRetryInterval = maxRetryInterval;
            return this;
        }

        /**
         * The min large message size.
         */
        public Builder minLargeMessageSize(int minLargeMessageSize) {
            this.minLargeMessageSize = minLargeMessageSize;
            return this;
        }

        /**
         * True to pre-acknowledge.
         */
        public Builder preAcknowledge() {
            this.preAcknowledge = true;
            return this;
        }

        /**
         * The producer max rate.
         */
        public Builder producerMaxRate(int producerMaxRate) {
            this.producerMaxRate = producerMaxRate;
            return this;
        }

        /**
         * The producer window size.
         */
        public Builder producerWindowSize(int producerWindowSize) {
            this.producerWindowSize = producerWindowSize;
            return this;
        }

        /**
         * The reconnect attempts.
         */
        public Builder reconnectAttempts(int reconnectAttempts) {
            this.reconnectAttempts = reconnectAttempts;
            return this;
        }

        /**
         * The retry interval.
         */
        public Builder retryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        /**
         * The retry interval multiplier.
         */
        public Builder retryIntervalMultiplier(BigDecimal retryIntervalMultiplier) {
            this.retryIntervalMultiplier = retryIntervalMultiplier;
            return this;
        }

        /**
         * The scheduled thread pool max size.
         */
        public Builder scheduledThreadPoolMaxSize(int scheduledThreadPoolMaxSize) {
            this.scheduledThreadPoolMaxSize = scheduledThreadPoolMaxSize;
            return this;
        }

        /**
         * The thread pool max size.
         */
        public Builder threadPoolMaxSize(int threadPoolMaxSize) {
            this.threadPoolMaxSize = threadPoolMaxSize;
            return this;
        }

        /**
         * The transaction batch size.
         */
        public Builder transactionBatchSize(int transactionBatchSize) {
            this.transactionBatchSize = transactionBatchSize;
            return this;
        }

        /**
         * True to use global pools.
         */
        public Builder useGlobalPools(boolean useGlobalPools) {
            this.useGlobalPools = useGlobalPools;
            return this;
        }

        /**
         * Specify whether to replace the existing http connector based on its
         * name. By default existing connector is not replaced and exception is
         * thrown.
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddConnectionFactory build() {
            check();
            return new AddConnectionFactory(this);
        }

        private void check() {
            if (entries == null || entries.isEmpty()) {
                throw new IllegalArgumentException("Jndi entries needs to be specified for connection factory");
            }
            if ((connectors != null && !connectors.isEmpty()) && discoveryGroup != null) {
                throw new IllegalArgumentException("Operation cannot include both parameter connectors and parameter discovery-group");
            }
        }
    }
}
