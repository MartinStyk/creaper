def address = null

def defHornet = { 'journal-file-size' (journalFileSize) }
def defArtemis = { 'journal' ('file-size': journalFileSize) }

if (messagingActivemq) {
    address = messagingActivemq.server
    if ( address.journal.any { it.name() == 'journal' } ) {
        address.journal.replaceNode defArtemis
    } else {
        address.appendNode defArtemis
    }
} else if (messagingHornetq) {
    address = messagingHornetq.'hornetq-server'
    if ( address.'journal-file-size'.any { it.name() == 'journal-file-size' } ) {
        address.'journal-file-size'.replaceNode defHornet
    } else {
        address.appendNode defHornet
    }
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}
