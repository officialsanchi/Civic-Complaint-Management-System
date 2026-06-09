package com.civic.complaint.service;

import com.civic.complaint.dto.request.ComplaintRequest;
import com.civic.complaint.dto.response.ComplaintResponse;
import com.civic.complaint.enums.ComplaintStatus;
import com.civic.complaint.enums.NotificationType;
import com.civic.complaint.enums.Role;
import com.civic.complaint.exception.AccessDeniedException;
import com.civic.complaint.exception.ResourceNotFoundException;
import com.civic.complaint.model.*;
import com.civic.complaint.repository.ComplaintRepository;
import com.civic.complaint.service.impl.ComplaintServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    @Mock private ComplaintRepository complaintRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private ComplaintServiceImpl complaintService;

    private User citizen;
    private User admin;
    private Complaint complaint;

    @BeforeEach
    void setUp() {
        citizen = User.builder()
                .id(1L).username("citizen1").email("c@test.com")
                .fullName("Citizen One").role(Role.CITIZEN).enabled(true).build();

        admin = User.builder()
                .id(2L).username("admin1").email("a@test.com")
                .fullName("Admin One").role(Role.ADMIN).enabled(true).build();

        Location location = Location.builder()
                .latitude(6.5244).longitude(3.3792)
                .address("123 Lagos Street").ward("Ikeja").lga("Lagos Island").state("Lagos")
                .build();

        complaint = Complaint.builder()
                .id(1L).title("Broken Road").description("Large pothole on main road")
                .status(ComplaintStatus.PENDING).category("Road")
                .reporter(citizen).location(location)
                .upvotes(0).reportedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create complaint and send notification")
    void createComplaint_Success() {
        ComplaintRequest.Create request = new ComplaintRequest.Create();
        request.setTitle("Broken Road");
        request.setDescription("Large pothole on main road");
        request.setCategory("Road");
        ComplaintRequest.LocationDto locDto = new ComplaintRequest.LocationDto();
        locDto.setAddress("123 Lagos Street");
        locDto.setLga("Lagos Island");
        locDto.setState("Lagos");
        request.setLocation(locDto);

        when(complaintRepository.save(any(Complaint.class))).thenReturn(complaint);

        ComplaintResponse.Full result = complaintService.createComplaint(request, citizen);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Broken Road");
        verify(notificationService).sendNotification(
                eq(citizen), anyString(), eq(NotificationType.COMPLAINT_SUBMITTED), eq(1L));
    }

    @Test
    @DisplayName("Should get complaint by ID")
    void getComplaintById_Success() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        ComplaintResponse.Full result = complaintService.getComplaintById(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Broken Road");
    }

    @Test
    @DisplayName("Should throw when complaint not found")
    void getComplaintById_NotFound_Throws() {
        when(complaintRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> complaintService.getComplaintById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Owner can update their PENDING complaint")
    void updateComplaint_OwnerSuccess() {
        ComplaintRequest.Update request = new ComplaintRequest.Update();
        request.setTitle("Updated Title");

        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any())).thenReturn(complaint);

        ComplaintResponse.Full result = complaintService.updateComplaint(1L, request, citizen);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Non-owner cannot update complaint")
    void updateComplaint_NonOwner_Throws() {
        User other = User.builder().id(5L).username("other").role(Role.CITIZEN).build();
        ComplaintRequest.Update request = new ComplaintRequest.Update();

        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> complaintService.updateComplaint(1L, request, other))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Admin can update status and notify reporter")
    void updateStatus_AdminSuccess() {
        ComplaintRequest.UpdateStatus request = new ComplaintRequest.UpdateStatus();
        request.setStatus(ComplaintStatus.IN_PROGRESS);
        request.setAdminNote("Assigned to road crew");

        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any())).thenReturn(complaint);

        ComplaintResponse.Full result = complaintService.updateStatus(1L, request, admin);

        assertThat(result).isNotNull();
        verify(notificationService).sendNotification(
                eq(citizen), anyString(), eq(NotificationType.STATUS_UPDATED), eq(1L));
    }

    @Test
    @DisplayName("Should return paginated user complaints")
    void getUserComplaints_Success() {
        Page<Complaint> page = new PageImpl<>(List.of(complaint));
        when(complaintRepository.findByReporter(eq(citizen), any(Pageable.class))).thenReturn(page);

        Page<ComplaintResponse.Full> result =
                complaintService.getUserComplaints(citizen, 0, 10, "reportedAt", "desc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Broken Road");
    }

    @Test
    @DisplayName("Delete complaint by owner succeeds")
    void deleteComplaint_OwnerSuccess() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        complaintService.deleteComplaint(1L, citizen);
        verify(complaintRepository).delete(complaint);
    }

    @Test
    @DisplayName("Delete complaint by non-owner throws")
    void deleteComplaint_NonOwner_Throws() {
        User other = User.builder().id(5L).username("other").role(Role.CITIZEN).build();
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(complaint));
        assertThatThrownBy(() -> complaintService.deleteComplaint(1L, other))
                .isInstanceOf(AccessDeniedException.class);
    }
}
