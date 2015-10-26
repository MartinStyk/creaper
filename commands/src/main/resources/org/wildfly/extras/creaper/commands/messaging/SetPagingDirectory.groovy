def address = null

def defArtemis = { 'paging-directory' ('path': path) }

if (messagingHornetq) {
    throw new UnsupportedOperationException("HornetQ subsystem is not supported")
}
address = messagingActivemq.server
if ( address.'paging-directory'.any { it.name() == 'paging-directory' } ) {
    address.'paging-directory'.@path = path
} else {
    address.appendNode defArtemis
}

if (!address) {
throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}
