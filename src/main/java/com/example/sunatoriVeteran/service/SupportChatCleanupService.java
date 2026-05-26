package com.example.sunatoriVeteran.service;

import com.example.sunatoriVeteran.model.SupportChat;
import com.example.sunatoriVeteran.repository.SupportChatRepository;
import com.example.sunatoriVeteran.repository.SupportMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportChatCleanupService {

    @Autowired
    private SupportChatRepository chatRepository;

    @Autowired
    private SupportMessageRepository messageRepository;

    // Run every hour to check for old closed chats
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupOldChats() {
        // Find chats closed more than 24 hours ago
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(24);
        List<SupportChat> oldChats = chatRepository.findByStatusAndClosedAtBefore("CLOSED", cutoffDate);

        if (!oldChats.isEmpty()) {
            System.out.println("Cleaning up " + oldChats.size() + " old closed support chats...");
            for (SupportChat chat : oldChats) {
                // Delete messages first to maintain referential integrity if there are constraints
                messageRepository.deleteByChatId(chat.getId());
                chatRepository.delete(chat);
            }
        }
    }
}
