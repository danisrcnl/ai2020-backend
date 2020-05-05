package it.polito.ai.lab2.controllers;

import it.polito.ai.lab2.dtos.CourseDTO;
import it.polito.ai.lab2.dtos.StudentDTO;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

public class ModelHelper {

    public static CourseDTO enrich(CourseDTO courseDTO) {
        Link link = linkTo(methodOn(CourseController.class).getOne(courseDTO.getName())).withSelfRel();
        courseDTO.add(link);
        return courseDTO;
    }

    public static StudentDTO enrich(StudentDTO studentDTO) {
        Link link = linkTo(methodOn(StudentController.class).getOne(studentDTO.getName())).withSelfRel();
        studentDTO.add(link);
        return studentDTO;
    }

}
