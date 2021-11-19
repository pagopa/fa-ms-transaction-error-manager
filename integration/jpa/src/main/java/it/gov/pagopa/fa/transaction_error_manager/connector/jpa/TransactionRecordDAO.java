package it.gov.pagopa.fa.transaction_error_manager.connector.jpa;

import it.gov.pagopa.bpd.common.connector.jpa.CrudJpaDAO;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Data Access Object to manage all CRUD operations to the database
 */
@Repository
public interface TransactionRecordDAO extends CrudJpaDAO<TransactionRecord, String> {

    List<TransactionRecord> findByToResubmit(Boolean toResubmit);

    @Query(value = "SELECT trx FROM TransactionRecord trx " +
            "WHERE trx.trxDate = :trxDate " +
            "AND trx.idTrxIssuer = :idTrxIssuer " +
            "AND trx.amount = :amount " +
            "AND trx.bin = :bin " +
            "AND trx.terminalId = :terminalId")
    List<TransactionRecord> findTransaction(@Param("trxDate") OffsetDateTime trxDate,
                                      @Param("idTrxIssuer") String idTrxIssuer,
                                      @Param("amount") BigDecimal amount,
                                      @Param("bin") String bin,
                                      @Param("terminalId") String terminalId );
}
