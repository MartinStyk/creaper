def address = null

if (messagingActivemq) {
    address = messagingActivemq.'jms-bridge'
} else if ( messagingHornetq) {
    address = messagingHornetq.'jms-bridge'
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration, or bridge doesn`t exist")
}

def bridge = address.find { it.@name == name }
if (!bridge) {
    throw new IllegalStateException("Can't remove bridge $name as it does not exist in the configuration")
}

bridge.replaceNode {}

