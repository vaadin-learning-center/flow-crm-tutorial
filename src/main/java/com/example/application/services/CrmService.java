package com.example.application.services;

import com.example.application.data.Company;
import com.example.application.data.Contact;
import com.example.application.data.PushSubscriptionEntity;
import com.example.application.data.Status;
import com.example.application.data.CompanyRepository;
import com.example.application.data.ContactRepository;
import com.example.application.data.PushSubscriptionRepository;
import com.example.application.data.StatusRepository;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.server.webpush.WebPush;
import com.vaadin.flow.server.webpush.WebPushKeys;
import com.vaadin.flow.server.webpush.WebPushSubscription;

@Service
public class CrmService {

    @Value("${public.key}")
    private String publicKey;
    @Value("${private.key}")
    private String privateKey;
    @Value("${subject}")
    private String subject;

    private WebPush webPush;

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final StatusRepository statusRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    public CrmService(ContactRepository contactRepository,
                      CompanyRepository companyRepository,
                      StatusRepository statusRepository,
                      PushSubscriptionRepository pushSubscriptionRepository) {
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
        this.statusRepository = statusRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    public List<Contact> findAllContacts(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return contactRepository.findAll();
        } else {
            return contactRepository.search(stringFilter);
        }
    }

    public long countContacts() {
        return contactRepository.count();
    }

    public void deleteContact(Contact contact) {
        contactRepository.delete(contact);
    }

    public void saveContact(Contact contact) {
        if (contact == null) {
            System.err.println("Contact is null. Are you sure you have connected your form to the application?");
            return;
        }
        contactRepository.save(contact);
    }

    public List<Company> findAllCompanies() {
        return companyRepository.findAll();
    }

    public List<Status> findAllStatuses(){
        return statusRepository.findAll();
    }

    public WebPush getWebPush() {
        if(webPush == null) {
            webPush = new WebPush(publicKey, privateKey, subject);
        }
        return webPush;
    }

    public boolean hasMultipleSubscriptions(String userName) {
        List<PushSubscriptionEntity> all = pushSubscriptionRepository.findAll();
        Optional<PushSubscriptionEntity> first = all.stream().filter(sub -> sub.getUserName().equals(userName)).findFirst();
        if(first.isPresent()) {
            return all.stream().filter(sub -> subscriptionsEqual(sub, first.get()) && !sub.getUserName().equals(userName)).count() > 0;
        }
        return false;
    }

    private boolean subscriptionsEqual(PushSubscriptionEntity sub, PushSubscriptionEntity pushSubscriptionEntity) {
        return sub.getEndpoint().equals(pushSubscriptionEntity.getEndpoint()) &&
               sub.getAuth().equals(pushSubscriptionEntity.getAuth()) &&
               sub.getP256dh().equals(pushSubscriptionEntity.getP256dh());
    }

    public WebPushSubscription getSubscription(String userName) {
        PushSubscriptionEntity pushSubscriptionEntity = getPushSubscription(userName);
        if(pushSubscriptionEntity != null) {
            return new WebPushSubscription(pushSubscriptionEntity.getEndpoint(), new WebPushKeys(pushSubscriptionEntity.getP256dh(), pushSubscriptionEntity.getAuth()));
        }
        return null;
    }

    public void removeSubscription(String userName) {
        PushSubscriptionEntity pushSubscriptionEntity = getPushSubscription(userName);
        if(pushSubscriptionEntity != null) {
            pushSubscriptionRepository.delete(pushSubscriptionEntity);
        }
    }

    public void removeSubscription(String userName, WebPushSubscription subscription) {
        if(subscription == null) {
            LoggerFactory.getLogger(CrmService.class).info("Removing user subscription without client match.");
            removeSubscription(userName);
            return;
        }
        Optional<PushSubscriptionEntity> pushSubscription = getPushSubscriptions(userName).stream().filter(sub -> sub.equalsSubscription(subscription)).findFirst();
        if(pushSubscription.isPresent()) {
            pushSubscriptionRepository.delete(pushSubscription.get());
        }
    }

    public List<PushSubscriptionEntity> getPushSubscriptions(String userName) {
        return pushSubscriptionRepository.findPushSubscriptionByUserName(userName);
    }

    private PushSubscriptionEntity getPushSubscription(String userName) {
        Optional<PushSubscriptionEntity> subscription = getPushSubscriptions(userName).stream().findFirst();
        if(subscription.isPresent()) {
            return subscription.get();
        }
        return null;
    }

    public void addSubscription(String userName, WebPushSubscription subscription) {
        PushSubscriptionEntity existingSubscription = getPushSubscription(userName);
        if (existingSubscription != null
                && existingSubscription.equalsSubscription(subscription)) {
            // Do not add a subscription if one already in db for user.
            return;
        }
        pushSubscriptionRepository.save(new PushSubscriptionEntity(userName,
                subscription.endpoint(), subscription.keys().p256dh(), subscription.keys().auth()));
        getPushSubscription(userName);
    }

    public List<PushSubscriptionEntity> getAllSubscriptions() {
        return pushSubscriptionRepository.findAll();
    }
}
