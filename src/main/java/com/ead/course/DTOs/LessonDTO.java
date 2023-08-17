package com.ead.course.DTOs;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LessonDTO {

    @NotBlank
    private String title;
    private String description;
    @NotBlank
    private String videoUrl;
}
