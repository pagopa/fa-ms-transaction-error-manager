package it.gov.pagopa.fa.transaction_error_manager.service;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.TransactionRecordDAO;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TransactionRecordServiceImplTest extends BaseTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private TransactionRecordDAO transactionRecordDAOMock;
    private TransactionRecordService transactionRecordService;

    @Before
    public void initTest() {
        reset(transactionRecordDAOMock);
        transactionRecordService = new TransactionRecordServiceImpl(transactionRecordDAOMock);
    }

    private TransactionRecord getRecord(){
        return TransactionRecord.builder()
                .amount(BigDecimal.valueOf(1))
                .bin("bin")
                .idTrxIssuer("idTrxIssuer")
                .terminalId("terminalId")
                .trxDate(OffsetDateTime.parse("2020-04-09T16:22:45.304Z"))
                .build();
    }

    @Test
    public void getList_OK() {
        doReturn(Collections.emptyList())
                .when(transactionRecordDAOMock).findByToResubmit(true);
        List<TransactionRecord> transactionRecordServiceList =
                transactionRecordService.findRecordsToResubmit();
        assertNotNull(transactionRecordServiceList);
        verify(transactionRecordDAOMock).findByToResubmit(true);
    }

    @Test
    public void getList_KO() {
        when(transactionRecordDAOMock.findByToResubmit(true)).thenThrow(new RuntimeException(""));
        expectedException.expect(Exception.class);
        transactionRecordService.findRecordsToResubmit();
        verify(transactionRecordDAOMock).findByToResubmit(true);
    }

    @Test
    public void save_OK() {
        TransactionRecord transactionRecord = TransactionRecord.builder().build();
        doReturn(transactionRecord).when(transactionRecordDAOMock).save(transactionRecord);
        transactionRecord = transactionRecordService.saveTransactionRecord(transactionRecord);
        assertNotNull(transactionRecord);
        verify(transactionRecordDAOMock).save(transactionRecord);
    }

    @Test
    public void save_KO() {
        when(transactionRecordDAOMock.save(Mockito.any())).thenAnswer(
                invocation -> {
                    throw new Exception();
                });
        expectedException.expect(Exception.class);
        transactionRecordService.saveTransactionRecord(TransactionRecord.builder().build());
        verify(transactionRecordDAOMock).save(Mockito.any());
    }

    @Test
    public void findRecordOK(){
        List result = new ArrayList();
        result.add(getRecord());

        doReturn(result).when(transactionRecordDAOMock)
                .findTransaction(getRecord().getTrxDate(),
                        getRecord().getIdTrxIssuer(),
                        getRecord().getAmount(),
                        getRecord().getBin(),
                        getRecord().getTerminalId());

        List<TransactionRecord> find = transactionRecordService.findRecord(getRecord());
        verify(transactionRecordDAOMock).findTransaction(any(),any(),any(),any(),any());
    }

    @Test
    public void findRecordKO(){

        doReturn(null).when(transactionRecordDAOMock)
                .findTransaction(getRecord().getTrxDate(),
                        getRecord().getIdTrxIssuer(),
                        getRecord().getAmount(),
                        getRecord().getBin(),
                        getRecord().getTerminalId());

        List find = transactionRecordService.findRecord(getRecord());

        verify(transactionRecordDAOMock).findTransaction(any(),any(),any(),any(),any());
        assertEquals(null,find);
    }
}