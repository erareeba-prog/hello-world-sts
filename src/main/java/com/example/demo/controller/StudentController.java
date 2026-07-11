package com.example.demo.controller;
import com.example.demo.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentController {
    private final StudentRepository repo;
    public StudentController(StudentRepository repo) {
        this.repo = repo;
    }
    @GetMapping("/students")
    public String list(Model model) {
        model.addAttribute("students", repo.findAll());
        return "students";
    }
}