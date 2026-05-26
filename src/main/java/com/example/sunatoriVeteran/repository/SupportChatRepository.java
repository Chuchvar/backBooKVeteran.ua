package com.example.sunatoriVeteran.repository;

import com.example.sunatoriVeteran.model.SupportChat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportChatRepository extends JpaRepository<SupportChat, Long> {
    List<SupportChat> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<SupportChat> findByStatusOrderByCreatedAtDesc(String status);
    List<SupportChat> findAllByOrderByCreatedAtDesc();
    List<SupportChat> findByAdminIdIsNullOrderByCreatedAtDesc();
    List<SupportChat> findByAdminIdOrderByCreatedAtDesc(Long adminId);
    List<SupportChat> findByStatusAndClosedAtBefore(String status, java.time.LocalDateTime date);
}
