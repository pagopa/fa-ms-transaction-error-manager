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

import java.util.Collections;
import java.util.List;

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

}