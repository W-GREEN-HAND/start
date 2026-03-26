package com.example.wmall.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;

import com.example.wmall.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FileUploadUtil {
    @Value("${creation.upload.base-path}")
    private String basePath;
    @Value("${creation.upload.max-size}")
    private long maxSize;
    @Value("${creation.upload.allowed-types}")
    private String allowedTypes;

    /**
     * 文件上传
     */
    public String upload(MultipartFile file) {

        // 1. 校验文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小超过限制（最大100MB）");
        }

        // 2. 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }
        String suffix = FileUtil.extName(originalFilename);
        String lowerSuffix = suffix.toLowerCase();
        // 更严谨的类型校验：按照逗号拆分后精确匹配
        boolean typeAllowed = false;
        for (String type : allowedTypes.split(",")) {
            if (lowerSuffix.equals(type.trim().toLowerCase())) {
                typeAllowed = true;
                break;
            }
        }
        if (!typeAllowed) {
            throw new BusinessException("不支持的文件类型：" + suffix);
        }

        // 3. 解析上传根目录为绝对路径，构建文件路径（按日期分目录）
        String rootPath = Paths.get(basePath).toFile().getAbsolutePath();
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File dir = new File(rootPath + File.separator + dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 4. 生成唯一文件名
        String fileName = IdUtil.fastSimpleUUID() + "." + suffix;
        String filePath = rootPath + File.separator + dateDir + File.separator + fileName;

        // 5. 上传文件
        try {
            file.transferTo(new File(filePath));
            // 返回前端可访问的 URL 路径：/creation/日期/文件名
            return "/creation/" +"upload/" + dateDir + "/" + fileName;
        } catch (Exception e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    public void delete(String filePath) {
        // filePath 一般为 /creation/20251216/xxx.jpg，需要还原为本地物理路径
        String rootPath = Paths.get(basePath).toFile().getAbsolutePath();
        String relativePath = filePath;
        if (relativePath.startsWith("/creation")) {
            relativePath = relativePath.substring("/creation".length());
        }
        // 去掉开头的 /，避免拼接出错
        if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
            relativePath = relativePath.substring(1);
        }
        File file = new File(rootPath + File.separator + relativePath);
        if (file.exists()) {
            file.delete();
        }
    }
}