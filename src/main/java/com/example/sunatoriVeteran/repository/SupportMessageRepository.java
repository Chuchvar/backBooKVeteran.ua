package com.example.sunatoriVeteran.repository;

import com.example.sunatoriVeteran.model.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findByChatIdOrderByCreatedAtAsc(Long chatId);
    void deleteByChatId(Long chatId);
}
