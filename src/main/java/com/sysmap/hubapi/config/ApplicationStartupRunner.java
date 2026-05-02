package com.sysmap.hubapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

@Component
public class ApplicationStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupRunner.class);
    private static final String DEFAULT_AVATAR_KEY = "avatars/default-avatar.png";

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public ApplicationStartupRunner(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureBucketExists();
        uploadDefaultAvatar();
    }

    private void ensureBucketExists() {
        try {
            s3Client.createBucket(b -> b.bucket(bucket));
            log.info("Bucket S3 '{}' criado.", bucket);
        } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException e) {
            log.debug("Bucket '{}' já existe.", bucket);
        } catch (Exception e) {
            log.warn("Não foi possível criar o bucket '{}': {}", bucket, e.getMessage());
        }
    }

    private void uploadDefaultAvatar() {
        try (InputStream is = getClass().getResourceAsStream("/default-avatar.png")) {
            if (is == null) {
                log.warn("default-avatar.png não encontrado nos resources.");
                return;
            }
            byte[] bytes = is.readAllBytes();
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(DEFAULT_AVATAR_KEY)
                            .contentType("image/webp")
                            .build(),
                    RequestBody.fromBytes(bytes));
            log.info("Avatar padrão enviado para s3://{}/{}", bucket, DEFAULT_AVATAR_KEY);
        } catch (Exception e) {
            log.warn("Falha ao enviar avatar padrão para S3: {}", e.getMessage());
        }
    }
}
