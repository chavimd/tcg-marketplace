package com.tcg.chatservice.service;

import com.tcg.chatservice.dto.ChatMessage;
import com.tcg.chatservice.model.MensajeChat;
import com.tcg.chatservice.repository.MensajeChatRepository;
import com.tcg.chatservice.repository.UsuarioRefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final MensajeChatRepository mensajeRepository;
    private final UsuarioRefRepository usuarioRepository;

    public ChatService(MensajeChatRepository mensajeRepository, UsuarioRefRepository usuarioRepository) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ChatMessage guardar(Long tradeId, Long emisorId, String contenido) {
        var emisor = usuarioRepository.getReferenceById(emisorId);
        MensajeChat mensaje = mensajeRepository.save(new MensajeChat(tradeId, emisor, contenido));
        return ChatMessage.from(mensaje);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> historial(Long tradeId) {
        return mensajeRepository.findAllByPropuestaIdOrderByCreatedAtAscIdAsc(tradeId).stream()
                .map(ChatMessage::from)
                .toList();
    }
}
