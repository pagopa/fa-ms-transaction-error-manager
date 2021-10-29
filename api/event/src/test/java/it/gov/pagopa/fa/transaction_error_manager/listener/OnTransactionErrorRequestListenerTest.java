package it.gov.pagopa.fa.transaction_error_manager.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.event.service.ErrorPublisherService;
import eu.sia.meda.eventlistener.BaseEventListenerTest;
import it.gov.pagopa.fa.transaction_error_manager.command.SaveTransactionRecordCommand;
import it.gov.pagopa.fa.transaction_error_manager.listener.factory.SaveTransactionCommandModelFactory;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


/**
 * Test class for the OnTransactionFilterRequestListener method
 */

@Import({OnTransactionErrorRequestListener.class})
@TestPropertySource(
        locations = "classpath:config/testTransactionRequestListener.properties",
        properties = {
                "listeners.eventConfigurations.items.OnTransactionErrorRequestListener.bootstrapServers=${spring.embedded.kafka.brokers}"
        })
public class OnTransactionErrorRequestListenerTest extends BaseEventListenerTest {


    @SpyBean
    ObjectMapper objectMapperSpy;
    @SpyBean
    OnTransactionErrorRequestListener onTransactionFilterRequestListenerSpy;
    @SpyBean
    SaveTransactionCommandModelFactory saveTransactionCommandModelFactorySpy;
    @MockBean
    SaveTransactionRecordCommand saveTransactionRecordCommandMock;
    @Value("${listeners.eventConfigurations.items.OnTransactionErrorRequestListener.topic}")
    private String topic;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaTemplate<String, String> template;
    @Autowired
    private EmbeddedKafkaBroker kafkaBroker;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.cloud.stream.kafka.binder.zkNodes}")
    private String zkNodes;

    @BeforeClass
    public static void setErrorLogging() {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.TRACE);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(
                onTransactionFilterRequestListenerSpy,
                saveTransactionCommandModelFactorySpy,
                saveTransactionRecordCommandMock);
        Mockito.doReturn(true).when(saveTransactionRecordCommandMock).execute();

    }

    @Override
    protected Object getRequestObject() {
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
                .bin("000001")
                .terminalId("0")
                .build();
    }

    @Override
    protected String getTopic() {
        return topic;
    }

    @Override
    protected void verifyInvocation(String json) {
        try {
            BDDMockito.verify(saveTransactionCommandModelFactorySpy, Mockito.atLeastOnce())
                    .createModel(Mockito.any());
            BDDMockito.verify(objectMapperSpy, Mockito.atLeastOnce())
                    .readValue(Mockito.anyString(), Mockito.eq(Transaction.class));
            BDDMockito.verify(saveTransactionRecordCommandMock, Mockito.atLeastOnce()).execute();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void exceptionTest() throws Exception {
        given(saveTransactionRecordCommandMock.execute()).willThrow(new Exception());
        prepareKOTest();
        verify(objectMapperSpy, atLeastOnce()).readValue(anyString(), eq(Transaction.class));
    }


    @Test
    public void exceptionPayloadNullTest() throws Exception {
        given(saveTransactionRecordCommandMock.execute()).willThrow(new Exception());
        doReturn(null).when(saveTransactionCommandModelFactorySpy).createModel(any());
        prepareKOTest();
        verify(objectMapperSpy, never()).readValue(anyString(), eq(Transaction.class));
    }


    /**
     * Wraps common code for the expeptions test.
     *
     * @throws InterruptedException
     * @throws JsonProcessingException
     */
    private void prepareKOTest() throws Exception {
        Thread.sleep(2000L);
        List<String> jsons = Arrays.asList(this.objectMapper.writeValueAsString(this.getRequestObject()));
        List<ProducerRecord<String, String>> records = Arrays.asList(
                new ProducerRecord(this.getTopic(), (Integer) null, (Object) null, jsons.get(0), null),
                new ProducerRecord(this.getTopic(), (Integer) null, (Object) null, null, null),
                new ProducerRecord(this.getTopic(), (Integer) null, (Object) null, "not_valid_string", null));
        Stream<ProducerRecord<String, String>> producerRecordStream = records.parallelStream();
        KafkaTemplate kafkaTemplate = this.template;
        producerRecordStream.forEach(kafkaTemplate::send);
        Thread.sleep(this.getSleepMillis());
        verify(saveTransactionCommandModelFactorySpy, atLeastOnce()).createModel(any());
        verify(saveTransactionRecordCommandMock, atLeastOnce()).execute();
    }

    @Override
    protected ErrorPublisherService getErrorPublisherService() {
        return null;
    }

}