def address = null
def attrsArtemis = [:]
if (nn(allowFailback)) attrsArtemis['allow-failback'] = allowFailback
if (nn(failbackDelay)) attrsArtemis['failback-delay'] = failbackDelay
if (nn(failoverOnServerShutdown)) attrsArtemis['failover-on-server-shutdown'] = failoverOnServerShutdown
if (nn(restartBackup)) attrsArtemis['restart-backup'] = restartBackup
if (nn(scaleDown)) attrsArtemis['scale-down'] = scaleDown
if (nn(scaleDownClusterName)) attrsArtemis['scale-down-cluster-name'] = scaleDownClusterName
if (nn(scaleDownConnectorsString)) attrsArtemis['scale-down-connectors'] = scaleDownConnectorsString
if (nn(scaleDownDiscoveryGroup)) attrsArtemis['scale-down-discovery-group'] = scaleDownDiscoveryGroup
if (nn(scaleDownGroupName)) attrsArtemis['scale-down-group-name'] = scaleDownGroupName

def defArtemis = { 'shared-store-slave' (attrsArtemis) }

if (messagingHornetq) {
    throw new UnsupportedOperationException("HornetQ subsystem is not supported")
}
address = messagingActivemq.server
if ( address.'shared-store-slave'.any { it.name() == 'shared-store-slave' } ) {
    address.'shared-store-slave'.replaceNode defArtemis
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
