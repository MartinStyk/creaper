def address = null

def defArtemis = { 'large-messages-directory' ('path': path) }

if (messagingHornetq) {
    throw new UnsupportedOperationException("HornetQ subsystem is not supported")
}
address = messagingActivemq.server
if ( address.'large-messages-directory'.any { it.name() == 'large-messages-directory' } ) {
    address.'large-messages-directory'.@path = path
} else {
    address.appendNode defArtemis
}

if (!address) {
throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}
