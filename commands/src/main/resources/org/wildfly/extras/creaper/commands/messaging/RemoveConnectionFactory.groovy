def address = null
if (messagingActivemq) {
    address = messagingActivemq.server.'connection-factory'
} else if (messagingHornetq) {
    throw new IllegalStateException("Offline operation implemented only for ActiveMQ")
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

def factory = address.find { it.@name == name }
if (!factory) {
    throw new IllegalStateException("Can't remove connection factory $name as it does not exist in the configuration")
}

factory.replaceNode {}
