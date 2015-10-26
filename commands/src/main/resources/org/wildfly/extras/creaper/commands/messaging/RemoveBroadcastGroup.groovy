def address = null

if (messagingActivemq) {
    address = messagingActivemq.server.'broadcast-group'
} else if ( messagingHornetq) {
    address = messagingHornetq.'hornetq-server'.'broadcast-groups'.'broadcast-group'
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

def connector = address.find { it.@name == name }
if (!connector) {
    throw new IllegalStateException("Can't remove broadcast-group $name as it does not exist in the configuration")
}

connector.replaceNode {}
