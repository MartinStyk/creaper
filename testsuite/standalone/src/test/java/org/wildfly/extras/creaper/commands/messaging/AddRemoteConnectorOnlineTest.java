package org.wildfly.extras.creaper.commands.messaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jboss.arquillian.junit.Arquillian;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author mstyk
 */
@RunWith(Arquillian.class)
public class AddRemoteConnectorOnlineTest {

    private static final String TEST_CONNECTOR_NAME = "conTest";

    private OnlineManagementClient client;
    private Operations ops;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
    }

    //@After
    public void close() throws IOException, CliException, OperationException, CommandFailedException {
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.REMOTE_CONNECTOR, TEST_CONNECTOR_NAME));
        client.close();
    }

    @Test
    public void addSimpleConnector_commandSucceeds() throws CommandFailedException, IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("use-nio", "true");
        params.put("use-nio-global-worker-pool", "true");

        client.apply(new AddRemoteConnector.Builder(TEST_CONNECTOR_NAME)
                .params(params)
                .socketBinding("http")
                .build());

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.REMOTE_CONNECTOR, TEST_CONNECTOR_NAME),
                "socket-binding");
        result.assertSuccess();
    }

    @Test
    public void addDuplicateAndRemove_commandSucceeds() throws CommandFailedException, IOException, OperationException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("use-nio", "true");
        params.put("use-nio-global-worker-pool", "true");

        client.apply(new AddRemoteConnector.Builder(TEST_CONNECTOR_NAME)
                .params(params)
                .socketBinding("http")
                .build());

        assertTrue("The connector should be created", ops.exists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.REMOTE_CONNECTOR, TEST_CONNECTOR_NAME)));

        client.apply(new AddRemoteConnector.Builder(TEST_CONNECTOR_NAME)
                .params(params)
                .socketBinding("http")
                .replaceExisting()
                .build());

        assertTrue("The connector should be replaced", ops.exists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.REMOTE_CONNECTOR, TEST_CONNECTOR_NAME)));

        client.apply(new RemoveRemoteConnector(TEST_CONNECTOR_NAME));

        assertFalse("The connector should be removed", ops.exists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.REMOTE_CONNECTOR, TEST_CONNECTOR_NAME)));
    }
}
