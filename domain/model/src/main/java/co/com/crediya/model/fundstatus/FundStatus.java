package co.com.crediya.model.fundstatus;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class FundStatus {

    private String id;
    private String name;
    private String description;
    private String creationDate;
}
