package co.com.crediya.usecase.handler;

import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindLoansTodayUseCaseTest {

    @Mock
    private FundApplicationRepository fundApplicationRepository;

    private FindLoansTodayUseCase findLoansTodayUseCase;

    @BeforeEach
    void setUp() {
        findLoansTodayUseCase = new FindLoansTodayUseCase(fundApplicationRepository);
    }

    @Test
    void execute_ShouldReturnTodaysFundApplications_WhenRepositoryHasData() {

        List<FundApplication> expectedApplications = createSampleFundApplications();

        when(fundApplicationRepository.findByUpdateDateBetween(anyLong(), anyLong()))
                .thenReturn(Flux.fromIterable(expectedApplications));


        Flux<FundApplication> result = findLoansTodayUseCase.execute();


        StepVerifier.create(result)
                .expectNext(expectedApplications.get(0))
                .expectNext(expectedApplications.get(1))
                .expectNext(expectedApplications.get(2))
                .verifyComplete();

        verify(fundApplicationRepository, times(1))
                .findByUpdateDateBetween(anyLong(), anyLong());
    }

    @Test
    void execute_ShouldReturnEmptyFlux_WhenRepositoryHasNoData() {

        when(fundApplicationRepository.findByUpdateDateBetween(anyLong(), anyLong()))
                .thenReturn(Flux.empty());


        Flux<FundApplication> result = findLoansTodayUseCase.execute();


        StepVerifier.create(result)
                .verifyComplete();

        verify(fundApplicationRepository, times(1))
                .findByUpdateDateBetween(anyLong(), anyLong());
    }

    @Test
    void execute_ShouldCallRepositoryWithCorrectTimeRange() {

        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> endTimeCaptor = ArgumentCaptor.forClass(Long.class);

        when(fundApplicationRepository.findByUpdateDateBetween(anyLong(), anyLong()))
                .thenReturn(Flux.empty());

        ZonedDateTime beforeExecution = ZonedDateTime.now(ZoneId.systemDefault());

        findLoansTodayUseCase.execute().subscribe();


        verify(fundApplicationRepository).findByUpdateDateBetween(
                startTimeCaptor.capture(),
                endTimeCaptor.capture()
        );

        Long startTimestamp = startTimeCaptor.getValue();
        Long endTimestamp = endTimeCaptor.getValue();

        ZonedDateTime startOfDay = beforeExecution.toLocalDate().atStartOfDay(beforeExecution.getZone());
        long expectedStartTimestamp = startOfDay.toInstant().toEpochMilli();

        ZonedDateTime endOfDay = beforeExecution.toLocalDate().atTime(23, 59, 59, 999000000).atZone(beforeExecution.getZone());
        long expectedEndTimestamp = endOfDay.toInstant().toEpochMilli();


        assertThat(startTimestamp).isCloseTo(expectedStartTimestamp, within(1000L));
        assertThat(endTimestamp).isCloseTo(expectedEndTimestamp, within(1000L));
        assertThat(endTimestamp).isGreaterThan(startTimestamp);
    }

    @Test
    void execute_ShouldHandleRepositoryError() {

        RuntimeException expectedException = new RuntimeException("Database connection error");

        when(fundApplicationRepository.findByUpdateDateBetween(anyLong(), anyLong()))
                .thenReturn(Flux.error(expectedException));

        Flux<FundApplication> result = findLoansTodayUseCase.execute();

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(fundApplicationRepository, times(1))
                .findByUpdateDateBetween(anyLong(), anyLong());
    }

    @Test
    void execute_ShouldUseSystemDefaultTimeZone() {

        when(fundApplicationRepository.findByUpdateDateBetween(anyLong(), anyLong()))
                .thenReturn(Flux.empty());

        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> endTimeCaptor = ArgumentCaptor.forClass(Long.class);

        findLoansTodayUseCase.execute().subscribe();


        verify(fundApplicationRepository).findByUpdateDateBetween(
                startTimeCaptor.capture(),
                endTimeCaptor.capture()
        );

        long timeDifference = endTimeCaptor.getValue() - startTimeCaptor.getValue();
        long expectedDayInMillis = ChronoUnit.DAYS.getDuration().toMillis();

        assertThat(timeDifference).isCloseTo(expectedDayInMillis, within(1000L));
    }



    private List<FundApplication> createSampleFundApplications() {
        FundApplication app1 = FundApplication.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("10000"))
                .term(12L)
                .email("user1@example.com")
                .status("APPROVED")
                .build();

        FundApplication app2 = FundApplication.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("25000"))
                .term(24L)
                .email("user2@example.com")
                .status("PENDING")
                .build();

        FundApplication app3 = FundApplication.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("15000"))
                .term(18L)
                .email("user3@example.com")
                .status("REJECTED")
                .build();

        return Arrays.asList(app1, app2, app3);
    }

    private static org.assertj.core.data.Offset<Long> within(long offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}