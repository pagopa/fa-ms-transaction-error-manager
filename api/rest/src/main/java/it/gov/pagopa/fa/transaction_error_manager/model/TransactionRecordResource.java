package it.gov.pagopa.fa.transaction_error_manager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecordResource {

    String idTrxAcquirer;

    String acquirerCode;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    OffsetDateTime trxDate;

    String hpan;

    String operationType;

    String circuitType;

    @NotNull
    String idTrxIssuer;

    String correlationId;

    @NotNull
    BigDecimal amount;

    String amountCurrency;

    String mcc;

    String mccDescription;

    String acquirerId;

    String merchantId;

    @NotNull
    String bin;

    @NotNull
    String terminalId;

    String fiscalCode;

}




