package com.example.application.services;

import com.example.application.data.entity.Company;
import com.example.application.data.entity.Contact;
import com.example.application.data.entity.PushSubscription;
import com.example.application.data.entity.Status;
import com.example.application.data.repository.CompanyRepository;
import com.example.application.data.repository.ContactRepository;
import com.example.application.data.repository.PushSubscriptionRepository;
import com.example.application.data.repository.StatusRepository;
import nl.martijndwars.webpush.Subscription;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.server.webpush.WebPush;

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
        List<PushSubscription> all = pushSubscriptionRepository.findAll();
        Optional<PushSubscription> first = all.stream().filter(sub -> sub.getUserName().equals(userName)).findFirst();
        if(first.isPresent()) {
            return all.stream().filter(sub -> subscriptionsEqual(sub, first.get()) && !sub.getUserName().equals(userName)).count() > 0;
        }
        return false;
    }

    private boolean subscriptionsEqual(PushSubscription sub, PushSubscription pushSubscription) {
        return sub.getEndpoint().equals(pushSubscription.getEndpoint()) &&
                sub.getAuth().equals(pushSubscription.getAuth()) &&
                sub.getP256dh().equals(pushSubscription.getP256dh());
    }

    public Subscription getSubscription(String userName) {
        PushSubscription pushSubscription = getPushSubscription(userName);
        if(pushSubscription != null) {
            return new Subscription(pushSubscription.getEndpoint(), new Subscription.Keys(pushSubscription.getP256dh(), pushSubscription.getAuth()));
        }
        return null;
    }

    public void removeSubscription(String userName) {
        PushSubscription pushSubscription = getPushSubscription(userName);
        if(pushSubscription != null) {
            pushSubscriptionRepository.delete(pushSubscription);
        }
    }

    public void removeSubscription(String userName, Subscription subscription) {
        if(subscription == null) {
            LoggerFactory.getLogger(CrmService.class).info("Removing user subscription without client match.");
            removeSubscription(userName);
            return;
        }
        Optional<PushSubscription> pushSubscription = getPushSubscriptions(userName).stream().filter(sub -> sub.equalsSubscription(subscription)).findFirst();
        if(pushSubscription.isPresent()) {
            pushSubscriptionRepository.delete(pushSubscription.get());
        }
    }

    public List<PushSubscription> getPushSubscriptions(String userName) {
        return pushSubscriptionRepository.findPushSubscriptionByUserName(userName);
    }

    private PushSubscription getPushSubscription(String userName) {
        Optional<PushSubscription> subscription = getPushSubscriptions(userName).stream().findFirst();
        if(subscription.isPresent()) {
            return subscription.get();
        }
        return null;
    }

    public void addSubscription(String userName, Subscription subscription) {
        PushSubscription existingSubscription = getPushSubscription(userName);
        if (existingSubscription != null
                && existingSubscription.equalsSubscription(subscription)) {
            // Do not add a subscription if one already in db for user.
            return;
        }
        pushSubscriptionRepository.save(new PushSubscription(userName, subscription.endpoint, subscription.keys.p256dh, subscription.keys.auth));
        getPushSubscription(userName);
    }

    public List<PushSubscription> getAllSubscriptions() {
        return pushSubscriptionRepository.findAll();
    }
}
