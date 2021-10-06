package it.gov.pagopa.fa.transaction_error_manager.model.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionRecordConstants {

    public static final String CITIZEN_VALIDATION_DATETIME_HEADER = "CITIZEN_VALIDATION_DATETIME";
    public static final String EXCEPTION_HEADER = "ERROR_DESC";
    public static final String LISTENER_HEADER = "LISTENER";
    public static final String REQUEST_ID_HEADER = "x-request-id";
    public static final String USER_ID_HEADER = "x-user-id";

    public static final Map<String, String> originListenerToTopic;

    static {
        originListenerToTopic = new HashMap<>();
        originListenerToTopic.put(
                "it.gov.pagopa.bpd.payment_instrument.listener.OnTransactionFilterRequestListener",
                "rtd-trx");
        originListenerToTopic.put(
                "it.gov.pagopa.fa.point_processor.listener.OnTransactionProcessRequestListener",
                "fa-trx");
        originListenerToTopic.put(
                "it.gov.pagopa.bpd.winning_transaction.listener.OnTransactionSaveRequestListener",
                "bpd-trx-cashback");
    }

}
