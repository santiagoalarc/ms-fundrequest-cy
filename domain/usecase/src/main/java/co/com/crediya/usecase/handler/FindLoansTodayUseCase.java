package co.com.crediya.usecase.handler;

import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class FindLoansTodayUseCase {

    private final FundApplicationRepository fundApplicationRepository;

    private final Logger log = Logger.getLogger(FindLoansTodayUseCase.class.getName());


    public Flux<FundApplication> execute(){


        log.info("Enter to FindLoansTodayUseCase ::");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());


        ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(now.getZone());
        long startTimestamp = startOfDay.toInstant().toEpochMilli();

        ZonedDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59, 999000000).atZone(now.getZone());
        long endTimestamp = endOfDay.toInstant().toEpochMilli();

        return fundApplicationRepository.findByUpdateDateBetween(startTimestamp, endTimestamp);

    }
}