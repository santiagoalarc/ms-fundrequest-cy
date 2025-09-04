package co.com.crediya.model.fundstatus;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class FundStatus {

    private UUID id;
    private String name;
    private String description;
    private String creationDate;
}
