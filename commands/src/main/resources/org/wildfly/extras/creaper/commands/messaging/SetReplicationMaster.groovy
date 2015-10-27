def address = null
def attrsArtemis = [:]
if (nn(clusterName)) attrsArtemis['cluster-name'] = clusterName
if (nn(groupName)) attrsArtemis['group-name'] = groupName
if (nn(checkForLiveServer)) attrsArtemis['check-for-live-server'] = checkForLiveServer

def defArtemis = { 'replication-master' (attrsArtemis) }

if (messagingHornetq) {
    throw new UnsupportedOperationException("HornetQ subsystem is not supported")
}
address = messagingActivemq.server
if ( address.'replication-master'.any { it.name() == 'replication-master' } ) {
    address.'replication-master'.replaceNode defArtemis
} else {
    address.appendNode defArtemis
}

if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
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
