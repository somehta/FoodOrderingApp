package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class PaymentDao {
    @PersistenceContext
    private EntityManager entityManager;

    //To get all payment methods
    public List<PaymentEntity> getAllPaymentMethods() {
        try {
            List<PaymentEntity> paymentMethods = entityManager.createNamedQuery("getAllPaymentMethods", PaymentEntity.class).getResultList();
            return paymentMethods;
        } catch (NoResultException nre) {
            return null;
        }
    }

    public PaymentEntity getPaymentByUuid(String uuidReceived){
        try {
            return entityManager.createNamedQuery("getPaymentByUUID", PaymentEntity.class).setParameter("uuid", uuidReceived).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

}
