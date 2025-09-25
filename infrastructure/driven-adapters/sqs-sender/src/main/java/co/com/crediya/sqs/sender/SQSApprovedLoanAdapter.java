package co.com.crediya.sqs.sender;

import co.com.crediya.model.events.gateway.EventGateway;
import co.com.crediya.model.fundapplication.FundApplication;
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
public class SQSApprovedLoanAdapter implements EventGateway {

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
                .queueUrl(properties.queueApprovedAmountLoanUrl())
                .messageBody(message)
                .build();
    }
    @Override
    public Mono<Void> sendMessage(FundApplication fundApplication) {

        return Mono.fromCallable(() -> buildApprovedAmountMessage(fundApplication))
                .doOnNext(message -> log.info("Sending notification for approved amount loan " +
                        fundApplication.getAmount()))
                .flatMap(this::send)
                .then();
    }

    private String buildApprovedAmountMessage(FundApplication fundApplication) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("approvedAmount", fundApplication.getAmount());

            return mapper.writeValueAsString(notification);
        } catch (Exception e) {
            throw new RuntimeException("Error creating approved amount message", e);
        }
    }
}
