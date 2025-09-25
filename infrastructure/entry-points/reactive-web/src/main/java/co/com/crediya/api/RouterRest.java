package co.com.crediya.api;

import co.com.crediya.api.config.FundPath;
import co.com.crediya.api.dto.CapacityReqDTO;
import co.com.crediya.api.dto.CapacityResDTO;
import co.com.crediya.api.dto.CreateFundApplication;
import co.com.crediya.api.dto.UpdateFundDTO;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
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

    @Bean
    @RouterOperation(operation = @Operation(
            operationId = "updateFundApplicationStatus",
            summary = "Update fund application status",
            description = "Updates the status of an existing fund application. This endpoint allows authorized advisors to change the status of fund applications (e.g., approve, reject, or mark as in review). The operation requires ASESOR authority and validates that the provided fund application ID exists in the system.",
            tags = { "Fund Application Management" },
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    description = "Fund application update data containing the application ID and new status",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateFundDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Fund application status updated successfully. Returns the updated fund application with the new status and modification timestamp.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = FundApplication.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request - Invalid request data. This can occur when: required fields are missing, UUID format is invalid, or the status value is not supported by the system.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Invalid or missing JWT token. Authentication is required to access this endpoint.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User does not have required ASESOR (advisor) authority to update fund application status.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found - Fund application with the specified ID does not exist in the system.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Unprocessable Entity - Business rule validation failed. This can occur when trying to change to an invalid status transition (e.g., from APPROVED back to PENDING).",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal Server Error - Unexpected error occurred while processing the fund application status update.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    ))
    public RouterFunction<ServerResponse> routerResponseFundReq(Handler handler){
        return route(PUT(fundPath.getFunds()), fundApplicationHandler::responseFundReq);
    }


    @Bean
    @RouterOperation(operation = @Operation(
            operationId = "calculateUserCapacity",
            summary = "Calculate user lending capacity",
            description = "Calculates the lending capacity for a specific user based on their financial profile and the requested fund application. This endpoint performs automatic capacity calculation considering user's income, expenses, existing debts, and other financial factors to determine the maximum amount that can be approved for lending. Requires ASESOR authority for access.",
            tags = { "Fund Application Management" },
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    description = "Capacity calculation request data containing user email and fund application ID",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CapacityReqDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User capacity calculated successfully. Returns the calculated lending capacity and related financial information for the specified user and fund application.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CapacityResDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request - Invalid request data. This can occur when: required fields (email or fundId) are missing, email format is invalid, or fundId is not a valid UUID format.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Invalid or missing JWT token. Authentication is required to access this endpoint.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User does not have required ASESOR (advisor) authority to perform capacity calculations.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found - Either the user with the specified email or the fund application with the provided ID does not exist in the system.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Unprocessable Entity - Business rule validation failed. This can occur when the user's financial data is incomplete or insufficient for capacity calculation, or when the fund application is not in a valid state for capacity assessment.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal Server Error - Unexpected error occurred while performing the capacity calculation process.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    ))
    public RouterFunction<ServerResponse> routeCalculateCapacity(Handler handler){
        return route(POST(fundPath.getCalculateCapacity()), fundApplicationHandler::calculateCapacity);
    }

    @Bean
    @RouterOperation(operation = @Operation(
            operationId = "healthCheck",
            summary = "Health check endpoint",
            description = "Simple health check endpoint to verify that the API service is running and responding correctly. This endpoint can be used by monitoring systems, load balancers, or orchestration tools to check service availability. No authentication is required for this endpoint.",
            tags = { "Health Check" },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service is healthy and running correctly",
                            content = @Content(
                                    mediaType = "text/plain",
                                    schema = @Schema(
                                            type = "string",
                                            example = "ok",
                                            description = "Simple text response indicating service health status"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error - Service may be experiencing issues",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "503",
                            description = "Service unavailable - Service is temporarily unable to handle requests",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    ))
    public RouterFunction<ServerResponse> healthCheck(){
        return route(GET("/health"), fundApplicationHandler::healthCheck);
    }

}