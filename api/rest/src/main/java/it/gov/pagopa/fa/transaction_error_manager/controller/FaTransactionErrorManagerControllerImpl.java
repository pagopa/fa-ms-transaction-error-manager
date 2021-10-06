package it.gov.pagopa.fa.transaction_error_manager.controller;

import eu.sia.meda.core.controller.StatelessController;
import it.gov.pagopa.fa.transaction_error_manager.command.SubmitFlaggedRecordsCommand;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * See {@link FaTransactionErrorManagerControllerImpl}
 */
@RestController
class FaTransactionErrorManagerControllerImpl extends StatelessController implements FaTransactionErrorManagerController {

    private final BeanFactory beanFactory;

    @Autowired
    FaTransactionErrorManagerControllerImpl(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void resubmitTransactions() throws Exception {
        SubmitFlaggedRecordsCommand command =
                beanFactory.getBean(SubmitFlaggedRecordsCommand.class);
        command.execute();
    }
}
