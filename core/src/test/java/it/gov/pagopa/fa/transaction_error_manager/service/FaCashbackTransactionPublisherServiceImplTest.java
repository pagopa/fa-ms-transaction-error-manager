package it.gov.pagopa.fa.transaction_error_manager.service;

import eu.sia.meda.BaseTest;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.fa.transaction_error_manager.publisher.FaCashbackTransactionPublisherConnector;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import it.gov.pagopa.fa.transaction_error_manager.service.transformer.HeaderAwareRequestTransformer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Class for unit-testing {@link FaTransactionPublisherService}
 */
public class FaCashbackTransactionPublisherServiceImplTest extends BaseTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private FaCashbackTransactionPublisherConnector faCashbackTransactionPublisherConnector;
    private FaCashbackTransactionPublisherService faCashbackTransactionPublisherService;

    @SpyBean
    private HeaderAwareRequestTransformer<Transaction> simpleEventRequestTransformerSpy;

    @SpyBean
    private SimpleEventResponseTransformer simpleEventResponseTransformerSpy;

    @Before
    public void initTest() {
        Mockito.reset(faCashbackTransactionPublisherConnector);
        faCashbackTransactionPublisherService =
                new FaCashbackTransactionPublisherServiceImpl(
                        faCashbackTransactionPublisherConnector,
                        simpleEventRequestTransformerSpy,
                        simpleEventResponseTransformerSpy);
    }

    @Test
    public void testSave_Ok() {

        try {

            BDDMockito.doReturn(true)
                    .when(faCashbackTransactionPublisherConnector)
                    .doCall(Mockito.eq(getSaveModel()), Mockito.any(), Mockito.any());

            faCashbackTransactionPublisherService
                    .publishFaCashbackTransactionEvent(getSaveModel(), getRecordHeaders());

            BDDMockito.verify(faCashbackTransactionPublisherConnector, Mockito.atLeastOnce())
                    .doCall(Mockito.eq(getSaveModel()), Mockito.any(), Mockito.any(), Mockito.any());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void testSave_KO_Connector() {

        BDDMockito.doAnswer(invocationOnMock -> {
            throw new Exception();
        }).when(faCashbackTransactionPublisherConnector)
                .doCall(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        expectedException.expect(Exception.class);
        faCashbackTransactionPublisherService.publishFaCashbackTransactionEvent(null, null);

        BDDMockito.verify(faCashbackTransactionPublisherConnector, Mockito.atLeastOnce())
                .doCall(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    protected Transaction getSaveModel() {
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
                .build();
    }

    private RecordHeaders getRecordHeaders() {
        return new RecordHeaders();
    }


}