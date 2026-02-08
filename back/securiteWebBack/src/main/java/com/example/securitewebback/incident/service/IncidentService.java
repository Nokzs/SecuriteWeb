package com.example.securitewebback.incident.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.securitewebback.incident.dto.*;
import com.example.securitewebback.user.repository.ProprietaireRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.incident.entity.Incident;
import com.example.securitewebback.incident.entity.IncidentPhoto;
import com.example.securitewebback.incident.entity.IncidentStatus;
import com.example.securitewebback.incident.entity.Vote;
import com.example.securitewebback.incident.entity.VoteChoice;
import com.example.securitewebback.incident.entity.VoteStatus;
import com.example.securitewebback.incident.entity.VoteSubmission;
import com.example.securitewebback.incident.repository.IncidentRepository;
import com.example.securitewebback.incident.repository.VoteRepository;
import com.example.securitewebback.incident.repository.VoteSubmissionRepository;
import com.example.securitewebback.storage.MinioService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final ApartmentRepository apartmentRepository;
    private final VoteRepository voteRepository;
    private final VoteSubmissionRepository voteSubmissionRepository; // Pour enregistrer le vote
    private final ProprietaireRepository proprietaireRepository; // Pour récupérer le votant
    private final MinioService minioService;

    public IncidentService(IncidentRepository incidentRepository,
                           ApartmentRepository apartmentRepository,
                           VoteRepository voteRepository,
                           VoteSubmissionRepository voteSubmissionRepository,
                           ProprietaireRepository proprietaireRepository,
                           MinioService minioService) {
        this.incidentRepository = incidentRepository;
        this.apartmentRepository = apartmentRepository;
        this.voteRepository = voteRepository;
        this.voteSubmissionRepository = voteSubmissionRepository;
        this.proprietaireRepository = proprietaireRepository;
        this.minioService = minioService;
    }

    // --- PROPRIÉTAIRE : Création Incident ---
    @Transactional
    public IncidentDto createIncident(CreateIncidentDto dto, Proprietaire reporter) {
        Apartment apartment = apartmentRepository.findById(dto.apartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Logement non trouvé"));

        // On récupère le bucket de l'immeuble dès maintenant
        String buildingBucket = apartment.getBuilding().getId().toString();

        Incident incident = dto.toEntity(apartment, reporter);
        incident.setStatus(IncidentStatus.PENDING);
        incidentRepository.save(incident);

        List<String> photoUrls = List.of();
        if (dto.photoFilenames() != null && !dto.photoFilenames().isEmpty()) {
            photoUrls = dto.photoFilenames().stream()
                    .map(filename -> {
                        try {
                            String id = UUID.randomUUID().toString();
                            // Nettoyage du nom de fichier (pour éviter les espaces/accents qui cassent l'URL)
                            String cleanName = filename.replaceAll("[^a-zA-Z0-9.]", "_");
                            String finalFilename = cleanName + "-" + id;

                            String objectPath = incident.getId().toString() + "/" + finalFilename;

                            IncidentPhoto photo = new IncidentPhoto();
                            photo.setFilename(finalFilename);
                            photo.setUrl(objectPath); // On stocke le chemin relatif
                            incident.addPhoto(photo);

                            return minioService.generatePresignedUploadUrl(buildingBucket, objectPath);

                        } catch (Exception e) {
                            log.error("Erreur génération lien upload", e);
                            return null;
                        }
                    })
                    .filter(url -> url != null)
                    .collect(Collectors.toList());
        }
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

    // --- PROPRIÉTAIRE : Votant ---
    @Transactional
    public void castVote(UUID voteId, VoteChoice choice, UUID voterId) {
        // 1. Récupérer le vote
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new EntityNotFoundException("Vote introuvable"));

        // 2. Vérifier si le vote est ouvert
        if (vote.getStatus() != VoteStatus.ONGOING || vote.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Ce vote est clos.");
        }

        // 3. Vérifier si le propriétaire a déjà voté
        if (voteSubmissionRepository.existsByVoteIdAndVoterId(voteId, voterId)) {
            throw new IllegalStateException("Vous avez déjà voté pour cette résolution.");
        }

        // 4. CALCULER LES TANTIÈMES (POIDS DU VOTE)
        // On récupère l'ID de l'immeuble concerné par l'incident
        UUID buildingId = vote.getIncident().getApartment().getBuilding().getId();

        // On somme les tantièmes de TOUS les appartements que possède ce propriétaire dans cet immeuble
        Integer totalTantiemes = apartmentRepository.sumTantiemesByOwnerAndBuilding(voterId, buildingId);

        if (totalTantiemes == null || totalTantiemes == 0) {
            throw new SecurityException("Vous ne possédez pas de lots (tantièmes) dans cet immeuble.");
        }

        // 5. Enregistrer la soumission
        Proprietaire voter = proprietaireRepository.getReferenceById(voterId);

        VoteSubmission submission = new VoteSubmission();
        submission.setVote(vote);
        submission.setVoter(voter);
        submission.setChoice(choice);
        submission.setTantiemesWeight(totalTantiemes);

        voteSubmissionRepository.save(submission);
    }

    public List<OwnerIncidentDto> getIncidentsForOwner(UUID ownerId) {

        // 1. Trouver les immeubles du propriétaire
        // On récupère ses appartements
        List<Apartment> ownerApartments = apartmentRepository.findByOwnerId(ownerId);

        // On extrait les IDs des immeubles (sans doublons)
        List<UUID> buildingIds = ownerApartments.stream()
                .map(appart -> appart.getBuilding().getId())
                .distinct()
                .collect(Collectors.toList());

        // Sécurité : Si le proprio n'a pas d'appart, liste vide
        if (buildingIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Récupérer TOUS les incidents de ces immeubles (pas juste les siens)
        List<Incident> incidents = incidentRepository.findAllByBuildingIds(buildingIds);

        // 3. Mapper vers le DTO
        return incidents.stream().map(incident -> {

            String voteId = null;
            Double voteAmount = null;
            LocalDateTime voteEndDate = null;
            boolean hasVoted = false;

            // Sécurité Status
            IncidentStatus status = incident.getStatus();
            if (status == null) status = IncidentStatus.PENDING;

            if (status == IncidentStatus.VOTED || status == IncidentStatus.PENDING || status == IncidentStatus.IGNORED) {
                // On cherche le vote (même s'il est fini, pour voir le résultat)
                // Note : on retire le filtre "ONGOING" strict si on veut voir l'historique,
                // mais pour voter, il faut que ce soit ONGOING.
                // Pour l'affichage simple, on prend le dernier vote créé.
                var lastVoteOpt = voteRepository.findFirstByIncidentIdAndStatusOrderByCreatedAtDesc(
                        incident.getId(),
                        VoteStatus.ONGOING // Pour l'instant on garde ONGOING pour le bouton de vote
                );

                // Astuce : Si tu veux afficher aussi les votes finis, il faudrait changer cette requête
                // mais gardons ça simple : on affiche le bouton de vote seulement si ONGOING.

                if (lastVoteOpt.isPresent()) {
                    Vote v = lastVoteOpt.get();
                    voteId = v.getId().toString();
                    voteAmount = v.getAmount();
                    voteEndDate = v.getEndDate();

                    hasVoted = voteSubmissionRepository.existsByVoteIdAndVoterId(v.getId(), ownerId);
                }
            }

            // Sécurité Date
            LocalDateTime safeDate;
            if (incident.getCreatedAt() != null) {
                safeDate = LocalDateTime.ofInstant(incident.getCreatedAt(), ZoneId.systemDefault());
            } else {
                safeDate = LocalDateTime.now();
            }

            List<String> signedUrls = incident.getPhotos().stream()
                    .map(photo -> {
                        try {
                            // Bucket = Building ID | Path = incidentID/filename
                            String bucket = incident.getApartment().getBuilding().getId().toString();
                            return minioService.presignedDownloadUrl(bucket, photo.getFilename());
                        } catch (Exception e) { return null; }
                    })
                    .filter(url -> url != null)
                    .collect(Collectors.toList());

            return new OwnerIncidentDto(
                    incident.getId().toString(),
                    incident.getTitle(),
                    status.name(),
                    safeDate,
                    voteId, voteAmount, voteEndDate, hasVoted,
                    signedUrls
            );
        }).collect(Collectors.toList());
    }



    // --- SYNDIC ---
    public Page<IncidentSyndicDto> getIncidentsForSyndic(UUID syndicId, Pageable pageable, String sortBy, IncidentStatus status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortBy != null) {
            switch (sortBy) {
                case "urgent" -> sort = Sort.by(Sort.Direction.DESC, "isUrgent").and(Sort.by(Sort.Direction.DESC, "createdAt"));
                case "building" -> sort = Sort.by(Sort.Direction.ASC, "apartment.building.name").and(Sort.by(Sort.Direction.DESC, "createdAt"));
                case "owner" -> sort = Sort.by(Sort.Direction.ASC, "reporter.nom").and(Sort.by(Sort.Direction.DESC, "createdAt"));
                default -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
            }
        }
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Specification<Incident> spec = (root, query, cb) -> {
            var apartmentJoin = root.join("apartment");
            var buildingJoin = apartmentJoin.join("building");
            var syndicPredicate = cb.equal(buildingJoin.get("syndic").get("id"), syndicId);
            if (status != null) {
                return cb.and(syndicPredicate, cb.equal(root.get("status"), status));
            }
            return syndicPredicate;
        };

        return incidentRepository.findAll(spec, sortedPageable).map(incident -> {

            List<String> signedUrls = incident.getPhotos().stream()
                    .map(photo -> {
                        try {
                            // Bucket = Building ID
                            String bucket = incident.getApartment().getBuilding().getId().toString();
                            // Path = incidentID / filename (selon ton code de création)
                            String objectPath = incident.getId().toString() + "/" + photo.getFilename();

                            return minioService.presignedDownloadUrl(bucket, objectPath);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            // On passe l'entité ET les liens au DTO
            return IncidentSyndicDto.fromEntity(incident, signedUrls);
        });
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
        if (incident.getStatus() == IncidentStatus.IGNORED) {
            throw new IllegalStateException("Impossible de créer un vote pour un incident ignoré");
        }
        Vote vote = new Vote();
        vote.setAmount(voteDto.amount());
        vote.setEndDate(voteDto.endDate());
        vote.setIncident(incident);
        vote.setCreatedBy(incident.getApartment().getBuilding().getSyndic());
        vote.setStatus(VoteStatus.ONGOING);
        voteRepository.save(vote);

        incident.setStatus(IncidentStatus.VOTED);
        incidentRepository.save(incident);
    }

    private Incident getIncidentChecked(UUID incidentId, UUID syndicId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident non trouvé"));
        UUID realSyndicId = incident.getApartment().getBuilding().getSyndic().getId();
        if (!realSyndicId.equals(syndicId)) {
            throw new SecurityException("Accès refusé.");
        }
        return incident;
    }

    public List<VoteSummaryDto> getVotesForSyndic(UUID syndicId) {
        List<Vote> votes = voteRepository.findAllBySyndicId(syndicId);

        return votes.stream().map(vote -> {
            // 1. Récupérer toutes les soumissions pour ce vote
            List<VoteSubmission> submissions = voteSubmissionRepository.findByVoteId(vote.getId());

            // 2. Calculer les sommes
            int sumFor = submissions.stream()
                    .filter(s -> s.getChoice() == VoteChoice.FOR)
                    .mapToInt(VoteSubmission::getTantiemesWeight).sum();

            int sumAgainst = submissions.stream()
                    .filter(s -> s.getChoice() == VoteChoice.AGAINST)
                    .mapToInt(VoteSubmission::getTantiemesWeight).sum();

            int sumAbstain = submissions.stream()
                    .filter(s -> s.getChoice() == VoteChoice.ABSTAIN)
                    .mapToInt(VoteSubmission::getTantiemesWeight).sum();

            int participation = sumFor + sumAgainst + sumAbstain;

            // 3. Récupérer le total de l'immeuble
            int totalBuilding = vote.getIncident().getApartment().getBuilding().getTotalTantieme();

            return new VoteSummaryDto(
                    vote.getId().toString(),
                    vote.getIncident().getTitle(),
                    vote.getStatus().name(),
                    vote.getEndDate(),
                    totalBuilding,
                    participation,
                    sumFor,
                    sumAgainst,
                    sumAbstain
            );
        }).collect(Collectors.toList());
    }

    public void closeVoteManually(UUID voteId) {
        // 1. Récupérer le vote
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote non trouvé"));

        // 2. Vérifier si déjà fermé
        if (vote.getStatus() != VoteStatus.ONGOING) {
            throw new RuntimeException("Ce vote est déjà clôturé");
        }

        // 3. Compter les points
        List<VoteSubmission> submissions = voteSubmissionRepository.findByVoteId(voteId);

        int forVotes = submissions.stream()
                .filter(s -> s.getChoice() == VoteChoice.FOR)
                .mapToInt(VoteSubmission::getTantiemesWeight).sum();

        int againstVotes = submissions.stream()
                .filter(s -> s.getChoice() == VoteChoice.AGAINST)
                .mapToInt(VoteSubmission::getTantiemesWeight).sum();

        // 4. Décision (Majorité simple)
        if (forVotes > againstVotes) {
            vote.setStatus(VoteStatus.PASSED);
            // Si accepté : L'incident est validé, on passe en "En cours de résolution"
            // Si tu n'as pas IN_PROGRESS, utilise RESOLVED ou crée le statut dans l'Enum
            vote.getIncident().setStatus(IncidentStatus.IN_PROGRESS);
        } else {
            vote.setStatus(VoteStatus.REJECTED);
            // Si refusé : L'incident est clos (On ne fera rien)
            vote.getIncident().setStatus(IncidentStatus.RESOLVED);
        }

        // 5. Date de fin et Sauvegarde
        vote.setEndDate(LocalDateTime.now());
        voteRepository.save(vote);
        incidentRepository.save(vote.getIncident());
    }
}