package com.example.securitewebback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.appartements.service.ApartmentService;
import com.example.securitewebback.auth.entity.Syndic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.securitewebback.appartements.dto.ApartementAndBuildingDto;
import com.example.securitewebback.appartements.dto.ApartementDto;
import com.example.securitewebback.appartements.dto.CreateAppartementDto;
import com.example.securitewebback.appartements.dto.UpdateApartmentDto;
import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.storage.MinioService;
import com.example.securitewebback.user.repository.ProprietaireRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ApartmentServiceTest {

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private ProprietaireRepository proprietaireRepository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private ApartmentService apartmentService;

    // --- Tests createApartment ---

    @Test
    @DisplayName("Doit créer un appartement avec succès et mettre à jour les tantièmes du bâtiment")
    void createApartment_Success() {
        // Arrange
        UUID buildingId = UUID.randomUUID();
        Building building = new Building();
        building.setId(buildingId);
        building.setTotalTantieme(1000);
        building.setCurrentTantieme(500);

        CreateAppartementDto dto = new CreateAppartementDto(
                "A101", 1, 50.0, 2, null, 100, buildingId, "owner@test.com"
        );

        Proprietaire owner = new Proprietaire();
        owner.setEmail("owner@test.com");
        owner.setAppartements(new ArrayList<>());

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
        when(proprietaireRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        when(apartmentRepository.save(any(Apartment.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Apartment result = apartmentService.createApartment(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNumero()).isEqualTo("A101");
        assertThat(result.getTantiemes()).isEqualTo(100);
        assertThat(building.getCurrentTantieme()).isEqualTo(600);
    }

    @Test
    @DisplayName("Doit échouer si le quota de tantièmes est dépassé")
    void createApartment_QuotaExceeded() {
        // Arrange
        UUID buildingId = UUID.randomUUID();
        Building building = new Building();
        building.setTotalTantieme(1000);
        building.setCurrentTantieme(950);

        CreateAppartementDto dto = new CreateAppartementDto(
                "A102", 1, 50.0, 2, null, 100, buildingId, null
        );

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));

        // Act & Assert
        assertThatThrownBy(() -> apartmentService.createApartment(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Quota dépassé");

        verify(apartmentRepository, never()).save(any());
    }

    // --- Tests getApartmentsBySyndicId ---

    @Test
    @DisplayName("Doit retourner la page d'appartements pour le syndic avec URL signée")
    void getApartmentsBySyndicId_Success() {
        // Arrange
        UUID buildingId = UUID.randomUUID();
        UUID aptId = UUID.randomUUID();
        String search = "";
        Pageable pageable = PageRequest.of(0, 10);

        // Création du Syndic pour éviter le NullPointer dans le DTO
        Syndic syndic = new Syndic();
        syndic.setId(UUID.randomUUID());

        Building building = new Building();
        building.setId(buildingId);
        building.setName("Résidence Test");
        // 👇 CORRECTION ICI : On attache un syndic au bâtiment
        building.setSyndic(syndic);

        Apartment apt = new Apartment();
        apt.setId(aptId);
        apt.setNumero("B202");
        apt.setPhotoFilename("photo.jpg");
        apt.setBuilding(building);
        apt.setTantiemes(100);

        Page<Apartment> page = new PageImpl<>(List.of(apt));

        when(apartmentRepository.findByBuildingIdAndNumeroContaining(buildingId, search, pageable))
                .thenReturn(page);
        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));

        String expectedObjectName = buildingId.toString() + "/" + aptId.toString() + "/photo.jpg";
        when(minioService.presignedDownloadUrl(expectedObjectName)).thenReturn("http://minio/signed-url");

        // Act
        ApartementAndBuildingDto result = apartmentService.getApartmentsBySyndicId(buildingId, pageable, search);

        // Assert
        assertThat(result.building().name()).isEqualTo("Résidence Test");
        assertThat(result.appartement().getContent()).hasSize(1);
        assertThat(result.appartement().getContent().get(0).signedLink()).isEqualTo("http://minio/signed-url");
    }

    // --- Tests getApartmentsByOwnerId ---

    @Test
    @DisplayName("Doit retourner les appartements du propriétaire avec URL signée (méthode 2 arguments)")
    void getApartmentsByOwnerId_Success() {
        UUID ownerId = UUID.randomUUID();
        UUID aptId = UUID.randomUUID();
        UUID buildingId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 5);

        Building building = new Building();
        building.setId(buildingId);

        Apartment apt = new Apartment();
        apt.setId(aptId);
        apt.setBuilding(building);
        apt.setPhotoFilename("my-photo.png");
        apt.setTantiemes(50);

        Page<Apartment> page = new PageImpl<>(List.of(apt));

        when(apartmentRepository.findByOwnerId(ownerId, pageable)).thenReturn(page);

        String expectedBucket = buildingId.toString();
        String expectedPath = aptId.toString() + "/my-photo.png";

        when(minioService.presignedDownloadUrl(expectedBucket, expectedPath))
                .thenReturn("http://minio/secure-link");

        Page<ApartementDto> result = apartmentService.getApartmentsByOwnerId(ownerId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).signedLink()).isEqualTo("http://minio/secure-link");
        verify(minioService).presignedDownloadUrl(expectedBucket, expectedPath);
    }

    // --- Tests update ---

    @Test
    @DisplayName("Doit mettre à jour un appartement et recalculer les tantièmes globaux")
    void update_Success_WithTantiemesChange() {
        // Arrange
        UUID aptId = UUID.randomUUID();
        Building building = new Building();
        building.setTotalTantieme(1000);
        building.setCurrentTantieme(500);

        Apartment apt = new Apartment();
        apt.setId(aptId);
        apt.setTantiemes(100);
        apt.setBuilding(building);
        apt.setNumero("OLD");

        UpdateApartmentDto dto = new UpdateApartmentDto(
                "NEW", 60.0, 3, 200, null, false, null
        );

        when(apartmentRepository.findById(aptId)).thenReturn(Optional.of(apt));
        when(apartmentRepository.save(any(Apartment.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Apartment updated = apartmentService.update(aptId, dto);

        // Assert
        assertThat(updated.getNumero()).isEqualTo("NEW");
        assertThat(updated.getTantiemes()).isEqualTo(200);
        // Ancien total (500) - Ancien apt (100) + Nouveau apt (200) = 600
        assertThat(building.getCurrentTantieme()).isEqualTo(600);
    }

    @Test
    @DisplayName("Doit supprimer l'ancienne photo si deletePhoto est true")
    void update_DeletePhoto() {
        // Arrange
        UUID aptId = UUID.randomUUID();
        Building building = new Building();
        building.setId(UUID.randomUUID());
        building.setTotalTantieme(1000);
        building.setCurrentTantieme(100);

        Apartment apt = new Apartment();
        apt.setId(aptId);
        apt.setTantiemes(50);
        apt.setBuilding(building);
        apt.setPhotoFilename("old-photo.jpg");

        UpdateApartmentDto dto = new UpdateApartmentDto(
                "101", 30.0, 1, 50, null, true, null // deletePhoto = true
        );

        when(apartmentRepository.findById(aptId)).thenReturn(Optional.of(apt));
        when(apartmentRepository.save(any(Apartment.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Apartment updated = apartmentService.update(aptId, dto);

        // Assert
        assertThat(updated.getPhotoFilename()).isNull();
        verify(minioService, times(1)).remove("old-photo.jpg");
    }

    @Test
    @DisplayName("Doit changer le propriétaire")
    void update_ChangeOwner() {
        // Arrange
        UUID aptId = UUID.randomUUID();
        Building building = new Building();
        building.setTotalTantieme(1000);

        Proprietaire oldOwner = new Proprietaire();
        oldOwner.setEmail("old@test.com");
        // 👇 CORRECTION ICI : Initialiser la liste
        oldOwner.setAppartements(new ArrayList<>());

        Proprietaire newOwner = new Proprietaire();
        newOwner.setEmail("new@test.com");
        // 👇 CORRECTION ICI : Initialiser la liste aussi
        newOwner.setAppartements(new ArrayList<>());

        Apartment apt = new Apartment();
        apt.setId(aptId);
        apt.setTantiemes(10);
        apt.setBuilding(building);

        // On simule que l'appart est déjà dans la liste de l'ancien proprio
        apt.setOwner(oldOwner);
        oldOwner.addApartement(apt);

        UpdateApartmentDto dto = new UpdateApartmentDto(
                "101", 30.0, 1, 10, "new@test.com", false, null
        );

        when(apartmentRepository.findById(aptId)).thenReturn(Optional.of(apt));
        when(proprietaireRepository.findByEmail("new@test.com")).thenReturn(Optional.of(newOwner));
        when(apartmentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        apartmentService.update(aptId, dto);

        // Assert
        verify(proprietaireRepository).findByEmail("new@test.com");
        // Vérification bonus : l'ancien ne doit plus l'avoir, le nouveau si
        assertThat(oldOwner.getAppartements()).doesNotContain(apt);
        assertThat(newOwner.getAppartements()).contains(apt);
    }

    @Test
    @DisplayName("Doit lever une exception si l'appartement à mettre à jour n'existe pas")
    void update_NotFound() {
        UUID id = UUID.randomUUID();
        UpdateApartmentDto dto = new UpdateApartmentDto(
                "101", 30.0, 1, 50, null, false, null
        );

        when(apartmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apartmentService.update(id, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }
}