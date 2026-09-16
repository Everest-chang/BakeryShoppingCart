package demo.usercart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.usercart.model.JwtUtility;
import demo.usercart.model.Order;
import demo.usercart.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtility jwtUtility;

    // 建立訂單
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody Order order,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {

        // 先驗證 Header、JWT 簽章與到期時間
        jwtUtility.extractUsernameFromAuthorization(
                authorization
        );

        // 暫時沿用既有 Service 的參數格式
        String token = authorization.trim()
                .split("\\s+", 2)[1];

        Order result = orderService.createOrder(
                order,
                "Bearer " + token
        );

        if (result == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok(result);
    }

    // 查詢指定會員的訂單
    @GetMapping("/{username}")
    public ResponseEntity<?> getOrdersByUsername(
            @PathVariable String username,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {

        String loginUsername =
                jwtUtility.extractUsernameFromAuthorization(
                        authorization
                );

        // 不可透過修改網址查詢其他會員
        if (!loginUsername.equals(username)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
                orderService.getOrdersByUsername(loginUsername)
        );
    }

    // 查詢單筆訂單
    @GetMapping("/orderid/{orderid}")
    public ResponseEntity<?> getOrdersById(
            @PathVariable Integer orderid,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {

        String loginUsername =
                jwtUtility.extractUsernameFromAuthorization(
                        authorization
                );

        Order order = orderService.getOrdersById(orderid);

        // 訂單不存在，或不屬於目前會員，統一回傳 404
        if (order == null
                || order.getUser() == null
                || !loginUsername.equals(
                        order.getUser().getUsername()
                )) {

            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(order);
    }

    // 目前沒有管理員權限機制，不開放查詢全部會員訂單
    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {

        jwtUtility.extractUsernameFromAuthorization(
                authorization
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();
    }
}