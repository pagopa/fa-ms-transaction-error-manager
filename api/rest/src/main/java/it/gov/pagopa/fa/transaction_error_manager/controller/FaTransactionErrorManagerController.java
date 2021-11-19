package it.gov.pagopa.fa.transaction_error_manager.controller;

import io.swagger.annotations.Api;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller to expose MicroService
 */
@Api(tags = "Bonus Pagamenti Digitali payment-instrument Controller")
@RequestMapping("/fa/transaction-error-manager")
public interface FaTransactionErrorManagerController {

    @PostMapping(value = "/resubmitTransactions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    void resubmitTransactions() throws Exception;

    @PostMapping(value = "/transaction/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    void transactionStatus(
            @RequestBody TransactionRecord transaction
            ) throws Exception;

}
