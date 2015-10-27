def address = null
def attrsArtemis = [:]
if (nn(requestBackup)) attrsArtemis['request-backup'] = requestBackup
if (nn(maxBackups)) attrsArtemis['max-backups'] = maxBackups
if (nn(backupRequestRetryInterval)) attrsArtemis['backup-request-retry-interval'] = backupRequestRetryInterval
if (nn(backupRequestRetries)) attrsArtemis['backup-request-retries'] = backupRequestRetries
if (nn(backupPortOffset)) attrsArtemis['backup-port-offset'] = backupPortOffset

def defArtemis = { 'shared-store-colocated' (attrsArtemis) }

if (messagingHornetq) {
    throw new UnsupportedOperationException("HornetQ subsystem is not supported")
}
address = messagingActivemq.server
if ( address.'shared-store-colocated'.any { it.name() == 'shared-store-colocated' } ) {
    address.'shared-store-colocated'.replaceNode defArtemis
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
