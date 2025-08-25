package co.com.crediya.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("fund_application")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FundEntity {

    @Id
    private String id;
    private String amount;
}
