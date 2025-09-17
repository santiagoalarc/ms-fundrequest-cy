package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.UserCapacity;
import reactor.core.publisher.Mono;

public interface CalculateCapacityGateway {

    Mono<String> calculateCapacity(UserCapacity userCapacity);

}
