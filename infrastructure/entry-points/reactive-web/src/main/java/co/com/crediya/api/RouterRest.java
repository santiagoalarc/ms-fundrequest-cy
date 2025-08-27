package co.com.crediya.api;

import co.com.crediya.api.config.FundPath;
import co.com.crediya.api.dto.CreateFundApplication;
import co.com.crediya.api.exceptions.ErrorResponse;
import co.com.crediya.model.fundapplication.FundApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final Handler fundApplicationHandler;
    private final FundPath fundPath;

    @Bean
    @RouterOperation(operation = @Operation(
            operationId = "createFundApplication",
            summary = "Create a new fund application",
            description = "Creates a new fund application request with validation of loan type limits and user existence",
            tags = { "Fund Application Management" },
            requestBody = @RequestBody(
                    description = "Fund application data to create",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateFundApplication.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Fund application created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = FundApplication.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid fund application data - Missing required fields, invalid format, or business rule validation failed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Loan type not found or user document identification not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Loan amount is out of the allowed range for the selected loan type",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    ))
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(fundPath.getFunds()), fundApplicationHandler::listenSaveFundApplication);
    }
}