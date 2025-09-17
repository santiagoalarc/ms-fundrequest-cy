package co.com.crediya.usecase.command.automaticcapacitycalculation;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.enums.FundStatusEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.UserCapacity;
import co.com.crediya.model.user.gateways.CalculateCapacityGateway;
import co.com.crediya.model.user.gateways.UserRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
class CalculateCapacityUseCaseTest {

    @Mock
    private FundApplicationRepository fundApplicationRepository;

    @Mock
    private CalculateCapacityGateway calculateCapacityGateway;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private UserRestService userRestService;

    @InjectMocks
    private CalculateCapacityUseCase calculateCapacityUseCase;

    private String email;
    private UUID fundId;
    private LoanType loanType;
    private User user;
    private FundApplication fundApplicationApproved;
    private FundApplication fundApplicationToEvaluate;
    private FundApplication fundApplicationRejected;

    @BeforeEach
    void setUp() {
        email = "test@example.com";
        fundId = UUID.randomUUID();
        loanType = LoanType.builder().id("1").interestRateTaa(15.0).build();
        user = User.builder().name("John").lastName("Doe").baseSalary("5000000").email(email).build();
        fundApplicationApproved = FundApplication.builder()
                .id(UUID.randomUUID())
                .idStatus(FundStatusEnum.APPROVED.getId())
                .idLoanType("1")
                .amount(new BigDecimal("1000000"))
                .term(12L)
                .build();
        fundApplicationToEvaluate = FundApplication.builder()
                .id(fundId)
                .idLoanType("1")
                .amount(new BigDecimal("2000000"))
                .term(24L)
                .build();
        fundApplicationRejected = FundApplication.builder()
                .id(UUID.randomUUID())
                .idStatus(FundStatusEnum.REJECTED.getId())
                .idLoanType("1")
                .amount(new BigDecimal("500000"))
                .term(6L)
                .build();

        FundAppCustomer fundAppCustomer = new FundAppCustomer();
        fundAppCustomer.setId(fundId);
        fundAppCustomer.setInterestRateTaa(loanType.getInterestRateTaa());
        fundAppCustomer.setTerm(fundApplicationToEvaluate.getTerm());
        fundAppCustomer.setAmount(fundApplicationToEvaluate.getAmount());

    }

    @Test
    void executeSuccess() {
        when(loanTypeRepository.findAll()).thenReturn(Flux.just(loanType));
        when(userRestService.findUsersByEmail(any(List.class))).thenReturn(Flux.just(user));
        when(fundApplicationRepository.findByEmail(email)).thenReturn(Flux.just(fundApplicationApproved, fundApplicationRejected));
        when(fundApplicationRepository.findById(fundId)).thenReturn(Mono.just(fundApplicationToEvaluate));
        when(calculateCapacityGateway.calculateCapacity(any(UserCapacity.class))).thenReturn(Mono.just("SUCCESS"));

        StepVerifier.create(calculateCapacityUseCase.execute(email, fundId))
                .expectNextMatches(userCapacity ->
                        userCapacity.getFullName().equals("John Doe") &&
                                userCapacity.getSalary().equals(new BigDecimal("5000000")) &&
                                userCapacity.getFundAppCustomers().size() == 1 &&
                                userCapacity.getFundToEvaluate().getId().equals(fundId)
                )
                .verifyComplete();

        Mockito.verify(loanTypeRepository).findAll();
        Mockito.verify(userRestService).findUsersByEmail(any(List.class));
        Mockito.verify(fundApplicationRepository).findByEmail(email);
        Mockito.verify(fundApplicationRepository).findById(fundId);
        Mockito.verify(calculateCapacityGateway).calculateCapacity(any(UserCapacity.class));
    }

    @Test
    void executeLoanTypeNotFound() {
        when(loanTypeRepository.findAll()).thenReturn(Flux.empty());
        when(userRestService.findUsersByEmail(any(List.class))).thenReturn(Flux.just(user));
        when(fundApplicationRepository.findByEmail(email)).thenReturn(Flux.just(fundApplicationApproved, fundApplicationRejected));

        StepVerifier.create(calculateCapacityUseCase.execute(email, fundId))
                .expectErrorMatches(throwable -> throwable instanceof FundException && ((FundException) throwable)
                        .getError().equals(FundErrorEnum.LOAN_TYPE_ID_NOT_FOUND))
                .verify();

        Mockito.verify(loanTypeRepository).findAll();
        Mockito.verify(userRestService).findUsersByEmail(any(List.class));
        Mockito.verify(fundApplicationRepository).findByEmail(email);
        //Mockito.verify(fundApplicationRepository).findById(fundId);
        Mockito.verifyNoInteractions(calculateCapacityGateway);
    }

    @Test
    void executeUserNotFound() {
        when(loanTypeRepository.findAll()).thenReturn(Flux.just(loanType));
        when(userRestService.findUsersByEmail(any(List.class))).thenReturn(Flux.empty());

        StepVerifier.create(calculateCapacityUseCase.execute(email, fundId))
                .expectErrorMatches(throwable -> throwable instanceof FundException && ((FundException) throwable)
                        .getError().equals(FundErrorEnum.USER_NOT_FOUND))
                .verify();

        Mockito.verify(loanTypeRepository).findAll();
        Mockito.verify(userRestService).findUsersByEmail(any(List.class));
        Mockito.verifyNoMoreInteractions(fundApplicationRepository);
        Mockito.verifyNoInteractions(calculateCapacityGateway);
    }
}