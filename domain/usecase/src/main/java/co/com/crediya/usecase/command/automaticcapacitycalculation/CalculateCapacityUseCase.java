package co.com.crediya.usecase.command.automaticcapacitycalculation;

import co.com.crediya.enums.FundErrorEnum;
import co.com.crediya.enums.FundStatusEnum;
import co.com.crediya.exceptions.FundException;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.fundapplication.gateways.FundApplicationRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.user.UserCapacity;
import co.com.crediya.model.user.gateways.CalculateCapacityGateway;
import co.com.crediya.model.user.gateways.UserRestService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CalculateCapacityUseCase {

    private final FundApplicationRepository fundApplicationRepository;
    private final CalculateCapacityGateway calculateCapacityGateway;
    private final LoanTypeRepository loanTypeRepository;
    private final UserRestService userRestService;

    private final Logger log = Logger.getLogger(CalculateCapacityUseCase.class.getName());

    public Mono<UserCapacity> execute(String email, UUID fundId) {

        log.info("ENTER TO CalculateCapacityUseCase with email: " + email);

        return fetchAllLoanTypes()
                .flatMap(loanTypeMap -> fetchUserCapacity(email, loanTypeMap))
                .flatMap(this::enrichWithApprovedFunds)
                .flatMap(userCapacity -> enrichWithFundToEvaluate(userCapacity, fundId))
                .flatMap(userCapacity -> calculateCapacityGateway.calculateCapacity(userCapacity)
                        .thenReturn(userCapacity));
    }

    private Mono<Map<String, LoanType>> fetchAllLoanTypes() {
        return loanTypeRepository.findAll()
                .collectMap(LoanType::getId);
    }

    private Mono<UserCapacity> fetchUserCapacity(String email, Map<String, LoanType> loanTypeMap) {
        return userRestService.findUsersByEmail(List.of(email))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.USER_NOT_FOUND))))
                .next()
                .map(user -> UserCapacity.builder()
                        .fullName(user.getName().concat(" ").concat(user.getLastName()))
                        .salary(new BigDecimal(user.getBaseSalary()))
                        .loanTypes(loanTypeMap)
                        .email(email)
                        .build());
    }

    private Mono<UserCapacity> enrichWithApprovedFunds(UserCapacity userCapacity) {
        return fundApplicationRepository.findByEmail(userCapacity.getEmail())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new FundException(FundErrorEnum.USER_NOT_FOUND))))
                .filter(fundAppCustomer -> FundStatusEnum.APPROVED.getId().equals(fundAppCustomer.getIdStatus()))
                .map(fundData -> mapToFundAppCustomer(fundData, userCapacity.getLoanTypes()))
                .collectList()
                .map(fundList -> userCapacity.toBuilder()
                        .fundAppCustomers(fundList)
                        .build());
    }

    private Mono<UserCapacity> enrichWithFundToEvaluate(UserCapacity userCapacity, UUID fundId) {
        return fundApplicationRepository.findById(fundId)
                .map(fundData -> mapToFundAppCustomer(fundData, userCapacity.getLoanTypes()))
                .map(fundAppCustomer -> userCapacity.toBuilder()
                        .fundToEvaluate(fundAppCustomer)
                        .build());
    }

    private FundAppCustomer mapToFundAppCustomer(FundApplication fundData, Map<String, LoanType> loanTypeMap) {
        LoanType loanType = loanTypeMap.get(fundData.getIdLoanType());
        if (loanType == null) {
            throw new FundException(FundErrorEnum.LOAN_TYPE_ID_NOT_FOUND);
        }
        FundAppCustomer fundAppCustomer = new FundAppCustomer();
        fundAppCustomer.setId(fundData.getId());
        fundAppCustomer.setInterestRateTaa(loanType.getInterestRateTaa());
        fundAppCustomer.setTerm(fundData.getTerm());
        fundAppCustomer.setAmount(fundData.getAmount());
        return fundAppCustomer;
    }
}