package com.shopmanagement.service;

import com.shopmanagement.entity.Customer;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.exception.ValidationException;
import com.shopmanagement.repository.CustomerRepository;
import com.shopmanagement.security.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    /**
     * Get all customers for the current shop with pagination
     */
    @Transactional(readOnly = true)
    public Page<Customer> getAllCustomers(Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return customerRepository.findByShopId(shopId, pageable);
    }
    
    /**
     * Search customers by name, phone, or email
     */
    @Transactional(readOnly = true)
    public Page<Customer> searchCustomers(String searchTerm, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return customerRepository.findByShopId(shopId, pageable);
        }
        
        return customerRepository.searchCustomers(shopId, searchTerm.trim(), pageable);
    }
    
    /**
     * Search customers by name
     */
    @Transactional(readOnly = true)
    public Page<Customer> searchByName(String name, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return customerRepository.findByShopIdAndNameContainingIgnoreCase(shopId, name, pageable);
    }
    
    /**
     * Search customers by phone number
     */
    @Transactional(readOnly = true)
    public Page<Customer> searchByPhone(String phoneNumber, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return customerRepository.findByShopIdAndPhoneNumber(shopId, phoneNumber, pageable);
    }
    
    /**
     * Search customers by email
     */
    @Transactional(readOnly = true)
    public Page<Customer> searchByEmail(String email, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return customerRepository.findByShopIdAndEmailContainingIgnoreCase(shopId, email, pageable);
    }
    
    /**
     * Get customer by ID (with shop-level isolation)
     */
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long customerId) {
        Long shopId = ShopContext.getCurrentShopId();
        return customerRepository.findByIdAndShopId(customerId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));
    }
    
    /**
     * Create a new customer
     */
    @Transactional
    public Customer createCustomer(Customer customer) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Validate phone number uniqueness within shop
        if (customerRepository.existsByShopIdAndPhoneNumber(shopId, customer.getPhoneNumber())) {
            throw new ValidationException("Customer with phone number " + 
                    customer.getPhoneNumber() + " already exists");
        }
        
        // Validate email uniqueness within shop if email is provided
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (customerRepository.existsByShopIdAndEmail(shopId, customer.getEmail())) {
                throw new ValidationException("Customer with email " + 
                        customer.getEmail() + " already exists");
            }
        }
        
        // Set shopId for multi-tenant isolation
        customer.setShopId(shopId);
        
        return customerRepository.save(customer);
    }
    
    /**
     * Update an existing customer
     */
    @Transactional
    public Customer updateCustomer(Long customerId, Customer customerUpdate) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Customer existingCustomer = customerRepository.findByIdAndShopId(customerId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));
        
        // Validate phone number uniqueness if changed
        if (!existingCustomer.getPhoneNumber().equals(customerUpdate.getPhoneNumber())) {
            if (customerRepository.existsByShopIdAndPhoneNumber(shopId, customerUpdate.getPhoneNumber())) {
                throw new ValidationException("Customer with phone number " + 
                        customerUpdate.getPhoneNumber() + " already exists");
            }
        }
        
        // Validate email uniqueness if changed
        if (customerUpdate.getEmail() != null && !customerUpdate.getEmail().trim().isEmpty()) {
            if (!customerUpdate.getEmail().equals(existingCustomer.getEmail())) {
                if (customerRepository.existsByShopIdAndEmail(shopId, customerUpdate.getEmail())) {
                    throw new ValidationException("Customer with email " + 
                            customerUpdate.getEmail() + " already exists");
                }
            }
        }
        
        // Update fields
        existingCustomer.setName(customerUpdate.getName());
        existingCustomer.setPhoneNumber(customerUpdate.getPhoneNumber());
        existingCustomer.setEmail(customerUpdate.getEmail());
        existingCustomer.setAddress(customerUpdate.getAddress());
        
        return customerRepository.save(existingCustomer);
    }
    
    /**
     * Delete a customer
     */
    @Transactional
    public void deleteCustomer(Long customerId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Customer customer = customerRepository.findByIdAndShopId(customerId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));
        
        // Check if customer has repair orders
        if (!customer.getRepairOrders().isEmpty()) {
            throw new ValidationException(
                    "Cannot delete customer with existing repair orders. " +
                    "Customer has " + customer.getRepairOrders().size() + " repair order(s).");
        }
        
        customerRepository.delete(customer);
    }
}
