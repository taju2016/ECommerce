package com.Ecommerce.project.service.Impl;

import com.Ecommerce.project.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String randomId = UUID.randomUUID().toString();
        String newFileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + newFileName;
        File folder = new File(path);
        if(!folder.exists()) {
            folder.mkdir();
        }
        Files.copy(file.getInputStream(), Paths.get(filePath));
        return newFileName;
    }
}
