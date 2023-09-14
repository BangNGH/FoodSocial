package com.example.foodsocialproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.twilio.rest.chat.v1.service.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "Events")
@AllArgsConstructor
@NoArgsConstructor
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "event_name")
    private String event_name;

    @Column(name = "event_image")
    private String event_image;

    @Column(name = "event_description", length = 3000)
    private String event_description;

    @Column(name = "start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date start_date;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "end_date")
    private Date end_date;

    @Column(name = "actived_event")
    private Boolean actived_event = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "organizer_id")
    private Users organizer;

    @ManyToMany(mappedBy = "events")
    @JsonIgnore
    private Set<Users> participants = new HashSet<>();

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnore
    private event_ingredient event_ingredient;
    @Transient
    public String getEventImagePath() {
        if (id == null||event_image==null) {
            return null;
        }
        return "/event-images/" + id + "/" + event_image;
    }
}
