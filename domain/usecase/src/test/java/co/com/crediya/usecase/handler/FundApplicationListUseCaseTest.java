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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private FundApplicationListUseCase useCase;

    private FundApplicationFilter filter;
    private PageRequestModel pageRequest;
    private FundStatus fundStatus;
    private User user;

    @BeforeEach
    void setUp() {

        fundStatus = FundStatus.builder()
                .id(UUID.fromString("c070f7a0-0b61-4172-88f0-1049b734c56e"))
                .name("APPROVED")
                .build();
        LoanType loanType = LoanType.builder()
                .id(UUID.fromString("6a02b1f8-00d0-4824-a212-07a75932599d"))
                .name("PERSONAL")
                .interestRateTaa(BigDecimal.valueOf(1.5))
                .build();
        user = User.builder()
                .email("test@email.com")
                .name("John")
                .lastName("Doe")
                .baseSalary("50000")
                .build();

        FundApplication fundApp = new FundApplication();
        fundApp.setIdStatus(fundStatus.getId());
        fundApp.setIdLoanType(loanType.getId());
        fundApp.setEmail(user.getEmail());
        fundApp.setAmount(BigDecimal.valueOf(1000));

        FundAppCustomer fundAppCustomer = (FundAppCustomer) fundApp;

        filter = FundApplicationFilter.builder()
                .status(fundStatus.getName())
                .loanType(loanType.getName())
                .build();
        pageRequest = PageRequestModel.builder().page(0).size(10).build();
        PagedResult<FundAppCustomer> pagedResult = PagedResult.<FundAppCustomer>builder()
                .content(List.of(fundAppCustomer))
                .totalElements(1L)
                .build();

        when(fundStatusRepository.findByName("APPROVED")).thenReturn(Mono.just(fundStatus));
        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));
        when(fundApplicationRepository.findPagedByFilter(any(FundApplicationFilter.class), any(PageRequestModel.class)))
                .thenReturn(Mono.just(pagedResult));
        when(loanTypeRepository.findAll()).thenReturn(Flux.just(loanType));
        when(fundStatusRepository.findAll()).thenReturn(Flux.just(fundStatus));
        when(userRestService.findUsersByEmail(any(List.class))).thenReturn(Flux.just(user));
        when(fundApplicationRepository.findAllByEmailIn(any(List.class))).thenReturn(Flux.just(fundApp));
    }

    @Test
    void findFundApplicationList_shouldReturnEnrichedFundApplications() {

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectNextMatches(result -> {
                    FundAppCustomer enrichedFundApp = result.getContent().get(0);
                    return "APPROVED".equals(enrichedFundApp.getStatus()) &&
                            "PERSONAL".equals(enrichedFundApp.getLoanType()) &&
                            user.getName().concat(" ").concat(user.getLastName()).equals(enrichedFundApp.getName()) &&
                            BigDecimal.valueOf(1000).equals(enrichedFundApp.getTotalDebt());
                })
                .verifyComplete();
    }

    @Test
    void findFundApplicationList_shouldHandleEmptyFilterValues() {

        FundApplicationFilter emptyFilter = new FundApplicationFilter();
        when(fundStatusRepository.findByName(any(String.class))).thenReturn(Mono.empty());
        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.findFundApplicationList(emptyFilter, pageRequest))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findFundApplicationList_shouldThrowExceptionForInvalidStatus() {

        filter.setStatus("INVALID_STATUS");
        when(fundStatusRepository.findByName("INVALID_STATUS")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof FundException &&
                                ((FundException) throwable).getError().equals(FundErrorEnum.FUND_STATUS_INVALID))
                .verify();
    }

    @Test
    void findFundApplicationList_shouldThrowExceptionForInvalidLoanType() {

        filter.setLoanType("INVALID_LOAN_TYPE");
        when(loanTypeRepository.findByName("INVALID_LOAN_TYPE")).thenReturn(Mono.empty());
        when(fundStatusRepository.findByName(any(String.class))).thenReturn(Mono.just(fundStatus));

        StepVerifier.create(useCase.findFundApplicationList(filter, pageRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof FundException &&
                                ((FundException) throwable).getError().equals(FundErrorEnum.LOAN_TYPE_INVALID))
                .verify();
    }
}