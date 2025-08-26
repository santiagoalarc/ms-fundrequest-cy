package co.com.crediya.usecase.fundapplication;

import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class FundApplicationUseCase {

    private final FundApplicationRepository fundApplicationRepository;

    public Mono<FundApplication> saveFundApplication(FundApplication fundApplication){
        return Mono.justOrEmpty(fundApplication)
                .flatMap(fundApplicationRepository::save);
    }
}
