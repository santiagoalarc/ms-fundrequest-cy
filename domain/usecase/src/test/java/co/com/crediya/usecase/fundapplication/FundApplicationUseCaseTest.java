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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

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

    private FundApplicationUseCase fundApplicationUseCase;

    private FundApplication fundApplication;
    private LoanType loanType;
    private User user;
    private String email;

    @BeforeEach
    void setUp() {
        fundApplicationUseCase = new FundApplicationUseCase(
                fundApplicationRepository,
                loanTypeRepository,
                userRestService
        );

        email = "test@example.com";

        fundApplication = FundApplication.builder()
                .documentIdentification("12345678")
                .amount(new BigDecimal("5000"))
                .term(12L)
                .email(email)
                .loanType("PERSONAL")
                .build();

        loanType = LoanType.builder()
                .id(UUID.randomUUID())
                .name("PERSONAL")
                .minAmount(new BigDecimal("1000"))
                .maxAmount(new BigDecimal("10000"))
                .interestRateTaa(new BigDecimal("15.5"))
                .autoValidation(false)
                .build();

        user = User.builder()
                .id("1")
                .name("John")
                .lastName("Doe")
                .email(email)
                .documentIdentification("12345678")
                .baseSalary("3000000")
                .build();
    }

    @Test
    void saveFundApplication_Success() {
        FundApplication expectedSavedFundApp = fundApplication.toBuilder()
                .idLoanType(loanType.getId())
                .idStatus(FundStatusEnum.PENDING.getId())
                .build();

        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber("12345678")).thenReturn(Mono.just(user));
        when(fundApplicationRepository.save(any(FundApplication.class))).thenReturn(Mono.just(expectedSavedFundApp));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectNext(expectedSavedFundApp)
                .verifyComplete();
    }

    @Test
    void saveFundApplication_TokenUserMismatch() {
        String differentEmail = "different@example.com";

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, differentEmail))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError() == FundErrorEnum.TOKEN_USER_MISMATCH)
                .verify();
    }

    @Test
    void saveFundApplication_LoanTypeNotFound() {
        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.empty());

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError() == FundErrorEnum.LOAN_TYPE_ID_NOT_FOUND)
                .verify();
    }

    @Test
    void saveFundApplication_LoanAmountOutOfRange_TooHigh() {
        fundApplication = fundApplication.toBuilder()
                .amount(new BigDecimal("15000"))
                .build();

        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError() == FundErrorEnum.LOAN_AMOUNT_OUT_OF_RANGE)
                .verify();
    }

    @Test
    void saveFundApplication_LoanAmountOutOfRange_TooLow() {
        fundApplication = fundApplication.toBuilder()
                .amount(new BigDecimal("500"))
                .build();

        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError() == FundErrorEnum.LOAN_AMOUNT_OUT_OF_RANGE)
                .verify();
    }

    @Test
    void saveFundApplication_DocumentIdentificationNotFound() {
        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber("12345678")).thenReturn(Mono.empty());

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError() == FundErrorEnum.DOCUMENT_IDENTIFICATION_NOT_FOUND)
                .verify();
    }

    @Test
    void saveFundApplication_NullFundApplication() {
        StepVerifier.create(fundApplicationUseCase.saveFundApplication(null, email))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError() == FundErrorEnum.TOKEN_USER_MISMATCH)
                .verify();
    }

    @Test
    void saveFundApplication_RepositoryError() {
        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber("12345678")).thenReturn(Mono.just(user));
        when(fundApplicationRepository.save(any(FundApplication.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void saveFundApplication_LoanTypeRepositoryError() {
        when(loanTypeRepository.findByName(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Loan type repository error")));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void saveFundApplication_UserServiceError() {
        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber(anyString()))
                .thenReturn(Mono.error(new RuntimeException("User service error")));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void saveFundApplication_ExactMinAmount() {
        fundApplication = fundApplication.toBuilder()
                .amount(new BigDecimal("1000"))
                .build();

        FundApplication expectedSavedFundApp = fundApplication.toBuilder()
                .idLoanType(loanType.getId())
                .idStatus(FundStatusEnum.PENDING.getId())
                .build();

        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber("12345678")).thenReturn(Mono.just(user));
        when(fundApplicationRepository.save(any(FundApplication.class))).thenReturn(Mono.just(expectedSavedFundApp));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectNext(expectedSavedFundApp)
                .verifyComplete();
    }

    @Test
    void saveFundApplication_ExactMaxAmount() {
        fundApplication = fundApplication.toBuilder()
                .amount(new BigDecimal("10000"))
                .build();

        FundApplication expectedSavedFundApp = fundApplication.toBuilder()
                .idLoanType(loanType.getId())
                .idStatus(FundStatusEnum.PENDING.getId())
                .build();

        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber("12345678")).thenReturn(Mono.just(user));
        when(fundApplicationRepository.save(any(FundApplication.class))).thenReturn(Mono.just(expectedSavedFundApp));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, email))
                .expectNext(expectedSavedFundApp)
                .verifyComplete();
    }
}