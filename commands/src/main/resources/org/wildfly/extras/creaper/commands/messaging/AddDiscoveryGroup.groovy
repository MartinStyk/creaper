// closure with XML structure definition
attrsHornet = ['name': name]

def definitionHornet = {
    'discovery-group'(attrsHornet) {
        if (nn(socketBinding)) 'socket-binding'(socketBinding)
        if (nn(jGroupsStack)) 'jgroups-stack' (jGroupsStack)
        if (nn(jGroupsChannel)) 'jgroups-channel' (jGroupsChannel)
        if (nn(refreshTimeout)) 'refresh-timeout'(refreshTimeout)
        if (nn(initialWaitTimeout)) 'initial-wait-timeout'(initialWaitTimeout)
    }
}

attrsArtemis = ['name': name]
if (nn(socketBinding)) attrsArtemis['socket-binding'] = socketBinding
if (nn(jGroupsStack)) attrsArtemis['jgroups-stack'] = jGroupsStack
if (nn(jGroupsChannel)) attrsArtemis['jgroups-channel'] = jGroupsChannel
if (nn(initialWaitTimeout)) attrsArtemis['initial-wait-timeout'] = initialWaitTimeout
if (nn(refreshTimeout)) attrsArtemis['refresh-timeout'] = refreshTimeout

def definitionArtemis = {
    'discovery-group'(attrsArtemis) { }
}

def address = null
if (messagingActivemq) {
    address = messagingActivemq.server.'discovery-group'
} else if (messagingHornetq) {
    address = messagingHornetq.'hornetq-server'.'discovery-groups'.'discovery-group'
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

// adding or replacing existing
def isExistingGroup = address.find { it.@name == name }
if (isExistingGroup && !replaceExisting) {
    throw new IllegalStateException("Discovery group $name already exists in configuration. Define different name or set parameter 'replaceExisting' to true.")
} else if (isExistingGroup) {
    if (messagingHornetq) {
        isExistingGroup.replaceNode definitionHornet
    } else if (messagingActivemq) {
        isExistingGroup.replaceNode definitionArtemis
    }
} else {
    if (messagingHornetq) {
        if ( messagingHornetq.'hornetq-server'.'discovery-groups'.any { it.name() == 'discovery-groups' } ) {
            messagingHornetq.'hornetq-server'.'discovery-groups'.appendNode definitionHornet
        } else {
            messagingHornetq.'hornetq-server'.appendNode {
                'discovery-groups'(definitionHornet)
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
