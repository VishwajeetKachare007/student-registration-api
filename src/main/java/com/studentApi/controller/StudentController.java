package com.studentApi.controller;

import com.studentApi.entity.Student;
import com.studentApi.repository.StudentRepository;
import com.studentApi.service.EmailService;
import com.studentApi.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private FileStorageService fileService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public String registerStudent(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam MultipartFile tenthMarksheet,
            @RequestParam MultipartFile twelfthMarksheet,
            @RequestParam MultipartFile aadharCard,
            @RequestParam MultipartFile profilePhoto
    ) {
        try {
            // Save files
            String folderName = name + "_" + System.currentTimeMillis();
            String tenthPath = fileService.storeFile(tenthMarksheet, folderName);
            String twelfthPath = fileService.storeFile(twelfthMarksheet, folderName);
            String aadharPath = fileService.storeFile(aadharCard, folderName);
            String photoPath = fileService.storeFile(profilePhoto, folderName);

            // Save student
            Student student = new Student(null, name, email, phone, tenthPath, twelfthPath, aadharPath, photoPath);
            studentRepo.save(student);

            // Send Email
            String[] attachments = {tenthPath, twelfthPath, aadharPath, photoPath};
            //Email Body

            String emailBody = String.format("""
                Dear %s,

                Your application has been successfully registered.

                Student Details:
                - Name: %s
                - Email: %s
                - Phone: %s

                Your documents are attached with this email.

                Regards,  
                SSE Admission Team
                """, name, name, email, phone);
            emailService.sendWithAttachments(email, "Registration Successful", emailBody, attachments);
            return "Student Registered Successfully and Email Sent!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}