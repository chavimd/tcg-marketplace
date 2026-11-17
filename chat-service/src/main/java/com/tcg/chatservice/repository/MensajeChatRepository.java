package com.tcg.chatservice.repository;

import com.tcg.chatservice.model.MensajeChat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {

    @EntityGraph(attributePaths = "emisor")
    List<MensajeChat> findAllByPropuestaIdOrderByCreatedAtAscIdAsc(Long propuestaId);
}
