def address = null

def defArtemis = { 'bindings-directory' ('path': path) }

if (messagingHornetq) {
    throw new UnsupportedOperationException("HornetQ subsystem is not supported")
}
address = messagingActivemq.server
if ( address.'bindings-directory'.any { it.name() == 'bindings-directory' } ) {
    address.'bindings-directory'.@path = path
} else {
    address.appendNode defArtemis
}

if (!address) {
throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}
