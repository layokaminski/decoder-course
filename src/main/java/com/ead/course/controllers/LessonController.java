package com.ead.course.controllers;

import com.ead.course.DTOs.LessonDTO;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.LessonService;
import com.ead.course.services.ModuleService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private ModuleService moduleService;

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Object> saveLesson(
            @PathVariable(value = "moduleId") UUID moduleId,
            @RequestBody @Valid LessonDTO lessonDTO
    ) {
        log.debug("POST saveLesson lessonDto received {} ", lessonDTO.toString());

        Optional<ModuleModel> moduleModelOptional = moduleService.findById(moduleId);

        if (!moduleModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module Not Found");
        }

        var lessonModel = new LessonModel();
        BeanUtils.copyProperties(lessonDTO, lessonModel);

        lessonModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC-3")));
        lessonModel.setModule(moduleModelOptional.get());

        log.debug("POST saveLesson lessonId saved {} ", lessonModel.getLessonId());
        log.info("Lesson saved successfully lessonId {} ", lessonModel.getLessonId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lessonService.save(lessonModel));
    }

    @DeleteMapping("/modules/{moduleId}/lessons/{lessonId}")
    public ResponseEntity<Object> deleteLesson(
            @PathVariable(value = "moduleId") UUID moduleId,
            @PathVariable(value = "lessonId") UUID lessonId
    ) {
        log.debug("DELETE deleteLesson lessonId received {} ", lessonId);

        Optional<LessonModel> lessonModelOptional = lessonService
                .findLessonIntoModule(moduleId, lessonId);

        if (!lessonModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lesson not found for this module");
        }

        log.debug("DELETE deleteLesson lessonId deleted {} ", lessonId);
        log.info("Lesson deleted successfully lessonId {} ", lessonId);

        lessonService.delete(lessonModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Lesson deleted successfully");
    }

    @PutMapping("/modules/{moduleId}/lessons/{lessonId}")
    public ResponseEntity<Object> updateLesson(
            @PathVariable(value = "moduleId") UUID moduleId,
            @PathVariable(value = "lessonId") UUID lessonId,
            @RequestBody @Valid LessonDTO lessonDTO
    ) {
        log.debug("PUT updateLesson lessonDto received {} ", lessonDTO.toString());

        Optional<LessonModel> lessonModelOptional = lessonService
                .findLessonIntoModule(moduleId, lessonId);

        if (!lessonModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lesson not found for this module");
        }

        var lessonModel = lessonModelOptional.get();

        BeanUtils.copyProperties(lessonDTO, lessonModel);

        log.debug("PUT updateLesson lessonId saved {} ", lessonModel.getLessonId());
        log.info("Lesson updated successfully lessonId {} ", lessonModel.getLessonId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(lessonService.save(lessonModel));
    }

    @GetMapping("/modules/{moduleId}/lessons/{lessonId}")
    public ResponseEntity<Object> getOneLesson(
            @PathVariable(value = "moduleId") UUID moduleId,
            @PathVariable(value = "lessonId") UUID lessonId
    ) {
        Optional<LessonModel> lessonModelOptional = lessonService
                .findLessonIntoModule(moduleId, lessonId);

        if (!lessonModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lesson not found for this module");
        }

        return ResponseEntity.status(HttpStatus.OK).body(lessonModelOptional.get());
    }

    @GetMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Page<LessonModel>> getAllLessons(
            SpecificationTemplate.LessonSpec spec,
            @PageableDefault(page = 0, size = 10, sort = "lessonId", direction = Sort.Direction.ASC)
            Pageable pageable,
            @PathVariable(value = "moduleId") UUID moduleId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                lessonService.findAllByModule(
                        SpecificationTemplate.lessonModuleId(moduleId)
                                .and(spec), pageable));
    }
}
