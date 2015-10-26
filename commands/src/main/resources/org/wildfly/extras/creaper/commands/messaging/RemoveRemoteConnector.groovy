def address = null

if (messagingActivemq) {
    address = messagingActivemq.server.'remote-connector'
} else if ( messagingHornetq) {
    address = messagingHornetq.'hornetq-server'.'connectors'.'netty-connector'
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

def connector = address.find { it.@name == name }
if (!connector) {
    throw new IllegalStateException("Can't remove connector $name as it does not exist in the configuration")
}

connector.replaceNode {}
