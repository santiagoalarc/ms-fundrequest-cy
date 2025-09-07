package co.com.crediya.usecase.command.fundapplicationresponse;


import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.enums.FundStatusEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationNotificationGateway;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundApplicationUpdateStatusUseCaseTest {

    @Mock
    private FundApplicationRepository fundApplicationRepository;

    @Mock
    private FundApplicationNotificationGateway fundApplicationNotificationGateway;

    @Mock
    private UserRestService userRestService;


    private FundApplicationUpdateStatusUseCase fundApplicationUpdateStatusUseCase;


    @BeforeEach
    void setUp() {

        fundApplicationUpdateStatusUseCase = new FundApplicationUpdateStatusUseCase(
                fundApplicationRepository,
                fundApplicationNotificationGateway,
                userRestService
        );


    }

    @Test
    void updateFundApplication_Success() {

        UUID fundId = UUID.randomUUID();
        String email = "joe@arroyo.com";

        FundApplication fundToUpdated = FundApplication.builder()
                .id(fundId)
                .status(FundStatusEnum.APPROVED.name())
                .email(email)
                .build();

        FundApplication fundAppFound = FundApplication.builder()
                .id(fundId)
                .status(FundStatusEnum.PENDING.name())
                .email(email)
                .build();
        
        User user = User.builder()
                .name("Jose")
                .lastName("Arroyo")
                .email(email)
                .build();

        FundApplication anyFundSaved = FundApplication.builder()
                .id(fundId)
                .email(email)
                .build();

        String expectedMessage = "Notification sent";

        when(fundApplicationRepository.findById(fundId)).thenReturn(Mono.just(fundAppFound));
        when(userRestService.findUsersByEmail(List.of(email))).thenReturn(Flux.just(user));
        when(fundApplicationNotificationGateway.notifyStatusChange(
                email,
                "ACTUALIZACIÓN DE ESTADO DE SOLICITUD DE PRÉSTAMO",
                "Jose Arroyo",
                FundStatusEnum.APPROVED.name()
        )).thenReturn(Mono.just(expectedMessage));
        when(fundApplicationRepository.save(any(FundApplication.class))).thenReturn(Mono.just(anyFundSaved));

        StepVerifier.create(fundApplicationUpdateStatusUseCase.execute(fundToUpdated))
                .expectNext(fundToUpdated)
                .verifyComplete();
    }

    @Test
    void fundApplicationNotFound_ERROR() {

        FundApplication fundToUpdated = FundApplication.builder()
                .id(UUID.randomUUID())
                .status(FundStatusEnum.APPROVED.name())
                .build();

        when(fundApplicationRepository.findById(fundToUpdated.getId())).thenReturn(Mono.empty());

        StepVerifier.create(fundApplicationUpdateStatusUseCase.execute(fundToUpdated))
                .expectErrorMatches(throwable -> throwable instanceof FundException
                        && ((FundException) throwable).getError() == FundErrorEnum.FUND_APPLICATION_ID_NOT_FOUND).verify();

    }

    @Test
    void fundApplicationStatusNotFound_ERROR() {

        FundApplication fundToUpdated = FundApplication.builder()
                .status("")
                .build();

        StepVerifier.create(fundApplicationUpdateStatusUseCase.execute(fundToUpdated))
                .expectErrorMatches(throwable -> throwable instanceof FundException
                        && ((FundException) throwable).getError() == FundErrorEnum.OBJECT_STATUS_ID_NOT_VALID).verify();
    }

    @Test
    void updateFundApplication_EmailNotFound_ERROR() {

        FundApplication fundToUpdated = FundApplication.builder()
                .id(UUID.randomUUID())
                .status(FundStatusEnum.APPROVED.name())
                .build();

        FundApplication fundAppFound = FundApplication.builder()
                .id(UUID.randomUUID())
                .status(FundStatusEnum.PENDING.name())
                .email("joe@arroyo.com")
                .build();

        when(fundApplicationRepository.findById(fundToUpdated.getId())).thenReturn(Mono.just(fundAppFound));
        when(userRestService.findUsersByEmail(List.of(fundAppFound.getEmail()))).thenReturn(Flux.empty());

        StepVerifier.create(fundApplicationUpdateStatusUseCase.execute(fundToUpdated))
                .expectErrorMatches(throwable -> throwable instanceof FundException
                        && ((FundException) throwable).getError() == FundErrorEnum.EMAIL_NOT_FOUND).verify();
    }


}