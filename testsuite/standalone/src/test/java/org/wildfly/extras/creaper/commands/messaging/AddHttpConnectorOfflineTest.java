package org.wildfly.extras.creaper.commands.messaging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.custommonkey.xmlunit.XMLUnit;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddHttpConnectorOfflineTest {

    private static final String TEST_QUEUE_NAME = "testConnector";
    private static final String TEST_QUEUE_NAME_JNDI = "java:/jms/queue/" + TEST_QUEUE_NAME;

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
            + "                    <http-connector name=\"http-connector\" socket-binding=\"http\">\n"
            + "                        <param key=\"http-upgrade-endpoint\" value=\"http-acceptor\"/>\n"
            + "                    </http-connector>\n"
            + "                    <http-connector name=\"http-connector-throughput\" socket-binding=\"http\">\n"
            + "                        <param key=\"http-upgrade-endpoint\" value=\"http-acceptor-throughput\"/>\n"
            + "                        <param key=\"batch-delay\" value=\"50\"/>\n"
            + "                    </http-connector>\n"
            + "                    <in-vm-connector name=\"in-vm\" server-id=\"0\"/>\n"
            + "                    <http-connector name=\"testConnector\" socket-binding=\"http\">\n"
            + "                        <param key=\"http-upgrade-endpoint\" value=\"http-acceptor\"/>\n"
            + "                    </http-connector>\n"
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
    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging:1.4\">"
            + "            <hornetq-server>\n"
            + "                  <journal-file-size>102400</journal-file-size>\n"
            + "\n"
            + "                <connectors>\n"
            + "                    <http-connector name=\"http-connector\" socket-binding=\"http\">\n"
            + "                        <param key=\"lets-do-it\" value=\"true\"/>\n"
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
            + "                <http-connector name=\"http-connector-throughput\" socket-binding=\"http\" endpoint=\"http-acceptor-throughput\">\n"
            + "                    <param name=\"batch-delay\" value=\"50\"/>\n"
            + "                </http-connector>\n"
            + "                <in-vm-connector name=\"in-vm\" server-id=\"0\"/>\n"
            + "                <http-acceptor name=\"http-acceptor\" http-listener=\"default\"/>"
            + "                <http-connector name=\"test\" socket-binding=\"http\" endpoint=\"http-acceptor\">\n"
            + "                    <param name=\"batch-delay\" value=\"55\"/>\n"
            + "                </http-connector>\n"
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
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddHttpConnector.Builder("http-connector")
                .socketBinding("http")
                .build());

        fail("Connector http-connector already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("lets-do-it", "true");
        params.put("http-upgrade-endpoint", "http-acceptor");

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddHttpConnector.Builder("http-connector")
                .socketBinding("http")
                .params(params)
                .replaceExisting()
                .build());
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void transform() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("http-upgrade-endpoint", "http-acceptor");

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddHttpConnector.Builder("testConnector")
                .socketBinding("http")
                .params(params)
                .build());
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void transformActiveMQ() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("batch-delay", "55");

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddHttpConnector.Builder("test")
                .socketBinding("http")
                .params(params)
                .endPoint("http-acceptor")
                .replaceExisting()
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }
}
