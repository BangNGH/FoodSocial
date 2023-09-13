package com.example.foodsocialproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "VerificationToken")
@NoArgsConstructor

public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expirationTime", nullable = false)
    private Date expirationTime;

    private static final int EXPIRATION_TIME =10;
    public VerificationToken(String token, Users user) {
        super();
        this.token = token;
        this.user = user;
        this.expirationTime = this.getTokenExpirationTime();
    }

    public VerificationToken(String token) {
        super();
        this.token = token;
        this.expirationTime = this.getTokenExpirationTime();
    }

    // tính toán và trả về thời gian hết hạn của token
    public Date getTokenExpirationTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, EXPIRATION_TIME);//để thêm một khoảng thời gian (EXPIRATION_TIME) vào thời gian hiện tại.
        return new Date(calendar.getTime().getTime());
    }
}
