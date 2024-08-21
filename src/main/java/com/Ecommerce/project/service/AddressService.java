package com.Ecommerce.project.service;

import com.Ecommerce.project.model.Users;
import com.Ecommerce.project.payload.AddressDTO;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, Users user);
    List<AddressDTO> getAddresses();
    AddressDTO getAddressesById(Long addressId);
    List<AddressDTO> getUserAddresses(Users user);
    AddressDTO updateAddress(Long addressId, AddressDTO addressDTO);
    String deleteAddress(Long addressId);
}
