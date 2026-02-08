package com.example.securitewebback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.expense.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.expense.dto.CreateExpenseDto;
import com.example.securitewebback.expense.entity.Expense;
import com.example.securitewebback.expense.repository.ExpenseRepository;
import com.example.securitewebback.invoice.RefundInfo;
import com.example.securitewebback.invoice.service.InvoicesService;
import com.example.securitewebback.payement.PaymentService;
import com.example.securitewebback.user.repository.SyndicRepository;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private SyndicRepository syndicRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private InvoicesService invoicesService;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(
                expenseRepository,
                buildingRepository,
                syndicRepository,
                paymentService,
                paymentService,
                invoicesService
        );
    }

    @Test
    void getExpensesByBuildingId_Success() {
        UUID buildingId = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();
        Page<Expense> page = new PageImpl<>(List.of(new Expense()));

        when(expenseRepository.findByBuildingId(buildingId, pageable)).thenReturn(page);

        Page<Expense> result = expenseService.getExpensesByBuildingId(buildingId.toString(), pageable);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Doit créer une dépense liée au bâtiment et à son syndic")
    void createExpense_Success() {
        UUID buildingId = UUID.randomUUID();
        Syndic syndic = new Syndic();
        syndic.setId(UUID.randomUUID());

        Building building = new Building();
        building.setId(buildingId);
        building.setSyndic(syndic);

        // CreateExpenseDto utilise toujours BigDecimal selon ton code précédent
        CreateExpenseDto dto = new CreateExpenseDto("Réparation Ascenseur", BigDecimal.valueOf(1500));

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> i.getArguments()[0]);

        Expense result = expenseService.createExpense(buildingId, dto);

        assertThat(result.getBuilding()).isEqualTo(building);
        assertThat(result.getSyndic()).isEqualTo(syndic);
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1500));
    }

    @Test
    void createExpense_BuildingNotFound() {
        UUID buildingId = UUID.randomUUID();
        CreateExpenseDto dto = new CreateExpenseDto("Test", BigDecimal.TEN);

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(buildingId, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Building not found");
    }

    // --- cancelExpense ---

    @Test
    @DisplayName("Doit rembourser les factures concernées et finaliser l'annulation")
    void cancelExpense_Success() throws Exception {
        UUID expenseId = UUID.randomUUID();
        String token = "dummy-token";

        // ✅ CORRECTION : Utilisation de double (100.0) car RefundInfo prend un double
        RefundInfo refund1 = new RefundInfo("user1@test.com", 100.0, UUID.randomUUID());
        RefundInfo refund2 = new RefundInfo("user2@test.com", 50.0, UUID.randomUUID());

        when(invoicesService.getRefundInfos(expenseId)).thenReturn(List.of(refund1, refund2));

        // Act
        expenseService.cancelExpense(expenseId, token);

        // Assert
        // ✅ CORRECTION : On vérifie avec anyDouble()
        verify(paymentService, times(2)).transfertRequest(anyString(), anyDouble(), anyString(), eq(token));

        verify(invoicesService).finalizeCancellation(expenseId);
    }

    @Test
    @DisplayName("Doit continuer le processus même si un remboursement échoue (try-catch)")
    void cancelExpense_PaymentFailure_ShouldNotBlock() throws Exception {
        UUID expenseId = UUID.randomUUID();
        String token = "dummy-token";

        // ✅ CORRECTION : double ici aussi
        RefundInfo refund1 = new RefundInfo("error@test.com", 100.0, UUID.randomUUID());
        RefundInfo refund2 = new RefundInfo("success@test.com", 50.0, UUID.randomUUID());

        when(invoicesService.getRefundInfos(expenseId)).thenReturn(List.of(refund1, refund2));

        // ✅ CORRECTION : Matcher eq(100.0) ou anyDouble()
        doThrow(new RuntimeException("API Paiement HS"))
                .when(paymentService).transfertRequest(eq("error@test.com"), anyDouble(), any(), any());

        // Act
        expenseService.cancelExpense(expenseId, token);

        // Assert
        verify(paymentService).transfertRequest(eq("success@test.com"), anyDouble(), any(), any());

        verify(invoicesService).finalizeCancellation(expenseId);
    }
}