package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.mapper.AddressMapper;
import com.example.ecommerce.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address createAddress(Long userId, Address address) {
        address.setUserId(userId);
        address.setDeleted(0);
        if (address.getDefaultFlag() == null) {
            address.setDefaultFlag(0);
        }
        if (Integer.valueOf(1).equals(address.getDefaultFlag())) {
            clearDefault(userId);
        }
        save(address);
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address updateAddress(Long userId, Long addressId, Address address) {
        getUserAddress(userId, addressId);
        address.setId(addressId);
        address.setUserId(userId);
        if (Integer.valueOf(1).equals(address.getDefaultFlag())) {
            clearDefault(userId);
        }
        updateById(address);
        return getById(addressId);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        getUserAddress(userId, addressId);
        removeById(addressId);
    }

    @Override
    public List<Address> listAddress(Long userId) {
        return list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getDeleted, 0)
                .orderByDesc(Address::getDefaultFlag)
                .orderByDesc(Address::getCreateTime));
    }

    @Override
    public Address getUserAddress(Long userId, Long addressId) {
        Address address = getById(addressId);
        if (address == null || !Integer.valueOf(0).equals(address.getDeleted()) || !address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("收货地址不存在");
        }
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long addressId) {
        Address address = getUserAddress(userId, addressId);
        clearDefault(userId);
        address.setDefaultFlag(1);
        updateById(address);
    }

    private void clearDefault(Long userId) {
        List<Address> addresses = list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getDeleted, 0));
        for (Address address : addresses) {
            address.setDefaultFlag(0);
            updateById(address);
        }
    }
}
