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

@Import({FaCashbackTransactionPublisherConnector.class})
@TestPropertySource(
        locations = "classpath:config/testFaCustomerTransactionPublisher.properties",
        properties = {
                "connectors.eventConfigurations.items.FaCashbackTransactionPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}"
        })
public class FaCashbackTransactionPublisherConnectorTest extends
        BaseEventConnectorTest<Transaction, Boolean, Transaction, Void, FaCashbackTransactionPublisherConnector> {

    @Value("${connectors.eventConfigurations.items.FaCashbackTransactionPublisherConnector.topic}")
    private String topic;

    @Autowired
    private FaCashbackTransactionPublisherConnector faCashbackTransactionPublisherConnector;

    @Override
    protected FaCashbackTransactionPublisherConnector getEventConnector() {
        return faCashbackTransactionPublisherConnector;
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