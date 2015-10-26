package org.wildfly.extras.creaper.commands.messaging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.custommonkey.xmlunit.XMLUnit;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddJmsBridgeOfflineTest {

    private static final String TEST_BRIDGE_NAME = "brTest";
    private static final String TEST_QUEUE_NAME = "sourceQueue";
    private static final String TEST_QUEUE_NAME_2 = "targetQueue";

    private static final String SUBSYTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging:1.4\">"
            + "            <hornetq-server>\n"
            + "<journal-file-size>102400</journal-file-size>\n"
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
            + "            </hornetq-server>\n"
            + "            <jms-bridge name=\"brTest\">\n"
            + "                <source>\n"
            + "                    <connection-factory name=\"java:/ConnectionFactory\"/>\n"
            + "                    <destination name=\"jms/queue/sourceQueue\"/>\n"
            + "                    <user>sUser</user>\n"
            + "                    <password>sPswd</password>\n"
            + "                    <context>\n"
            + "                        <property key=\"java.naming.factory.initial\" value=\"org.jboss.naming.remote.client.InitialContextFactory\"/>\n"
            + "                        <property key=\"java.naming.provider.url\" value=\"http-remoting://10.16.100.40:8080\"/>\n"
            + "                    </context>\n"
            + "                </source>\n"
            + "                <target>\n"
            + "                    <connection-factory name=\"java:jboss/exported/jms/RemoteConnectionFactory\"/>\n"
            + "                    <destination name=\"jms/queue/targetQueue\"/>\n"
            + "                    <user>tUser</user>\n"
            + "                    <password>tPswd</password>\n"
            + "                    <context>\n"
            + "                        <property key=\"java.naming.factory.initial\" value=\"org.jboss.naming.remote.client.InitialContextFactory\"/>\n"
            + "                        <property key=\"java.naming.provider.url\" value=\"http-remoting://10.16.100.40:8080\"/>\n"
            + "                        <property key=\"jboss.naming.client.connect.timeout\" value=\"3000\"/>\n"
            + "                    </context>\n"
            + "                </target>\n"
            + "                <quality-of-service>AT_MOST_ONCE</quality-of-service>\n"
            + "                <failure-retry-interval>1000</failure-retry-interval>\n"
            + "                <max-retries>-1</max-retries>\n"
            + "                <max-batch-size>10</max-batch-size>\n"
            + "                <max-batch-time>1000</max-batch-time>\n"
            + "                <selector string=\"mySelector\"/>\n"
            + "                <subscription-name>subName</subscription-name>\n"
            + "                <client-id>clientId</client-id>\n"
            + "                <add-messageID-in-header>true</add-messageID-in-header>\n"
            + "            </jms-bridge>"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED_REPLACE_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "           <server name=\"default\">\n"
            + "                <security enabled=\"false\"/>\n"
            + "                <journal file-size=\"10485760\"/>\n"
            + "            </server>\n"
            + "<jms-bridge name=\"brTest\" quality-of-service=\"AT_MOST_ONCE\" failure-retry-interval=\"1000\" max-retries=\"-1\" max-batch-size=\"10\" max-batch-time=\"1000\" selector=\"mySelector\" subscription-name=\"subName\" client-id=\"clientId\" add-messageID-in-header=\"true\">\n"
            + "                <source connection-factory=\"java:/ConnectionFactory\" destination=\"jms/queue/sourceQueue\" user=\"sUser\" password=\"sPswd\">\n"
            + "<source-context>\n"
            + "                    <property name=\"java.naming.factory.initial\" value=\"org.jboss.naming.remote.client.InitialContextFactory\"/>\n"
            + "                    <property name=\"java.naming.provider.url\" value=\"http-remoting://10.16.100.40:8080\"/>\n"
            + "                    <property name=\"jboss.naming.client.connect.timeout\" value=\"3000\"/>\n"
            + "</source-context>\n"
            + "</source>\n"
            + "<target connection-factory=\"java:jboss/exported/jms/RemoteConnectionFactory\" destination=\"jms/queue/targetQueue\" user=\"tUser\" password=\"tPswd\"/>\n"
            + "            </jms-bridge>"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "           <server name=\"default\">\n"
            + "                <security enabled=\"false\"/>\n"
            + "                <journal file-size=\"10485760\"/>\n"
            + "            </server>\n"
            + "<jms-bridge name=\"brTest\" quality-of-service=\"AT_MOST_ONCE\" failure-retry-interval=\"1000\" max-retries=\"-1\" max-batch-size=\"10\" max-batch-time=\"1000\" selector=\"mySelector\" subscription-name=\"subName\" client-id=\"clientId\" add-messageID-in-header=\"true\">\n"
            + "                <source connection-factory=\"java:/ConnectionFactory\" destination=\"jms/queue/sourceQueue\" user=\"sUser\" password=\"sPswd\">\n"
            + "<source-context>\n"
            + "                    <property name=\"java.naming.factory.initial\" value=\"org.jboss.naming.remote.client.InitialContextFactory\"/>\n"
            + "                    <property name=\"java.naming.provider.url\" value=\"http-remoting://10.16.100.40:8080\"/>\n"
            + "                    <property name=\"jboss.naming.client.connect.timeout\" value=\"3000\"/>\n"
            + "</source-context>\n"
            + "</source>\n"
            + "<target connection-factory=\"java:jboss/exported/jms/RemoteConnectionFactory\" destination=\"jms/queue/targetQueue\" user=\"tUser\" password=\"tPswd\">\n"
            + "<target-context>\n"
            + "                    <property name=\"jboss.naming.client.connect.timeout\" value=\"3000\"/>\n"
            + "</target-context>\n"
            + "</target>\n"
            + "            </jms-bridge>"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYTEM_ORIGINAL_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <security enabled=\"false\"/>\n"
            + "                <journal file-size=\"10485760\"/>\n"
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

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddJmsBridge.Builder(TEST_BRIDGE_NAME)
                .sourceDestination("jms/queue/" + TEST_QUEUE_NAME)
                .targetDestination("jms/queue/" + TEST_QUEUE_NAME_2)
                .sourceConnectionFactory("java:/ConnectionFactory")
                .targetConnectionFactory("java:jboss/exported/jms/RemoteConnectionFactory")
                .qualityOfService("AT_MOST_ONCE")
                .failureRetryInterval(1000)
                .maxRetries(-1)
                .maxBatchSize(10)
                .maxBatchTime(1000)
                .addMessageIdInHeader()
                .clientId("clientId")
                .selector("mySelector")
                .subscriptionName("subName")
                .build());

        fail("Connector http-connector already exists in configuration, exception should be thrown");
    }

    @Test
    public void transform() throws Exception {

        Map<String, String> sourceCtx = new HashMap<String, String>();
        sourceCtx.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
        sourceCtx.put("java.naming.provider.url", "http-remoting://10.16.100.40:8080");
        Map<String, String> targetCtx = new HashMap<String, String>();
        targetCtx.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
        targetCtx.put("java.naming.provider.url", "http-remoting://10.16.100.40:8080");
        targetCtx.put("jboss.naming.client.connect.timeout", "3000");

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddJmsBridge.Builder(TEST_BRIDGE_NAME)
                .sourceDestination("jms/queue/" + TEST_QUEUE_NAME)
                .targetDestination("jms/queue/" + TEST_QUEUE_NAME_2)
                .sourceConnectionFactory("java:/ConnectionFactory")
                .targetConnectionFactory("java:jboss/exported/jms/RemoteConnectionFactory")
                .qualityOfService("AT_MOST_ONCE")
                .failureRetryInterval(1000)
                .maxRetries(-1)
                .maxBatchSize(10)
                .maxBatchTime(1000)
                .addMessageIdInHeader()
                .clientId("clientId")
                .selector("mySelector")
                .subscriptionName("subName")
                .sourceContext(sourceCtx)
                .sourcePassword("sPswd")
                .sourceUser("sUser")
                .targetContext(targetCtx)
                .targetPassword("tPswd")
                .targetUser("tUser")
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void transformActiveMQ() throws Exception {

        Map<String, String> sourceCtx = new HashMap<String, String>();
        sourceCtx.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
        sourceCtx.put("java.naming.provider.url", "http-remoting://10.16.100.40:8080");
        sourceCtx.put("jboss.naming.client.connect.timeout", "3000");
        Map<String, String> targetCtx = new HashMap<String, String>();
        targetCtx.put("jboss.naming.client.connect.timeout", "3000");

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddJmsBridge.Builder(TEST_BRIDGE_NAME)
                .sourceDestination("jms/queue/" + TEST_QUEUE_NAME)
                .targetDestination("jms/queue/" + TEST_QUEUE_NAME_2)
                .sourceConnectionFactory("java:/ConnectionFactory")
                .targetConnectionFactory("java:jboss/exported/jms/RemoteConnectionFactory")
                .qualityOfService("AT_MOST_ONCE")
                .failureRetryInterval(1000)
                .maxRetries(-1)
                .maxBatchSize(10)
                .maxBatchTime(1000)
                .addMessageIdInHeader()
                .clientId("clientId")
                .selector("mySelector")
                .subscriptionName("subName")
                .sourceContext(sourceCtx)
                .sourcePassword("sPswd")
                .sourceUser("sUser")
                .targetContext(targetCtx)
                .targetPassword("tPswd")
                .targetUser("tUser")
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void replaceActiveMQ() throws Exception {

        Map<String, String> sourceCtx = new HashMap<String, String>();
        sourceCtx.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
        sourceCtx.put("java.naming.provider.url", "http-remoting://10.16.100.40:8080");
        sourceCtx.put("jboss.naming.client.connect.timeout", "3000");
        Map<String, String> targetCtx = new HashMap<String, String>();
        targetCtx.put("jboss.naming.client.connect.timeout", "3000");

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddJmsBridge.Builder(TEST_BRIDGE_NAME)
                .sourceDestination("jms/queue/" + TEST_QUEUE_NAME)
                .targetDestination("jms/queue/" + TEST_QUEUE_NAME_2)
                .sourceConnectionFactory("java:/ConnectionFactory")
                .targetConnectionFactory("java:jboss/exported/jms/RemoteConnectionFactory")
                .qualityOfService("AT_MOST_ONCE")
                .failureRetryInterval(1000)
                .maxRetries(-1)
                .maxBatchSize(10)
                .maxBatchTime(1000)
                .addMessageIdInHeader()
                .clientId("clientId")
                .selector("mySelector")
                .subscriptionName("subName")
                .sourceContext(sourceCtx)
                .sourcePassword("sPswd")
                .sourceUser("sUser")
                .targetPassword("tPswd")
                .targetUser("tUser")
                .replaceExisting()
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }
}
