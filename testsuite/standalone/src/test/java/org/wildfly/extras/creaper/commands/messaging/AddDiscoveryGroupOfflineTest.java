package org.wildfly.extras.creaper.commands.messaging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;
import org.wildfly.extras.creaper.core.CommandFailedException;

public class AddDiscoveryGroupOfflineTest {

    private static final String SUBSYTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:messaging:1.4\">"
            + "            <hornetq-server>\n"
            + "                  <journal-file-size>102400</journal-file-size>\n"
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
            + "                <discovery-groups>\n"
            + "                    <discovery-group name=\"testName\">\n"
            + "                        <socket-binding>messaging</socket-binding>\n"
            + "                        <refresh-timeout>10000</refresh-timeout>\n"
            + "                        <initial-wait-timeout>10</initial-wait-timeout>\n"
            + "                    </discovery-group>\n"
            + "                </discovery-groups>"
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
            + "                <discovery-groups>\n"
            + "                    <discovery-group name=\"testName\">\n"
            + "                        <socket-binding>messaging</socket-binding>\n"
            + "                        <refresh-timeout>10000</refresh-timeout>\n"
            + "                        <initial-wait-timeout>10</initial-wait-timeout>\n"
            + "                    </discovery-group>\n"
            + "                    <discovery-group name=\"testName2\">\n"
            + "                        <jgroups-stack>stack</jgroups-stack>\n"
            + "                        <jgroups-channel>channel</jgroups-channel>\n"
            + "                        <refresh-timeout>1000</refresh-timeout>\n"
            + "                        <initial-wait-timeout>1</initial-wait-timeout>\n"
            + "                    </discovery-group>\n"
            + "                </discovery-groups>"
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
            + "                <discovery-groups>\n"
            + "                    <discovery-group name=\"testName\">\n"
            + "                        <socket-binding>messaging1</socket-binding>\n"
            + "                        <refresh-timeout>100000</refresh-timeout>\n"
            + "                        <initial-wait-timeout>100</initial-wait-timeout>\n"
            + "                    </discovery-group>\n"
            + "                </discovery-groups>"
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
            + "            <discovery-group name=\"testName\" jgroups-stack=\"stack\" jgroups-channel=\"channel\" initial-wait-timeout=\"1\" refresh-timeout=\"2\"/> "
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
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddDiscoveryGroup.Builder("testName")
                .socketBinding("messaging")
                .refreshTimeout(10000)
                .initialWaitTimeout(10)
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

        client.apply(new AddDiscoveryGroup.Builder("testName")
                .socketBinding("messaging")
                .build());

        Assert.fail("Exception should have been thrown.");
    }

    @Test
    public void replace() throws Exception {

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddDiscoveryGroup.Builder("testName")
                .socketBinding("messaging1")
                .refreshTimeout(100000)
                .initialWaitTimeout(100)
                .replaceExisting()
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void transform2() throws Exception {

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_EXPECTED_TRANSFORM, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddDiscoveryGroup.Builder("testName2")
                .jGroupsChannel("channel")
                .jGroupsStack("stack")
                .refreshTimeout(1000)
                .initialWaitTimeout(1)
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM2, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void transformActiveMQ() throws Exception {

        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new AddDiscoveryGroup.Builder("testName")
                .jGroupsStack("stack")
                .jGroupsChannel("channel")
                .initialWaitTimeout(1)
                .refreshTimeout(2)
                .build());

        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYSTEM_EXPECTED_TRANSFORM_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }
}
