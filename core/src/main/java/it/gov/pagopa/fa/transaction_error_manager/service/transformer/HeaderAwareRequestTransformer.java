package it.gov.pagopa.fa.transaction_error_manager.service.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.config.LoggerUtils;
import eu.sia.meda.event.request.EventRequest;
import eu.sia.meda.event.transformer.IEventRequestTransformer;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Primary
public class HeaderAwareRequestTransformer<I> implements IEventRequestTransformer<I, I> {
    private static final Logger logger = LoggerUtils.getLogger(
            it.gov.pagopa.fa.transaction_error_manager.service.transformer.HeaderAwareRequestTransformer.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public HeaderAwareRequestTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EventRequest<I> transform(I payload, Object... args) {
        try {
            EventRequest<I> request = new EventRequest<>();
            if (payload instanceof byte[]) {
                request.setPayload((byte[]) payload);
            } else {
                request.setPayload(this.objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8));
            }

            Headers headers = args.length > 0 ? (Headers) args[0] : new RecordHeaders();
            request.setHeaders(headers);

            return request;
        } catch (JsonProcessingException var4) {
            logger.error("Something went wrong trying to serialize payload");
            throw new IllegalStateException("Cannot serialize payload!", var4);
        }
    }
}