def definitionHornet = {
    'cluster-connection'(name: name) {
        if (nn(clusterConnectionAddress)) 'address'(clusterConnectionAddress)
        if (nn(connectorName)) 'connector-ref'(connectorName)
        if (nn(checkPeriod)) 'check-period' (checkPeriod)
        if (nn(discoveryGroup)) 'discovery-group' (discoveryGroup)
        if (nn(connectionTtl)) { 'connection-ttl' (connectionTtl) }
        if (nn(minLargeMessageSize)) 'min-large-message-size' (minLargeMessageSize)
        if (nn(callTimeout)) 'call-timeout' (callTimeout)
        if (nn(callFailoverTimeout)) 'call-failover-timeout' (callFailoverTimeout)
        if (nn(retryInterval)) 'retry-interval'(retryInterval)
        if (nn(retryIntervalMultiplier)) 'retry-interval-multiplier' (retryIntervalMultiplier)
        if (nn(maxRetryInterval)) 'max-retry-interval' (maxRetryInterval)
        if (nn(messageLoadBalancingType)) 'message-load-balancing-type' (messageLoadBalancingType)
        if (nn(initialConnectAttempts)) 'initial-connect-attempts' (initialConnectAttempts)
        if (nn(reconnectAttempts)) 'reconnect-attempts' (reconnectAttempts)
        if (nn(useDuplicateDetection)) 'use-duplicate-detection' (useDuplicateDetection)
        if (nn(maxHops)) 'max-hops' (maxHops)
        if (nn(confirmationWindowSize)) 'confirmation-window-size' (confirmationWindowSize)
        if (nn(notificationAttempts)) 'notification-attempts' (notificationAttempts)
        if (nn(notificationInterval)) 'notification-interval' (notificationInterval)
        if (nn(allowDirectConnectionsOnly)) {
            if (nn(staticConnectors)) {
            'static-connectors' ('allow-direct-connections-only': allowDirectConnectionsOnly) {
                    staticConnectors.each { 'connector-ref'(it) }
                }
            }
        } else {
            if (nn(staticConnectors)) {
            'static-connectors' {
                    staticConnectors.each { 'connector-ref'(it) }
                }
            }
        }
    }
}

attrsArtemis = ['name': name]
if (nn(clusterConnectionAddress)) attrsArtemis['address'] = clusterConnectionAddress
if (nn(connectorName)) attrsArtemis['connector-name'] = connectorName
if (nn(checkPeriod)) attrsArtemis['check-period'] = checkPeriod
if (nn(discoveryGroup)) attrsArtemis['discovery-group'] = discoveryGroup
if (nn(connectionTtl)) attrsArtemis['connection-ttl'] = connectionTtl
if (nn(minLargeMessageSize)) attrsArtemis['min-large-message-size'] = minLargeMessageSize
if (nn(callTimeout)) attrsArtemis['call-timeout'] = callTimeout
if (nn(callFailoverTimeout)) attrsArtemis['call-failover-timeout'] = callFailoverTimeout
if (nn(retryInterval)) attrsArtemis['retry-interval'] = retryInterval
if (nn(retryIntervalMultiplier)) attrsArtemis['retry-interval-multiplier'] = retryIntervalMultiplier
if (nn(maxRetryInterval)) attrsArtemis['max-retry-interval'] = maxRetryInterval
if (nn(messageLoadBalancingType)) attrsArtemis['message-load-balancing-type'] = messageLoadBalancingType
if (nn(initialConnectAttempts)) attrsArtemis['initial-connect-attempts'] = initialConnectAttempts
if (nn(reconnectAttempts)) attrsArtemis['reconnect-attempts'] = reconnectAttempts
if (nn(useDuplicateDetection)) attrsArtemis['use-duplicate-detection'] = useDuplicateDetection
if (nn(maxHops)) attrsArtemis['max-hops'] = maxHops
if (nn(confirmationWindowSize)) attrsArtemis['confirmation-window-size'] = confirmationWindowSize
if (nn(notificationAttempts)) attrsArtemis['notification-attempts'] = notificationAttempts
if (nn(notificationInterval)) attrsArtemis['notification-interval'] = notificationInterval
if (nn(staticConnectorsString)) attrsArtemis['static-connectors'] = staticConnectorsString
if (nn(allowDirectConnectionsOnly)) attrsArtemis['allow-direct-connections-only'] = allowDirectConnectionsOnly

def definitionArtemis = {
    'cluster-connection'(attrsArtemis) {}
}

def address = null
if (messagingActivemq) {
    address = messagingActivemq.server.'cluster-connection'
} else if (messagingHornetq) {
    address = messagingHornetq.'hornetq-server'.'cluster-connections'.'cluster-connection'
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

// adding or replacing existing
def isExisting = address.find { it.@name == name }
if (isExisting && !replaceExisting) {
    throw new IllegalStateException("Cluster connection $name already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (isExisting) {
    if (messagingHornetq) {
        isExisting.replaceNode definitionHornet
    } else if (messagingActivemq) {
        isExisting.replaceNode definitionArtemis
    }
} else {
    if (messagingHornetq) {
        if ( messagingHornetq.'hornetq-server'.'cluster-connections'.any { it.name() == 'cluster-connections' } ) {
            messagingHornetq.'hornetq-server'.'cluster-connections'.appendNode definitionHornet
        } else {
            messagingHornetq.'hornetq-server'.appendNode {
                'cluster-connections'(definitionHornet)
            }
        }
    } else if (messagingActivemq) {
        messagingActivemq.server.appendNode definitionArtemis
    }
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
