package com.bijay.miniostorage.resource;

import com.bijay.miniostorage.service.NotificationService;
import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/files")
@Slf4j
public class UploadResource {

    private final MinioService minioService;

    public UploadResource(MinioService minioService) {
        this.minioService = minioService;
    }


    @PostMapping
    public void addAttachment(@RequestParam("file") MultipartFile file) {
        Path path = Paths.get(Objects.requireNonNull(file.getOriginalFilename()));
        log.info("The designated path is :: {}", path);
        try {
            minioService.upload(path, file.getInputStream(), file.getContentType());
        } catch (MinioException e) {
            throw new IllegalStateException("The file cannot be upload on the internal storage." +
                    " Please retry later", e);
        } catch (IOException e) {
            throw new IllegalStateException("The file cannot be read", e);
        }
    }

    @PostMapping("/sub-dir/{subDirectory}")
    public void addAttachmentIntoSubDirectory
            (@PathVariable("subDirectory") String subDirectory,
             @RequestParam("file") MultipartFile file) {
        Path path = Paths.get(subDirectory + "/" + file.getOriginalFilename());
        log.info("The designated path is :: {}", path);
        try {
            minioService.upload(path, file.getInputStream(), file.getContentType());
        } catch (MinioException e) {
            throw new IllegalStateException("The file cannot be upload on the internal storage." +
                    " Please retry later", e);
        } catch (IOException e) {
            throw new IllegalStateException("The file cannot be read", e);
        }
    }

    @GetMapping
    public List<Item> getAllList() throws MinioException {
        return minioService.list();
    }

    //FOR NO SUB-DIRECTORY CASE
    @GetMapping("/{object}")
    public void getObject(@PathVariable("object") String object,
                          HttpServletResponse response)
            throws IOException, MinioException {
        InputStream inputStream = minioService.get(Paths.get(object));
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        // SET THE CONTENT TYPE AND ATTACHMENT HEADER.
        response.addHeader("Content-disposition", "attachment;filename=" + object);
        response.setContentType(URLConnection.guessContentTypeFromName(object));

        // COPY THE STREAM TO THE RESPONSE'S OUTPUT STREAM.
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }

    //FOR SUB-DIRECTORY CASE
    @GetMapping("/{subDirectory}/{object}")
    public void getObjectWithSubDirectory(@PathVariable("subDirectory") String subDirectory,
                                          @PathVariable("object") String object,
                                          HttpServletResponse response)
            throws IOException, MinioException {
        InputStream inputStream = minioService.get((Paths.get(subDirectory +"/" + object)));
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        // SET THE CONTENT TYPE AND ATTACHMENT HEADER.
        response.addHeader("Content-disposition", "attachment;filename=" + object);
        response.setContentType(URLConnection.guessContentTypeFromName(object));

        // COPY THE STREAM TO THE RESPONSE'S OUTPUT STREAM.
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping("/{object}")
    public void deleteFiles(@PathVariable("object") String object) throws MinioException {
        minioService.remove(Paths.get(object));
    }


}
