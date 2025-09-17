package co.com.crediya.model.user;

import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import co.com.crediya.model.loantype.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserCapacity {

    private UUID fundId;
    private String status;
    private String fullName;
    private String email;
    private BigDecimal salary;
    private BigDecimal debtMax;
    private BigDecimal monthlyDebt;
    private BigDecimal availableCapacity;
    private List<FundAppCustomer> fundAppCustomers;
    private Map<String, LoanType> loanTypes;
    private FundApplication fundToEvaluate;
}
