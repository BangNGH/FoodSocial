package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.Events;
import com.example.foodsocialproject.entity.Posts;
import com.example.foodsocialproject.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;


    public Events saveEvent(Events event) {
      return this.eventRepository.save(event);
    }

    public List<Events> getList() {
        return eventRepository.findAll();
    }

    public Events getEvent(UUID eventid) {
        return this.eventRepository.findById(eventid).get();
    }

    public Events findById(String eventID) {
        return this.eventRepository.findById(UUID.fromString(eventID)).get();
    }
}
