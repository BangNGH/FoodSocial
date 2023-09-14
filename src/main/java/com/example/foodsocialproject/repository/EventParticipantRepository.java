package com.example.foodsocialproject.repository;

import com.example.foodsocialproject.entity.EventParticipants;
import com.example.foodsocialproject.entity.Events;
import com.example.foodsocialproject.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipants, UUID> {

    Long countById(UUID id);

    @Query("SELECT e FROM EventParticipants e where e.event.id =?1 and e.user.id =?2")
    EventParticipants findParticipants(UUID eventID, UUID id);


}
