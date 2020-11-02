package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.common.UtilityClass;
import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

//This Class handles all service related to the address

@Service
public class AddressService {
    @Autowired
    AddressDao addressDao; //Handles all data related to the addressEntity

    @Autowired
    CustomerAuthDao customerAuthDao; //Handles all data related to the customerAuthEntity

    @Autowired
    UtilityClass utilityClass;// It Provides Data Check methods for various cases

    @Autowired
    StateDao stateDao; //Handles all data related to the StateEntity

    @Autowired
    CustomerAddressDao customerAddressDao; //Handles all Data of CustomerAddressEntity

    @Autowired
    OrderDao orderDao; //Handles all Data of Orders Entity

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private CustomerService customerBusinessService;

    /* This method is to saveAddress.Takes the Address and state entity and saves the Address to the DB.
    If error throws exception with error code and error message.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity, StateEntity stateEntity) throws SaveAddressException {

        //Checking if any field is empty in the address entity.
        if (addressEntity.getCity() == null || addressEntity.getFlatBuilNo() == null || addressEntity.getPincode() == null || addressEntity.getLocality() == null) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }
        //Checking if pincode is valid
        if (!utilityClass.isPincodeValid(addressEntity.getPincode())) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }

        //Setting state to the address
        addressEntity.setState(stateEntity);

        //Passing the addressEntity to addressDao saveAddress method which returns saved address.
        AddressEntity savedAddress = addressDao.saveAddress(addressEntity);

        //returning SavedAddress
        return savedAddress;

    }

    /*This method is to getAllAddress of the customerEntity.This method takes Customer Entity and returns list of AddressEntity.
     */
    public List<AddressEntity> getAllAddress(CustomerEntity customerEntity) {

        //Creating List of AddressEntities.
        List<AddressEntity> addressEntities = new LinkedList<>();

        //Calls Method of customerAddressDao,getAllCustomerAddressByCustomer and returns AddressList.
        List<CustomerAddressEntity> customerAddressEntities = customerAddressDao.getAllCustomerAddressByCustomer(customerEntity);
        if (customerAddressEntities != null) { //Checking if CustomerAddressEntity is null else extracting address and adding to the addressEntites list.
            customerAddressEntities.forEach(customerAddressEntity -> {
                addressEntities.add(customerAddressEntity.getAddress());
            });
        }

        return addressEntities;

    }

    /*public List<AddressEntity> getAllAddress(final String bearerToken) throws AuthorizationFailedException {

        customerBusinessService.validateAccessToken(bearerToken);

        CustomerAuthEntity customerAuthTokenEntity = customerDao.getCustomerAuthToken(bearerToken);

        return customerAddressDao.getAddressForCustomerByUuid(customerAuthTokenEntity.getCustomer().getUuid());
    }*/

    /*This method is to getStateByUUID using UUID of state.
    If error throws exception with error code and error message.
     */
    public StateEntity getStateByUUID(String uuid) throws SaveAddressException, AddressNotFoundException {
        //Calls getStateByUuid od StateDao to get all the State details.
        StateEntity stateEntity = stateDao.getStateByUuid(uuid);
        /*if(stateEntity == ""){
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }
        else*/
        if (stateEntity == null) {//Checking if its null to return error message.
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        } else {
            return stateEntity;
        }
    }

    /*This method is to saveCustomerAddressEntity.This method takes Customer Entity and AddressEntity and returns CustomerAddressEntity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddressEntity saveCustomerAddressEntity(CustomerEntity customerEntity, AddressEntity addressEntity) {

        //Creating new CustomerAddressEntity and setting the data.
        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setCustomer(customerEntity);
        customerAddressEntity.setAddress(addressEntity);

        //Saving the newly Created CustomerAddressEntity in the DB.
        CustomerAddressEntity createdCustomerAddressEntity = customerAddressDao.saveCustomerAddress(customerAddressEntity);
        return createdCustomerAddressEntity;

    }

    /*This method is to getAddressByUUID of the customerEntity & using Address UUID.This method returns Address Entity.If error throws exception with error code and error message.
     */
    public AddressEntity getAddressByUUID(String addressUuid, CustomerEntity customerEntity) throws AuthorizationFailedException, AddressNotFoundException {
        if (addressUuid == null) {//Check for Address UUID not being empty
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }

        //Calls getAddressByUuid method of addressDao to get addressEntity
        AddressEntity addressEntity = addressDao.getAddressByUuid(addressUuid);
        if (addressEntity == null) {//Checking if null throws corresponding exception.
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        //Getting CustomerAddressEntity by address
        CustomerAddressEntity customerAddressEntity = customerAddressDao.getCustomerAddressByAddress(addressEntity);

        //Checking if the address belong to the customer requested.If no throws corresponding exception.
        if (customerAddressEntity.getCustomer().getUuid() == customerEntity.getUuid()) {
            return addressEntity;
        } else {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

    }

    /*This method is to deleteAddress of the customerEntity.This method returns Address Entity.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity deleteAddress(AddressEntity addressEntity) {

        //Calls getOrdersByAddress of orderDao to orders with corresponding address.
        List<OrderEntity> ordersEntities = orderDao.getOrdersByAddress(addressEntity);

        if (ordersEntities == null || ordersEntities.isEmpty()) { //Checking if no orders are present with this address.
            //Calls deleteAddress of addressDao to delete the corresponding address.
            AddressEntity deletedAddressEntity = addressDao.deleteAddress(addressEntity);
            return deletedAddressEntity;
        } else {
            //Updating the active status
            addressEntity.setActive(0);

            //Calls updateAddressActiveStatus method of addressDao to update address active status.
            AddressEntity updatedAddressActiveStatus = addressDao.updateAddressActiveStatus(addressEntity);
            return updatedAddressActiveStatus;
        }
    }

   /* @Transactional
    public AddressEntity deleteAddress(String addressUuid, String bearerToken)
            throws AuthorizationFailedException, AddressNotFoundException {

        customerBusinessService.validateAccessToken(bearerToken);
        CustomerAuthEntity customerAuthTokenEntity = customerBusinessService.getCustomerAuthToken(bearerToken);

        if (addressUuid == null) {
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty.");
        }

        AddressEntity addressEntity = addressDao.getAddressByUuid(addressUuid);
        CustomerAddressEntity customerAddressEntity = customerAddressDao.getCustAddressByCustIdAddressId(customerAuthTokenEntity.getCustomer(), addressEntity);

        if (addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id.");
        } else if (customerAddressEntity == null) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

        return addressDao.deleteAddressByUuid(addressEntity);
    }*/


    /*This method is to getAllStates in DB.
     */
    public List<StateEntity> getAllStates() {
        //Calls getAllStates of stateDao to get all States.
        List<StateEntity> stateEntities = stateDao.getAllStates();
        return stateEntities;
    }

    @Transactional
    public AddressEntity getAddressById(final Long addressId) {
        return addressDao.getAddressById(addressId);
    }

    @Transactional
    public AddressEntity getAddressByUuid(final String addressUuid) {
        return addressDao.getAddressByUuid(addressUuid);
    }

    @Transactional
    public StateEntity getStateById(Long stateId) {
        return stateDao.getStateById(stateId);
    }
}
