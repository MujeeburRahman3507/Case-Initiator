package com.CaseTest.service;

import com.CaseTest.dto.RequestDTO;
import com.CaseTest.dto.ResponseDTO;
import com.CaseTest.model.AuditLog;
import com.CaseTest.model.Request;
import com.CaseTest.repository.AuditRepository;
import com.CaseTest.repository.RequestRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private BPMClientService bpmClientService;



    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.recipient}")
    private String recipientEmail;

    @Value("${mail.smtp.sender}")
    private String senderEmail;

    // Method to get audit logs by statusCode
//    public List<AuditLog> getAuditLogsByStatusCode(String statusCode) {
//        return auditRepository.findByStatusCode(statusCode);
//    }

    public List<AuditLog> getAuditLogsByRequestId(UUID requestId) {
        return auditRepository.findByrequestId(requestId);
    }


//
//    @Scheduled(fixedRateString = "${scheduler.fixedRate:300000}") // Default 5 minutes
//    public void processPendingRequests() {
//        List<Request> pendingRequests = requestRepository.findAll()
//                .stream()
//                .filter(request -> "PENDING".equals(request.getStatus()))
//                .collect(Collectors.toList());
//
//        for (Request request : pendingRequests) {
//            RequestDTO requestDTO = new RequestDTO();
//            requestDTO.setProcesskey(request.getProcesskey());
//            requestDTO.setMyvariable1(request.getMyvariable1());
//            processRequest(requestDTO);
//        }
//    }

    public ResponseDTO processRequest(RequestDTO requestDTO) {
        // Validate processkey
        if (requestDTO.getProcesskey() == null || requestDTO.getProcesskey().isEmpty()) {
            throw new IllegalArgumentException("Process key cannot be null or empty.");
        }

        // Initialize and save Request
        Request request = new Request();
        request.setProcesskey(requestDTO.getProcesskey());
        request.setMyvariable1(requestDTO.getMyvariable1());
        request.setMyvariable2(requestDTO.getMyvariable2());
        request.setStatus("PENDING");

        try {
            requestRepository.save(request);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Failed to save request due to database constraints.", e);
        }

        // Call BPM service and get response
        ResponseDTO response;
        try {
            response = bpmClientService.invokeBPM(requestDTO);
        } catch (Exception e) {
            // Log and update request in case of BPM service failure
            request.setStatus("FAILED");
            requestRepository.save(request);
            throw new IllegalStateException("Failed to invoke BPM service.", e);
        }

        // Update request status based on BPM response
        request.setStatus(response.getWsstatuscode().equals("0000") ? "SUCCESS" : "FAILED");
        requestRepository.save(request);

        // Create and save AuditLog
        AuditLog auditLog = new AuditLog();
        auditLog.setRequestId(request.getId());
        auditLog.setRequestData(requestDTO.toString());
        auditLog.setStatusCode(response.getWsstatuscode());
        auditLog.setStatusMessage(response.getWsmessage());

        try {
            auditRepository.save(auditLog);
        } catch (Exception e) {
            // Log failure to save audit log but don't block the response
            System.err.println("Failed to save audit log: " + e.getMessage());
        }

        return response;
    }


    public List<Request> fetchRequestsByDateRange(String startDate, String endDate) {
        try {
            // Define the date format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Parse input dates
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            // Convert to LocalDateTime
            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.plusDays(1).atStartOfDay(); // Inclusive of the entire end date

            // Query the database
            return requestRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd.", e);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching requests by date range.", e);
        }
    }


    // Retry failed requests

    public ResponseDTO retryRequests(List<UUID> requestIds) {
        try {
            // Fetch all requests by their IDs
            List<Request> requests = requestRepository.findAllById(requestIds);

            if (requests.isEmpty()) {
                return new ResponseDTO("0001", "No requests found for retry", null);
            }

            for (Request request : requests) {
                // Retry only FAILED requests
                if (!"SUCCESS".equals(request.getStatus())) {


                    RequestDTO requestDTO = new RequestDTO();
                    requestDTO.setProcesskey("CaseInitiator"); // Assuming the process key remains constant
                    requestDTO.setMyvariable1(request.getMyvariable1());
                    requestDTO.setMyvariable2(request.getMyvariable2());




                    try {
                        // Retry the process by using the original request ID
                        ResponseDTO response = bpmClientService.invokeBPM(requestDTO);

                        if ("0000".equals(response.getWsstatuscode())) {
                            // Update the request status to SUCCESS in DB
                            request.setStatus("SUCCESS");
                        } else {
                            // Handle retry failure from BPM response
                            request.setStatus("FAILED");
                        }
                        requestRepository.save(request);

                        // Log success in the audit log
                        AuditLog log = new AuditLog();
                        log.setRequestId(request.getId());
                        log.setStatusCode(response.getWsstatuscode());
                        log.setStatusMessage(response.getWsmessage());
                        auditRepository.save(log);

                    } catch (Exception ex) {
                        // Log failure in the audit log
                        AuditLog log = new AuditLog();
                        log.setRequestId(request.getId());
                        log.setStatusCode("0001");
                        log.setStatusMessage("Retry failed for requestId: " + request.getId() + ". Error: " + ex.getMessage());
                        auditRepository.save(log);
                    }
                }
            }

            return new ResponseDTO("0000", "Retry completed. Check audit logs for details.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDTO("0001", "Error during retry", e.getMessage());
        }
    }


    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    public void sendFailureEmails() {
        // Fetch failed requests from the audit log
        List<AuditLog> failedLogs = auditRepository.findAll()
                .stream()
                .filter(log -> "0001".equals(log.getStatusCode())) // Filtering for failed logs
                .collect(Collectors.toList());

        if (!failedLogs.isEmpty()) {
            // Generate CSV content
            String csvContent = generateCsv(failedLogs);

            // Send the email with the CSV attachment
            sendEmailWithAttachment(csvContent);
        }
    }

    private String generateCsv(List<AuditLog> logs) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID,Request Data,Status Message,Timestamp\n");
        for (AuditLog log : logs) {
            csvBuilder.append(log.getId())
                    .append(",\"").append(log.getRequestData().replace("\"", "\"\"")).append("\"")
                    .append(",\"").append(log.getStatusMessage().replace("\"", "\"\"")).append("\"")
                    .append(",").append(log.getTimestamp())
                    .append("\n");
        }
        return csvBuilder.toString();
    }

    private void sendEmailWithAttachment(String csvContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("Failure Notifications");
            helper.setText("Please find the attached CSV file for failed requests.", true);

            // Attach the CSV content as a file
            helper.addAttachment("failed_requests.csv", new ByteArrayResource(csvContent.getBytes()));

            mailSender.send(message);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send failure notification email", e);
        }
    }
}
