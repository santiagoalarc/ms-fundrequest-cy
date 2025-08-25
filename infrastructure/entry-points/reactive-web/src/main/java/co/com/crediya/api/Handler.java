package co.com.crediya.api;

import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.usecase.fundapplication.FundApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final FundApplicationUseCase fundApplicationUseCase;

    public Mono<ServerResponse> listenSaveFundApplication(ServerRequest serverRequest){
        return serverRequest.bodyToMono(FundApplication.class)
                .flatMap(fundApplicationUseCase::saveFundApplication)
                .flatMap(savedFundApplication -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedFundApplication));
    }
}
