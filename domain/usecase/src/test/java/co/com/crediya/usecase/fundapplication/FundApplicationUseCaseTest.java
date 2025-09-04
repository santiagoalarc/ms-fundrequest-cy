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
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FundApplicationUseCaseTest {

    private FundApplicationRepository fundApplicationRepository;
    private LoanTypeRepository loanTypeRepository;
    private UserRestService userRestService;
    private FundApplicationUseCase fundApplicationUseCase;

    @BeforeEach
    void setUp() {
        fundApplicationRepository = Mockito.mock(FundApplicationRepository.class);
        loanTypeRepository = Mockito.mock(LoanTypeRepository.class);
        userRestService = Mockito.mock(UserRestService.class);
        fundApplicationUseCase = new FundApplicationUseCase(fundApplicationRepository, loanTypeRepository, userRestService);
    }

    @Test
    void saveFundApplication_success() {
        FundApplication fundApplication = FundApplication.builder()
                .email("test@email.com")
                .loanType("PERSONAL")
                .amount(BigDecimal.valueOf(5000))
                .documentIdentification("123456")
                .build();

        UUID loanTypeId = UUID.randomUUID();

        LoanType loanType = LoanType.builder()
                .id(loanTypeId)
                .name("PERSONAL")
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(10000))
                .build();

        FundApplication savedFund = fundApplication.toBuilder()
                .idLoanType(loanTypeId)
                .idStatus(FundStatusEnum.PENDING.getId())
                .build();

        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber("123456")).thenReturn(Mono.just(new User()));
        when(fundApplicationRepository.save(any(FundApplication.class))).thenReturn(Mono.just(savedFund));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "test@email.com"))
                .expectNextMatches(result ->
                        loanTypeId.equals(result.getIdLoanType())
                                && FundStatusEnum.PENDING.getId().equals(result.getIdStatus()))
                .verifyComplete();

        verify(fundApplicationRepository).save(any(FundApplication.class));
    }


    @Test
    void saveFundApplication_tokenUserMismatch() {
        FundApplication fundApplication = FundApplication.builder()
                .email("other@email.com")
                .build();

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "test@email.com"))
                .expectErrorMatches(error ->
                        error instanceof FundException
                                && ((FundException) error).getError() == FundErrorEnum.TOKEN_USER_MISMATCH)
                .verify();
    }

    @Test
    void saveFundApplication_loanTypeNotFound() {
        FundApplication fundApplication = FundApplication.builder()
                .email("test@email.com")
                .loanType("UNKNOWN")
                .amount(BigDecimal.valueOf(2000))
                .documentIdentification("123456")
                .build();

        when(loanTypeRepository.findByName("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "test@email.com"))
                .expectErrorMatches(error ->
                        error instanceof FundException
                                && ((FundException) error).getError() == FundErrorEnum.LOAN_TYPE_ID_NOT_FOUND)
                .verify();
    }

    @Test
    void saveFundApplication_amountOutOfRange() {
        FundApplication fundApplication = FundApplication.builder()
                .email("test@email.com")
                .loanType("PERSONAL")
                .amount(BigDecimal.valueOf(50000))
                .documentIdentification("123456")
                .build();

        LoanType loanType = LoanType.builder()
                .id(UUID.randomUUID())
                .name("PERSONAL")
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(10000))
                .build();

        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "test@email.com"))
                .expectErrorMatches(error ->
                        error instanceof FundException
                                && ((FundException) error).getError() == FundErrorEnum.LOAN_AMOUNT_OUT_OF_RANGE)
                .verify();
    }

    @Test
    void saveFundApplication_documentIdentificationNotFound() {
        FundApplication fundApplication = FundApplication.builder()
                .email("test@email.com")
                .loanType("PERSONAL")
                .amount(BigDecimal.valueOf(2000))
                .documentIdentification("123456")
                .build();

        LoanType loanType = LoanType.builder()
                .id(UUID.randomUUID())
                .name("PERSONAL")
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(10000))
                .build();

        when(loanTypeRepository.findByName("PERSONAL")).thenReturn(Mono.just(loanType));
        when(userRestService.findUserByDocumentNumber("123456")).thenReturn(Mono.empty());

        StepVerifier.create(fundApplicationUseCase.saveFundApplication(fundApplication, "test@email.com"))
                .expectErrorMatches(error ->
                        error instanceof FundException
                                && ((FundException) error).getError() == FundErrorEnum.DOCUMENT_IDENTIFICATION_NOT_FOUND)
                .verify();
    }
}
