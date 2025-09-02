package co.com.crediya.usecase.fundapplication;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.enums.FundStatusEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRestService;
import co.com.crediya.usecase.command.fundapplication.FundApplicationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundApplicationUseCaseTest {

    @Mock
    private FundApplicationRepository fundApplicationRepository;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private UserRestService userRestService;

    @InjectMocks
    private FundApplicationUseCase fundApplicationUseCase;

    private FundApplication fundApplication;
    private LoanType loanType;
    private User user;
    private final String userEmail = "test@crediya.com";

    @BeforeEach
    void setUp() {

        fundApplication = FundApplication.builder()
                .documentIdentification("123456789")
                .email(userEmail)
                .amount(BigDecimal.valueOf(1000000))
                .idLoanType("credito_de_libre_inversion")
                .build();

        loanType = LoanType.builder()
                .name("credito_de_libre_inversion")
                .minAmount(BigDecimal.valueOf(500000))
                .maxAmount(BigDecimal.valueOf(2000000))
                .build();

        user = User.builder()
                .email(userEmail)
                .documentIdentification("123456789")
                .build();
    }

    @Test
    void saveFundApplication_Success_ReturnsFundApplicationWithPendingStatus() {

        when(loanTypeRepository.findByName(anyString())).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber(anyString())).thenReturn(Mono.just(user));
        when(fundApplicationRepository.save(any(FundApplication.class))).thenReturn(Mono.just(fundApplication.toBuilder().statusId(FundStatusEnum.PENDING.getId()).build()));


        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, userEmail))
                .expectNextMatches(savedApplication ->
                        savedApplication.getStatusId().equals(FundStatusEnum.PENDING.getId()) &&
                                savedApplication.getEmail().equals(userEmail) &&
                                savedApplication.getAmount().equals(fundApplication.getAmount()))
                .verifyComplete();
    }

    @Test
    void saveFundApplication_MismatchUserEmail_ThrowsFundException() {

        String wrongEmail = "wrong@crediya.com";

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, wrongEmail))
                .expectErrorMatches(throwable ->
                        throwable instanceof FundException &&
                                ((FundException) throwable).getError().equals(FundErrorEnum.TOKEN_USER_MISMATCH))
                .verify();
    }

    @Test
    void saveFundApplication_LoanTypeNotFound_ThrowsFundException() {

        when(loanTypeRepository.findByName(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, userEmail))
                .expectErrorMatches(throwable ->
                        throwable instanceof FundException &&
                                ((FundException) throwable).getError().equals(FundErrorEnum.LOAN_TYPE_ID_NOT_FOUND))
                .verify();
    }

    @Test
    void saveFundApplication_LoanAmountOutOfRange_ThrowsFundException() {

        FundApplication outOfRangeApplication = fundApplication.toBuilder().amount(BigDecimal.valueOf(3000000)).build();
        when(loanTypeRepository.findByName(anyString())).thenReturn(Mono.just(loanType));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(outOfRangeApplication, userEmail))
                .expectErrorMatches(throwable ->
                        throwable instanceof FundException &&
                                ((FundException) throwable).getError().equals(FundErrorEnum.LOAN_AMOUNT_OUT_OF_RANGE))
                .verify();
    }

    @Test
    void saveFundApplication_UserNotFoundByDocument_ThrowsFundException() {

        when(loanTypeRepository.findByName(anyString())).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber( anyString())).thenReturn(Mono.empty());

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, userEmail))
                .expectErrorMatches(throwable ->
                        throwable instanceof FundException &&
                                ((FundException) throwable).getError().equals(FundErrorEnum.DOCUMENT_IDENTIFICATION_NOT_FOUND))
                .verify();
    }
}
