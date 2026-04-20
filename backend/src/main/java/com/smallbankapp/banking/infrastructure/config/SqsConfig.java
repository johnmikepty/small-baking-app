package com.smallbankapp.banking.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * SQS client configuration.
 * When sqs.endpoint is set (LocalStack), uses static credentials and custom endpoint.
 * When sqs.endpoint is empty (AWS real), uses DefaultCredentialsProvider (IAM role / env vars).
 */
@Configuration
public class SqsConfig {

    @Value("${sqs.endpoint:}")
    private String sqsEndpoint;

    @Value("${sqs.region:us-east-2}")
    private String sqsRegion;

    @Value("${sqs.access-key:test}")
    private String accessKey;

    @Value("${sqs.secret-key:test}")
    private String secretKey;

    @Bean
    public SqsClient sqsClient() {
        var builder = SqsClient.builder()
                .region(Region.of(sqsRegion));

        if (sqsEndpoint != null && !sqsEndpoint.isBlank()) {
            // LocalStack or custom endpoint
            builder.endpointOverride(URI.create(sqsEndpoint))
                   .credentialsProvider(
                       StaticCredentialsProvider.create(
                           AwsBasicCredentials.create(accessKey, secretKey)
                       )
                   );
        } else {
            // AWS real — uses IAM role / environment variables
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
