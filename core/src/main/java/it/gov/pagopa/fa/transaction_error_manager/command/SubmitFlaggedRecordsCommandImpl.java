package it.gov.pagopa.fa.transaction_error_manager.command;

import eu.sia.meda.async.util.AsyncUtils;
import eu.sia.meda.core.command.BaseCommand;
import eu.sia.meda.core.interceptors.BaseContextHolder;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import it.gov.pagopa.fa.transaction_error_manager.model.constants.TransactionRecordConstants;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import it.gov.pagopa.fa.transaction_error_manager.service.FaCashbackTransactionPublisherService;
import it.gov.pagopa.fa.transaction_error_manager.service.FaTransactionPublisherService;
import it.gov.pagopa.fa.transaction_error_manager.service.RtdTransactionPublisherService;
import it.gov.pagopa.fa.transaction_error_manager.service.TransactionRecordService;
import it.gov.pagopa.fa.transaction_error_manager.service.mapper.TransactionMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base implementation of the SaveTransactionCommandInterface, extending Meda BaseCommand class, the command
 * represents the class interacted with at api level, hiding the multiple calls to the integration connectors
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
class SubmitFlaggedRecordsCommandImpl extends BaseCommand<Boolean> implements SubmitFlaggedRecordsCommand {

    private static final String DATE_TIME_PATTERN = "dd/MM/yyyy hh:mm:ss.SSSXXXXX";

    private TransactionRecordService transactionRecordService;
    private RtdTransactionPublisherService rtdTransactionPublisherService;
    private FaTransactionPublisherService faTransactionPublisherService;
    private FaCashbackTransactionPublisherService faCashbackTransactionPublisherService;
    private TransactionMapper transactionMapper;
    private final Consumer<TransactionRecord> handleTransactionRecordAsynchronously =
            t -> callAsyncService(() -> handleTransactionRecord(t) );

    public SubmitFlaggedRecordsCommandImpl(
            TransactionRecordService transactionRecordService,
            RtdTransactionPublisherService rtdTransactionPublisherService,
            FaTransactionPublisherService faTransactionPublisherService,
            FaCashbackTransactionPublisherService faCashbackTransactionPublisherService,
            TransactionMapper transactionMapper) {
        this.transactionRecordService = transactionRecordService;
        this.rtdTransactionPublisherService = rtdTransactionPublisherService;
        this.faTransactionPublisherService = faTransactionPublisherService;
        this.transactionMapper = transactionMapper;
        this.faCashbackTransactionPublisherService = faCashbackTransactionPublisherService;
    }

    /**
     * Implementation of the MEDA Command doExecute method, contains the logic for the inbound transaction
     * management, calls the REST endpoint to check if it the related paymentInstrument is active, and eventually
     * sends the Transaction to the proper outbound channel. In case of an error, send a
     *
     * @return boolean to indicate if the command is successfully executed
     */

    @Override
    public Boolean doExecute() {

        this.callAsyncService(() -> {
            try {

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
                OffsetDateTime execStart = OffsetDateTime.now();

                List<TransactionRecord> transactionRecordList = transactionRecordService.findRecordsToResubmit();

                transactionRecordList.forEach( this.handleTransactionRecordAsynchronously );

                OffsetDateTime endExec = OffsetDateTime.now();
                log.info("Executed SubmitFlaggedRecordsCommand for transaction" +
                                "- Started at {}, Ended at {} - Total exec time: {}",
                        dateTimeFormatter.format(execStart),
                        dateTimeFormatter.format(endExec),
                        ChronoUnit.MILLIS.between(execStart, endExec));

                return true;

            } catch (Exception e) {
                logger.error("Error occurred while attempting to submit flagged records");
                throw e;
            }

        });

        return true;
    }

    private boolean handleTransactionRecord(TransactionRecord transactionRecord) {
        try {

            final Transaction transaction = transactionMapper.mapTransaction(transactionRecord);
            final RecordHeaders recordHeaders = buildHeaders(transactionRecord);

            switch (transactionRecord.getOriginTopic()) {
                case "fa-trx":
                    faTransactionPublisherService.publishFaTransactionEvent(transaction, recordHeaders);
                    break;
                case "rtd-trx":
                    rtdTransactionPublisherService.publishRtdTransactionEvent(transaction, recordHeaders);
                    break;
                case "bpd-trx-cashback":
                    faCashbackTransactionPublisherService
                            .publishFaCashbackTransactionEvent(transaction, recordHeaders);
                    break;
                default:
                    log.warn("Origin topic {} not recognized!", transactionRecord.getOriginTopic());
                    break;
            }

            transactionRecord.setToResubmit(false);
            transactionRecord.setLastResubmitDate(OffsetDateTime.now());
            transactionRecordService.saveTransactionRecord(transactionRecord);

            return true;

        } catch (Exception e) {
            logger.error("Error occurred while attempting to submit flagged record: {}, {}, {}, {}, {}",
                    transactionRecord.getAcquirerCode(),
                    transactionRecord.getIdTrxAcquirer(),
                    transactionRecord.getTrxDate(),
                    transactionRecord.getAcquirerId(),
                    transactionRecord.getOperationType());
            throw e;
        }
    }

    protected void setAsyncUtils(AsyncUtils asyncUtils) {
        this.asyncUtils = asyncUtils;
    }

    @NotNull
    private RecordHeaders buildHeaders(TransactionRecord transactionRecord) {
        String requestId = buildRequestId(transactionRecord);
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add(TransactionRecordConstants.REQUEST_ID_HEADER, requestId.getBytes());
        BaseContextHolder.getApplicationContext().setRequestId(requestId);
        recordHeaders.add(TransactionRecordConstants.USER_ID_HEADER,
                "rtd-ms-transaction-error-manager".getBytes());
        recordHeaders.add(TransactionRecordConstants.LISTENER_HEADER,
                transactionRecord.getOriginListener() == null ?
                        null :
                        transactionRecord.getOriginListener().getBytes());
        recordHeaders.add(TransactionRecordConstants.CUSTOMER_VALIDATION_DATETIME_HEADER,
                transactionRecord.getCustomerValidationDate() == null ?
                        null :
                        transactionRecord.getCustomerValidationDate().toString().getBytes());
        return recordHeaders;
    }

    @NotNull
    private String buildRequestId(TransactionRecord transactionRecord) {
        return transactionRecord.getOriginRequestId() == null ?
                "Resubmitted" :
                "Resubmitted;".concat(transactionRecord.getOriginRequestId());
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

    @Autowired
    public void setRtdTransactionPublisherService(
            RtdTransactionPublisherService rtdTransactionPublisherService) {
        this.rtdTransactionPublisherService = rtdTransactionPublisherService;
    }

    @Autowired
    public void setFaTransactionPublisherService(
            FaTransactionPublisherService faTransactionPublisherService) {
        this.faTransactionPublisherService = faTransactionPublisherService;
    }

    @Autowired
    public void setFaCashbackTransactionPublisherService(
            FaCashbackTransactionPublisherService faCashbackTransactionPublisherService) {
        this.faCashbackTransactionPublisherService = faCashbackTransactionPublisherService;
    }

}
