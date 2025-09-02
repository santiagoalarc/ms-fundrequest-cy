package co.com.crediya.api;

import co.com.crediya.api.config.BaseValidator;
import co.com.crediya.api.dto.CreateFundApplication;
import co.com.crediya.api.mapper.FundDtoMapper;
import co.com.crediya.api.security.JwtProvider;
import co.com.crediya.usecase.command.fundapplication.FundApplicationUseCase;
import co.com.crediya.usecase.handler.FundApplicationListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Handler {

    private final FundApplicationUseCase fundApplicationUseCase;
    private final FundApplicationListUseCase fundApplicationListUseCase;
    private final JwtProvider jwtProvider;
    private final FundDtoMapper fundDtoMapper;

    @PreAuthorize("hasAuthority('USER')")
    public Mono<ServerResponse> listenSaveFundApplication(ServerRequest serverRequest){

        String token = serverRequest.exchange().getAttribute("token");
        String email = jwtProvider.getSubject(token);

        return serverRequest.bodyToMono(CreateFundApplication.class)
                .doOnNext(user -> BaseValidator.validate(user, "PAYLOAD_NOT_CONTAIN_MINIMUM_FIELDS"))
                .map(fundDtoMapper::toModel)
                .flatMap(fundApp -> fundApplicationUseCase.saveFundApplication(fundApp, email))
                .flatMap(savedFundApplication -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedFundApplication));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<ServerResponse> getFundApplicationList(ServerRequest serverRequest){

        String email = serverRequest.queryParam("email").orElse("");
        String status = serverRequest.queryParam("status").orElse("");
        String loanType = serverRequest.queryParam("loanType").orElse("");
        Optional<String> pageSizeString = serverRequest.queryParam("size");
        Optional<String> pageNumberString = serverRequest.queryParam("page");
        int size = 10;
        int page = 0;

        try {
            if (pageSizeString.isPresent()) {
                size = Integer.parseInt(pageSizeString.get());
            }
            if (pageNumberString.isPresent()) {
                page = Integer.parseInt(pageNumberString.get());
            }
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest().bodyValue("Size and page must be valid integers.");
        }

        return fundApplicationListUseCase.findFundApplicationList(email, status, loanType, size, page)
                .flatMap(fundApplicationsPage -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fundApplicationsPage));

    }
}
