package it.gov.pagopa.fa.transaction_error_manager.command;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import it.gov.pagopa.fa.transaction_error_manager.model.TransactionCommandModel;
import it.gov.pagopa.fa.transaction_error_manager.model.constants.TransactionRecordConstants;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import it.gov.pagopa.fa.transaction_error_manager.service.TransactionRecordService;
import it.gov.pagopa.fa.transaction_error_manager.service.mapper.TransactionMapper;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SaveTransactionRecordCommandImplTest extends BaseTest {

    @Rule public ExpectedException exceptionRule = ExpectedException.none();
    @Mock TransactionRecordService transactionRecordService;
    @Spy  TransactionMapper transactionMapperSpy;

    @Test
    public void TestExecute_OK_VoidData() {

        Transaction transaction = getRequestModel();
        TransactionRecord transactionRecord = getSavedModel();
        Headers headers = new RecordHeaders();

        ArgumentCaptor<TransactionRecord> argument = ArgumentCaptor.forClass(TransactionRecord.class);
        doReturn(transactionRecord).when(transactionRecordService)
                .saveTransactionRecord(transactionRecord);
        SaveTransactionRecordCommandImpl saveTransactionCommand = new SaveTransactionRecordCommandImpl(
                TransactionCommandModel.builder().payload(transaction).headers(headers).build(),
                transactionRecordService,
                transactionMapperSpy);
        Boolean executed = saveTransactionCommand.doExecute();
        assertTrue(executed);
        verify(transactionMapperSpy).mapTransactionRecord(transaction);
        verify(transactionRecordService).saveTransactionRecord(argument.capture());
        assertNotNull(argument.getValue().getRecordId());
        assertNull(argument.getValue().getExceptionMessage());
        assertEquals(false, argument.getValue().getToResubmit());
        assertEquals("rtd-trx", argument.getValue().getOriginTopic());

    }

    @Test
    public void TestExecute_OK_PIRecord() {

        Transaction transaction = getRequestModel();
        TransactionRecord transactionRecord = getSavedModel();
        Headers headers = new RecordHeaders();
        headers.add(TransactionRecordConstants.EXCEPTION_HEADER, "test".getBytes());
        headers.add(TransactionRecordConstants.LISTENER_HEADER,
                "it.gov.pagopa.bpd.payment_instrument.listener.OnTransactionFilterRequestListener".getBytes());
        headers.add(TransactionRecordConstants.REQUEST_ID_HEADER, "requestId".getBytes());

        ArgumentCaptor<TransactionRecord> argument = ArgumentCaptor.forClass(TransactionRecord.class);
        doReturn(transactionRecord).when(transactionRecordService)
                .saveTransactionRecord(transactionRecord);

        SaveTransactionRecordCommandImpl saveTransactionCommand = new SaveTransactionRecordCommandImpl(
                TransactionCommandModel.builder().payload(transaction).headers(headers).build(),
                transactionRecordService,
                transactionMapperSpy);
        Boolean executed = saveTransactionCommand.doExecute();
        assertTrue(executed);
        verify(transactionMapperSpy).mapTransactionRecord(transaction);
        verify(transactionRecordService).saveTransactionRecord(argument.capture());
        assertNotNull(argument.getValue().getRecordId());
        assertEquals("test", argument.getValue().getExceptionMessage());
        assertEquals(false, argument.getValue().getToResubmit());
        assertEquals("rtd-trx", argument.getValue().getOriginTopic());
    }

    @Test
    public void TestExecute_OK_WTRecord() {

        Transaction transaction = getRequestModel();
        TransactionRecord transactionRecord = getSavedModel();
        Headers headers = new RecordHeaders();
        headers.add(TransactionRecordConstants.EXCEPTION_HEADER, "test".getBytes());
        headers.add(TransactionRecordConstants.LISTENER_HEADER,
                "it.gov.pagopa.bpd.winning_transaction.listener.OnTransactionSaveRequestListener".getBytes());
        headers.add(TransactionRecordConstants.REQUEST_ID_HEADER, "requestId".getBytes());

        ArgumentCaptor<TransactionRecord> argument = ArgumentCaptor.forClass(TransactionRecord.class);
        doReturn(transactionRecord).when(transactionRecordService)
                .saveTransactionRecord(transactionRecord);

        SaveTransactionRecordCommandImpl saveTransactionCommand = new SaveTransactionRecordCommandImpl(
                TransactionCommandModel.builder().payload(transaction).headers(headers).build(),
                transactionRecordService,
                transactionMapperSpy);
        Boolean executed = saveTransactionCommand.doExecute();
        assertTrue(executed);
        verify(transactionMapperSpy).mapTransactionRecord(transaction);
        verify(transactionRecordService).saveTransactionRecord(argument.capture());
        assertNotNull(argument.getValue().getRecordId());
        assertEquals("test", argument.getValue().getExceptionMessage());
        assertEquals(false, argument.getValue().getToResubmit());
        assertEquals("bpd-trx-cashback", argument.getValue().getOriginTopic());
    }

    @Test
    public void TestExecute_OK_WTRecord_case2() {

        Transaction transaction = getRequestModel();
        TransactionRecord transactionRecord = getSavedModel();
        Headers headers = new RecordHeaders();
        headers.add(TransactionRecordConstants.EXCEPTION_HEADER, "test".getBytes());
        headers.add(TransactionRecordConstants.LISTENER_HEADER,
                "it.gov.pagopa.bpd.winning_transaction.listener.OnTransactionSaveRequestListener".getBytes());
        headers.add(TransactionRecordConstants.REQUEST_ID_HEADER, "requestId".getBytes());
        headers.add(TransactionRecordConstants.CUSTOMER_VALIDATION_DATETIME_HEADER, "2020-12-21T13:15:30+01:00".getBytes());

        ArgumentCaptor<TransactionRecord> argument = ArgumentCaptor.forClass(TransactionRecord.class);
        doReturn(transactionRecord).when(transactionRecordService)
                .saveTransactionRecord(transactionRecord);

        SaveTransactionRecordCommandImpl saveTransactionCommand = new SaveTransactionRecordCommandImpl(
                TransactionCommandModel.builder().payload(transaction).headers(headers).build());
        saveTransactionCommand.setTransactionMapper(transactionMapperSpy);
        saveTransactionCommand.setTransactionRecordService(transactionRecordService);

        Boolean executed = saveTransactionCommand.doExecute();
        assertTrue(executed);
        verify(transactionMapperSpy).mapTransactionRecord(transaction);
        verify(transactionRecordService).saveTransactionRecord(argument.capture());
        assertNotNull(argument.getValue().getRecordId());
        assertEquals("test", argument.getValue().getExceptionMessage());
        assertEquals(false, argument.getValue().getToResubmit());
        assertEquals("bpd-trx-cashback", argument.getValue().getOriginTopic());
    }


    @Test
    public void TestExecute_KO_Validation() {

        Transaction transaction = getRequestModel();
        transaction.setAcquirerCode(null);
        Headers headers = new RecordHeaders();

        SaveTransactionRecordCommandImpl saveTransactionCommand = new SaveTransactionRecordCommandImpl(
                TransactionCommandModel.builder().payload(transaction).headers(headers).build(),
                transactionRecordService,
                transactionMapperSpy);
        exceptionRule.expect(AssertionError.class);
        saveTransactionCommand.doExecute();
        verifyZeroInteractions(transactionRecordService);

    }

    @Test
    public void TestExecute_KO_NullException() {

        Transaction transaction = getRequestModel();
        transaction.setAcquirerCode(null);

        SaveTransactionRecordCommandImpl saveTransactionCommand = new SaveTransactionRecordCommandImpl(
                TransactionCommandModel.builder().payload(transaction).build(),
                transactionRecordService,
                transactionMapperSpy);
        exceptionRule.expect(NullPointerException.class);
        saveTransactionCommand.doExecute();
        verifyZeroInteractions(transactionRecordService);

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
                .operationType("01")
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
                .build();
    }

}