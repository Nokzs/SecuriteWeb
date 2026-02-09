package com.example.securitewebback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.building.service.BuildingService;
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

import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.building.dto.BuildingDto;
import com.example.securitewebback.building.dto.CreateBuildingDto;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.storage.MinioService;
import com.example.securitewebback.user.repository.SyndicRepository;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private MinioService minioService;

    @Mock
    private SyndicRepository syndicRepository;

    @InjectMocks
    private BuildingService buildingService;

    // --- Tests getBuildingsBySyndicId ---

    @Test
    @DisplayName("Doit retourner une page de BuildingDto avec URL signée si photo présente")
    void getBuildingsBySyndicId_WithPhoto() throws Exception {
        // Arrange
        UUID syndicId = UUID.randomUUID();
        UUID buildingId = UUID.randomUUID();
        String search = "";
        Pageable pageable = PageRequest.of(0, 10);

        Syndic syndic = new Syndic();
        syndic.setId(syndicId);

        Building building = new Building();
        building.setId(buildingId);
        building.setName("Residence Test");
        building.setAdresse("1 rue du test");
        building.setPhotoFilename("facade.jpg");
        building.setSyndic(syndic);
        building.setTotalTantieme(1000);
        building.setCurrentTantieme(0);

        Page<Building> page = new PageImpl<>(List.of(building));

        when(buildingRepository.findByNameContainingIgnoreCaseAndSyndicId(search, syndicId, pageable))
                .thenReturn(page);

        // Simulation MinIO (Format ID/Filename selon ton code)
        String objectName = buildingId.toString() + "/facade.jpg";
        when(minioService.presignedDownloadUrl(objectName)).thenReturn("http://minio/signed-link");

        // Act
        Page<BuildingDto> result = buildingService.getBuildingsBySyndicId(syndicId, pageable, search);

        // Assert
        assertThat(result).isNotEmpty();
        BuildingDto dto = result.getContent().get(0);
        assertThat(dto.photoFilename()).isEqualTo("http://minio/signed-link"); // Le champ photoFilename du DTO contient le lien signé
        assertThat(dto.name()).isEqualTo("Residence Test");
    }

    @Test
    @DisplayName("Doit retourner une page de BuildingDto sans URL si pas de photo")
    void getBuildingsBySyndicId_NoPhoto() {
        // Arrange
        UUID syndicId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Syndic syndic = new Syndic();
        syndic.setId(syndicId);

        Building building = new Building();
        building.setId(UUID.randomUUID());
        building.setName("Residence No Photo");
        building.setAdresse("2 rue du test");
        building.setPhotoFilename(null);
        building.setSyndic(syndic);

        Page<Building> page = new PageImpl<>(List.of(building));

        when(buildingRepository.findByNameContainingIgnoreCaseAndSyndicId("", syndicId, pageable))
                .thenReturn(page);

        // Act
        Page<BuildingDto> result = buildingService.getBuildingsBySyndicId(syndicId, pageable, "");

        // Assert
        assertThat(result.getContent().get(0).photoFilename()).isNull();
        // Vérifie qu'on n'a pas appelé MinIO
        verify(minioService, never()).presignedDownloadUrl(any());
    }

    // --- Tests createBuilding ---

    @Test
    @DisplayName("Doit créer un bâtiment et l'associer au syndic")
    void createBuilding_Success() {
        // Arrange
        UUID syndicId = UUID.randomUUID();
        Syndic syndic = new Syndic();
        syndic.setId(syndicId);

        CreateBuildingDto dto = new CreateBuildingDto("New Res", "123 Rue", "photo.png");

        when(syndicRepository.findById(syndicId)).thenReturn(Optional.of(syndic));
        when(buildingRepository.save(any(Building.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Building created = buildingService.createBuilding(dto, syndicId);

        // Assert
        assertThat(created.getName()).isEqualTo("New Res");
        assertThat(created.getSyndic()).isEqualTo(syndic);
        assertThat(created.getPhotoFilename()).startsWith("photo.png-"); // Vérifie la génération de l'UUID
    }

    @Test
    @DisplayName("Doit échouer si le syndic n'existe pas")
    void createBuilding_SyndicNotFound() {
        // Arrange
        UUID syndicId = UUID.randomUUID();
        CreateBuildingDto dto = new CreateBuildingDto("Fail Res", "Rue", null);

        when(syndicRepository.findById(syndicId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> buildingService.createBuilding(dto, syndicId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Syndic non trouvé");

        verify(buildingRepository, never()).save(any());
    }
}