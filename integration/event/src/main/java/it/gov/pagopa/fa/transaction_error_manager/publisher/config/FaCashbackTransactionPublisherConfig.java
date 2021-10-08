package it.gov.pagopa.fa.transaction_error_manager.publisher.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for the TransactionErrorPublisherConnector
 */

@Configuration
@PropertySource("classpath:config/faCustomerTransactionPublisher.properties")
public class FaCashbackTransactionPublisherConfig {
}
