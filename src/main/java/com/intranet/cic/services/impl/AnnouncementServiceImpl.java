package com.intranet.cic.services.impl;

import com.intranet.cic.dtos.AnnouncementDTO;
import com.intranet.cic.entities.Announcement;
import com.intranet.cic.execeptions.IntranetException;
import com.intranet.cic.repositories.AnnouncementRepository;
import com.intranet.cic.services.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Announcement> getAllAnnouncements() {
        try{
            return announcementRepository.findAll();
        } catch (Exception exception){
            log.error("Failed to get all announcements", exception);
            throw new IntranetException("Failed to get all announcements", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Announcement getAnnouncementById(Long id) {
        try{
            return announcementRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Announcement Not found", HttpStatus.NOT_FOUND)
                    );
        } catch (IntranetException intranetException) {

            log.warn("Announcement not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Announcement Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting Announcement", exception);
            throw new IntranetException("Failed to get Announcement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public Announcement createAnnouncement(AnnouncementDTO announcementDTO) {
        try{
            Announcement announcement = modelMapper.map(announcementDTO, Announcement.class);
            if (announcement.getIsRead() == null) {
                announcement.setIsRead(false);
            }
            return announcementRepository.save(announcement);
        } catch (Exception exception){
            log.error("Failed to create Announcement", exception);
            throw new IntranetException("Failed to create Announcement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Announcement updateAnnouncement(Long id, AnnouncementDTO announcementDTO) {
        try{
            Announcement announcement = announcementRepository.findById(id)
                    .orElseThrow(() -> new IntranetException("Announcement Not found", HttpStatus.NOT_FOUND)
                    );

            modelMapper.map(announcementDTO, Announcement.class);

            if (announcement.getIsRead() == null) {
                announcement.setIsRead(false);
            }
            return announcementRepository.save(announcement);
        }  catch (IntranetException intranetException) {

            log.warn("Announcement not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Announcement Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error updating Announcement", exception);
            throw new IntranetException("Failed to update Announcement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public void deleteAnnouncement(Long id) {
        try{
            Announcement announcement = announcementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Announcement not found with id: " + id));

            announcementRepository.delete(announcement);

        } catch (IntranetException intranetException) {

            log.warn("Announcement not found with id: {} to fetch", id, intranetException);
            throw new IntranetException("Announcement Not found", HttpStatus.NOT_FOUND);

        } catch (Exception exception) {

            log.error("Error deleting Announcement", exception);
            throw new IntranetException("Failed to delete Announcement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Announcement markAsRead(Long id) {
       try{

           Announcement announcement = announcementRepository.findById(id)
                   .orElseThrow(() -> new RuntimeException("Announcement not found with id: " + id));
           announcement.setIsRead(true);
           return announcementRepository.save(announcement);

       } catch (IntranetException intranetException) {

           log.warn("Announcement not found with id: {} to fetch", id, intranetException);
           throw new IntranetException("Announcement Not found", HttpStatus.NOT_FOUND);

       } catch (Exception exception) {

           log.error("Error updating Announcement", exception);
           throw new IntranetException("Failed to update Announcement", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }
}
