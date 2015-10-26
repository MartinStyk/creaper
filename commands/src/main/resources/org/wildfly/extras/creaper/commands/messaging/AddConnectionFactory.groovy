attrsArtemis = ['name': name]
if (nn(connectorsString)) attrsArtemis['connectors'] = connectorsString
if (nn(entriesString)) attrsArtemis['entries'] = entriesString
if (nn(discoveryGroup)) attrsArtemis['discovery-group'] = discoveryGroup
if (nn(ha)) attrsArtemis['ha'] = ha
if (nn(clientFailureCheckPeriod)) attrsArtemis['client-failure-check-period'] = clientFailureCheckPeriod
if (nn(connectonTtl)) attrsArtemis['connection-ttl'] = connectonTtl
if (nn(callTimeout)) attrsArtemis['call-timeout'] = callTimeout
if (nn(callFailoverTimeout)) attrsArtemis['call-failover-timeout'] = callFailoverTimeout
if (nn(consumerWindowSize)) attrsArtemis['consumer-window-size'] = consumerWindowSize
if (nn(consumerMaxRate)) attrsArtemis['consumer-max-rate'] = consumerMaxRate
if (nn(confirmationWindowSize)) attrsArtemis['confirmation-window-size'] = confirmationWindowSize
if (nn(producerMaxRate)) attrsArtemis['producer-max-rate'] = producerMaxRate

if (nn(compressLargeMessages)) attrsArtemis['compress-large-messages'] = compressLargeMessages
if (nn(cacheLargeMessageClient)) attrsArtemis['cache-large-message-client'] = cacheLargeMessageClient
if (nn(minLargeMessageSize)) attrsArtemis['min-large-message-size'] = minLargeMessageSize
if (nn(clientId)) attrsArtemis['client-id'] = clientId
if (nn(dupsOkBatchSize)) attrsArtemis['dups-ok-batch-size'] = dupsOkBatchSize

if (nn(transactionBatchSize)) attrsArtemis['transaction-batch-size'] = transactionBatchSize
if (nn(blockOnAcknowledge)) attrsArtemis['block-on-acknowledge'] = blockOnAcknowledge
if (nn(blockOnDurableSend)) attrsArtemis['block-on-durable-send'] = blockOnDurableSend
if (nn(blockOnNonDurableSend)) attrsArtemis['block-on-non-durable-send'] = blockOnNonDurableSend
if (nn(autoGroup)) attrsArtemis['auto-group'] = autoGroup
if (nn(preAcknowledge)) attrsArtemis['pre-acknowledge'] = preAcknowledge
if (nn(retryInterval)) attrsArtemis['retry-interval'] = retryInterval
if (nn(retryIntervalMultiplier)) attrsArtemis['retry-interval-multiplier'] = retryIntervalMultiplier
if (nn(maxRetryInterval)) attrsArtemis['max-retry-interval'] = maxRetryInterval
if (nn(reconnectAttempts)) attrsArtemis['reconnect-attempts'] = reconnectAttempts
if (nn(failoverOnInitialConnection)) attrsArtemis['failover-on-initial-connection'] = failoverOnInitialConnection
if (nn(useGlobalPools)) attrsArtemis['use-global-pools'] = useGlobalPools
if (nn(scheduledThreadPoolMaxSize)) attrsArtemis['scheduled-thread-pool-max-size'] = scheduledThreadPoolMaxSize
if (nn(threadPoolMaxSize)) attrsArtemis['thread-pool-max-size'] = threadPoolMaxSize
if (nn(groupId)) attrsArtemis['group-id'] = groupId
if (nn(factoryType)) attrsArtemis['factory-type'] = factoryType
def definitionArtemis = {
    'connection-factory'(attrsArtemis) {}
}

def address = null
if (messagingActivemq) {
    address = messagingActivemq.server.'connection-factory'
} else if (messagingHornetq) {
    throw new IllegalStateException("Offline operation implemented only for ActiveMQ")
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

// adding or replacing existing
def isExisting = address.find { it.@name == name }
if (isExisting && !replaceExisting) {
    throw new IllegalStateException("Connection factory $name already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (isExisting) {
    isExisting.replaceNode definitionArtemis
} else {
    messagingActivemq.server.appendNode definitionArtemis
}

/**
 * Checking if parameter is not null.
 * We can't use if(object) ... as object could be null or false
 * and we need to differentiate such states
 */
def nn(Object... object) {
    if (object == null) return false
    return object.any { it != null }
}
