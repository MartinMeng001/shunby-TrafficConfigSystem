package com.traffic.config.util;

import com.traffic.config.exception.ConfigException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public final class FileUtil {

    private FileUtil() {
        // 工具类，禁止实例化
    }

    // ==================== 基础文件操作 ====================

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    public static boolean exists(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 检查文件是否存在
     *
     * @param file 文件对象
     * @return 文件是否存在
     */
    public static boolean exists(File file) {
        return file != null && file.exists();
    }

    /**
     * 创建文件，如果父目录不存在则创建
     *
     * @param filePath 文件路径
     * @return 创建的文件对象
     * @throws ConfigException 创建失败时抛出
     */
    public static File createFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        try {
            Path path = Paths.get(filePath);

            // 创建父目录
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                log.debug("创建目录: {}", parentDir);
            }

            // 创建文件
            if (!Files.exists(path)) {
                Files.createFile(path);
                log.debug("创建文件: {}", path);
            }

            return path.toFile();

        } catch (IOException e) {
            log.error("创建文件失败: {}", filePath, e);
            throw new ConfigException("FILE_CREATE_ERROR", "创建文件失败: " + filePath, e);
        }
    }

    /**
     * 创建目录
     *
     * @param dirPath 目录路径
     * @return 创建的目录对象
     * @throws ConfigException 创建失败时抛出
     */
    public static File createDirectory(String dirPath) {
        if (!StringUtils.hasText(dirPath)) {
            throw new IllegalArgumentException("目录路径不能为空");
        }

        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.debug("创建目录: {}", path);
            }
            return path.toFile();

        } catch (IOException e) {
            log.error("创建目录失败: {}", dirPath, e);
            throw new ConfigException("DIR_CREATE_ERROR", "创建目录失败: " + dirPath, e);
        }
    }

    /**
     * 删除文件或目录
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean delete(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    deleteDirectory(path);
                } else {
                    Files.delete(path);
                }
                log.debug("删除文件/目录: {}", path);
                return true;
            }
            return false;

        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 递归删除目录
     *
     * @param dirPath 目录路径
     * @throws IOException 删除失败时抛出
     */
    private static void deleteDirectory(Path dirPath) throws IOException {
        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @param replaceExisting 是否覆盖已存在的文件
     * @throws ConfigException 复制失败时抛出
     */
    public static void copyFile(String sourcePath, String targetPath, boolean replaceExisting) {
        if (!StringUtils.hasText(sourcePath) || !StringUtils.hasText(targetPath)) {
            throw new IllegalArgumentException("源路径和目标路径不能为空");
        }

        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            // 创建目标目录
            Path targetDir = target.getParent();
            if (targetDir != null && !Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // 复制文件
            if (replaceExisting) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(source, target);
            }

            log.debug("文件复制成功: {} -> {}", source, target);

        } catch (IOException e) {
            log.error("文件复制失败: {} -> {}", sourcePath, targetPath, e);
            throw new ConfigException("FILE_COPY_ERROR",
                    String.format("文件复制失败: %s -> %s", sourcePath, targetPath), e);
        }
    }

    /**
     * 移动文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @param replaceExisting 是否覆盖已存在的文件
     * @throws ConfigException 移动失败时抛出
     */
    public static void moveFile(String sourcePath, String targetPath, boolean replaceExisting) {
        if (!StringUtils.hasText(sourcePath) || !StringUtils.hasText(targetPath)) {
            throw new IllegalArgumentException("源路径和目标路径不能为空");
        }

        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            // 创建目标目录
            Path targetDir = target.getParent();
            if (targetDir != null && !Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // 移动文件
            if (replaceExisting) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(source, target);
            }

            log.debug("文件移动成功: {} -> {}", source, target);

        } catch (IOException e) {
            log.error("文件移动失败: {} -> {}", sourcePath, targetPath, e);
            throw new ConfigException("FILE_MOVE_ERROR",
                    String.format("文件移动失败: %s -> %s", sourcePath, targetPath), e);
        }
    }

    // ==================== 文件内容操作 ====================

    /**
     * 读取文件内容为字符串
     *
     * @param filePath 文件路径
     * @param charset 字符编码
     * @return 文件内容
     * @throws ConfigException 读取失败时抛出
     */
    public static String readFileToString(String filePath, Charset charset) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new ConfigException("FILE_NOT_FOUND", "文件不存在: " + filePath);
            }

            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, charset != null ? charset : StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            throw new ConfigException("FILE_READ_ERROR", "读取文件失败: " + filePath, e);
        }
    }

    /**
     * 读取文件内容为字符串（UTF-8编码）
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    public static String readFileToString(String filePath) {
        return readFileToString(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件所有行
     *
     * @param filePath 文件路径
     * @param charset 字符编码
     * @return 文件行列表
     * @throws ConfigException 读取失败时抛出
     */
    public static List<String> readLines(String filePath, Charset charset) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new ConfigException("FILE_NOT_FOUND", "文件不存在: " + filePath);
            }

            return Files.readAllLines(path, charset != null ? charset : StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("读取文件行失败: {}", filePath, e);
            throw new ConfigException("FILE_READ_ERROR", "读取文件行失败: " + filePath, e);
        }
    }

    /**
     * 读取文件所有行（UTF-8编码）
     *
     * @param filePath 文件路径
     * @return 文件行列表
     */
    public static List<String> readLines(String filePath) {
        return readLines(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 写入字符串到文件
     *
     * @param filePath 文件路径
     * @param content 文件内容
     * @param charset 字符编码
     * @param append 是否追加模式
     * @throws ConfigException 写入失败时抛出
     */
    public static void writeStringToFile(String filePath, String content, Charset charset, boolean append) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        try {
            Path path = Paths.get(filePath);

            // 创建父目录
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // 写入文件
            if (append) {
                Files.write(path, content.getBytes(charset != null ? charset : StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.write(path, content.getBytes(charset != null ? charset : StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            log.debug("写入文件成功: {}", path);

        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
            throw new ConfigException("FILE_WRITE_ERROR", "写入文件失败: " + filePath, e);
        }
    }

    /**
     * 写入字符串到文件（UTF-8编码，覆盖模式）
     *
     * @param filePath 文件路径
     * @param content 文件内容
     */
    public static void writeStringToFile(String filePath, String content) {
        writeStringToFile(filePath, content, StandardCharsets.UTF_8, false);
    }

    /**
     * 写入行列表到文件
     *
     * @param filePath 文件路径
     * @param lines 行列表
     * @param charset 字符编码
     * @param append 是否追加模式
     * @throws ConfigException 写入失败时抛出
     */
    public static void writeLines(String filePath, List<String> lines, Charset charset, boolean append) {
        if (!StringUtils.hasText(filePath) || lines == null) {
            throw new IllegalArgumentException("文件路径和行列表不能为空");
        }

        try {
            Path path = Paths.get(filePath);

            // 创建父目录
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // 写入文件
            if (append) {
                Files.write(path, lines, charset != null ? charset : StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.write(path, lines, charset != null ? charset : StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            log.debug("写入文件行成功: {}", path);

        } catch (IOException e) {
            log.error("写入文件行失败: {}", filePath, e);
            throw new ConfigException("FILE_WRITE_ERROR", "写入文件行失败: " + filePath, e);
        }
    }

    // ==================== 文件信息获取 ====================

    /**
     * 获取文件大小
     *
     * @param filePath 文件路径
     * @return 文件大小（字节）
     */
    public static long getFileSize(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return 0L;
        }

        try {
            Path path = Paths.get(filePath);
            return Files.exists(path) ? Files.size(path) : 0L;
        } catch (IOException e) {
            log.warn("获取文件大小失败: {}", filePath);
            return 0L;
        }
    }

    /**
     * 获取文件最后修改时间
     *
     * @param filePath 文件路径
     * @return 最后修改时间戳（毫秒）
     */
    public static long getLastModified(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return 0L;
        }

        try {
            Path path = Paths.get(filePath);
            return Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : 0L;
        } catch (IOException e) {
            log.warn("获取文件修改时间失败: {}", filePath);
            return 0L;
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名（不包含点）
     */
    public static String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 获取不带扩展名的文件名
     *
     * @param fileName 文件名
     * @return 不带扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }

    // ==================== 文件安全操作 ====================

    /**
     * 计算文件MD5值
     *
     * @param filePath 文件路径
     * @return MD5值（16进制字符串）
     * @throws ConfigException 计算失败时抛出
     */
    public static String calculateMD5(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new ConfigException("FILE_NOT_FOUND", "文件不存在: " + filePath);
            }

            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("计算文件MD5失败: {}", filePath, e);
            throw new ConfigException("FILE_MD5_ERROR", "计算文件MD5失败: " + filePath, e);
        }
    }

    /**
     * 比较两个文件是否相同
     *
     * @param filePath1 文件1路径
     * @param filePath2 文件2路径
     * @return 是否相同
     */
    public static boolean isSameFile(String filePath1, String filePath2) {
        if (!StringUtils.hasText(filePath1) || !StringUtils.hasText(filePath2)) {
            return false;
        }

        try {
            Path path1 = Paths.get(filePath1);
            Path path2 = Paths.get(filePath2);

            if (!Files.exists(path1) || !Files.exists(path2)) {
                return false;
            }

            // 先比较文件大小
            if (Files.size(path1) != Files.size(path2)) {
                return false;
            }

            // 再比较MD5
            return calculateMD5(filePath1).equals(calculateMD5(filePath2));

        } catch (Exception e) {
            log.warn("比较文件失败: {} vs {}", filePath1, filePath2);
            return false;
        }
    }

    // ==================== 备份和压缩操作 ====================

    /**
     * 创建带时间戳的备份文件名
     *
     * @param originalFileName 原始文件名
     * @return 备份文件名
     */
    public static String generateBackupFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            throw new IllegalArgumentException("原始文件名不能为空");
        }

        String nameWithoutExt = getFileNameWithoutExtension(originalFileName);
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        if (StringUtils.hasText(extension)) {
            return nameWithoutExt + "_backup_" + timestamp + "." + extension;
        } else {
            return originalFileName + "_backup_" + timestamp;
        }
    }

    /**
     * 压缩文件
     *
     * @param sourceFilePath 源文件路径
     * @param zipFilePath ZIP文件路径
     * @throws ConfigException 压缩失败时抛出
     */
    public static void zipFile(String sourceFilePath, String zipFilePath) {
        if (!StringUtils.hasText(sourceFilePath) || !StringUtils.hasText(zipFilePath)) {
            throw new IllegalArgumentException("源文件路径和ZIP文件路径不能为空");
        }

        try {
            Path sourcePath = Paths.get(sourceFilePath);
            Path zipPath = Paths.get(zipFilePath);

            if (!Files.exists(sourcePath)) {
                throw new ConfigException("FILE_NOT_FOUND", "源文件不存在: " + sourceFilePath);
            }

            // 创建ZIP文件目录
            Path zipDir = zipPath.getParent();
            if (zipDir != null && !Files.exists(zipDir)) {
                Files.createDirectories(zipDir);
            }

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                ZipEntry entry = new ZipEntry(sourcePath.getFileName().toString());
                zos.putNextEntry(entry);

                Files.copy(sourcePath, zos);
                zos.closeEntry();
            }

            log.debug("文件压缩成功: {} -> {}", sourcePath, zipPath);

        } catch (IOException e) {
            log.error("文件压缩失败: {} -> {}", sourceFilePath, zipFilePath, e);
            throw new ConfigException("FILE_ZIP_ERROR",
                    String.format("文件压缩失败: %s -> %s", sourceFilePath, zipFilePath), e);
        }
    }

    /**
     * 解压缩文件
     *
     * @param zipFilePath ZIP文件路径
     * @param destDirPath 解压目标目录
     * @return 解压出的文件列表
     * @throws ConfigException 解压失败时抛出
     */
    public static List<String> unzipFile(String zipFilePath, String destDirPath) {
        if (!StringUtils.hasText(zipFilePath) || !StringUtils.hasText(destDirPath)) {
            throw new IllegalArgumentException("ZIP文件路径和目标目录不能为空");
        }

        List<String> extractedFiles = new ArrayList<>();

        try {
            Path zipPath = Paths.get(zipFilePath);
            Path destPath = Paths.get(destDirPath);

            if (!Files.exists(zipPath)) {
                throw new ConfigException("FILE_NOT_FOUND", "ZIP文件不存在: " + zipFilePath);
            }

            // 创建目标目录
            if (!Files.exists(destPath)) {
                Files.createDirectories(destPath);
            }

            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path entryPath = destPath.resolve(entry.getName());

                    // 安全检查：防止ZIP炸弹攻击
                    if (!entryPath.normalize().startsWith(destPath.normalize())) {
                        throw new ConfigException("ZIP_SECURITY_ERROR", "不安全的ZIP条目: " + entry.getName());
                    }

                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        // 创建父目录
                        Path parentDir = entryPath.getParent();
                        if (parentDir != null && !Files.exists(parentDir)) {
                            Files.createDirectories(parentDir);
                        }

                        Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                        extractedFiles.add(entryPath.toString());
                    }

                    zis.closeEntry();
                }
            }

            log.debug("文件解压成功: {} -> {}, 解压文件数: {}", zipPath, destPath, extractedFiles.size());
            return extractedFiles;

        } catch (IOException e) {
            log.error("文件解压失败: {} -> {}", zipFilePath, destDirPath, e);
            throw new ConfigException("FILE_UNZIP_ERROR",
                    String.format("文件解压失败: %s -> %s", zipFilePath, destDirPath), e);
        }
    }
}