package com.universite.reseausocial.controller;

import com.universite.reseausocial.dto.ReportRequest;
import com.universite.reseausocial.dto.ReportResponse;
import com.universite.reseausocial.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReportResponse response = reportService.createReport(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ReportResponse> reports = reportService.getAllReports(status, userDetails.getUsername());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReportResponse report = reportService.getReportById(id, userDetails.getUsername());
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReportResponse report = reportService.resolveReport(id, userDetails.getUsername());
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ReportResponse> rejectReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReportResponse report = reportService.rejectReport(id, userDetails.getUsername());
        return ResponseEntity.ok(report);
    }
}
