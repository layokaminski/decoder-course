package com.ead.course.controllers;

import com.ead.course.DTOs.CourseDTO;
import com.ead.course.models.CourseModel;
import com.ead.course.services.CourseService;
import com.ead.course.specifications.SpecificationTemplate;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Log4j2
@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<Object> saveCourse(@RequestBody @Valid CourseDTO courseDTO) {
        log.debug("POST saveCourse courseDto received {} ", courseDTO.toString());

        var courseModel = new CourseModel();

        BeanUtils.copyProperties(courseDTO, courseModel);

        courseModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC-3")));
        courseModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC-3")));

        log.debug("POST saveCourse courseId saved {} ", courseModel.getCourseId());
        log.info("Course saved successfully courseId {} ", courseModel.getCourseId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(courseService.save(courseModel));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Object> deleteCourse(
            @PathVariable(value = "courseId") UUID courseId
            ) {
        log.debug("DELETE deleteCourse courseId received {} ", courseId);

        Optional<CourseModel> courseModelOptional = courseService.findById(courseId);

        if (!courseModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course Not Found");
        }

        courseService.delete(courseModelOptional.get());

        log.debug("DELETE deleteCourse courseId deleted {} ", courseId);
        log.info("Course deleted successfully courseId {} ", courseId);

        return ResponseEntity.status(HttpStatus.OK).body("Course deleted successfully");
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<Object> deleteCourse(
            @PathVariable(value = "courseId") UUID courseId,
            @RequestBody @Valid CourseDTO courseDTO
    ) {
        log.debug("PUT updateCourse courseDto received {} ", courseDTO.toString());

        Optional<CourseModel> courseModelOptional = courseService.findById(courseId);

        if (!courseModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course Not Found");
        }

        var courseModel = courseModelOptional.get();

        BeanUtils.copyProperties(courseDTO, courseModel);
        courseModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC-3")));

        log.debug("PUT updateCourse courseId saved {} ", courseModel.getCourseId());
        log.info("Course updated successfully courseId {} ", courseModel.getCourseId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(courseService.save(courseModel));
    }

    @GetMapping
    public ResponseEntity<Page<CourseModel>> getAllCourses(
            SpecificationTemplate.CourseSpec spec,
            @PageableDefault(page = 0, size = 10, sort = "courseId", direction = Sort.Direction.ASC)
            Pageable pageable,
            @RequestParam(required = false) UUID userId
    ) {
        Page<CourseModel> courseModelPage = null;

        if (userId != null) {
            courseModelPage = courseService.findAll(SpecificationTemplate.courseUserId(userId).and(spec), pageable);
        } else {
            courseModelPage = courseService.findAll(spec, pageable);
        }

        if (!courseModelPage.isEmpty()) {
            for (CourseModel course: courseModelPage.toList()) {
                course.add(linkTo(methodOn(CourseController.class).getCourseById(course.getCourseId())).withSelfRel());
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(courseModelPage);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Object> getCourseById(@PathVariable(value = "courseId") UUID courseId) {
        Optional<CourseModel> courseModelOptional = courseService.findById(courseId);

        if (!courseModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(courseModelOptional.get());
        }
    }
}
