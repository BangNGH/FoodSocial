package com.example.foodsocialproject.event;

import com.example.foodsocialproject.entity.Users;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RegistrationCompleteEvent extends ApplicationEvent {
    private Users user;
    private String applicationUrl;

    public RegistrationCompleteEvent( Users user, String applicationUrl) {
        super(user);
        this.user = user;
        this.applicationUrl = applicationUrl;
    }
}
