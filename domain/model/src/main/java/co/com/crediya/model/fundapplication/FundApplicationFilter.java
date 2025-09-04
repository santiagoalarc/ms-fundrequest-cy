package co.com.crediya.model.fundapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FundApplicationFilter {
    private String email;
    private String status = "";
    private String statusId = "";
    private String loanType = "";
    private String loanTypeId = "";

}