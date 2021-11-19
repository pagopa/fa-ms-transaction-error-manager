package it.gov.pagopa.fa.transaction_error_manager.model.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionRecordConstants {

    public static final String CUSTOMER_VALIDATION_DATETIME_HEADER = "CUSTOMER_VALIDATION_DATETIME";
    public static final String EXCEPTION_HEADER = "ERROR_DESC";
    public static final String LISTENER_HEADER = "LISTENER";
    public static final String REQUEST_ID_HEADER = "x-request-id";
    public static final String USER_ID_HEADER = "x-user-id";

    public static final Map<String, String> originListenerToTopic;

    static {
        originListenerToTopic = new HashMap<>();
        originListenerToTopic.put(
                "it.gov.pagopa.fa.transaction.listener.OnTransactionValidateRequestListener",
                "rtd-trx");
        originListenerToTopic.put(
                "it.gov.pagopa.fa.transaction.listener.OnTransactionProcessRequestListener",
                "fa-trx");
        originListenerToTopic.put(
                "it.gov.pagopa.fa.payment_instrument.listener.OnTransactionFilterRequestListener",
                "fa-trx-payment-instrument");
        originListenerToTopic.put(
                "it.gov.pagopa.fa.merchant.listener.OnTransactionFilterRequestListener",
                "fa-trx-merchant");
        originListenerToTopic.put(
                "it.gov.pagopa.fa.customer.listener.OnTransactionFilterRequestListener",
                "fa-trx-customer");
    }

}
