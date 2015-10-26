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

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

/**
 *
 * @author mstyk
 */
public class RemoveConnectionFactoryOfflineTest {

    private static final String TEST_NAME = "testConnectionFactory";

    private static final String SUBSYTEM_ORIGINAL_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <address-setting name=\"#\" dead-letter-address=\"jms.queue.DLQ\" expiry-address=\"jms.queue.ExpiryQueue\" max-size-bytes=\"10485760\" page-size-bytes=\"2097152\" message-counter-history-day-limit=\"10\" redistribution-delay=\"1000\"/>\n"
            + " <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "                <connection-factory name=\"RemoteConnectionFactory\" connectors=\"http-connector\" entries=\"java:jboss/exported/jms/RemoteConnectionFactory\"/>\n"
            + "                <pooled-connection-factory name=\"activemq-ra\" connectors=\"in-vm\" entries=\"java:/JmsXA java:jboss/DefaultJMSConnectionFactory\" transaction=\"xa\"/> "
            + "            </server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <address-setting name=\"#\" dead-letter-address=\"jms.queue.DLQ\" expiry-address=\"jms.queue.ExpiryQueue\" max-size-bytes=\"10485760\" page-size-bytes=\"2097152\" message-counter-history-day-limit=\"10\" redistribution-delay=\"1000\"/>\n"
            + " <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "                <connection-factory name=\"RemoteConnectionFactory\" connectors=\"http-connector\" entries=\"java:jboss/exported/jms/RemoteConnectionFactory\"/>\n"
            + "                <pooled-connection-factory name=\"activemq-ra\" connectors=\"in-vm\" entries=\"java:/JmsXA java:jboss/DefaultJMSConnectionFactory\" transaction=\"xa\"/> "
            + " <connection-factory name=\"testConnectionFactory\" connectors=\"http-connector http-connector-throughput\" entries=\"jms/connectionFactory1 jms/connectionFactory2\" ha=\"true\" client-failure-check-period=\"3\" connection-ttl=\"5\" call-timeout=\"2\" call-failover-timeout=\"1\" consumer-window-size=\"7\" consumer-max-rate=\"6\" confirmation-window-size=\"4\" producer-max-rate=\"11\" compress-large-messages=\"true\" cache-large-message-client=\"true\" min-large-message-size=\"10\" client-id=\"cId\" dups-ok-batch-size=\"8\" transaction-batch-size=\"16\" block-on-acknowledge=\"true\" block-on-non-durable-send=\"true\" auto-group=\"true\" pre-acknowledge=\"true\" retry-interval=\"13\" retry-interval-multiplier=\"10\" max-retry-interval=\"9\" reconnect-attempts=\"12\" failover-on-initial-connection=\"true\" use-global-pools=\"false\" scheduled-thread-pool-max-size=\"14\" thread-pool-max-size=\"15\" group-id=\"gid\" factory-type=\"TOPIC\"/>"
            + "            </server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        // ignore whitespaces difference in "text" node
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test(expected = CommandFailedException.class)
    public void nonExisting() throws Exception {

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveConnectionFactory("xxx"));

        fail("Connection doesnt exist in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new RemoveConnectionFactory(TEST_NAME));
        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }

}
