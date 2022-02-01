package it.gov.pagopa.fa.transaction_error_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.DummyConfiguration;
import eu.sia.meda.config.ArchConfiguration;
import eu.sia.meda.error.config.LocalErrorConfig;
import eu.sia.meda.error.handler.MedaExceptionHandler;
import eu.sia.meda.error.service.impl.LocalErrorManagerServiceImpl;
import it.gov.pagopa.fa.transaction_error_manager.command.SubmitFlaggedRecordsCommand;
import it.gov.pagopa.fa.transaction_error_manager.connector.jpa.model.TransactionRecord;
import it.gov.pagopa.fa.transaction_error_manager.model.TransactionRecordResource;
import it.gov.pagopa.fa.transaction_error_manager.service.TransactionRecordService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@WebMvcTest(value = {FaTransactionErrorManagerControllerImpl.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
        FaTransactionErrorManagerControllerImpl.class,
        DummyConfiguration.class,
        MedaExceptionHandler.class,
        LocalErrorManagerServiceImpl.class
})
@Import(LocalErrorConfig.class)
@TestPropertySource(properties = {
        "error-manager.enabled=true",
        "spring.application.name=fa-ms-transaction-error-manager-api-rest"
})
public class FaTransactionErrorManagerControllerImplTest {

    private final String BASE_URL = "/fa/transaction-error-manager";
    @Autowired
    MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ArchConfiguration().objectMapper();
    @MockBean
    private SubmitFlaggedRecordsCommand saveTransactionRecordCommand;
    @MockBean
    private TransactionRecordService transactionRecordService;

    private TransactionRecord getRecord(){
        return TransactionRecord.builder()
                .amount(BigDecimal.valueOf(1))
                .bin("bin")
                .idTrxIssuer("idTrxIssuer")
                .terminalId("terminalId")
                .trxDate(OffsetDateTime.parse("2020-04-09T16:22:45.304Z"))
                .build();
    }

    private TransactionRecordResource getRecordResource(){
        return TransactionRecordResource.builder()
                .amount(BigDecimal.valueOf(1))
                .bin("bin")
                .idTrxIssuer("idTrxIssuer")
                .terminalId("terminalId")
                .trxDate(OffsetDateTime.parse("2020-04-09T16:22:45.304Z"))
                .build();
    }

    @Test
    public void resubmitTransactions_OK() {

        try {
            doReturn(true).when(saveTransactionRecordCommand).execute();
            mockMvc.perform(
                    MockMvcRequestBuilders.post(BASE_URL.concat("/resubmitTransactions"))
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                    .andReturn();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void findTransactionOK() throws Exception {
        List resultList = new ArrayList();
        resultList.add(getRecord());

        doReturn(resultList).when(transactionRecordService).findRecord(any(TransactionRecord.class));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL.concat("/transaction/status"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(getRecordResource())))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        List<TransactionRecord> pageResult = objectMapper.readValue(result.getResponse().getContentAsString(),
                List.class);

        assertNotNull(pageResult);
        verify(transactionRecordService).findRecord(getRecord());
    }

    @Test
    public void findTransactionKO() throws Exception {
        doReturn(null).when(transactionRecordService).findRecord(any(TransactionRecord.class));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL.concat("/transaction/status"))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(getRecordResource())))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();

        verify(transactionRecordService).findRecord(getRecord());
    }

}