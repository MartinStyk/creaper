package org.wildfly.extras.creaper.commands.messaging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;
import static org.junit.Assert.fail;

/**
 *
 * @author mstyk
 */
public class RemoveClusterConnectionOfflineTest {

    private static final String TEST_CON_NAME = "testConnection";

    private static final String SUBSYSTEM_EXPECTED_TRANSFORM = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging:1.4\">"
            + "            <hornetq-server>\n"
            + "                  <journal-file-size>102400</journal-file-size>\n"
            + "                <acceptors>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor\"/>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor-throughput\">\n"
            + "                        <param key=\"batch-delay\" value=\"50\"/>\n"
            + "                        <param key=\"direct-deliver\" value=\"false\"/>\n"
            + "                    </http-acceptor>\n"
            + "                    <in-vm-acceptor name=\"in-vm\" server-id=\"0\"/>\n"
            + "                </acceptors>\n"
            + "                <cluster-connections>\n"
            + "                    <cluster-connection name=\"testConnection\">\n"
            + "                        <address>address</address>\n"
            + "                        <connector-ref>connector</connector-ref>\n"
            + "                        <check-period>1000</check-period>\n"
            + "                        <connection-ttl>100</connection-ttl>\n"
            + "                        <min-large-message-size>1</min-large-message-size>\n"
            + "                        <call-timeout>101</call-timeout>\n"
            + "                        <call-failover-timeout>100</call-failover-timeout>\n"
            + "                        <retry-interval>60</retry-interval>\n"
            + "                        <retry-interval-multiplier>1</retry-interval-multiplier>\n"
            + "                        <max-retry-interval>500</max-retry-interval>\n"
            + "                        <initial-connect-attempts>5</initial-connect-attempts>\n"
            + "                        <reconnect-attempts>6</reconnect-attempts>\n"
            + "                        <use-duplicate-detection>false</use-duplicate-detection>\n"
            + "                        <max-hops>50</max-hops>\n"
            + "                        <confirmation-window-size>10</confirmation-window-size>\n"
            + "                        <notification-attempts>7</notification-attempts>\n"
            + "                        <notification-interval>10</notification-interval>\n"
            + "                        <static-connectors allow-direct-connections-only=\"true\">\n"
            + "                            <connector-ref>\n"
            + "                                connector1\n"
            + "                            </connector-ref>\n"
            + "                            <connector-ref>\n"
            + "                                connector2\n"
            + "                            </connector-ref>\n"
            + "                        </static-connectors>\n"
            + "                    </cluster-connection>\n"
            + "                </cluster-connections>"
            + "            </hornetq-server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED_TRANSFORM2 = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging:1.4\">"
            + "            <hornetq-server>\n"
            + "                  <journal-file-size>102400</journal-file-size>\n"
            + "                <acceptors>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor\"/>\n"
            + "                    <http-acceptor http-listener=\"default\" name=\"http-acceptor-throughput\">\n"
            + "                        <param key=\"batch-delay\" value=\"50\"/>\n"
            + "                        <param key=\"direct-deliver\" value=\"false\"/>\n"
            + "                    </http-acceptor>\n"
            + "                    <in-vm-acceptor name=\"in-vm\" server-id=\"0\"/>\n"
            + "                </acceptors>\n"
            + "                <cluster-connections>\n"
            + "                    <cluster-connection name=\"testConnection\">\n"
            + "                        <address>address</address>\n"
            + "                        <connector-ref>connector</connector-ref>\n"
            + "                        <check-period>1000</check-period>\n"
            + "                        <connection-ttl>100</connection-ttl>\n"
            + "                        <min-large-message-size>1</min-large-message-size>\n"
            + "                        <call-timeout>101</call-timeout>\n"
            + "                        <call-failover-timeout>100</call-failover-timeout>\n"
            + "                        <retry-interval>60</retry-interval>\n"
            + "                        <retry-interval-multiplier>1</retry-interval-multiplier>\n"
            + "                        <max-retry-interval>500</max-retry-interval>\n"
            + "                        <initial-connect-attempts>5</initial-connect-attempts>\n"
            + "                        <reconnect-attempts>6</reconnect-attempts>\n"
            + "                        <use-duplicate-detection>false</use-duplicate-detection>\n"
            + "                        <max-hops>50</max-hops>\n"
            + "                        <confirmation-window-size>10</confirmation-window-size>\n"
            + "                        <notification-attempts>7</notification-attempts>\n"
            + "                        <notification-interval>10</notification-interval>\n"
            + "                        <static-connectors allow-direct-connections-only=\"true\">\n"
            + "                            <connector-ref>\n"
            + "                                connector1\n"
            + "                            </connector-ref>\n"
            + "                            <connector-ref>\n"
            + "                                connector2\n"
            + "                            </connector-ref>\n"
            + "                        </static-connectors>\n"
            + "                    </cluster-connection>\n"
            + "                    <cluster-connection name=\"testConnection1\">\n"
            + "                        <address>address</address>\n"
            + "                        <connector-ref>connector</connector-ref>\n"
            + "                        <check-period>1000</check-period>\n"
            + "                        <connection-ttl>100</connection-ttl>\n"
            + "                        <min-large-message-size>1</min-large-message-size>\n"
            + "                        <call-timeout>101</call-timeout>\n"
            + "                        <call-failover-timeout>100</call-failover-timeout>\n"
            + "                        <retry-interval>60</retry-interval>\n"
            + "                        <retry-interval-multiplier>1</retry-interval-multiplier>\n"
            + "                        <max-retry-interval>500</max-retry-interval>\n"
            + "                        <initial-connect-attempts>5</initial-connect-attempts>\n"
            + "                        <reconnect-attempts>6</reconnect-attempts>\n"
            + "                        <use-duplicate-detection>false</use-duplicate-detection>\n"
            + "                        <max-hops>50</max-hops>\n"
            + "                        <confirmation-window-size>10</confirmation-window-size>\n"
            + "                        <notification-attempts>7</notification-attempts>\n"
            + "                        <notification-interval>10</notification-interval>\n"
            + "                    </cluster-connection>\n"
            + "                </cluster-connections>"
            + "            </hornetq-server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYTEM_ORIGINAL_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + " "
            + "            </server>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "                <cluster-connection name=\"testConnection\" address=\"address\" connector-name=\"connector\" check-period=\"1000\" connection-ttl=\"100\" min-large-message-size=\"1\" call-timeout=\"101\" call-failover-timeout=\"100\" retry-interval=\"60\" retry-interval-multiplier=\"1\" max-retry-interval=\"500\" initial-connect-attempts=\"5\" reconnect-attempts=\"6\" use-duplicate-detection=\"false\" max-hops=\"50\" confirmation-window-size=\"10\" notification-attempts=\"7\" notification-interval=\"10\" static-connectors=\"connector1 connector2\" allow-direct-connections-only=\"true\"/> "
            + "            </server>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void transform() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM2, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM2, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveClusterConnection(TEST_CON_NAME + "1"));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformNotExists() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM2, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM2, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveClusterConnection("NotExisting"));

        fail("The connection should not exist in configuration, so an exception should be thrown");
    }

    @Test
    public void transformActiveMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveClusterConnection(TEST_CON_NAME));

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformNotExistsActiveMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveClusterConnection("NotExisting"));

        fail("The connection should not exist in configuration, so an exception should be thrown");
    }
}
