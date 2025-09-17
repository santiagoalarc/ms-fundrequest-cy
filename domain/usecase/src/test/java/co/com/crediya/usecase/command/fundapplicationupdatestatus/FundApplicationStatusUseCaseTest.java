package co.com.crediya.usecase.command.fundapplicationupdatestatus;
import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.enums.FundStatusEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundApplicationStatusUseCaseTest {

    @Mock
    private FundApplicationRepository fundApplicationRepository;

    @InjectMocks
    private FundApplicationStatusUseCase fundApplicationStatusUseCase;

    private FundApplication fundApplication;

    private FundApplication fundApplicationUpdated;

    @BeforeEach
    void setUp() {
        fundApplication = FundApplication.builder()
                .id(UUID.randomUUID())
                .status("APPROVED")
                .build();
        fundApplicationUpdated = FundApplication.builder()
                .id(UUID.randomUUID())
                .idStatus(FundStatusEnum.APPROVED.getId())
                .status("APPROVED")
                .build();
    }

    @Test
    void executeSuccess() {
        when(fundApplicationRepository.findById(fundApplication.getId())).thenReturn(Mono.just(fundApplication));
        when(fundApplicationRepository.save(any(FundApplication.class))).thenReturn(Mono.just(fundApplicationUpdated));

        StepVerifier.create(fundApplicationStatusUseCase.execute(fundApplication))
                .expectNextMatches(fundApp -> fundApp.getId().equals(fundApplicationUpdated.getId())
                        && fundApp.getIdStatus().equals(FundStatusEnum.APPROVED.getId()))
                .verifyComplete();

        Mockito.verify(fundApplicationRepository).findById(fundApplication.getId());
        Mockito.verify(fundApplicationRepository).save(any(FundApplication.class));
    }

    @Test
    void executeStatusNameNotValid() {
        fundApplication.setStatus("INVALID_STATUS");

        StepVerifier.create(fundApplicationStatusUseCase.execute(fundApplication))
                .expectErrorMatches(throwable -> throwable instanceof FundException && ((FundException) throwable).getError()
                        .equals(FundErrorEnum.OBJECT_STATUS_ID_NOT_VALID))
                .verify();

        Mockito.verifyNoInteractions(fundApplicationRepository);
    }

    @Test
    void executeFundApplicationIdNotFound() {
        when(fundApplicationRepository.findById(fundApplication.getId())).thenReturn(Mono.empty());

        StepVerifier.create(fundApplicationStatusUseCase.execute(fundApplication))
                .expectErrorMatches(throwable -> throwable instanceof FundException && ((FundException) throwable).getError()
                        .equals(FundErrorEnum.FUND_APPLICATION_ID_NOT_FOUND))
                .verify();

        Mockito.verify(fundApplicationRepository).findById(fundApplication.getId());
        Mockito.verify(fundApplicationRepository, Mockito.never()).save(any(FundApplication.class));
    }
}