package com.notifications.service;

import com.notifications.dto.CreateNotificationRequest;
import com.notifications.dto.NotificationEvent;
import com.notifications.model.Notification;
import com.notifications.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Notification> findAll(String recipient, Boolean read) {
        if (recipient != null && read != null) {
            return repository.findByRecipientAndRead(recipient, read);
        }
        if (recipient != null) {
            return repository.findByRecipient(recipient);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Notification findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    @Transactional
    public Notification create(CreateNotificationRequest request) {
        Notification notification = new Notification(request.getRecipient(), request.getMessage());
        return repository.save(notification);
    }

    @Transactional
    public Notification createFromEvent(NotificationEvent event) {
        Notification notification = new Notification(event.getRecipient(), event.getMessage());
        return repository.save(notification);
    }

    @Transactional
    public Notification markAsRead(Long id) {
        Notification notification = findById(id);
        notification.setRead(true);
        return repository.save(notification);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotificationNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
