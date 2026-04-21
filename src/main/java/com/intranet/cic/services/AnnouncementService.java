package com.intranet.cic.services;

import com.intranet.cic.dtos.AnnouncementDTO;
import com.intranet.cic.entities.Announcement;

import java.util.List;

public interface AnnouncementService {

    Announcement getAnnouncementById(Long id);
    List<Announcement> getAllAnnouncements();
    Announcement createAnnouncement(AnnouncementDTO announcementDTO);
    Announcement updateAnnouncement(Long id, AnnouncementDTO announcementDTO);
    void deleteAnnouncement(Long id);
    Announcement markAsRead(Long id);
}
