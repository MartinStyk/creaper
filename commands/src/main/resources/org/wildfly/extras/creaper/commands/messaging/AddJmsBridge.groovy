// definition hornetq
def definitionHornet = {
    'jms-bridge'(name: bridgeName) {
        source {
            if (nn(sourceConnectionFactory)) 'connection-factory' (name: sourceConnectionFactory)
            if (nn(sourceDestination)) destination (name: sourceDestination)
            if (nn(sourceUser)) user (sourceUser)
            if (nn(sourcePassword)) password (sourcePassword)
            if (nn(sourceContext)) {
                context {
                    sourceContext.each {
            'property'(key: it.key, value: it.value)
                    }
                }
            }
        }
        target {
            if (nn(targetConnectionFactory)) 'connection-factory' (name: targetConnectionFactory)
            if (nn(targetDestination)) destination (name: targetDestination)
            if (nn(targetUser)) user (targetUser)
            if (nn(targetPassword)) password (targetPassword)
            if (nn(targetContext)) {
                context {
                    targetContext.each {
            'property'(key: it.key, value: it.value)
                    }
                }
            }
        }
        if (nn(qualityOfService)) 'quality-of-service' (qualityOfService)
        if (nn(failureRetryInterval)) 'failure-retry-interval' (failureRetryInterval)
        if (nn(module)) 'module' (module)
        if (nn(maxRetries)) 'max-retries' (maxRetries)
        if (nn(maxBatchSize)) 'max-batch-size' (maxBatchSize)
        if (nn(maxBatchTime)) 'max-batch-time' (maxBatchTime)
        if (nn(selector)) { 'selector' (string: selector) }
        if (nn(subscriptionName)) 'subscription-name' (subscriptionName)
        if (nn(clientId)) 'client-id' (clientId)
        if (nn(addMessageIdInHeader)) 'add-messageID-in-header' (addMessageIdInHeader)
    }
}

attrs = [name: bridgeName]
if (nn(qualityOfService)) attrs['quality-of-service'] = qualityOfService
if (nn(failureRetryInterval)) attrs['failure-retry-interval'] = failureRetryInterval
if (nn(maxRetries)) attrs['max-retries'] = maxRetries
if (nn(maxBatchSize)) attrs['max-batch-size'] = maxBatchSize
if (nn(maxBatchTime)) attrs['max-batch-time'] = maxBatchTime
if (nn(maxRetries)) attrs['max-retries'] = maxRetries
if (nn(selector)) attrs['selector'] = selector
if (nn(subscriptionName)) attrs['subscription-name'] = subscriptionName
if (nn(clientId)) attrs['client-id'] = clientId
if (nn(module)) attrs['module'] = module
if (nn(addMessageIdInHeader)) attrs['add-messageID-in-header'] = addMessageIdInHeader

sourceAttrs = ['connection-factory': sourceConnectionFactory]
if (nn(sourceDestination)) sourceAttrs['destination'] = sourceDestination
if (nn(sourceUser)) sourceAttrs['user'] = sourceUser
if (nn(sourcePassword)) sourceAttrs['password'] = sourcePassword

targetAttrs = ['connection-factory': targetConnectionFactory]
if (nn(targetDestination)) targetAttrs['destination'] = targetDestination
if (nn(targetUser)) targetAttrs['user'] = targetUser
if (nn(targetPassword)) targetAttrs['password'] = targetPassword

def definitionArtemis = {
    'jms-bridge'(attrs) {
        if (nn(sourceContext)) {
            source(sourceAttrs) {
            'source-context' {
                    sourceContext.each {
            'property'(name: it.key, value: it.value)
                    }
                }
            }
        } else {
            source(sourceAttrs)
        }
        if (nn(targetContext)) {
            target(targetAttrs) {
            'target-context' {
                    targetContext.each {
            'property'(name: it.key, value: it.value)
                    }
                }
            }
        } else {
            target(targetAttrs)
        }
    }
}

def resourceDefinition = null
def bridgeAddress = null
if (messagingActivemq) {
    bridgeAddress = messagingActivemq.'jms-bridge'
    resourceDefinition = definitionArtemis
} else if (messagingHornetq) {
    bridgeAddress = messagingHornetq.'jms-bridge'
    resourceDefinition = definitionHornet
}
if (!bridgeAddress) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

// adding or replacing existing
def isExisting = bridgeAddress.find { it.@name == bridgeName }
if (isExisting && !replaceExisting) {
    throw new IllegalStateException("Bridge $name already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (isExisting) {
    isExisting.replaceNode resourceDefinition
}
else {
    if (messagingHornetq) {
        messagingHornetq.appendNode resourceDefinition
    } else if (messagingActivemq) {
        messagingActivemq.appendNode resourceDefinition
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
