package co.com.crediya.usecase.command.fundapplicationupdatestatus;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.usecase.common.ValidateApprovalStatusUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundApplicationStatusUseCaseTest {

    @InjectMocks
    private FundApplicationStatusUseCase useCase;

    @Mock
    private ValidateApprovalStatusUseCase validateApprovalStatusUseCase;

    @Mock
    private FundApplicationRepository fundApplicationRepository;

    private FundApplication fundApplication;

    @BeforeEach
    void setUp() {
        fundApplication = FundApplication.builder()
                .id(UUID.randomUUID())
                .status("APPROVED")
                .idStatus("01")
                .build();
    }

    @Test
    void testExecute_Success() {
        when(fundApplicationRepository.findById(fundApplication.getId())).thenReturn(Mono.just(fundApplication));
        when(fundApplicationRepository.save(any(FundApplication.class))).thenReturn(Mono.just(fundApplication));
        when(validateApprovalStatusUseCase.validateStatusAndSend(any(FundApplication.class))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(fundApplication))
                .expectNext(fundApplication)
                .verifyComplete();

        verify(fundApplicationRepository).findById(fundApplication.getId());
        verify(fundApplicationRepository).save(any(FundApplication.class));
        verify(validateApprovalStatusUseCase).validateStatusAndSend(any(FundApplication.class));
    }

    @Test
    void testExecute_InvalidStatus() {
        fundApplication.setStatus("INVALID_STATUS");

        StepVerifier.create(useCase.execute(fundApplication))
                .expectErrorMatches(throwable -> throwable instanceof FundException && ((FundException) throwable).getError() == FundErrorEnum.OBJECT_STATUS_ID_NOT_VALID)
                .verify();
    }

    @Test
    void testExecute_FundApplicationNotFound() {
        when(fundApplicationRepository.findById(fundApplication.getId())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(fundApplication))
                .expectErrorMatches(throwable -> throwable instanceof FundException && ((FundException) throwable).getError() == FundErrorEnum.FUND_APPLICATION_ID_NOT_FOUND)
                .verify();
    }
}