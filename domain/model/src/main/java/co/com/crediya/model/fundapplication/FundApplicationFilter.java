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
    private String status;
    private String loanType;

    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    public boolean hasStatus() {
        return status != null && !status.trim().isEmpty();
    }

    public boolean hasLoanType() {
        return loanType != null && !loanType.trim().isEmpty();
    }

}