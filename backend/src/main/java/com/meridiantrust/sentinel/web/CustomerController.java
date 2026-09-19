package com.meridiantrust.sentinel.web;

import com.meridiantrust.sentinel.dto.Dtos.CustomerDetail;
import com.meridiantrust.sentinel.dto.Dtos.CustomerSummary;
import com.meridiantrust.sentinel.dto.Dtos.TransactionView;
import com.meridiantrust.sentinel.service.CustomerService;
import com.meridiantrust.sentinel.web.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ApiResponse<Page<CustomerSummary>> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String riskRating) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(customerService.list(riskRating, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDetail> detail(@PathVariable String id) {
        return ApiResponse.ok(customerService.detail(id), id);
    }

    @GetMapping("/{id}/transactions")
    public ApiResponse<List<TransactionView>> transactions(@PathVariable String id) {
        return ApiResponse.ok(customerService.transactions(id), id);
    }
}
