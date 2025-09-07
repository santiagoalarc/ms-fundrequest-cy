package co.com.crediya.api;

import co.com.crediya.api.config.BaseValidator;
import co.com.crediya.api.dto.CreateFundApplication;
import co.com.crediya.api.dto.FundAppFilterDTO;
import co.com.crediya.api.mapper.FundDtoMapper;
import co.com.crediya.api.security.JwtProvider;
import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import co.com.crediya.usecase.command.fundapplication.FundApplicationUseCase;
import co.com.crediya.usecase.handler.FundApplicationListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final FundApplicationUseCase fundApplicationUseCase;
    private final FundApplicationListUseCase fundApplicationListUseCase;
    private final JwtProvider jwtProvider;
    private final FundDtoMapper fundDtoMapper;

    @PreAuthorize("hasAuthority('USER')")
    public Mono<ServerResponse> listenSaveFundApplication(ServerRequest serverRequest) {

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

    @PreAuthorize("hasAuthority('ASESOR')")
    public Mono<ServerResponse> getFundApplicationList(ServerRequest serverRequest) {

        String email = serverRequest.queryParam("email").orElse("");
        String status = serverRequest.queryParam("status").orElse("");
        String loanType = serverRequest.queryParam("loanType").orElse("");
        String pageSizeString = serverRequest.queryParam("size").orElse("10");
        String pageNumberString = serverRequest.queryParam("page").orElse("1");
        int size, page;

        try {
            size = Integer.parseInt(pageSizeString);
            page = Integer.parseInt(pageNumberString);
        } catch (NumberFormatException e) {
            size = 10;
            page = 1;
        }

        FundApplicationFilter filter = FundApplicationFilter.builder()
                .email(email)
                .loanType(loanType)
                .status(status)
                .build();

        PageRequestModel pageRequestModel = PageRequestModel.builder()
                .size(size)
                .page(page)
                .build();

        return fundApplicationListUseCase.findFundApplicationList(filter, pageRequestModel)
                .flatMap(pagedResponse -> Flux.fromIterable(pagedResponse.getContent())
                        .map(fundDtoMapper::toResponse)
                        .collectList()
                        .map(contentList -> PagedResult.<FundAppFilterDTO>builder()
                                .content(contentList)
                                .totalPages(pagedResponse.getTotalPages())
                                .page(pagedResponse.getPage())
                                .size(pagedResponse.getSize())
                                .build())
                )
                .flatMap(fundApplicationsPage -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fundApplicationsPage));

    }
}
