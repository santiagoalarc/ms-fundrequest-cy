package co.com.crediya.model.fundapplication;

import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundstatus.FundStatus;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class QueryFundApplication {

    Map<String, FundStatus> fundStatusMap;
    Map<String, LoanType> loanTypeMap;
    Map<String, User> userMap;
    List<String> emails;
    PagedResult<FundAppCustomer> fundAppCustomerPaged;
}
