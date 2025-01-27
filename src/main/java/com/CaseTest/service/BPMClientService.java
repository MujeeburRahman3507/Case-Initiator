package com.CaseTest.service;

import java.util.Base64;
import com.CaseTest.dto.BPMRequestDTO;
import com.CaseTest.dto.RequestDTO;
import com.CaseTest.dto.ResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class BPMClientService {
    @Value("${bpm.engine.schema}")
    private String bpmEngineSchema;

    @Value("${flowable.base.url}")
    private String flowableBaseUrl;

    @Value("${flowable.start-process.url}")
    private String flowableStartProcessUrl;

    @Value("${baw.base.url}")
    private String bawBaseUrl;

    @Value("${baw.start-process.url}")
    private String bawStartProcessUrl;

    @Value("${flowable.username}")
    private String flowableUsername;

    @Value("${flowable.password}")
    private String flowablePassword;

    private final RestTemplate restTemplate;

    public BPMClientService() {
        this.restTemplate = new RestTemplate();
    }

    public ResponseDTO invokeBPM(RequestDTO requestDTO) {
        try {
            // Build BPM Request
            BPMRequestDTO bpmRequestDTO = buildBPMRequest(requestDTO);
            String url = buildUrl();

            // Prepare HTTP headers with authorization
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = flowableUsername + ":" + flowablePassword;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            // Send POST request
            HttpEntity<BPMRequestDTO> requestEntity = new HttpEntity<>(bpmRequestDTO, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            // Process Response
            return new ResponseDTO("0000", "Success", Collections.singletonMap("data", response.getBody()));
        } catch (HttpClientErrorException e) {
            return new ResponseDTO("0001", "HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), null);
        } catch (Exception e) {
            return new ResponseDTO("0001", "Failure: " + e.getMessage(), null);
        }
    }

    private BPMRequestDTO buildBPMRequest(RequestDTO requestDTO) {
        BPMRequestDTO bpmRequestDTO = new BPMRequestDTO();

        // Map process key and variable to Flowable fields
        bpmRequestDTO.setProcessDefinitionKey(requestDTO.getProcesskey());
        bpmRequestDTO.setBusinessKey(requestDTO.getMyvariable1());

        // Create variables list
        BPMRequestDTO.BPMVariable variable1 = new BPMRequestDTO.BPMVariable();
        variable1.setName("myVariable1");
        variable1.setType("string");
        variable1.setValue(requestDTO.getMyvariable1());

        BPMRequestDTO.BPMVariable variable2 = new BPMRequestDTO.BPMVariable();
        variable2.setName("myVariable2");
        variable2.setType("string");
        variable2.setValue(requestDTO.getMyvariable2());

        bpmRequestDTO.setVariables(List.of(variable1, variable2));
        System.out.println("Constructed BPMRequestDTO: " + bpmRequestDTO);
        // Debugging output for variables
        System.out.println("BPMRequestDTO:");
        System.out.println("Process Key: " + bpmRequestDTO.getProcessDefinitionKey());
        System.out.println("Business Key: " + bpmRequestDTO.getBusinessKey());
        for (BPMRequestDTO.BPMVariable variable : bpmRequestDTO.getVariables()) {
            System.out.println("Variable Name: " + variable.getName() + ", Type: " + variable.getType() + ", Value: " + variable.getValue());
        }
        return bpmRequestDTO;
    }


    private String buildUrl() {
        if ("FLOWABLE".equalsIgnoreCase(bpmEngineSchema)) {
            return flowableBaseUrl + flowableStartProcessUrl;
        } else if ("BAW".equalsIgnoreCase(bpmEngineSchema)) {
            return bawBaseUrl + bawStartProcessUrl;
        } else {
            throw new IllegalArgumentException("Unsupported BPM engine schema: " + bpmEngineSchema);
        }
    }
    }
