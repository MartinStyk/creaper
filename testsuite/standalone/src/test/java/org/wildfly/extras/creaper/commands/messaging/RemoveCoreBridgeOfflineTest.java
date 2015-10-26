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

public class RemoveCoreBridgeOfflineTest {

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
            + "                 <bridges>\n"
            + "                    <bridge name=\"brTest1\">\n"
            + "                        <queue-name>startQ</queue-name>\n"
            + "                        <forwarding-address>endQ</forwarding-address>\n"
            + "                        <ha>true</ha>\n"
            + "                        <filter string=\"filter\"/>\n"
            + "                        <min-large-message-size>100</min-large-message-size>\n"
            + "                        <check-period>100</check-period>\n"
            + "                        <connection-ttl>5000</connection-ttl>\n"
            + "                        <retry-interval>1000</retry-interval>\n"
            + "                        <retry-interval-multiplier>1.5</retry-interval-multiplier>\n"
            + "                        <max-retry-interval>100</max-retry-interval>\n"
            + "                        <initial-connect-attempts>10</initial-connect-attempts>\n"
            + "                        <reconnect-attempts>10</reconnect-attempts>\n"
            + "                        <reconnect-attempts-on-same-node>5</reconnect-attempts-on-same-node>\n"
            + "                        <use-duplicate-detection>true</use-duplicate-detection>\n"
            + "                        <user>tester</user>\n"
            + "                        <password>pswd</password>\n"
            + "                        <static-connectors>\n"
            + "                            <connector-ref>\n"
            + "                                connector1\n"
            + "                            </connector-ref>\n"
            + "                            <connector-ref>\n"
            + "                                connector2\n"
            + "                            </connector-ref>\n"
            + "                        </static-connectors>\n"
            + "                    </bridge>\n"
            + "                </bridges>"
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
            + " <bridges>\n"
            + "                    <bridge name=\"brTest1\">\n"
            + "                        <queue-name>startQ</queue-name>\n"
            + "                        <forwarding-address>endQ</forwarding-address>\n"
            + "                        <ha>true</ha>\n"
            + "                        <filter string=\"filter\"/>\n"
            + "                        <min-large-message-size>100</min-large-message-size>\n"
            + "                        <check-period>100</check-period>\n"
            + "                        <connection-ttl>5000</connection-ttl>\n"
            + "                        <retry-interval>1000</retry-interval>\n"
            + "                        <retry-interval-multiplier>1.5</retry-interval-multiplier>\n"
            + "                        <max-retry-interval>100</max-retry-interval>\n"
            + "                        <initial-connect-attempts>10</initial-connect-attempts>\n"
            + "                        <reconnect-attempts>10</reconnect-attempts>\n"
            + "                        <reconnect-attempts-on-same-node>5</reconnect-attempts-on-same-node>\n"
            + "                        <use-duplicate-detection>true</use-duplicate-detection>\n"
            + "                        <user>tester</user>\n"
            + "                        <password>pswd</password>\n"
            + "                        <static-connectors>\n"
            + "                            <connector-ref>\n"
            + "                                connector1\n"
            + "                            </connector-ref>\n"
            + "                            <connector-ref>\n"
            + "                                connector2\n"
            + "                            </connector-ref>\n"
            + "                        </static-connectors>\n"
            + "                    </bridge>\n"
            + "                    <bridge name=\"brTest\">\n"
            + "                        <queue-name>startQ</queue-name>\n"
            + "                        <forwarding-address>endQ</forwarding-address>\n"
            + "                        <retry-interval>1000</retry-interval>\n"
            + "                        <retry-interval-multiplier>1.5</retry-interval-multiplier>\n"
            + "                        <static-connectors>\n"
            + "                            <connector-ref>\n"
            + "                                connector1\n"
            + "                            </connector-ref>\n"
            + "                            <connector-ref>\n"
            + "                                connector2\n"
            + "                            </connector-ref>\n"
            + "                        </static-connectors>\n"
            + "                    </bridge>\n"
            + "                </bridges>"
            + "            </hornetq-server>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYTEM_ORIGINAL_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <security enabled=\"false\"/>\n"
            + "                <journal file-size=\"10485760\"/>\n"
            + "                <security-setting name=\"#\">\n"
            + "                    <role name=\"guest\" send=\"true\" consume=\"true\" create-non-durable-queue=\"true\" delete-non-durable-queue=\"true\"/>\n"
            + "                </security-setting>\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <jms-queue name=\"testQueue\" entries=\"java:jboss/exported/jms/queue/testQueue\"/>\n"
            + "                <jms-queue name=\"InQueue\" entries=\"java:jboss/exported/jms/queue/InQueue\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "                <pooled-connection-factory name=\"activemq-ra\" connectors=\"in-vm\" entries=\"java:/JmsXA java:jboss/DefaultJMSConnectionFactory\" transaction=\"xa\"/>\n"
            + "            </server>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <security enabled=\"false\"/>\n"
            + "                <journal file-size=\"10485760\"/>\n"
            + "                <security-setting name=\"#\">\n"
            + "                    <role name=\"guest\" send=\"true\" consume=\"true\" create-non-durable-queue=\"true\" delete-non-durable-queue=\"true\"/>\n"
            + "                </security-setting>\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <jms-queue name=\"testQueue\" entries=\"java:jboss/exported/jms/queue/testQueue\"/>\n"
            + "                <jms-queue name=\"InQueue\" entries=\"java:jboss/exported/jms/queue/InQueue\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "                <pooled-connection-factory name=\"activemq-ra\" connectors=\"in-vm\" entries=\"java:/JmsXA java:jboss/DefaultJMSConnectionFactory\" transaction=\"xa\"/>\n"
            + "             <bridge name=\"testBridge\" queue-name=\"jms.queue.sourceQueue\" forwarding-address=\"jms.queue.targetQueue\" retry-interval=\"1000\" retry-interval-multiplier=\"1.5\" confirmation-window-size=\"1000000000\" static-connectors=\"con1 con2\" use-duplicate-detection=\"true\"/>\n"
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

        client.apply(new RemoveCoreBridge("brTest"));
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformNotExists() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM2, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM2, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveCoreBridge("NotExisting"));

        fail("The bridge should not exist in configuration, so an exception should be thrown");
    }

    @Test
    public void transformActiveMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveCoreBridge("testBridge"));

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformNotExistsActiveMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveCoreBridge("NotExisting"));

        fail("The bridge should not exist in configuration, so an exception should be thrown");
    }
}
