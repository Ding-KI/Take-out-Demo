package com.sky.controller.admin;

/**
 * 通用接口
 */

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "Common interface")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("Upload file")
    public Result<String> upload(MultipartFile file){ // Upload file, parameter name and front end keep consistent{
        log.info("Upload file: {}", file);
        try {
            // Original file name
            String originalFilename = file.getOriginalFilename();
            // Extract file name suffix
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // Generate new file name
            String objectName = UUID.randomUUID().toString() + suffix;
            // Get file request path
            String filePath = aliOssUtil.upload(file.getBytes(),objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("File upload failed: {}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}


