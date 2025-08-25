package co.com.crediya.model.fundapplication;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class FundApplication {

    private String id;
    private String amount;
}
