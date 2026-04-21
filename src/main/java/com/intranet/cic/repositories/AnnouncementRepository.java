package com.intranet.cic.repositories;

import com.intranet.cic.entities.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement,Long> {
}
