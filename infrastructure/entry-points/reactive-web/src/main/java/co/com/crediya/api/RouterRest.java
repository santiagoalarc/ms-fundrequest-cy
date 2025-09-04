package co.com.crediya.api;

import co.com.crediya.api.config.FundPath;
import co.com.crediya.api.dto.CreateFundApplication;
import co.com.crediya.api.exceptions.ErrorResponse;
import co.com.crediya.model.common.PagedResult;
import co.com.crediya.model.fundapplication.FundApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
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

    @Bean
    @RouterOperation(operation = @Operation(
            operationId = "getFundApplicationList",
            summary = "Get paginated list of fund applications",
            description = "Retrieves a paginated and filtered list of fund applications with enriched customer information including loan types, status details, user data, and calculated total debt. This endpoint supports filtering by email, status, and loan type with pagination capabilities.",
            tags = { "Fund Application Management" },
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(
                            name = "email",
                            description = "Filter fund applications by customer email address. If provided, only applications from users with this email will be returned.",
                            in = ParameterIn.QUERY,
                            schema = @Schema(type = "string", format = "email", example = "customer@example.com")
                    ),
                    @Parameter(
                            name = "status",
                            description = "Filter fund applications by their current status. Common values include: PENDING, APPROVED, REJECTED, IN_REVIEW, CANCELLED.",
                            in = ParameterIn.QUERY,
                            schema = @Schema(type = "string", example = "APPROVED")
                    ),
                    @Parameter(
                            name = "loanType",
                            description = "Filter fund applications by loan type name. This should match the exact name of the loan type configured in the system.",
                            in = ParameterIn.QUERY,
                            schema = @Schema(type = "string", example = "Personal Loan")
                    ),
                    @Parameter(
                            name = "page",
                            description = "Page number for pagination (1-based indexing). Must be a positive integer.",
                            in = ParameterIn.QUERY,
                            schema = @Schema(type = "integer", minimum = "1", defaultValue = "1", example = "1")
                    ),
                    @Parameter(
                            name = "size",
                            description = "Number of fund applications to return per page. Must be between 1 and 100.",
                            in = ParameterIn.QUERY,
                            schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10", example = "10")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Fund applications retrieved successfully. Returns a paginated result with enriched fund application data including customer information, loan details, and current status.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResult.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request - Invalid query parameters (e.g., invalid page/size values, malformed email format)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Invalid or missing JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User does not have required ASESOR (advisor) authority to access fund application lists",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found - Specified status or loan type in filters does not exist in the system",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal Server Error - Unexpected error occurred while processing the request",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    ))
    public RouterFunction<ServerResponse> routerFundApplicationList(Handler handler){
        return route(GET(fundPath.getFundsPageable()), fundApplicationHandler::getFundApplicationList);
    }
}