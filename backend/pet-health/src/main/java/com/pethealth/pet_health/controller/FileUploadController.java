package com.pethealth.pet_health.controller;

import com.pethealth.pet_health.entity.Pet;
import com.pethealth.pet_health.service.PetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final PetService petService;

    // 图片保存目录（相对路径，保存在项目根目录的 uploads 文件夹）
    private static final String UPLOAD_DIR = "uploads/avatars/";

    public FileUploadController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping("/avatar/{petId}")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long petId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 创建目录
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            // 生成唯一文件名
            String ext = file.getOriginalFilename() != null
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID() + ext;

            // 保存文件
            Path path = Paths.get(UPLOAD_DIR + filename);
            Files.write(path, file.getBytes());

            // 更新宠物头像路径
            Pet pet = petService.getPetById(petId);
            pet.setAvatar("/avatars/" + filename);
            petService.updatePet(petId, pet);

            Map<String, String> res = new HashMap<>();
            res.put("avatarUrl", "/avatars/" + filename);
            return ResponseEntity.ok(res);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("上传失败: " + e.getMessage());
        }
    }
}