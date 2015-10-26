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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

@RunWith(Arquillian.class)
public class AddClusterConnectionOnlineTest {

    private static final String TEST_NAME = "testConnection";
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
                .and(MessagingConstants.CLUSTER_CONNECTION, TEST_NAME));
        administration.reloadIfRequired();
        client.close();
    }

    @Test
    public void addAllParams_commandSucceeds() throws CommandFailedException, IOException,
            InterruptedException, TimeoutException {

        List<String> staticCons = new ArrayList<String>();
        staticCons.add("connector1");
        staticCons.add("connector2");

        client.apply(new AddClusterConnection.Builder(TEST_NAME)
                .checkPeriod(1)
                .confirmationWindowSize(10)
                .connectionTtl(100)
                .connectorName("connector")
                .allowDirectConnectionsOnly()
                .callFailoverTimeout(100)
                .callTimeout(101)
                .checkPeriod(1000)
                .clusterConnectionAddress("address")
                .initialConnectAttempts(5)
                .maxHops(50)
                .maxRetryInterval(500)
                .messageLoadBalancingType("BLAA")
                .minLargeMessageSize(1)
                .notificationAttempts(7)
                .notificationInterval(10)
                .reconnectAttempts(6)
                .retryInterval(60)
                .retryIntervalMultiplier(BigDecimal.ONE)
                .staticConnectors(staticCons)
                .useDuplicateDetection(false)
                .build());

        administration.reloadIfRequired();

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.CLUSTER_CONNECTION, TEST_NAME),
                "retry-interval");
        result.assertSuccess();
    }
}
