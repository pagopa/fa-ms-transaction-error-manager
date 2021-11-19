package it.gov.pagopa.fa.transaction_error_manager.command;

import eu.sia.meda.BaseTest;
import eu.sia.meda.async.util.AsyncUtils;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import it.gov.pagopa.fa.transaction_error_manager.service.FaCashbackTransactionPublisherService;
import it.gov.pagopa.fa.transaction_error_manager.service.FaTransactionPublisherService;
import it.gov.pagopa.fa.transaction_error_manager.service.RtdTransactionPublisherService;
import it.gov.pagopa.fa.transaction_error_manager.service.TransactionRecordService;
import it.gov.pagopa.fa.transaction_error_manager.service.mapper.TransactionMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class SubmitFlaggedRecordsCommandImplTest extends BaseTest {

    @Rule public ExpectedException exceptionRule = ExpectedException.none();
    @Mock TransactionRecordService transactionRecordService;
    @Mock RtdTransactionPublisherService rtdTransactionPublisherService;
    @Mock FaTransactionPublisherService faTransactionPublisherService;
    @Mock FaCashbackTransactionPublisherService faCashbackTransactionPublisherService;
    @Spy  TransactionMapper transactionMapperSpy;
    @Spy  AsyncUtils asyncUtilsSpy;

    @Before
    public void initTest() {
        initMocks(this);
    }

    @Test
    public void TestExecute_OK_RTD() {
        SubmitFlaggedRecordsCommandImpl saveTransactionCommand = prepareTest("rtd-trx");
        Boolean executed = saveTransactionCommand.doExecute();
        assertTrue(executed);
        verify(rtdTransactionPublisherService).publishRtdTransactionEvent(eq(getRequestModel()), any());
    }

    @Test
    public void TestExecute_OK_FA() {
        SubmitFlaggedRecordsCommandImpl saveTransactionCommand = prepareTest(
                "fa-trx", "originRequestId", "originListener");
        Boolean executed = saveTransactionCommand.doExecute();
        assertTrue(executed);
        verify(faTransactionPublisherService).publishFaTransactionEvent(eq(getRequestModel()), any());
    }

    @Test
    public void TestExecute_OK_FA_case2() {
        SubmitFlaggedRecordsCommandImpl saveTransactionCommand = prepareTest(
                "fa-trx", "originRequestId", "originiListener", "validationDate");
        Boolean executed = saveTransactionCommand.doExecute();
        assertTrue(executed);
        verify(faTransactionPublisherService).publishFaTransactionEvent(eq(getRequestModel()), any());
    }

    @Test
    public void TestExecute_OK_BPD() {
        SubmitFlaggedRecordsCommandImpl saveTransactionCommand = prepareTest("bpd-trx-cashback");
        Boolean executed = saveTransactionCommand.doExecute();
        assertTrue(executed);
        verify(faCashbackTransactionPublisherService).publishFaCashbackTransactionEvent(eq(getRequestModel()), any());
    }

    @Test
    public void TestExecute_OK_NotRecognized() {
        TransactionRecord transactionRecord = getSavedModel();
        transactionRecord.setOriginTopic("not_recognized_value");
        doReturn(singletonList(transactionRecord)).when(transactionRecordService).findRecordsToResubmit();
        SubmitFlaggedRecordsCommandImpl saveTransactionCommand = new SubmitFlaggedRecordsCommandImpl(
                transactionRecordService,
                rtdTransactionPublisherService,
                faTransactionPublisherService,
                faCashbackTransactionPublisherService,
                transactionMapperSpy);
        saveTransactionCommand.setTransactionRecordService(transactionRecordService);
        saveTransactionCommand.setRtdTransactionPublisherService(rtdTransactionPublisherService);
        saveTransactionCommand.setFaTransactionPublisherService(faTransactionPublisherService);
        saveTransactionCommand.setFaCashbackTransactionPublisherService(faCashbackTransactionPublisherService);
        saveTransactionCommand.setTransactionMapper(transactionMapperSpy);
        saveTransactionCommand.setAsyncUtils(asyncUtilsSpy);
        Boolean executed = saveTransactionCommand.doExecute();
        assertTrue(executed);
        verifyNoPublish();
    }

    @Test
    public void throw_exception_when_service_error(){
        SubmitFlaggedRecordsCommandImpl saveTransactionCommand = new SubmitFlaggedRecordsCommandImpl(
                transactionRecordService,
                rtdTransactionPublisherService,
                faTransactionPublisherService,
                faCashbackTransactionPublisherService,
                transactionMapperSpy);
        saveTransactionCommand.setAsyncUtils(asyncUtilsSpy);
        doThrow(new RuntimeException("")).when(transactionRecordService).findRecordsToResubmit();
        try{
            saveTransactionCommand.doExecute();
            fail("Expected an exception here");
        } catch (Throwable t){
            assertEquals(RuntimeException.class, t.getClass());
            verifyNoPublish();
        }
    }

    @Test
    public void throw_exception_when_handle_transaction_error(){
        SubmitFlaggedRecordsCommandImpl saveTransactionCommand = prepareTest("fa-trx");
        doThrow(new RuntimeException("")).when(transactionRecordService).saveTransactionRecord(any());
        try{
            saveTransactionCommand.doExecute();
            fail("Expected an exception here");
        } catch (Throwable t){
            assertEquals(RuntimeException.class, t.getClass());
            verify(faTransactionPublisherService).publishFaTransactionEvent(eq(getRequestModel()), any());
        }
    }

    private SubmitFlaggedRecordsCommandImpl prepareTest(final String... args) {
        TransactionRecord transactionRecord = getSavedModel();
        transactionRecord.setOriginTopic(args[0]);
        transactionRecord.setOriginRequestId(args.length > 1 ? args[1] : null);
        transactionRecord.setOriginListener(args.length > 2 ? args[2] : null);
        transactionRecord.setCustomerValidationDate(args.length > 3 ? OffsetDateTime.now() : null);
        doReturn(singletonList(transactionRecord)).when(transactionRecordService).findRecordsToResubmit();
        SubmitFlaggedRecordsCommandImpl saveTransactionCommand = new SubmitFlaggedRecordsCommandImpl(
                transactionRecordService,
                rtdTransactionPublisherService,
                faTransactionPublisherService,
                faCashbackTransactionPublisherService,
                transactionMapperSpy);
        saveTransactionCommand.setAsyncUtils(asyncUtilsSpy);
        return saveTransactionCommand;
    }

    protected Transaction getRequestModel() {
        return Transaction.builder()
                .idTrxAcquirer("1")
                .acquirerCode("001")
                .trxDate(OffsetDateTime.parse("2020-04-09T16:22:45.304Z"))
                .amount(BigDecimal.valueOf(1313.13))
                .operationType("00")
                .hpan("hpan")
                .merchantId("0")
                .circuitType("00")
                .mcc("813")
                .idTrxIssuer("0")
                .amountCurrency("833")
                .correlationId("1")
                .acquirerId("0")
                .bin("000001")
                .terminalId("0")
                .fiscalCode("fiscalCode")
                .build();
    }

    protected TransactionRecord getSavedModel() {
        return TransactionRecord.builder()
                .idTrxAcquirer("1")
                .acquirerCode("001")
                .trxDate(OffsetDateTime.parse("2020-04-09T16:22:45.304Z"))
                .amount(BigDecimal.valueOf(1313.13))
                .operationType("00")
                .hpan("hpan")
                .merchantId("0")
                .circuitType("00")
                .mcc("813")
                .idTrxIssuer("0")
                .amountCurrency("833")
                .correlationId("1")
                .acquirerId("0")
                .awardPeriodId(1L)
                .score(BigDecimal.ONE)
                .bin("000001")
                .terminalId("0")
                .fiscalCode("fiscalCode")
                .lastResubmitDate(null)
                .toResubmit(true)
                .originListener("listener")
                .recordId("recordid")
                .originTopic("rtd-trx")
                .build();
    }

    private void verifyNoPublish() {
        verify(faTransactionPublisherService, never())
                .publishFaTransactionEvent(eq(getRequestModel()), any());
        verify(faCashbackTransactionPublisherService, never())
                .publishFaCashbackTransactionEvent(eq(getRequestModel()), any());
        verify(rtdTransactionPublisherService, never())
                .publishRtdTransactionEvent(eq(getRequestModel()), any());
    }

}