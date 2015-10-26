package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

/**
 *
 * @author mstyk
 */
@RunWith(Arquillian.class)
public class AddDiscoveryGroupOnlineTest {

    private static final String TEST_NAME = "testName";
    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void close() throws IOException, CliException, OperationException,
            CommandFailedException, InterruptedException, TimeoutException {
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.DISCOVERY_GROUP, TEST_NAME));
        administration.reloadIfRequired();
        client.close();
    }

    @Test
    public void add_commandSucceeds() throws CommandFailedException, IOException,
            InterruptedException, TimeoutException {

        client.apply(new AddDiscoveryGroup.Builder(TEST_NAME)
                .initialWaitTimeout(100)
                .refreshTimeout(1)
                .socketBinding("messaging")
                .build());

        administration.reloadIfRequired();

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.DISCOVERY_GROUP, TEST_NAME),
                "socket-binding");
        result.assertSuccess();
    }

    @Test
    public void add2_commandSucceeds() throws CommandFailedException, IOException,
            InterruptedException, TimeoutException {
        client.apply(new AddDiscoveryGroup.Builder(TEST_NAME)
                .jGroupsChannel("channel")
                .jGroupsStack("stack")
                .build());

        administration.reloadIfRequired();

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.DISCOVERY_GROUP, TEST_NAME),
                "jgroups-stack");
        result.assertSuccess();
    }
}
