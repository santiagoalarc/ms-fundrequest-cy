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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void setUp() {
        fundApplication = FundApplication.builder()
                .id(UUID.randomUUID().toString())
                .amount(new BigDecimal("10000"))
                .term(12L)
                .email("test@example.com")
                .idLoanType(UUID.randomUUID().toString())
                .documentIdentification("12345678")
                .build();

        loanType = LoanType.builder()
                .id(fundApplication.getIdLoanType())
                .name("Personal Loan")
                .maxAmount(new BigDecimal("50000"))
                .minAmount(new BigDecimal("1000"))
                .interestRateTaa(new BigDecimal("12.5"))
                .autoValidation(true)
                .build();

        user = User.builder()
                .id(UUID.randomUUID().toString())
                .documentIdentification("12345678")
                .email("test@example.com")
                .build();
    }

    @Test
    void saveFundApplication_Success() {

        FundApplication expectedSavedApplication = fundApplication.toBuilder()
                .statusId(FundStatusEnum.PENDING.getId())
                .build();

        when(loanTypeRepository.findById(fundApplication.getIdLoanType()))
                .thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber(fundApplication.getDocumentIdentification(), ""))
                .thenReturn(Mono.just(user));
        when(fundApplicationRepository.save(any(FundApplication.class)))
                .thenReturn(Mono.just(expectedSavedApplication));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "", fundApplication.getEmail()))
                .expectNext(expectedSavedApplication)
                .verifyComplete();
    }

    @Test
    void saveFundApplication_WhenLoanTypeNotFound_ShouldThrowException() {

        when(loanTypeRepository.findById(fundApplication.getIdLoanType()))
                .thenReturn(Mono.empty());

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "", fundApplication.getEmail()))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError().equals(FundErrorEnum.LOAN_TYPE_ID_NOT_FOUND))
                .verify();
    }

    @Test
    void saveFundApplication_WhenAmountBelowMinimum_ShouldThrowException() {

        FundApplication lowAmountApplication = fundApplication.toBuilder()
                .amount(new BigDecimal("500"))
                .build();

        when(loanTypeRepository.findById(lowAmountApplication.getIdLoanType()))
                .thenReturn(Mono.just(loanType));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(lowAmountApplication, "", lowAmountApplication.getEmail()))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError().equals(FundErrorEnum.LOAN_AMOUNT_OUT_OF_RANGE))
                .verify();
    }

    @Test
    void saveFundApplication_WhenAmountAboveMaximum_ShouldThrowException() {

        FundApplication highAmountApplication = fundApplication.toBuilder()
                .amount(new BigDecimal("100000"))
                .build();

        when(loanTypeRepository.findById(highAmountApplication.getIdLoanType()))
                .thenReturn(Mono.just(loanType));


        StepVerifier.create(fundApplicationUseCase.saveFundApplication(highAmountApplication, "", highAmountApplication.getEmail()))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError().equals(FundErrorEnum.LOAN_AMOUNT_OUT_OF_RANGE))
                .verify();
    }

    @Test
    void saveFundApplication_WhenUserNotFound_ShouldThrowException() {

        when(loanTypeRepository.findById(fundApplication.getIdLoanType()))
                .thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber(fundApplication.getDocumentIdentification(), ""))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "", fundApplication.getEmail()))
                .expectErrorMatches(throwable -> throwable instanceof FundException &&
                        ((FundException) throwable).getError().equals(FundErrorEnum.DOCUMENT_IDENTIFICATION_NOT_FOUND))
                .verify();
    }

    @Test
    void saveFundApplication_WhenAmountEqualsMinimum_ShouldSucceed() {

        FundApplication minAmountApplication = fundApplication.toBuilder()
                .amount(loanType.getMinAmount())
                .build();

        FundApplication expectedSavedApplication = minAmountApplication.toBuilder()
                .statusId(FundStatusEnum.PENDING.getId())
                .build();

        when(loanTypeRepository.findById(minAmountApplication.getIdLoanType()))
                .thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber(minAmountApplication.getDocumentIdentification(), ""))
                .thenReturn(Mono.just(user));
        when(fundApplicationRepository.save(any(FundApplication.class)))
                .thenReturn(Mono.just(expectedSavedApplication));


        StepVerifier.create(fundApplicationUseCase.saveFundApplication(minAmountApplication, "", minAmountApplication.getEmail()))
                .expectNext(expectedSavedApplication)
                .verifyComplete();
    }

    @Test
    void saveFundApplication_WhenAmountEqualsMaximum_ShouldSucceed() {

        FundApplication maxAmountApplication = fundApplication.toBuilder()
                .amount(loanType.getMaxAmount())
                .build();

        FundApplication expectedSavedApplication = maxAmountApplication.toBuilder()
                .statusId(FundStatusEnum.PENDING.getId())
                .build();

        when(loanTypeRepository.findById(maxAmountApplication.getIdLoanType()))
                .thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber(maxAmountApplication.getDocumentIdentification(), ""))
                .thenReturn(Mono.just(user));
        when(fundApplicationRepository.save(any(FundApplication.class)))
                .thenReturn(Mono.just(expectedSavedApplication));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(maxAmountApplication, "", maxAmountApplication.getEmail()))
                .expectNext(expectedSavedApplication)
                .verifyComplete();
    }

    @Test
    void saveFundApplication_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        RuntimeException repositoryException = new RuntimeException("Database error");

        when(loanTypeRepository.findById(fundApplication.getIdLoanType()))
                .thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber(fundApplication.getDocumentIdentification(), ""))
                .thenReturn(Mono.just(user));
        when(fundApplicationRepository.save(any(FundApplication.class)))
                .thenReturn(Mono.error(repositoryException));

        // When & Then
        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "", fundApplication.getEmail()))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void saveFundApplication_WhenUserServiceThrowsException_ShouldPropagateException() {
        // Given
        RuntimeException userServiceException = new RuntimeException("User service error");

        when(loanTypeRepository.findById(fundApplication.getIdLoanType()))
                .thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber(fundApplication.getDocumentIdentification(), ""))
                .thenReturn(Mono.error(userServiceException));

        // When & Then
        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "", fundApplication.getEmail()))
                .expectError(RuntimeException.class)
                .verify();
    }
}