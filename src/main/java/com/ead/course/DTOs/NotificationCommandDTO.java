package com.ead.course.DTOs;

import lombok.Data;

import java.util.UUID;

@Data
public class NotificationCommandDTO {
    private String title;
    private String message;
    private UUID userId;
}
