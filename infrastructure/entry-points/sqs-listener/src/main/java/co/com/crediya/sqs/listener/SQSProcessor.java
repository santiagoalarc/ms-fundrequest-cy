package co.com.crediya.sqs.listener;

import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.sqs.listener.dto.MessageBodyDto;
import co.com.crediya.usecase.command.fundapplicationupdatestatus.FundApplicationStatusUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final FundApplicationStatusUseCase fundApplicationStatusUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        System.out.println(message.body());

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            MessageBodyDto messageBody = objectMapper.readValue(message.body(), MessageBodyDto.class);
            String status = messageBody.getStatus();
            String id = messageBody.getId();

            System.out.println("Status: " + status);
            System.out.println("ID: " + id);

            return fundApplicationStatusUseCase.execute(FundApplication.builder()
                    .status(status)
                    .id(UUID.fromString(id))
                    .build())
                    .then();

        } catch (IOException e) {
            System.err.println("Error al parsear el mensaje: " + e.getMessage());
            return Mono.error(e);
        }
    }
}
