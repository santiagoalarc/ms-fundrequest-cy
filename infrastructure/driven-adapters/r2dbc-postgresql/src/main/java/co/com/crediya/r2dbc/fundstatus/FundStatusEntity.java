package co.com.crediya.r2dbc.fundstatus;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("fund_status")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FundStatusEntity {

    private String id;
    private String name;
    private String description;
    @Column("creation_date")
    private String creationDate;
}
