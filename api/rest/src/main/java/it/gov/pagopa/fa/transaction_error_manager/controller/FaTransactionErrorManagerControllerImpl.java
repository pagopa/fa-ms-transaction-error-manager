package it.gov.pagopa.fa.transaction_error_manager.controller;

import eu.sia.meda.core.controller.StatelessController;
import it.gov.pagopa.fa.transaction_error_manager.command.SubmitFlaggedRecordsCommand;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import it.gov.pagopa.fa.transaction_error_manager.model.TransactionRecordResource;
import it.gov.pagopa.fa.transaction_error_manager.service.TransactionRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * See {@link FaTransactionErrorManagerControllerImpl}
 */
@RestController
class FaTransactionErrorManagerControllerImpl extends StatelessController implements FaTransactionErrorManagerController {

    private final BeanFactory beanFactory;
    private final TransactionRecordService transactionRecordService;

    @Autowired
    FaTransactionErrorManagerControllerImpl(BeanFactory beanFactory,
                                            TransactionRecordService transactionRecordService) {
        this.beanFactory = beanFactory;
        this.transactionRecordService = transactionRecordService;
    }

    @Override
    public void resubmitTransactions() throws Exception {
        SubmitFlaggedRecordsCommand command =
                beanFactory.getBean(SubmitFlaggedRecordsCommand.class);
        command.execute();
    }

    @Override
    public List<TransactionRecord> transactionStatus(TransactionRecordResource transaction) throws Exception {

        logger.debug("FaTransactionErrorManagerControllerImpl.transactionStatus - Searching for transaction [{}]", transaction);

        TransactionRecord toSearch = new TransactionRecord();
        BeanUtils.copyProperties(transaction,toSearch);

        return transactionRecordService.findRecord(toSearch);
    }
}
