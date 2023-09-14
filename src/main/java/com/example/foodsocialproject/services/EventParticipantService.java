package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.EventParticipants;
import com.example.foodsocialproject.entity.Events;
import com.example.foodsocialproject.entity.Posts;
import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.repository.EventParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventParticipantService {
    private final EventParticipantRepository eventParticipantRepository;

    public void saveRela(Events event, Users joinUser) {
        EventParticipants newParticipants = new EventParticipants();
        newParticipants.setEvent(event);
        newParticipants.setUser(joinUser);
        this.eventParticipantRepository.save(newParticipants);
    }

    public EventParticipants findEventParticipants(String eventID, Posts post) {
        EventParticipants newParticipants = eventParticipantRepository.findParticipants(UUID.fromString(eventID), post.getUser().getId());
        newParticipants.setPost(post);
        return this.eventParticipantRepository.save(newParticipants);
    }

    public EventParticipants findEventParticipants(UUID eventID, Users user) {
        return eventParticipantRepository.findParticipants(eventID, user.getId());
    }
}
