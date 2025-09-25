package co.com.crediya.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.paths")
public class FundPath {

    private String funds;
    private String fundsById;
    private String fundsPageable;
    private String calculateCapacity;
    private String findLoansToday;
}