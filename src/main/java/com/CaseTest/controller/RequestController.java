package com.CaseTest.controller;

import com.CaseTest.dto.RequestDTO;
import com.CaseTest.dto.ResponseDTO;
import com.CaseTest.model.AuditLog;
import com.CaseTest.model.Request;
import com.CaseTest.service.BPMClientService;
import com.CaseTest.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    private BPMClientService bpmClientService;
    @PostMapping("/start")
    public ResponseEntity<ResponseDTO> processRequest(@RequestBody RequestDTO requestDTO) {
        if (requestDTO.getProcesskey() == null || requestDTO.getProcesskey().isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseDTO("0001", "Process key is required.", requestDTO));
        }
        if (requestDTO.getMyvariable1() == null || requestDTO.getMyvariable1().isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseDTO("0001", "MyVariable1 is required.", requestDTO));
        }
        try {
            // Call the service to process the request
            ResponseDTO response = requestService.processRequest(requestDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Log the error and return a structured ResponseDTO
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO("0001", e.getMessage(), requestDTO));
        } catch (Exception e) {
            // Handle unexpected errors and include RequestDTO in the response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO("0001", "An unexpected error occurred.", requestDTO));
        }
    }

    // Retry requests
    @PostMapping("/retry")
    public ResponseDTO retryRequests(@RequestBody List<UUID> requestIds) {
        return requestService.retryRequests(requestIds);
    }


    @GetMapping("/fetchByDate")
    public List<Request> getRequestsByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return requestService.fetchRequestsByDateRange(startDate, endDate);
    }

    @GetMapping("/audit-logs")
    public List<AuditLog> getAuditLogs(@RequestParam("requestId") UUID requestId) {
        System.out.println("Fetching data for Request ID: " + requestId);
        return requestService.getAuditLogsByRequestId(requestId);
    }




}
