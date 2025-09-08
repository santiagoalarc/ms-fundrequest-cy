package co.com.crediya.model.fundapplication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FundAppCustomer  extends  FundApplication {

    private String name;
    private String baseSalary;
    private Double interestRateTaa;
    private BigDecimal monthlyAmount;
    private String status;
}
