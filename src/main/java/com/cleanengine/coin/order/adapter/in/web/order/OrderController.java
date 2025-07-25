package com.cleanengine.coin.order.adapter.in.web.order;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.order.application.OrderCancelService;
import com.cleanengine.coin.order.application.OrderService;
import com.cleanengine.coin.order.application.dto.OrderCancelResult;
import com.cleanengine.coin.order.application.dto.OrderCommand;
import com.cleanengine.coin.order.application.dto.OrderInfo;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderCancelService orderCancelService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto.CreateOrder>> createOrder(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody @Valid OrderRequestDto.CreateOrderRequest createOrderRequest) {
        Integer userId = user.getUserId();

        OrderCommand.CreateOrder createOrderCommand = createOrderRequest.toOrderCommand(userId);
        OrderInfo<?> orderInfo = orderService.createOrder(createOrderCommand);

        return ApiResponse.success(OrderResponseDto.CreateOrder.from(orderInfo), HttpStatus.CREATED)
                .toResponseEntity();
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<OrderCancelResult>> cancelOrder(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestParam Long orderId) {
        if (orderId == null) throw new IllegalArgumentException("orderId cannot be null.");

        OrderCancelResult orderCancelResult = orderCancelService.cancelOrder(orderId, user.getUserId());

        return ApiResponse.success(orderCancelResult, HttpStatus.OK)
                .toResponseEntity();
    }
}
