package it.gov.pagopa.fa.transaction_error_manager.command;

import eu.sia.meda.core.command.BaseCommand;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import it.gov.pagopa.fa.transaction_error_manager.model.TransactionCommandModel;
import it.gov.pagopa.fa.transaction_error_manager.model.constants.TransactionRecordConstants;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import it.gov.pagopa.fa.transaction_error_manager.service.TransactionRecordService;
import it.gov.pagopa.fa.transaction_error_manager.service.mapper.TransactionMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Base implementation of the SaveTransactionCommandInterface, extending Meda BaseCommand class, the command
 * represents the class interacted with at api level, hiding the multiple calls to the integration connectors
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
class SaveTransactionRecordCommandImpl extends BaseCommand<Boolean> implements SaveTransactionRecordCommand {

    private TransactionRecordService transactionRecordService;
    private TransactionCommandModel transactionCommandModel;
    private TransactionMapper transactionMapper;


    public SaveTransactionRecordCommandImpl(TransactionCommandModel transactionCommandModel) {
        this.transactionCommandModel = transactionCommandModel;
    }

    public SaveTransactionRecordCommandImpl(
            TransactionCommandModel transactionCommandModel,
            TransactionRecordService transactionRecordService,
            TransactionMapper transactionMapper) {
        this.transactionCommandModel = transactionCommandModel;
        this.transactionRecordService = transactionRecordService;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Implementation of the MEDA Command doExecute method, contains the logic for the inbound transaction
     * management, calls the REST endpoint to check if it the related paymentInstrument is active, and eventually
     * sends the Transaction to the proper outbound channel. In case of an error, send a
     *
     * @return boolean to indicate if the command is successfully executed
     */

    @SneakyThrows
    @Override
    public Boolean doExecute() {

        Transaction transaction = transactionCommandModel.getPayload();
        Headers headers = transactionCommandModel.getHeaders();

        try {

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss.SSSXXXXX");
            OffsetDateTime execStart = OffsetDateTime.now();

            TransactionRecord transactionRecord =
                    transactionMapper.mapTransactionRecord(transaction);
            transactionRecord.setToResubmit(false);
            Header exceptionHeader = headers.lastHeader(TransactionRecordConstants.EXCEPTION_HEADER);
            transactionRecord.setExceptionMessage(
                    exceptionHeader == null ? null : new String(exceptionHeader.value()));
            Header listenerHeader = headers.lastHeader(TransactionRecordConstants.LISTENER_HEADER);
            transactionRecord.setOriginListener(listenerHeader == null ? null
                    : new String(listenerHeader.value()));
            String originTopic = listenerHeader == null ? "rtd-trx" : TransactionRecordConstants
                    .originListenerToTopic.get(new String(listenerHeader.value()));
            transactionRecord.setOriginTopic(originTopic);
            Header requestIdHeader = headers.lastHeader(TransactionRecordConstants.REQUEST_ID_HEADER);
            transactionRecord.setOriginRequestId(
                    requestIdHeader == null ? null : new String(requestIdHeader.value()));
            Header validationTimeHeader = headers.lastHeader(
                    TransactionRecordConstants.CUSTOMER_VALIDATION_DATETIME_HEADER);
            transactionRecord.setCustomerValidationDate(
                    validationTimeHeader == null || validationTimeHeader.value() == null ? null :
                            OffsetDateTime.parse(new String(validationTimeHeader.value())));
            transactionRecord.setRecordId(UUID.randomUUID().toString());
            transactionRecordService.saveTransactionRecord(transactionRecord);

            OffsetDateTime endExec = OffsetDateTime.now();
            log.info("Executed SaveTransactionRecordCommandImpl for transaction: {}, {}, {} " +
                            "- Started at {}, Ended at {} - Total exec time: {}",
                    transaction.getIdTrxAcquirer(),
                    transaction.getAcquirerCode(),
                    transaction.getTrxDate(),
                    dateTimeFormatter.format(execStart),
                    dateTimeFormatter.format(endExec),
                    ChronoUnit.MILLIS.between(execStart, endExec));

            return true;

        } catch (Exception e) {

            if (Objects.nonNull(transaction) && logger.isErrorEnabled()) {
                logger.error("Error occurred while attempting to record the transaction: {}, {}, {}",
                        transaction.getIdTrxAcquirer(), transaction.getAcquirerCode(), transaction.getTrxDate());
                logger.error(e.getMessage(), e);
            }

            throw e;
        }

    }

    @Autowired
    public void setTransactionMapper(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    @Autowired
    public void setTransactionRecordService(
            TransactionRecordService transactionRecordService) {
        this.transactionRecordService = transactionRecordService;
    }

}
