def address = null

def defArtemis = { 'journal-directory' ('path': path) }

if (messagingHornetq) {
    throw new UnsupportedOperationException("HornetQ subsystem is not supported")
}
address = messagingActivemq.server
if ( address.'journal-directory'.any { it.name() == 'journal-directory' } ) {
    address.'journal-directory'.@path = path
} else {
    address.appendNode defArtemis
}

if (!address) {
throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}
