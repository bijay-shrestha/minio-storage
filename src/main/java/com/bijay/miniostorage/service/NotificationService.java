package com.bijay.miniostorage.service;

import com.jlefebure.spring.boot.minio.notification.MinioNotification;
import io.minio.notification.NotificationInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    @MinioNotification(value={"s3:ObjectAccessed:Get"})
    public void handleGetPdf(NotificationInfo notificationInfo){
        log.info(Arrays
                .stream(notificationInfo.records)
                .map(notificationEvent -> "RECEIVING EVENT " +
                notificationEvent.eventName +
                " for " + notificationEvent.s3.object.key)
                .collect(Collectors.joining(","))
        );
    }

    @MinioNotification({"s3:ObjectCreated:Post"})
    public void handleUpload(NotificationInfo notificationInfo) {
        log.info(Arrays
                .stream(notificationInfo.records)
                .map(notificationEvent -> "Receiving event " +
                        notificationEvent.eventName + " for " +
                        notificationEvent.s3.object.key)
                .collect(Collectors.joining(","))
        );
    }
}
