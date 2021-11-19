package it.gov.pagopa.fa.transaction_error_manager.service;

import eu.sia.meda.service.BaseService;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.TransactionRecordDAO;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @See TransactionRecordService
 */
@Service
class TransactionRecordServiceImpl extends BaseService implements TransactionRecordService {

    private final TransactionRecordDAO transactionRecordDAO;

    @Autowired
    public TransactionRecordServiceImpl(TransactionRecordDAO transactionRecordDAO) {
        this.transactionRecordDAO = transactionRecordDAO;
    }

    @Override
    public TransactionRecord saveTransactionRecord(TransactionRecord transactionRecord) {
        return transactionRecordDAO.save(transactionRecord);
    }

    @Override
    public List<TransactionRecord> findRecordsToResubmit() {
        return transactionRecordDAO.findByToResubmit(true);
    }

    @Override
    public List<TransactionRecord> findRecord(TransactionRecord input) {
        return transactionRecordDAO.findTransaction(input.getTrxDate(), input.getIdTrxIssuer(), input.getAmount(), input.getBin(), input.getTerminalId());
    }

}
