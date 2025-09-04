package co.com.crediya.model.fundapplication;

import co.com.crediya.model.fundstatus.FundStatus;
import co.com.crediya.model.loantype.LoanType;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class FundAppQuery {

    Map<String, LoanType> loanTypeMap;
    Map<UUID, FundStatus> fundStatusMap;
    FundAppCustomer fundAppCustomer;
}
