package co.com.crediya.usecase.handler;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.FundApplicationFilter;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.model.fundstatus.FundStatus;
import co.com.crediya.model.fundstatus.gateways.FundStatusRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundApplicationListUseCaseTest {

    @Mock
    private FundApplicationRepository fundApplicationRepository;

    @Mock
    private FundStatusRepository fundStatusRepository;

    @Mock
    private UserRestService userRestService;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    private FundApplicationListUseCase useCase;

    private FundApplicationFilter filter;
    private PageRequestModel pageRequest;
    private FundAppCustomer fundAppCustomer;
    private PagedResult<FundAppCustomer> pagedResult;

    @BeforeEach
    void setUp() {
        useCase = new FundApplicationListUseCase(
                fundApplicationRepository,
                fundStatusRepository,
                userRestService,
                loanTypeRepository
        );

        filter = FundApplicationFilter.builder()
                .email("test@example.com")
                .status("")
                .loanType("")
                .build();

        pageRequest = PageRequestModel.builder()
                .page(0)
                .size(10)
                .build();

        fundAppCustomer = new FundAppCustomer();
        fundAppCustomer.setEmail("test@example.com");
        fundAppCustomer.setAmount(BigDecimal.valueOf(10000));
        fundAppCustomer.setIdStatus(UUID.randomUUID().toString());
        fundAppCustomer.setIdLoanType(UUID.randomUUID().toString());

        pagedResult = PagedResult.<FundAppCustomer>builder()
                .content(Collections.singletonList(fundAppCustomer))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();
    }

    @Test
    void findFundApplicationList_WithEmptyFilters_ShouldReturnPagedResult() {
        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.empty());
        when(fundStatusRepository.findAll()).thenReturn(Flux.empty());
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.empty());
        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.just(createFundStatus("APPROVED")));
        when(fundApplicationRepository.findAllByEmailIn(anyList())).thenReturn(Flux.empty());

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> result.getTotalElements() == 1)
                .verifyComplete();
    }

    @Test
    void findFundApplicationList_WithStatusFilter_ShouldValidateAndApplyFilter() {
        filter.setStatus("PENDING");

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.empty());
        when(fundStatusRepository.findAll()).thenReturn(Flux.empty());
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.empty());
        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.just(createFundStatus("APPROVED")));
        when(fundApplicationRepository.findAllByEmailIn(anyList())).thenReturn(Flux.empty());

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> result.getTotalElements() == 1)
                .verifyComplete();
    }

    @Test
    void findFundApplicationList_WithLoanTypeFilter_ShouldValidateAndApplyFilter() {
        filter.setLoanType("PERSONAL");

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.empty());
        when(fundStatusRepository.findAll()).thenReturn(Flux.empty());
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.empty());
        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.just(createFundStatus("APPROVED")));
        when(fundApplicationRepository.findAllByEmailIn(anyList())).thenReturn(Flux.empty());

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> result.getTotalElements() == 1)
                .verifyComplete();
    }

    @Test
    void mapFundWithLoanType_ShouldEnrichWithLoanTypeData() {
        LoanType loanType = createLoanType();
        fundAppCustomer.setIdLoanType(loanType.getId());

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.just(loanType));
        when(fundStatusRepository.findAll()).thenReturn(Flux.empty());
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.empty());
        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.just(createFundStatus("APPROVED")));
        when(fundApplicationRepository.findAllByEmailIn(anyList())).thenReturn(Flux.empty());

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> {
                    FundAppCustomer app = result.getContent().getFirst();
                    return "PERSONAL".equals(app.getLoanType()) &&
                            BigDecimal.valueOf(15.5).equals(app.getInterestRateTaa());
                })
                .verifyComplete();
    }

    @Test
    void mapFundWithUserInfo_ShouldEnrichWithUserData() {
        User user = createUser();

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.empty());
        when(fundStatusRepository.findAll()).thenReturn(Flux.empty());
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.just(user));
        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.just(createFundStatus("APPROVED")));
        when(fundApplicationRepository.findAllByEmailIn(anyList())).thenReturn(Flux.empty());

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> {
                    FundAppCustomer app = result.getContent().getFirst();
                    return "John Doe".equals(app.getName()) && "5000".equals(app.getBaseSalary());
                })
                .verifyComplete();
    }

    @Test
    void mapFundWithFundStatus_ShouldEnrichWithStatusData() {
        FundStatus fundStatus = createFundStatus("APPROVED");
        fundAppCustomer.setIdStatus(fundStatus.getId());

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.empty());
        when(fundStatusRepository.findAll()).thenReturn(Flux.just(fundStatus));
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.empty());
        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.just(fundStatus));
        when(fundApplicationRepository.findAllByEmailIn(anyList())).thenReturn(Flux.empty());

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> {
                    FundAppCustomer app = result.getContent().getFirst();
                    return "APPROVED".equals(app.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void mapFundWithTotalAmountApproved_ShouldCalculateTotalDebt() {
        FundStatus approvedStatus = createFundStatus("APPROVED");
        FundApplication approvedApp = createFundApplication(BigDecimal.valueOf(5000), approvedStatus.getId());

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.empty());
        when(fundStatusRepository.findAll()).thenReturn(Flux.empty());
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.empty());
        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.just(approvedStatus));
        when(fundApplicationRepository.findAllByEmailIn(anyList())).thenReturn(Flux.just(approvedApp));

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> {
                    FundAppCustomer app = result.getContent().getFirst();
                    return BigDecimal.valueOf(5000).equals(app.getTotalDebt());
                })
                .verifyComplete();
    }

    @Test
    void mapFundWithTotalAmountApproved_WhenApprovedStatusNotFound_ShouldThrowException() {
        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.empty());
        when(fundStatusRepository.findAll()).thenReturn(Flux.empty());
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.empty());
        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError() == FundErrorEnum.STATUS_FUND_NAME_NOT_FOUND)
                .verify();
    }

    @Test
    void findFundApplicationList_WithEmptyContent_ShouldReturnEmptyResult() {
        PagedResult<FundAppCustomer> emptyResult = PagedResult.<FundAppCustomer>builder()
                .content(List.of())
                .page(0)
                .size(10)
                .totalElements(0)
                .totalPages(0)
                .build();

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(emptyResult));

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> result.getContent().isEmpty())
                .verifyComplete();
    }

    @Test
    void findFundApplicationList_WithMultipleApprovedApplications_ShouldSumTotalDebt() {
        FundStatus approvedStatus = createFundStatus("APPROVED");
        List<FundApplication> approvedApps = Arrays.asList(
                createFundApplication(BigDecimal.valueOf(3000), approvedStatus.getId()),
                createFundApplication(BigDecimal.valueOf(2000), approvedStatus.getId())
        );

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.empty());
        when(fundStatusRepository.findAll()).thenReturn(Flux.empty());
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.empty());
        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.just(approvedStatus));
        when(fundApplicationRepository.findAllByEmailIn(anyList())).thenReturn(Flux.fromIterable(approvedApps));

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> {
                    FundAppCustomer app = result.getContent().getFirst();
                    return BigDecimal.valueOf(5000).equals(app.getTotalDebt());
                })
                .verifyComplete();
    }

    private FundStatus createFundStatus(String name) {
        return FundStatus.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .description(name + " status")
                .creationDate("2024-01-01")
                .build();
    }

    private LoanType createLoanType() {
        return LoanType.builder()
                .id(UUID.randomUUID().toString())
                .name("PERSONAL")
                .interestRateTaa(BigDecimal.valueOf(15.5))
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .email("test@example.com")
                .name("John")
                .lastName("Doe")
                .baseSalary("5000")
                .documentIdentification("12345678")
                .build();
    }

    private FundApplication createFundApplication(BigDecimal amount, String statusId) {
        return FundApplication.builder()
                .email("test@example.com")
                .amount(amount)
                .idStatus(statusId)
                .idLoanType(UUID.randomUUID().toString())
                .term(12L)
                .documentIdentification("12345678")
                .build();
    }
}