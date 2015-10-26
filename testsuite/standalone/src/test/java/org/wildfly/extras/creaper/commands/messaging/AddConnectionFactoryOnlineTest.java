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
public class AddConnectionFactoryOnlineTest {

    private static final String TEST_NAME = "testConnectionFactory";
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
                .and(MessagingConstants.CONNECTION_FACTORY, TEST_NAME));
        administration.reloadIfRequired();
        client.close();
    }

    @Test
    public void addAllParams_commandSucceeds() throws CommandFailedException, IOException,
            InterruptedException, TimeoutException {

        List<String> staticCons = new ArrayList<String>();
        staticCons.add("http-connector");
        staticCons.add("http-connector-throughput");

        List<String> enties = new ArrayList<>();
        enties.add("jms/connectionFactory1");
        enties.add("jms/connectionFactory2");

        client.apply(new AddConnectionFactory.Builder(TEST_NAME)
                .autoGroup()
                .blockOnAcknowledge()
                .blockOnDurableSend(true)
                .blockOnNonDurableSend()
                .cacheLargeMessageClient()
                .callFailoverTimeout(1)
                .callTimeout(2)
                .clientFailureCheckPeriod(3)
                .clientId("cId")
                .compressLargeMessages()
                .confirmationWindowSize(4)
                .connectionLoadBalancingPolicyClassName("org.apache.activemq.artemis.api.core.client.loadbalance.RoundRobinConnectionLoadBalancingPolicy")
                .connectonTtl(5)
                .connectors(staticCons)
                .consumerMaxRate(6)
                .consumerWindowSize(7)
                .dupsOkBatchSize(8)
                .entries(enties)
                .factoryType("TOPIC")
                .failoverOnInitialConnection()
                .groupId("gid")
                .ha()
                .maxRetryInterval(9)
                .minLargeMessageSize(10)
                .preAcknowledge()
                .producerMaxRate(11)
                .reconnectAttempts(12)
                .retryInterval(13)
                .retryIntervalMultiplier(BigDecimal.TEN)
                .scheduledThreadPoolMaxSize(14)
                .threadPoolMaxSize(15)
                .transactionBatchSize(16)
                .useGlobalPools(false)
                .build()
        );

        administration.reloadIfRequired();

        ModelNodeResult result = ops.readAttribute(
                MessagingUtils.address(client, MessagingUtils.DEFAULT_SERVER_NAME)
                .and(MessagingConstants.CONNECTION_FACTORY, TEST_NAME),
                "retry-interval");
        result.assertSuccess();
    }
}
