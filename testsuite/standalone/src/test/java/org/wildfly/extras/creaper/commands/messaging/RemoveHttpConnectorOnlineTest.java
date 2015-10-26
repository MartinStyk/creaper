package org.wildfly.extras.creaper.commands.messaging;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class RemoveHttpConnectorOnlineTest {

    private Operations ops;
    private OnlineManagementClient client;
    private Administration administration;

    private static final String TEST_NAME = "conTest";

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void cleanup() throws Exception {
        try {
            ops.removeIfExists(MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                    .and(MessagingConstants.HTTP_CONNECTOR, TEST_NAME));
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void removeHttpConnector() throws CommandFailedException, IOException, OperationException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("http-upgrade-endpoint", "http-acceptor");

        client.apply(new AddHttpConnector.Builder(TEST_NAME)
                .params(params)
                .socketBinding("http")
                .build());

        assertTrue("The connector should be created", ops.exists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and(MessagingConstants.HTTP_CONNECTOR, TEST_NAME)));

        client.apply(new RemoveHttpConnector(TEST_NAME));

        assertFalse("The connector should be removed", ops.exists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                        .and(MessagingConstants.HTTP_CONNECTOR, TEST_NAME)));
    }
}
