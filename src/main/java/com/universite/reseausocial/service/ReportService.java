package com.universite.reseausocial.service;

import com.universite.reseausocial.dto.ReportRequest;
import com.universite.reseausocial.dto.ReportResponse;
import com.universite.reseausocial.dto.UserResponse;
import com.universite.reseausocial.entity.Comment;
import com.universite.reseausocial.entity.Post;
import com.universite.reseausocial.entity.Report;
import com.universite.reseausocial.entity.User;
import com.universite.reseausocial.exception.ResourceNotFoundException;
import com.universite.reseausocial.exception.UnauthorizedException;
import com.universite.reseausocial.repository.CommentRepository;
import com.universite.reseausocial.repository.PostRepository;
import com.universite.reseausocial.repository.ReportRepository;
import com.universite.reseausocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportResponse createReport(ReportRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (request.getPostId() == null && request.getCommentId() == null) {
            throw new IllegalArgumentException("Un signalement doit cibler une publication ou un commentaire");
        }

        Post post = null;
        if (request.getPostId() != null) {
            post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Publication introuvable"));
        }

        Comment comment = null;
        if (request.getCommentId() != null) {
            comment = commentRepository.findById(request.getCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Commentaire introuvable"));
        }

        Report report = Report.builder()
                .post(post)
                .comment(comment)
                .user(user)
                .raison(request.getRaison())
                .details(request.getDetails())
                .status("PENDING")
                .build();

        Report savedReport = reportRepository.save(report);
        return mapToReportResponse(savedReport);
    }

    public List<ReportResponse> getAllReports(String status, String userEmail) {
        verifyAuthenticatedUser(userEmail);
        List<Report> reports;
        if (status != null && !status.isEmpty()) {
            reports = reportRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        } else {
            reports = reportRepository.findAllByOrderByCreatedAtDesc();
        }
        return reports.stream().map(this::mapToReportResponse).collect(Collectors.toList());
    }

    public ReportResponse getReportById(Long id, String userEmail) {
        verifyAuthenticatedUser(userEmail);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Signalement introuvable avec l'id : " + id));
        return mapToReportResponse(report);
    }

    @Transactional
    public ReportResponse resolveReport(Long id, String userEmail) {
        verifyModerator(userEmail);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Signalement introuvable avec l'id : " + id));
        report.setStatus("RESOLVED");
        return mapToReportResponse(reportRepository.save(report));
    }

    @Transactional
    public ReportResponse rejectReport(Long id, String userEmail) {
        verifyModerator(userEmail);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Signalement introuvable avec l'id : " + id));
        report.setStatus("REJECTED");
        return mapToReportResponse(reportRepository.save(report));
    }

    private void verifyModerator(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        if (!"MODERATOR".equalsIgnoreCase(user.getRole())) {
            throw new UnauthorizedException("Accès réservé aux modérateurs");
        }
    }

    private void verifyAuthenticatedUser(String userEmail) {
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    public ReportResponse mapToReportResponse(Report report) {
        String postContent = null;
        String postAuthor = null;
        Long postId = null;
        if (report.getPost() != null) {
            postId = report.getPost().getId();
            postContent = report.getPost().getContenu();
            postAuthor = report.getPost().getUser().getPrenom() + " " + report.getPost().getUser().getNom();
        }

        String commentContent = null;
        String commentAuthor = null;
        Long commentId = null;
        if (report.getComment() != null) {
            commentId = report.getComment().getId();
            commentContent = report.getComment().getContenu();
            commentAuthor = report.getComment().getUser().getPrenom() + " " + report.getComment().getUser().getNom();
        }

        return ReportResponse.builder()
                .id(report.getId())
                .postId(postId)
                .postContent(postContent)
                .postAuthor(postAuthor)
                .commentId(commentId)
                .commentContent(commentContent)
                .commentAuthor(commentAuthor)
                .reportedBy(UserResponse.builder()
                        .id(report.getUser().getId())
                        .nom(report.getUser().getNom())
                        .prenom(report.getUser().getPrenom())
                        .email(report.getUser().getEmail())
                        .filiere(report.getUser().getFiliere())
                        .niveau(report.getUser().getNiveau())
                        .photoProfil(report.getUser().getPhotoProfil())
                        .role(report.getUser().getRole())
                        .build())
                .raison(report.getRaison())
                .details(report.getDetails())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
