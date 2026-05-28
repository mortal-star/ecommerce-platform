package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.security.SecurityUserDetails;
import com.example.ecommerce.service.AddressService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public Result<Address> create(@AuthenticationPrincipal SecurityUserDetails principal, @RequestBody Address address) {
        return Result.success("收货地址新增成功", addressService.createAddress(principal.getUserId(), address));
    }

    @PutMapping("/{addressId}")
    public Result<Address> update(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long addressId, @RequestBody Address address) {
        return Result.success("收货地址修改成功", addressService.updateAddress(principal.getUserId(), addressId, address));
    }

    @DeleteMapping("/{addressId}")
    public Result<Void> delete(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long addressId) {
        addressService.deleteAddress(principal.getUserId(), addressId);
        return Result.success("收货地址删除成功", null);
    }

    @GetMapping
    public Result<List<Address>> list(@AuthenticationPrincipal SecurityUserDetails principal) {
        return Result.success(addressService.listAddress(principal.getUserId()));
    }

    @GetMapping("/{addressId}")
    public Result<Address> detail(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long addressId) {
        return Result.success(addressService.getUserAddress(principal.getUserId(), addressId));
    }

    @PutMapping("/{addressId}/default")
    public Result<Void> setDefault(@AuthenticationPrincipal SecurityUserDetails principal, @PathVariable Long addressId) {
        addressService.setDefault(principal.getUserId(), addressId);
        return Result.success("默认地址设置成功", null);
    }
}
