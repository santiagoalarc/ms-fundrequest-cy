package co.com.crediya.usecase.common;


import co.com.crediya.enums.FundStatusEnum;
import co.com.crediya.model.events.gateway.EventGateway;
import co.com.crediya.model.fundapplication.FundApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidateApprovalStatusUseCaseTest {

    @InjectMocks
    private ValidateApprovalStatusUseCase validateApprovalStatusUseCase;

    @Mock
    private EventGateway eventGateway;

    private FundApplication rejectedFundApplication;

    @BeforeEach
    void setUp() {
        FundApplication approvedFundApplication = FundApplication.builder()
                .id(UUID.randomUUID())
                .status("APPROVED")
                .idStatus(FundStatusEnum.APPROVED.getId())
                .build();

        rejectedFundApplication = FundApplication.builder()
                .id(UUID.randomUUID())
                .status("REJECTED")
                .idStatus(FundStatusEnum.REJECTED.getId())
                .build();
    }

    @Test
    void testValidateStatusAndSend_WhenStatusIsNotApproved_ShouldNotSendMessage() {

        StepVerifier.create(validateApprovalStatusUseCase.validateStatusAndSend(rejectedFundApplication))
                .verifyComplete();

        verify(eventGateway, never()).sendMessage(any());
    }

    @Test
    void testValidateStatusAndSend_WhenIdStatusIsNull_ShouldNotSendMessage() {
        FundApplication fundApplicationWithNullStatus = FundApplication.builder()
                .id(UUID.randomUUID())
                .status("APPROVED")
                .idStatus(null)
                .build();


        StepVerifier.create(validateApprovalStatusUseCase.validateStatusAndSend(fundApplicationWithNullStatus))
                .verifyComplete();

        verify(eventGateway, never()).sendMessage(any());
    }

    @Test
    void testValidateStatusAndSend_WhenIdStatusIsEmpty_ShouldNotSendMessage() {
        FundApplication fundApplicationWithEmptyStatus = FundApplication.builder()
                .id(UUID.randomUUID())
                .status("APPROVED")
                .idStatus("")
                .build();

        StepVerifier.create(validateApprovalStatusUseCase.validateStatusAndSend(fundApplicationWithEmptyStatus))
                .verifyComplete();

        verify(eventGateway, never()).sendMessage(any());
    }

}