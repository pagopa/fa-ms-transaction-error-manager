package it.gov.pagopa.fa.transaction_error_manager.model;

import it.gov.pagopa.bpd.common.connector.jpa.model.BaseEntity;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"recordId"}, callSuper = false)
@Table(name = "fa_transaction_record")
public class TransactionRecordResource extends BaseEntity {

    String idTrxAcquirer;
    String acquirerCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    OffsetDateTime trxDate;

    String hpan;

    String operationType;

    String circuitType;

    String idTrxIssuer;

    String correlationId;

    BigDecimal amount;

    String amountCurrency;

    String mcc;

    String mccDescription;

    BigDecimal score;

    Long awardPeriodId;

    String acquirerId;

    String merchantId;

    String bin;

    String terminalId;

    String fiscalCode;

    OffsetDateTime customerValidationDate;

}




