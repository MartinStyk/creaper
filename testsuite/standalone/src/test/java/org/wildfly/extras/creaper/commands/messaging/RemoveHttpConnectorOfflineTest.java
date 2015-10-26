package org.wildfly.extras.creaper.commands.messaging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;
import static org.junit.Assert.fail;
/**
 *
 * @author mstyk
 */
public class RemoveHttpConnectorOfflineTest {

    private static final String SUBSYTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging:1.4\">"
            + "            <hornetq-server>\n"
            + "                  <journal-file-size>102400</journal-file-size>\n"
            + "\n"
            + "                <connectors>\n"
            + "                    <http-connector name=\"http-connector\" socket-binding=\"http\">\n"
            + "                        <param key=\"http-upgrade-endpoint\" value=\"http-acceptor\"/>\n"
            + "                    </http-connector>\n"
            + "                    <http-connector name=\"http-connector-throughput\" socket-binding=\"http\">\n"
            + "                        <param key=\"http-upgrade-endpoint\" value=\"http-acceptor-throughput\"/>\n"
            + "                        <param key=\"batch-delay\" value=\"50\"/>\n"
            + "                    </http-connector>\n"
            + "                    <in-vm-connector name=\"in-vm\" server-id=\"0\"/>\n"
            + "                </connectors>\n"
            + "\n"
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
            + "\n"
            + "                <connectors>\n"
            + "                    <http-connector name=\"http-connector-throughput\" socket-binding=\"http\">\n"
            + "                        <param key=\"http-upgrade-endpoint\" value=\"http-acceptor-throughput\"/>\n"
            + "                        <param key=\"batch-delay\" value=\"50\"/>\n"
            + "                    </http-connector>\n"
            + "                    <in-vm-connector name=\"in-vm\" server-id=\"0\"/>\n"
            + "                </connectors>\n"
            + "\n"
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

    private static final String SUBSYTEM_ORIGINAL_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <address-setting name=\"#\" dead-letter-address=\"jms.queue.DLQ\" expiry-address=\"jms.queue.ExpiryQueue\" max-size-bytes=\"10485760\" page-size-bytes=\"2097152\" message-counter-history-day-limit=\"10\" redistribution-delay=\"1000\"/>\n"
            + "                <http-connector name=\"http-connector\" socket-binding=\"http\" endpoint=\"http-acceptor\"/>\n"
            + "                <http-connector name=\"http-connector-throughput\" socket-binding=\"http\" endpoint=\"http-acceptor-throughput\">\n"
            + "                    <param name=\"batch-delay\" value=\"50\"/>\n"
            + "                </http-connector>\n"
            + "                <in-vm-connector name=\"in-vm\" server-id=\"0\"/>\n"
            + "                <http-acceptor name=\"http-acceptor\" http-listener=\"default\"/>"
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
            + "                <http-connector name=\"http-connector\" socket-binding=\"http\" endpoint=\"http-acceptor\"/>\n"
            + "                <in-vm-connector name=\"in-vm\" server-id=\"0\"/>\n"
            + "                <http-acceptor name=\"http-acceptor\" http-listener=\"default\"/>"
            + "            </server>\n"
            + "        </subsystem>\n"
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
        Files.write(SUBSYTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveHttpConnector("http-connector"));

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformNotExists() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveHttpConnector("NotExisting"));

        fail("The connector should not exist in configuration, so an exception should be thrown");
    }

    @Test
    public void transformActiveMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveHttpConnector("http-connector-throughput"));

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformNotExistsActiveMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveHttpConnector("NotExisting"));

        fail("The connector should not exist in configuration, so an exception should be thrown");
    }
}
