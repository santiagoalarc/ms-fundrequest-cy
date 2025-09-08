package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.CreateFundApplication;
import co.com.crediya.api.dto.FundAppFilterDTO;
import co.com.crediya.api.dto.UpdateFundDTO;
import co.com.crediya.model.fundapplication.FundAppCustomer;
import co.com.crediya.model.fundapplication.FundApplication;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FundDtoMapper {

    FundApplication toModel(CreateFundApplication createFundApplication);

    FundAppFilterDTO toResponse(FundAppCustomer fundAppCustomer);

    FundApplication toModel(UpdateFundDTO updateFundDTO);

    UpdateFundDTO toResponse(FundApplication fundApplication);

}
