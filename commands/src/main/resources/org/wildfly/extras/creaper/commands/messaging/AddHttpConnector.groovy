// closure with XML structure definition
attrsHornet = ['name': name]
if (nn(socketbinding)) attrsHornet['socket-binding'] = socketbinding

def definitionHornet = {
    'http-connector'(attrsHornet) {
        if (nn(params)) {
            params.each {
                'param'(key: it.key, value: it.value)
            }
        }
    }
}

attrsArtemis = ['name': name]
if (nn(socketbinding)) attrsArtemis['socket-binding'] = socketbinding
if (nn(endpoint)) attrsArtemis['endpoint'] = endpoint

def definitionArtemis = {
    'http-connector'(attrsArtemis) {
        if (nn(params)) {
            params.each {
                'param'(name: it.key, value: it.value)
            }
        }
    }
}

def connectorAddress = null
if (messagingActivemq) {
    connectorAddress = messagingActivemq.server.'http-connector'
} else if (messagingHornetq) {
    connectorAddress = messagingHornetq.'hornetq-server'.'connectors'.'http-connector'
}
if (!connectorAddress) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

// adding or replacing existing
def isExistingConnector = connectorAddress.find { it.@name == name }
if (isExistingConnector && !replaceExisting) {
    throw new IllegalStateException("Connector $name already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (isExistingConnector) {
    if (messagingHornetq) {
        isExistingConnector.replaceNode definitionHornet
    } else if (messagingActivemq) {
        isExistingConnector.replaceNode definitionArtemis
    }
} else {
    if (messagingHornetq) {
        if ( messagingHornetq.'hornetq-server'.'connectors'.any { it.name() == 'connectors' } ) {
            messagingHornetq.'hornetq-server'.'connectors'.appendNode definitionHornet
        } else {
            messagingHornetq.'hornetq-server'.appendNode {
                connectors(definitionHornet)
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
