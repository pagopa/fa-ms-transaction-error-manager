package it.gov.pagopa.fa.transaction_error_manager.publisher;

import eu.sia.meda.event.BaseEventConnectorTest;
import eu.sia.meda.util.TestUtils;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Test class for the PointTransactionPublisherConnector class
 */

@Import({FaTransactionPublisherConnector.class})
@TestPropertySource(
        locations = "classpath:config/testFaTransactionPublisher.properties",
        properties = {
                "connectors.eventConfigurations.items.FaTransactionPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}"
        })
public class FaTransactionPublisherConnectorTest extends
        BaseEventConnectorTest<Transaction, Boolean, Transaction, Void, FaTransactionPublisherConnector> {

    @Value("${connectors.eventConfigurations.items.FaTransactionPublisherConnector.topic}")
    private String topic;

    @Autowired
    private FaTransactionPublisherConnector faTransactionPublisherConnector;

    @Override
    protected FaTransactionPublisherConnector getEventConnector() {
        return faTransactionPublisherConnector;
    }

    @Override
    protected Transaction getRequestObject() {
        return TestUtils.mockInstance(new Transaction());
    }

    @Override
    protected String getTopic() {
        return topic;
    }

}