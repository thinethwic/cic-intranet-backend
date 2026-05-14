package com.intranet.cic.configs;


import com.google.api.services.storage.StorageScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class GcsConfig {

    @Value("${gcs.credentials-path}")
    private String credentialsPath;

    @Bean
    public Storage googleCloudStorage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ClassPathResource("gcs-credentials.json").getInputStream())
                .createScoped(StorageScopes.CLOUD_PLATFORM);

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
