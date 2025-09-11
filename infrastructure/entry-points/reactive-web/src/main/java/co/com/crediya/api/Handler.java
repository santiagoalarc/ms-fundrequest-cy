package co.com.crediya.api;

import co.com.crediya.api.config.BaseValidator;
import co.com.crediya.api.dto.CapacityReqDTO;
import co.com.crediya.api.dto.CreateFundApplication;
import co.com.crediya.api.dto.FundAppFilterDTO;
import co.com.crediya.api.dto.UpdateFundDTO;
import co.com.crediya.api.mapper.CapacityMapper;
import co.com.crediya.api.mapper.FundDtoMapper;
import co.com.crediya.api.security.JwtProvider;
import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import co.com.crediya.usecase.command.automaticcapacitycalculation.CalculateCapacityUseCase;
import co.com.crediya.usecase.command.fundapplication.FundApplicationUseCase;
import co.com.crediya.usecase.command.fundapplicationupdatestatus.FundApplicationUpdateStatusUseCase;
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
    private final FundApplicationUpdateStatusUseCase fundApplicationResponseUseCase;
    private final CalculateCapacityUseCase calculateCapacityUseCase;
    private final JwtProvider jwtProvider;
    private final FundDtoMapper fundDtoMapper;
    private final CapacityMapper capacityMapper;

    private final static String EMPTY = "";

    @PreAuthorize("hasAuthority('USER')")
    public Mono<ServerResponse> listenSaveFundApplication(ServerRequest serverRequest) {

        String token = serverRequest.exchange().getAttribute("token");
        String email = jwtProvider.getSubject(token);

        return serverRequest.bodyToMono(CreateFundApplication.class)
                .doOnNext(createFund -> BaseValidator.validate(createFund, "PAYLOAD_NOT_CONTAIN_MINIMUM_FIELDS"))
                .map(fundDtoMapper::toModel)
                .flatMap(fundApp -> fundApplicationUseCase.saveFundApplication(fundApp, email))
                .flatMap(savedFundApplication -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedFundApplication));
    }

    @PreAuthorize("hasAuthority('ASESOR')")
    public Mono<ServerResponse> getFundApplicationList(ServerRequest serverRequest) {

        String email = serverRequest.queryParam("email").orElse(EMPTY);
        String status = serverRequest.queryParam("status").orElse(EMPTY);
        String loanType = serverRequest.queryParam("loanType").orElse(EMPTY);
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

    @PreAuthorize("hasAuthority('ASESOR')")
    public Mono<ServerResponse> responseFundReq(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(UpdateFundDTO.class)
                .map(fundDtoMapper::toModel)
                .doOnNext(fundApplication -> BaseValidator.validate(fundApplication, "PAYLOAD_NOT_CONTAIN_MINIMUM_FIELDS"))
                .flatMap(fundApplicationResponseUseCase::execute)
                .map(fundDtoMapper::toResponse)
                .flatMap(updateFund -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updateFund)
                );
    }

    @PreAuthorize("hasAuthority('ASESOR')")
    public Mono<ServerResponse> calculateCapacity(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(CapacityReqDTO.class)
                .doOnNext(capacityReqDTO -> BaseValidator.validate(capacityReqDTO, "PAYLOAD_NOT_CONTAIN_MINIMUM_FIELDS"))
                .map(capacityMapper::toRequest)
                .flatMap(userCapacity -> calculateCapacityUseCase.execute(userCapacity.getEmail(), userCapacity.getFundId()))
                .map(capacityMapper::toResponse)
                .flatMap(capacityOUTDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(capacityOUTDto)
                );
    }
}
