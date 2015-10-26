// closure with XML structure definition
attrsHornet = ['name': name]

filterDef = ['string': filter]

def definitionHornet = {
    'bridge'(attrsHornet) {
        if (nn(queueName)) 'queue-name'(queueName)
        if (nn(forwardingAddress)) 'forwarding-address'(forwardingAddress)
        if (nn(ha)) 'ha' (ha)
        if (nn(filter)) { 'filter' (filterDef) }
        if (nn(minLargeMessageSize)) 'min-large-message-size' (minLargeMessageSize)
        if (nn(checkPeriod)) 'check-period' (checkPeriod)
        if (nn(connectionTtl)) 'connection-ttl' (connectionTtl)
        if (nn(retryInterval)) 'retry-interval'(retryInterval)
        if (nn(retryIntervalMultiplier)) 'retry-interval-multiplier' (retryIntervalMultiplier)
        if (nn(maxRetryInterval)) 'max-retry-interval' (maxRetryInterval)
        if (nn(initialConnectAttempts)) 'initial-connect-attempts' (initialConnectAttempts)
        if (nn(reconnectAttempts)) 'reconnect-attempts' (reconnectAttempts)
        if (nn(reconnectAttemptsOnSameNode)) 'reconnect-attempts-on-same-node' (reconnectAttemptsOnSameNode)
        if (nn(useDuplicateDetection)) 'use-duplicate-detection' (useDuplicateDetection)
        if (nn(user)) 'user' (user)
        if (nn(password)) 'password' (password)
        if (nn(discoveryGroup)) 'discovery-group' (discoveryGroup)
        if (nn(transformerClassName)) 'transformer-class-name' (transformerClassName)
        if (staticConnectors) {
            'static-connectors' {
                staticConnectorsList.each {
                    'connector-ref'(it)
                }
            }
        }
    }
}

attrsArtemis = ['name': name]
if (nn(queueName)) attrsArtemis['queue-name'] = queueName
if (nn(forwardingAddress)) attrsArtemis['forwarding-address'] = forwardingAddress
if (nn(retryInterval)) attrsArtemis['retry-interval'] = retryInterval
if (nn(retryIntervalMultiplier)) attrsArtemis['retry-interval-multiplier'] = retryIntervalMultiplier
if (nn(confirmationWindowSize)) attrsArtemis['confirmation-window-size'] = confirmationWindowSize
if (nn(staticConnectors)) attrsArtemis['static-connectors'] = staticConnectors
if (nn(useDuplicateDetection)) attrsArtemis['use-duplicate-detection'] = useDuplicateDetection
if (nn(ha)) attrsArtemis['ha'] = ha
if (nn(checkPeriod)) attrsArtemis['check-period'] = checkPeriod
if (nn(connectionTtl)) attrsArtemis['connection-ttl'] = connectionTtl
if (nn(discoveryGroup)) attrsArtemis['discovery-group'] = discoveryGroup
if (nn(filter)) attrsArtemis['filter'] = filter
if (nn(initialConnectAttempts)) attrsArtemis['initial-connect-attempts'] = initialConnectAttempts
if (nn(maxRetryInterval)) attrsArtemis['max-retry-interval'] = maxRetryInterval
if (nn(minLargeMessageSize)) attrsArtemis['min-large-message-size'] = minLargeMessageSize
if (nn(password)) attrsArtemis['password'] = password
if (nn(user)) attrsArtemis['user'] = user
if (nn(reconnectAttempts)) attrsArtemis['reconnect-attempts'] = reconnectAttempts
if (nn(reconnectAttemptsOnSameNode)) attrsArtemis['reconnect-attempts-on-same-node'] = reconnectAttemptsOnSameNode
if (nn(transformerClassName)) attrsArtemis['transformer-class-name'] = transformerClassName

def definitionArtemis = {
    'bridge'(attrsArtemis) {}
}

def bridgeAddress = null
if (messagingActivemq) {
    bridgeAddress = messagingActivemq.server.'bridge'
} else if (messagingHornetq) {
    bridgeAddress = messagingHornetq.'hornetq-server'.'bridges'.'bridge'
}
if (!bridgeAddress) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

// adding or replacing existing
def isExisting = bridgeAddress.find { it.@name == name }
if (isExisting && !replaceExisting) {
    throw new IllegalStateException("Bridge $name already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (isExisting) {
    if (messagingHornetq) {
        isExisting.replaceNode definitionHornet
    } else if (messagingActivemq) {
        isExisting.replaceNode definitionArtemis
    }
} else {
    if (messagingHornetq) {
        if ( messagingHornetq.'hornetq-server'.bridges.any { it.name() == 'bridges' } ) {
            messagingHornetq.'hornetq-server'.bridges.appendNode definitionHornet
        } else {
            messagingHornetq.'hornetq-server'.appendNode {
                bridges(definitionHornet)
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
