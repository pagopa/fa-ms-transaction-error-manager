package it.gov.pagopa.fa.transaction_error_manager.service.mapper;

import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TransactionMapperTest {

    private TransactionMapper classUnderTest;

    @Before
    public void init(){
        classUnderTest = new TransactionMapper();
    }

    @Test
    public void mapTransactionRecordTest() {
        Transaction transaction = new Transaction();
        transaction.setIdTrxAcquirer("idTrxAcquirer");
        TransactionRecord transactionRecord = classUnderTest.mapTransactionRecord(transaction);
        Assert.assertNotNull(transactionRecord);
        assertEquals("idTrxAcquirer", transactionRecord.getIdTrxAcquirer());
    }

    @Test
    public void mapTransactionRecordNullTest() {
        Transaction transaction = null;
        TransactionRecord transactionRecord = classUnderTest.mapTransactionRecord(transaction);
        assertNull(transactionRecord);
    }

    @Test
    public void mapTransactionTest() {
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setIdTrxAcquirer("idTrxAcquirer");
        Transaction transaction = classUnderTest.mapTransaction(transactionRecord);
        Assert.assertNotNull(transaction);
        assertEquals("idTrxAcquirer", transaction.getIdTrxAcquirer());
    }

    @Test
    public void mapTransactionNullTest() {
        TransactionRecord transactionRecord = null;
        Transaction transaction = classUnderTest.mapTransaction(transactionRecord);
        assertNull(transaction);
    }

}
