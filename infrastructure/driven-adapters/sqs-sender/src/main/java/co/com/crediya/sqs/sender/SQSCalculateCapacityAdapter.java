package co.com.crediya.sqs.sender;

import co.com.crediya.model.user.UserCapacity;
import co.com.crediya.model.user.gateways.CalculateCapacityGateway;
import co.com.crediya.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSCalculateCapacityAdapter implements CalculateCapacityGateway {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper mapper;


    public Mono<String> send(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueCapacityUrl())
                .messageBody(message)
                .build();
    }

    @Override
    public Mono<String> calculateCapacity(UserCapacity userCapacity) {
        return Mono.fromCallable(() -> buildCapacityMessage(userCapacity))
                .doOnNext(message -> log.info("Sending notification for fund application to email Status change to " +
                        userCapacity.getEmail()))
                .flatMap(this::send);
    }

    private String buildCapacityMessage(UserCapacity userCapacity) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("email", userCapacity.getEmail());
            notification.put("full_name", userCapacity.getFullName());
            notification.put("salary", userCapacity.getSalary());
            notification.put("fundList", userCapacity.getFundAppCustomers());
            notification.put("fundToEvaluate", userCapacity.getFundToEvaluate());

            return mapper.writeValueAsString(notification);
        } catch (Exception e) {
            throw new RuntimeException("Error creating calculation capacity message", e);
        }
    }
}
