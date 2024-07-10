package com.eventostec.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    private EventRepository eventRepository;
    private AddressService addressService;

    public EventService(EventRepository eventRepository, AddressService addressService){
        this.eventRepository = eventRepository;
        this.addressService = addressService;
    }

    public Event createEvent(EventRequestDTO data){
        String imgUrl = null;

        if(data.image() != null){
            imgUrl = this.uploadImg(data.image());
        }

        Event newEvent = new Event();
        newEvent.setTitle(data.title());
        newEvent.setDescription(data.description());
        newEvent.setEventUrl(data.eventUrl());
        newEvent.setDate(new Date(data.date()));
        newEvent.setImgUrl(imgUrl);
        newEvent.setRemote(data.remote());

        eventRepository.save(newEvent);

        if(!data.remote()){
            this.addressService.createAddress(data, newEvent);
        }

        return newEvent;

    }

    public List<EventResponseDTO> getUpComingEvents(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = this.eventRepository.findUpComingEvents(new Date(), pageable);
        return eventsPage.map(event -> new EventResponseDTO(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDate(),
                        event.getAddress() != null ? event.getAddress().getCity() : "",
                        event.getAddress() != null ? event.getAddress().getUf() : "",
                        event.getRemote(),
                        event.getEventUrl(),
                        event.getImgUrl()))
                .stream()
                .toList();
    }

    public List<EventResponseDTO> getFilteredEvents(int page, int size, String title, String city, String uf, Date startDate, Date endDate){
        Pageable pageable = PageRequest.of(page, size);

        title = (title != null) ? title : "";
        city = (city != null) ? city : "";
        uf = (uf != null) ? uf : "";
        startDate  = (startDate != null) ? startDate : new Date(0);
        endDate = (endDate != null) ? endDate : new Date();

        Pageable pageable1 = PageRequest.of(page,size);

        Page<Event> eventsPage = this.eventRepository.findFilteredEvents(   title, city, uf, startDate, endDate, pageable);
        return eventsPage.map(event -> new EventResponseDTO(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getDate(),
                        event.getAddress() != null ? event.getAddress().getCity() : "",
                        event.getAddress() != null ? event.getAddress().getUf() : "",
                        event.getRemote(),
                        event.getEventUrl(),
                        event.getImgUrl()))
                .stream()
                .toList();
    }

    private String uploadImg(MultipartFile multipartFile){
        String filename = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

        try {
            File file = this.converMultipartToFile(multipartFile);
            s3Client.putObject(bucketName, filename, file);
            file.delete();
            return s3Client.getUrl(bucketName, filename).toString();
        } catch(Exception e){
            System.out.println("Erro ao subir arquivo" + e);
            return "";
        }

    }

    private File converMultipartToFile(MultipartFile multipartFile) throws IOException {
        File convFile = new File(multipartFile.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(multipartFile.getBytes());
        fos.close();
        return convFile;
    }
}
