package org.wildfly.extras.creaper.commands.messaging;

/**
 *
 * @author mstyk
 */
public final class MessagingConstants {

    public static final String HTTP_CONNECTOR = "http-connector";
    public static final String CORE_BRIDGE = "bridge";
    public static final String JMS_BRIDGE = "jms-bridge";
    public static final String CLUSTER_CONNECTION = "cluster-connection";
    public static final String SECURITY_ENABLED = "security-enabled";
    public static final String CLUSTER_PASSWORD = "cluster-password";
    public static final String JOURNAL_FILE_SIZE = "journal-file-size";
    public static final String JOURNAL_TYPE = "journal-type";
    public static final String REMOTE_CONNECTOR = "remote-connector";
    public static final String BROADCAST_GROUP = "broadcast-group";
    public static final String DISCOVERY_GROUP = "discovery-group";
    public static final String CONNECTION_FACTORY = "connection-factory";

    private MessagingConstants() {
    } // avoid instantiation
}
