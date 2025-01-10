package spc.cloud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;


import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import spc.cloud.dto.LogEventDto;

@Service
public class LogService {
    private final CloudWatchLogsClient logsClient;
    private final String logGroupName;
    private final String logStreamName;
    private String nextSequenceToken;

    public LogService(
            @Value("${aws.cloudwatch.logs.region}") String region,
            @Value("${aws.cloudwatch.logs.endpoint}") String endpoint,
            @Value("${aws.cloudwatch.logs.accessKey}") String accessKey,
            @Value("${aws.cloudwatch.logs.secretKey}") String secretKey,
            @Value("${aws.cloudwatch.logs.groupName}") String logGroupName,
            @Value("${aws.cloudwatch.logs.streamName}") String logStreamName) {

        this.logGroupName = logGroupName;
        this.logStreamName = logStreamName;

        CloudWatchLogsClientBuilder builder = CloudWatchLogsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        this.logsClient = builder.build();
        createLogGroupIfNotExists();
        createLogStreamIfNotExists();
    }

    public void putLogEvent(String message) {
        InputLogEvent logEvent = InputLogEvent.builder()
                .timestamp(Instant.now().toEpochMilli())
                .message(message)
                .build();

        PutLogEventsRequest.Builder requestBuilder = PutLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .logEvents(logEvent);

        if (nextSequenceToken != null) {
            requestBuilder.sequenceToken(nextSequenceToken);
        }

        PutLogEventsResponse response = logsClient.putLogEvents(requestBuilder.build());
        nextSequenceToken = response.nextSequenceToken();
    }

    private void createLogGroupIfNotExists() {
        try {
            logsClient.createLogGroup(CreateLogGroupRequest.builder()
                    .logGroupName(logGroupName).build());
        } catch (ResourceAlreadyExistsException ignored) {
            // Group exists
        }
    }

    private void createLogStreamIfNotExists() {
        try {
            logsClient.createLogStream(CreateLogStreamRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .build());
        } catch (ResourceAlreadyExistsException ignored) {
            // Stream exists
        }
    }

    public List<LogEventDto> getLogEvents() {
        GetLogEventsResponse response = logsClient.getLogEvents(
                GetLogEventsRequest.builder()
                        .logGroupName(logGroupName)
                        .logStreamName(logStreamName)
                        .build()
        );

        return response.events().stream()
                .map(e -> new LogEventDto(e.message(), e.timestamp()))
                .collect(Collectors.toList());
    }

}
