package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.domain.strategy.service.StrategyAllowApprovalService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@Slf4j
@RequestMapping("/v1")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TraderStrategyAllowController implements TraderStrategyAllowControllerDocs {

    private final StrategyAllowApprovalService strategyAllowApprovalService;

    @Override
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @PostMapping("/strategy/approve-open/{id}")
    public ResponseEntity<APIResponse<String>> postApproveOpenStrategy(
            @PathVariable Long id
    ) {
        try {
            strategyAllowApprovalService.approveOpen(id);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail(ErrorCode.BAD_REQUEST));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(APIResponse.fail(ErrorCode.DUPLICATE_RESOURCE));
        }
    }

    @Override
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @PatchMapping("/strategy/approve-cancel/{id}")
    public ResponseEntity<APIResponse<String>> postApproveCancelStrategy(
            @PathVariable Long id
    ) {
        try {
            strategyAllowApprovalService.approveCancel(id);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(APIResponse.success());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.fail(ErrorCode.NOT_FOUND));
        }
    }
}
