package com.unibe.chatwebsocket.repository;

import com.unibe.chatwebsocket.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
}
