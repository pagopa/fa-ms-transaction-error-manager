package it.gov.pagopa.fa.transaction_error_manager.service.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.event.request.EventRequest;
import it.gov.pagopa.fa.transaction_error_manager.model.constants.TransactionRecordConstants;
import it.gov.pagopa.fa.transaction_error_manager.publisher.model.Transaction;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

public class HeaderAwareRequestTransformerTest {

    private HeaderAwareRequestTransformer<Transaction> transactionTransformer;
    private HeaderAwareRequestTransformer<byte[]> byteArrayTransformer;

    @Spy private ObjectMapper objectMapper;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        transactionTransformer = new HeaderAwareRequestTransformer<>(objectMapper);
        byteArrayTransformer = new HeaderAwareRequestTransformer<>(objectMapper);
    }

    @Test
    public void transformTest(){
        byte[] payload = "payload".getBytes(StandardCharsets.UTF_8);
        Headers headers = new RecordHeaders();
        Header header = new RecordHeader(
                TransactionRecordConstants.LISTENER_HEADER,
                "header".getBytes(StandardCharsets.UTF_8));
        headers.add(header);

        EventRequest<byte[]> result = byteArrayTransformer.transform(payload, headers);

        assertNotNull(result);
        assertNotNull(result.getPayload());
        assertNotNull(result.getHeaders());
        assertTrue(result.getPayload().length > 0);
        assertNotNull(result.getHeaders().lastHeader(TransactionRecordConstants.LISTENER_HEADER));
    }

    @Test
    public void transformWithoutHeadersTest(){
        byte[] payload = "payload".getBytes(StandardCharsets.UTF_8);

        EventRequest<byte[]> result = byteArrayTransformer.transform(payload);
        
        assertNotNull(result);
        assertNotNull(result.getPayload());
        assertNotNull(result.getHeaders());
        assertTrue(result.getPayload().length > 0);
        assertNull(result.getHeaders().lastHeader(TransactionRecordConstants.LISTENER_HEADER));
    }

    @Test
    public void transformTransactionTest() throws JsonProcessingException {
        Transaction payload = new Transaction();
        payload.setIdTrxAcquirer("idTrxAcquirer");
        //given(objectMapper.writeValueAsString(any(Transaction.class))).willReturn("json");
        Headers headers = new RecordHeaders();
        Header header = new RecordHeader(
                TransactionRecordConstants.REQUEST_ID_HEADER,
                "id".getBytes(StandardCharsets.UTF_8));
        headers.add(header);

        EventRequest<Transaction> result = transactionTransformer.transform(payload, headers);

        assertNotNull(result);
        assertNotNull(result.getPayload());
        assertNotNull(result.getHeaders());
        assertTrue(result.getPayload().length > 0);
        assertNotNull(result.getHeaders().lastHeader(TransactionRecordConstants.REQUEST_ID_HEADER));
    }

    @Test
    public void transformInvalidRequestTest() throws JsonProcessingException {
        Transaction payload = new Transaction();
        given(objectMapper.writeValueAsString(any(Transaction.class))).willThrow(JsonProcessingException.class);

        try{
            transactionTransformer.transform(payload);
            fail("Expected a JsonProcessingException to be thrown here");
        } catch (Throwable t){
            assertEquals(IllegalStateException.class, t.getClass());
        }
    }

}