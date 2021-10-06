package it.gov.pagopa.fa.transaction_error_manager.service;

import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.fa.transaction_error_manager.publisher.FaTransactionPublisherConnector;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import it.gov.pagopa.fa.transaction_error_manager.service.transformer.HeaderAwareRequestTransformer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the PointTransactionPublisherService, defines the service used for the interaction
 * with the PointTransactionPublisherConnector
 */

@Service
class FaTransactionPublisherServiceImpl implements FaTransactionPublisherService {

    private final FaTransactionPublisherConnector faTransactionPublisherConnector;
    private final HeaderAwareRequestTransformer<Transaction> simpleEventRequestTransformer;
    private final SimpleEventResponseTransformer simpleEventResponseTransformer;

    @Autowired
    public FaTransactionPublisherServiceImpl(FaTransactionPublisherConnector faTransactionPublisherConnector,
                                             HeaderAwareRequestTransformer<Transaction> simpleEventRequestTransformer,
                                             SimpleEventResponseTransformer simpleEventResponseTransformer) {
        this.faTransactionPublisherConnector = faTransactionPublisherConnector;
        this.simpleEventRequestTransformer = simpleEventRequestTransformer;
        this.simpleEventResponseTransformer = simpleEventResponseTransformer;
    }

    /**
     * Calls the PointTransactionPublisherService, passing the transaction to be used as message payload
     *
     * @param transaction OutgoingTransaction instance to be used as payload for the outbound channel used bu the related connector
     */

    @Override
    public void publishFaTransactionEvent(Transaction transaction, RecordHeaders recordHeaders) {
        faTransactionPublisherConnector.doCall(
                transaction, simpleEventRequestTransformer, simpleEventResponseTransformer, recordHeaders);
    }
}
