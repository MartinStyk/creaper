package org.wildfly.extras.creaper.commands.messaging;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

/**
 *
 * @author mstyk
 */
public class SetSharedStoreColocatedOfflineTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();
    private static final String SUBSYTEM_ORIGINAL_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "            </server>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYTEM_EXPECTED_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "<shared-store-colocated request-backup=\"true\" max-backups=\"1\" backup-port-offset=\"10005\"/>"
            + "            </server>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYTEM_REPLACE_ACTIVEMQ = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "         <subsystem xmlns=\"urn:jboss:domain:messaging-activemq:1.0\">\n"
            + "            <server name=\"default\">\n"
            + "                <jms-queue name=\"ExpiryQueue\" entries=\"java:/jms/queue/ExpiryQueue\"/>\n"
            + "                <jms-queue name=\"DLQ\" entries=\"java:/jms/queue/DLQ\"/>\n"
            + "                <connection-factory name=\"InVmConnectionFactory\" connectors=\"in-vm\" entries=\"java:/ConnectionFactory\"/>\n"
            + "<shared-store-colocated backup-port-offset=\"1000\"/>"
            + "            </server>\n"
            + "        </subsystem>"
            + "    </profile>\n"
            + "</server>";

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void replace() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_EXPECTED_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_EXPECTED_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new SetSharedStoreColocated.Builder()
                .backupPortOffset(1000)
                .build());
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYTEM_REPLACE_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void add() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYTEM_ORIGINAL_ACTIVEMQ, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYTEM_ORIGINAL_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new SetSharedStoreColocated.Builder()
                .backupPortOffset(10005)
                .requestBackup()
                .maxBackups(1)
                .build());
        System.out.println(Files.toString(cfg, Charsets.UTF_8));
        assertXmlIdentical(SUBSYTEM_EXPECTED_ACTIVEMQ, Files.toString(cfg, Charsets.UTF_8));
    }
}
