package com.example.securitewebback.incident.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.securitewebback.incident.dto.CreateVoteDto;
import com.example.securitewebback.incident.dto.IncidentSyndicDto;
import com.example.securitewebback.incident.entity.*;
import com.example.securitewebback.incident.repository.VoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.incident.dto.CreateIncidentDto;
import com.example.securitewebback.incident.dto.IncidentDto;
import com.example.securitewebback.incident.repository.IncidentRepository;
import com.example.securitewebback.storage.MinioService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final ApartmentRepository apartmentRepository;
    private final VoteRepository voteRepository; // Ajout du repo des votes
    private final MinioService minioService;

    public IncidentService(IncidentRepository incidentRepository,
                           ApartmentRepository apartmentRepository,
                           VoteRepository voteRepository,
                           MinioService minioService) {
        this.incidentRepository = incidentRepository;
        this.apartmentRepository = apartmentRepository;
        this.voteRepository = voteRepository;
        this.minioService = minioService;
    }

    // --- PARTIE PROPRIÉTAIRE ---

    @Transactional
    public IncidentDto createIncident(CreateIncidentDto dto, Proprietaire reporter) {

        Apartment apartment = apartmentRepository.findById(dto.apartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Logement non trouvé"));

        Incident incident = dto.toEntity(apartment, reporter);
        // Par défaut le statut est PENDING (défini dans l'entité ou ici)
        incident.setStatus(IncidentStatus.PENDING);

        incidentRepository.save(incident);

        // Générer les URLs pour les photos via MinIO
        List<String> photoUrls = List.of();
        if (dto.photoFilenames() != null && !dto.photoFilenames().isEmpty()) {
            photoUrls = dto.photoFilenames().stream()
                    .filter(filename -> filename != null && !filename.isEmpty())
                    .map(filename -> {
                        try {
                            // On ajoute un UUID pour éviter les collisions de noms
                            String id = UUID.randomUUID().toString();
                            String finalFilename = filename + "-" + id; // ex: fuite.jpg-550e8400...

                            // Structure : incidentId/nomFichier
                            String objectName = incident.getId().toString() + "/" + finalFilename;

                            IncidentPhoto photo = new IncidentPhoto();
                            photo.setFilename(finalFilename); // On stocke le nom utilisé dans MinIO

                            String presignedUrl = minioService.generatePresignedUrl(objectName);

                            // Attention : l'entité stocke l'URL courte ou le chemin,
                            // mais le DTO renvoie l'URL signée complète pour l'upload immédiat
                            photo.setUrl(objectName);
                            incident.addPhoto(photo);

                            return presignedUrl;
                        } catch (Exception e) {
                            log.error("Erreur génération lien photo incident", e);
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
        }

        // Persister l'incident avec les photos liées
        incidentRepository.save(incident);

        return new IncidentDto(
                incident.getId().toString(),
                incident.getTitle(),
                incident.getDescription(),
                incident.isUrgent(),
                incident.getApartment().getId().toString(),
                incident.getReporter().getId().toString(),
                incident.getCreatedAt(),
                photoUrls
        );
    }

    // --- PARTIE SYNDIC ---

    public Page<IncidentSyndicDto> getIncidentsForSyndic(UUID syndicId, Pageable pageable, String sortBy, IncidentStatus status) {

        // 1. Gestion du tri (Sorting) selon la doc
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Par défaut: Récents

        if (sortBy != null) {
            switch (sortBy) {
                case "urgent":
                    // D'abord les urgents, puis par date
                    sort = Sort.by(Sort.Direction.DESC, "isUrgent")
                            .and(Sort.by(Sort.Direction.DESC, "createdAt"));
                    break;
                case "building":
                    // Par nom d'immeuble, puis par date
                    sort = Sort.by(Sort.Direction.ASC, "apartment.building.name")
                            .and(Sort.by(Sort.Direction.DESC, "createdAt"));
                    break;
                case "owner":
                    // Par nom de famille propriétaire, puis par date
                    sort = Sort.by(Sort.Direction.ASC, "reporter.nom")
                            .and(Sort.by(Sort.Direction.DESC, "createdAt"));
                    break;
                case "recent":
                default:
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }

        // On recrée un Pageable avec le bon tri
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // 2. Création de la Specification (Filtres SQL dynamiques)
        Specification<Incident> spec = (root, query, cb) -> {
            // Jointures pour atteindre le syndic : Incident -> Apartment -> Building -> Syndic
            var apartmentJoin = root.join("apartment");
            var buildingJoin = apartmentJoin.join("building");

            // Filtre 1 : Seulement les immeubles de ce syndic
            var syndicPredicate = cb.equal(buildingJoin.get("syndic").get("id"), syndicId);

            // Filtre 2 : Statut (optionnel)
            if (status != null) {
                return cb.and(syndicPredicate, cb.equal(root.get("status"), status));
            }
            System.out.println(syndicPredicate);
            return syndicPredicate;
        };

        // 3. Récupération et Mapping
        Page<Incident> incidents = incidentRepository.findAll(spec, sortedPageable);

        return incidents.map(IncidentSyndicDto::fromEntity);
    }

    @Transactional
    public void ignoreIncident(UUID incidentId, UUID syndicId) {
        Incident incident = getIncidentChecked(incidentId, syndicId);
        incident.setStatus(IncidentStatus.IGNORED);
        incidentRepository.save(incident);
    }

    @Transactional
    public void createVote(UUID incidentId, CreateVoteDto voteDto, UUID syndicId) {
        Incident incident = getIncidentChecked(incidentId, syndicId);

        // Vérification métier : on ne peut voter que si c'est en attente (optionnel)
        if (incident.getStatus() == IncidentStatus.IGNORED) {
            throw new IllegalStateException("Impossible de créer un vote pour un incident ignoré");
        }

        // 1. Création du Vote
        Vote vote = new Vote();
        vote.setAmount(voteDto.amount());
        vote.setEndDate(voteDto.endDate());
        vote.setIncident(incident);
        // On récupère le syndic via l'incident (plus sûr) ou via le repo
        vote.setCreatedBy(incident.getApartment().getBuilding().getSyndic());
        vote.setStatus(VoteStatus.ONGOING);

        voteRepository.save(vote);

        // 2. Mise à jour de l'incident
        incident.setStatus(IncidentStatus.VOTED);
        incidentRepository.save(incident);
    }

    // --- PRIVATE HELPERS ---

    /**
     * Récupère un incident et vérifie qu'il appartient bien à un immeuble géré par le syndic connecté.
     */
    private Incident getIncidentChecked(UUID incidentId, UUID syndicId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident non trouvé"));

        UUID realSyndicId = incident.getApartment().getBuilding().getSyndic().getId();
        if (!realSyndicId.equals(syndicId)) {
            throw new SecurityException("Accès refusé : cet incident ne concerne pas vos immeubles.");
        }
        return incident;
    }
}