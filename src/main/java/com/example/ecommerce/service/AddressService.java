package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.entity.Address;

import java.util.List;

public interface AddressService extends IService<Address> {
    Address createAddress(Long userId, Address address);
    Address updateAddress(Long userId, Long addressId, Address address);
    void deleteAddress(Long userId, Long addressId);
    List<Address> listAddress(Long userId);
    Address getUserAddress(Long userId, Long addressId);
    void setDefault(Long userId, Long addressId);
}
