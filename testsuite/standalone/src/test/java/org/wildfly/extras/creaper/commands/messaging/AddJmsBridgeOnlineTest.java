package org.wildfly.extras.creaper.commands.messaging;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;

@RunWith(Arquillian.class)
public class AddJmsBridgeOnlineTest {

    private static final String TEST_BRIDGE_NAME = "brTest";
    private static final String TEST_QUEUE_NAME = "sourceQueue";
    private static final String TEST_QUEUE_NAME_2 = "targetQueue";
    private static final List<String> TEST_QUEUE_ENTRIES
            = Collections.singletonList("java:/jms/queue/" + TEST_QUEUE_NAME);
    private static final List<String> TEST_QUEUE_ENTRIES_2
            = Collections.singletonList("java:/jms/queue/" + TEST_QUEUE_NAME_2);

    private OnlineManagementClient client;
    private Operations ops;

    @Before
    public void connect() throws IOException, CommandFailedException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        client.apply(new AddQueue.Builder(TEST_QUEUE_NAME).jndiEntries(TEST_QUEUE_ENTRIES).build());
        client.apply(new AddQueue.Builder(TEST_QUEUE_NAME_2).jndiEntries(TEST_QUEUE_ENTRIES_2).build());
    }

    @After
    public void close() throws IOException, CliException, OperationException, CommandFailedException {
        ops.removeIfExists(
                MessagingUtils.subsystemAddress(client).and(MessagingConstants.JMS_BRIDGE, TEST_BRIDGE_NAME));
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-queue", TEST_QUEUE_NAME));
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME).and("jms-queue", TEST_QUEUE_NAME_2));
        client.close();
    }

    @Test
    public void addAllParamsBridge_commandSucceeds() throws CommandFailedException, IOException {

        Map<String, String> sourceCtx = new HashMap<String, String>();
        sourceCtx.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
        sourceCtx.put("java.naming.provider.url", "http-remoting://10.16.100.40:8080");
        Map<String, String> targetCtx = new HashMap<String, String>();
        targetCtx.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
        targetCtx.put("java.naming.provider.url", "http-remoting://10.16.100.40:8080");
        targetCtx.put("jboss.naming.client.connect.timeout", "3000");

        try {
            client.apply(new AddJmsBridge.Builder(TEST_BRIDGE_NAME + "1")
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
        } catch (Exception e) {
            //bridge cannot be deployed, only testing its existence
        }
        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.subsystemAddress(client)
                .and(MessagingConstants.JMS_BRIDGE, TEST_BRIDGE_NAME + "1"),
                "client-id");
        result.assertSuccess();
    }
}
