package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.CapacityReqDTO;
import co.com.crediya.api.dto.CapacityResDTO;
import co.com.crediya.model.user.UserCapacity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapacityMapper {

    CapacityResDTO toResponse(UserCapacity capacity);

    UserCapacity toRequest(CapacityReqDTO capacityReqDTO);
}
