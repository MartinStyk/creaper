def address = null

def defHornet = { 'journal-type' (journalType) }
def defArtemis = { 'journal' ('type': journalType) }

if (messagingActivemq) {
    address = messagingActivemq.server
    if ( address.journal.any { it.name() == 'journal' } ) {
        address.journal.@type = journalType
    } else {
        address.appendNode defArtemis
    }
} else if (messagingHornetq) {
    address = messagingHornetq.'hornetq-server'
    if ( address.'journal-type'.any { it.name() == 'journal-type' } ) {
        address.'journal-type'.replaceNode defHornet
    } else {
        address.appendNode defHornet
    }
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}
