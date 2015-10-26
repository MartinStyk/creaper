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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

@RunWith(Arquillian.class)
public class AddCoreBridgeOnlineTest {

    private static final String TEST_BRIDGE_NAME = "brTest";
    private static final String TEST_QUEUE_NAME = "startQ";
    private static final String TEST_QUEUE_NAME_2 = "endQ";
    private static final long RETRY_INT = 1000;
    private static final BigDecimal RETRY_INT_MULT = new BigDecimal(1.5, MathContext.UNLIMITED);

    private OnlineManagementClient client;
    private Operations ops;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
    }

    @After
    public void close() throws IOException, CliException, OperationException, CommandFailedException {
        ops.removeIfExists(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.CORE_BRIDGE, TEST_BRIDGE_NAME));
        client.close();
    }

    @Test
    public void addSimpleBridge_commandSucceeds() throws CommandFailedException, IOException {

        List<String> staticCons = new ArrayList<String>();
        staticCons.add("connector1");
        staticCons.add("connector2");

        client.apply(new AddCoreBridge.Builder(TEST_BRIDGE_NAME)
                .queueName(TEST_QUEUE_NAME)
                .forwardingAddress(TEST_QUEUE_NAME_2)
                .retryInterval(RETRY_INT)
                .retryIntervalMultiplier(RETRY_INT_MULT)
                .staticConnectors(staticCons)
                .build());

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.CORE_BRIDGE, TEST_BRIDGE_NAME),
                "retry-interval");
        result.assertSuccess();
    }

    @Test
    public void addAllParamsBridge_commandSucceeds() throws CommandFailedException, IOException {

        List<String> staticCons = new ArrayList<String>();
        staticCons.add("connector1");
        staticCons.add("connector2");

        client.apply(new AddCoreBridge.Builder(TEST_BRIDGE_NAME + "1")
                .queueName(TEST_QUEUE_NAME)
                .forwardingAddress(TEST_QUEUE_NAME_2)
                .retryInterval(RETRY_INT)
                .retryIntervalMultiplier(RETRY_INT_MULT)
                .checkPeriod(100)
                .confirmationWindowSize(5000)
                .connectionTtl(5000)
                .filter("filter")
                .ha()
                .initialConnectAttempts(10)
                .maxRetryInterval(100)
                .minLargeMessageSize(100)
                .password("pswd")
                .reconnectAttempts(10)
                .reconnectAttemptsOnSameNode(5)
                .useDuplicateDetection()
                .user("tester")
                .staticConnectors(staticCons)
                .build());

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.CORE_BRIDGE, TEST_BRIDGE_NAME + "1"),
                "retry-interval");
        result.assertSuccess();
    }
}
