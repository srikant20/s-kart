package com.ecommerce.project.services;

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

        //file name of current/original file
        String originalFilename = file.getOriginalFilename();

        //generate a unique file name
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFilename.substring(originalFilename.lastIndexOf(".")));
        String filePath = path + File.separator + fileName;

        //check if path exists and create
        File folder = new File(path);
        if(!folder.exists())
            folder.mkdir();

        //upload to the server
        Files.copy(file.getInputStream(), Paths.get(filePath));
        //returning file name
        return fileName;
    }
}
