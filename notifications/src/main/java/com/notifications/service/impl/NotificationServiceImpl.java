package com.notifications.service.impl;

import com.notifications.dto.CreateNotificationRequest;
import com.notifications.dto.NotificationEvent;
import com.notifications.entity.Notification;
import com.notifications.exception.ResourceNotFoundException;
import com.notifications.repository.NotificationRepository;
import com.notifications.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public Notification findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Override
    public Notification create(CreateNotificationRequest request) {
        Notification notification = new Notification(request.getRecipient(), request.getMessage());
        return repository.save(notification);
    }

    @Override
    public Notification createFromEvent(NotificationEvent event) {
        Notification notification = new Notification(event.getRecipient(), event.getMessage());
        return repository.save(notification);
    }

    @Override
    public Notification markAsRead(Long id) {
        Notification notification = findById(id);
        notification.setRead(true);
        return repository.save(notification);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
