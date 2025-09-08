package co.com.crediya.usecase.handler;

import co.com.crediya.model.common.PageRequestModel;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundAppCustomer;
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
import java.util.Collections;
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
    private User user;
    private LoanType loanType;
    private FundStatus fundStatus;

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

        user = createUser();
        loanType = createLoanType();
        fundStatus = createFundStatus("PENDING");

        fundAppCustomer = new FundAppCustomer();
        fundAppCustomer.setEmail(user.getEmail());
        fundAppCustomer.setAmount(BigDecimal.valueOf(10000));
        fundAppCustomer.setIdStatus(fundStatus.getId());
        fundAppCustomer.setIdLoanType(loanType.getId());
        fundAppCustomer.setTerm(12L);

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
        when(loanTypeRepository.findAll()).thenReturn(Flux.just(loanType));
        when(fundStatusRepository.findAll()).thenReturn(Flux.just(fundStatus));
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.just(user));

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> result.getTotalElements() == 1)
                .verifyComplete();
    }

    @Test
    void findFundApplicationList_WithStatusFilter_ShouldValidateAndApplyFilter() {
        filter.setStatus("PENDING");
        fundAppCustomer.setIdStatus(fundStatus.getId());

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.just(loanType));
        when(fundStatusRepository.findAll()).thenReturn(Flux.just(fundStatus));
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.just(user));

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> result.getTotalElements() == 1)
                .verifyComplete();
    }

    @Test
    void findFundApplicationList_WithLoanTypeFilter_ShouldValidateAndApplyFilter() {
        filter.setLoanType("PERSONAL");
        fundAppCustomer.setIdLoanType(loanType.getId());

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.just(loanType));
        when(fundStatusRepository.findAll()).thenReturn(Flux.just(fundStatus));
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.just(user));

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> result.getTotalElements() == 1)
                .verifyComplete();
    }

    @Test
    void mapFundWithUserInfo_ShouldEnrichWithUserData() {
        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.just(loanType));
        when(fundStatusRepository.findAll()).thenReturn(Flux.just(fundStatus));
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.just(user));

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> {
                    FundAppCustomer app = result.getContent().getFirst();
                    return "John Doe".equals(app.getName()) && "5000".equals(app.getBaseSalary()) && "test@example.com".equals(app.getEmail());
                })
                .verifyComplete();
    }

    @Test
    void mapFundWithFundStatus_ShouldEnrichWithStatusData() {
        fundAppCustomer.setIdStatus(fundStatus.getId());

        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.just(loanType));
        when(fundStatusRepository.findAll()).thenReturn(Flux.just(fundStatus));
        when(userRestService.findUsersByEmail(anyList())).thenReturn(Flux.just(user));

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> {
                    FundAppCustomer app = result.getContent().getFirst();
                    return "PENDING".equals(app.getStatus());
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
                .interestRateTaa(15.5)
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
}