package com.bijay.miniostorage.resource;

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

    private MinioService minioService;

    public UploadResource(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping
    public List<Item> testMinio() throws MinioException {
        return minioService.list();
    }

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

    @PostMapping
    public void addAttachement(@RequestParam("file") MultipartFile file) {
        Path path = Paths.get(Objects.requireNonNull("bijay/"+file.getOriginalFilename()));
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
}
