def address = null

if (messagingActivemq) {
    address = messagingActivemq.server.'cluster-connection'
} else if ( messagingHornetq) {
    address = messagingHornetq.'hornetq-server'.'cluster-connections'.'cluster-connection'
}
if (!address) {
    throw new IllegalStateException("Neither ActiveMQ nor HornetQ messaging subsystem exists in configuration")
}

def object = address.find { it.@name == name }
if (!object) {
    throw new IllegalStateException("Can't remove cluster connection $name as it does not exist in the configuration")
}
if ( messagingHornetq && !address.any()) {
    messagingHornetq.'hornetq-server'.'cluster-connections'.replaceNode {}
}
object.replaceNode {}

def nn(Object... object) {
    if (object == null) return false
    return object.any { it != null }
}
