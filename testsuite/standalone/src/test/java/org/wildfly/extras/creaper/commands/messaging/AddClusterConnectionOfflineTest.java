package org.wildfly.extras.creaper.commands.messaging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.custommonkey.xmlunit.XMLUnit;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;
import org.wildfly.extras.creaper.core.CommandFailedException;

public class AddClusterConnectionOfflineTest {

    private static final String TEST_CON_NAME = "testConnection";

    private static final String SUBSYTEM_ORIGINAL = ""
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
            + "            </hornetq-server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
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
    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
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
            + "                        <max-hops>500</max-hops>\n"
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
        // ignore whitespaces difference in "text" node
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void transform() throws Exception {
        List<String> staticCons = new ArrayList<String>();
        staticCons.add("connector1");
        staticCons.add("connector2");
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddClusterConnection.Builder(TEST_CON_NAME)
                .checkPeriod(1)
                .confirmationWindowSize(10)
                .connectionTtl(100)
                .connectorName("connector")
                .allowDirectConnectionsOnly()
                .callFailoverTimeout(100)
                .callTimeout(101)
                .checkPeriod(1000)
                .clusterConnectionAddress("address")
                .initialConnectAttempts(5)
                .maxHops(50)
                .maxRetryInterval(500)
                .minLargeMessageSize(1)
                .notificationAttempts(7)
                .notificationInterval(10)
                .reconnectAttempts(6)
                .retryInterval(60)
                .retryIntervalMultiplier(BigDecimal.ONE)
                .staticConnectors(staticCons)
                .useDuplicateDetection(false)
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void replace_fail() throws Exception {
        List<String> staticCons = new ArrayList<String>();
        staticCons.add("connector1");
        staticCons.add("connector2");
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddClusterConnection.Builder(TEST_CON_NAME)
                .checkPeriod(1)
                .confirmationWindowSize(10)
                .connectionTtl(100)
                .connectorName("connector")
                .allowDirectConnectionsOnly()
                .callFailoverTimeout(100)
                .callTimeout(101)
                .checkPeriod(1000)
                .clusterConnectionAddress("address")
                .initialConnectAttempts(5)
                .maxHops(500)
                .maxRetryInterval(500)
                .minLargeMessageSize(1)
                .notificationAttempts(7)
                .notificationInterval(10)
                .reconnectAttempts(6)
                .retryInterval(60)
                .retryIntervalMultiplier(BigDecimal.ONE)
                .staticConnectors(staticCons)
                .useDuplicateDetection(false)
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void replace() throws Exception {
        List<String> staticCons = new ArrayList<String>();
        staticCons.add("connector1");
        staticCons.add("connector2");
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddClusterConnection.Builder(TEST_CON_NAME)
                .checkPeriod(1)
                .confirmationWindowSize(10)
                .connectionTtl(100)
                .connectorName("connector")
                .allowDirectConnectionsOnly()
                .callFailoverTimeout(100)
                .callTimeout(101)
                .checkPeriod(1000)
                .clusterConnectionAddress("address")
                .initialConnectAttempts(5)
                .maxHops(500)
                .maxRetryInterval(500)
                .minLargeMessageSize(1)
                .notificationAttempts(7)
                .notificationInterval(10)
                .reconnectAttempts(6)
                .retryInterval(60)
                .retryIntervalMultiplier(BigDecimal.ONE)
                .staticConnectors(staticCons)
                .useDuplicateDetection(false)
                .replaceExisting()
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void transformWithAlreadyExistingClusterConnectionsNode() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddClusterConnection.Builder(TEST_CON_NAME + "1")
                .checkPeriod(1)
                .confirmationWindowSize(10)
                .connectionTtl(100)
                .connectorName("connector")
                .allowDirectConnectionsOnly()
                .callFailoverTimeout(100)
                .callTimeout(101)
                .checkPeriod(1000)
                .clusterConnectionAddress("address")
                .initialConnectAttempts(5)
                .maxHops(50)
                .maxRetryInterval(500)
                .minLargeMessageSize(1)
                .notificationAttempts(7)
                .notificationInterval(10)
                .reconnectAttempts(6)
                .retryInterval(60)
                .retryIntervalMultiplier(BigDecimal.ONE)
                .useDuplicateDetection(false)
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM2, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void transformActiveMQ() throws Exception {

        List<String> staticCons = new ArrayList<String>();
        staticCons.add("connector1");
        staticCons.add("connector2");

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddClusterConnection.Builder(TEST_CON_NAME)
                .checkPeriod(1)
                .confirmationWindowSize(10)
                .connectionTtl(100)
                .connectorName("connector")
                .allowDirectConnectionsOnly()
                .callFailoverTimeout(100)
                .callTimeout(101)
                .checkPeriod(1000)
                .clusterConnectionAddress("address")
                .initialConnectAttempts(5)
                .maxHops(50)
                .maxRetryInterval(500)
                .minLargeMessageSize(1)
                .notificationAttempts(7)
                .notificationInterval(10)
                .reconnectAttempts(6)
                .retryInterval(60)
                .retryIntervalMultiplier(BigDecimal.ONE)
                .staticConnectors(staticCons)
                .useDuplicateDetection(false)
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }
}
