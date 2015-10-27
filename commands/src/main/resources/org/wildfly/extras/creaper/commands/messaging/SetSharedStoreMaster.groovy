def address = null
def attrsArtemis = [:]
if (nn(failbackDelay)) attrsArtemis['failback-delay'] = failbackDelay
if (nn(failoverOnServerShutdown)) attrsArtemis['failover-on-server-shutdown'] = failoverOnServerShutdown

def defArtemis = { 'shared-store-master' (attrsArtemis) }

if (messagingHornetq) {
    throw new UnsupportedOperationException("HornetQ subsystem is not supported")
}
address = messagingActivemq.server
if ( address.'shared-store-master'.any { it.name() == 'shared-store-master' } ) {
    address.'shared-store-master'.replaceNode defArtemis
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
