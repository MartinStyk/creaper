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

public class RemoveJmsBridgeOfflineTest {

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
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void transform() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveJmsBridge("brTest"));
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformNotExists() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveJmsBridge("NotExisting"));

        fail("The bridge should not exist in configuration, so an exception should be thrown");
    }

    @Test
    public void transformActiveMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveJmsBridge("brTest"));

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void transformNotExistsActiveMQ() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new RemoveJmsBridge("NotExisting"));

        fail("The bridge should not exist in configuration, so an exception should be thrown");
    }
}
