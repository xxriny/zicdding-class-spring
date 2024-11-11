package com.example.zicdding.domain.file.service;

import com.example.zicdding.domain.file.entity.FileData;
import com.example.zicdding.domain.file.repository.FileRepository;
import com.example.zicdding.domain.user.repository.UserRepository;
import com.example.zicdding.global.util.JwtUtil;
import com.example.zicdding.security.provider.JwtProvider;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MinioClient minioClient;
    private final JwtUtil jwtUtil;
    @Value("${minio.bucket-name}")
    private String bucketName;

    private final FileRepository fileDataRepository;
    public void uploadFile(MultipartFile file, String tableName) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String fileName = UUID.randomUUID().toString() + "." + file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream,file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = jwtUtil.getUserIdFromAuthentication(authentication);

        Long targetId = null;
        if("TB_USER".equals(tableName)){
            targetId = userId;
        }else if("TB_ITNEWS".equals(tableName)){
            targetId = null;
        }
        FileData fileData = FileData.builder()
                .originName(file.getOriginalFilename())
                .changedName(fileName)
                .path(getImageUrl(fileName))
                .targetId(targetId)
                .ext(file.getContentType())
                .size(file.getSize())
                .build();

        fileDataRepository.save(fileData);

    }
    public String getImageUrl(String fileName){
        return String.format("%s/%s", bucketName, fileName);
    }
}
