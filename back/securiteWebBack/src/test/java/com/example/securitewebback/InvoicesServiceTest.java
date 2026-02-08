package com.example.securitewebback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.invoice.service.InvoicesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.expense.entity.Expense;
import com.example.securitewebback.expense.expenseEnum.ExpenseStatut;
import com.example.securitewebback.expense.repository.ExpenseRepository;
import com.example.securitewebback.invoice.RefundInfo;
import com.example.securitewebback.invoice.entity.Invoice;
import com.example.securitewebback.invoice.invoiceEnum.InvoiceStatut;
import com.example.securitewebback.invoice.repository.InvoicesRepository;
import com.example.securitewebback.payement.PaymentService;

@ExtendWith(MockitoExtension.class)
class InvoicesServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private InvoicesRepository invoiceRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private InvoicesService invoicesService;

    // --- generateInvoicesForBuilding ---

    @Test
    @DisplayName("Doit générer des factures proportionnelles aux tantièmes pour chaque propriétaire")
    void generateInvoicesForBuilding_Success() {
        // Arrange
        UUID buildingId = UUID.randomUUID();
        Building building = new Building();
        building.setId(buildingId);

        // Proprio A : 200 tantièmes
        Proprietaire ownerA = new Proprietaire();
        ownerA.setId(UUID.randomUUID());
        Apartment apt1 = new Apartment();
        apt1.setOwner(ownerA);
        apt1.setTantiemes(200);

        // Proprio B : 800 tantièmes
        Proprietaire ownerB = new Proprietaire();
        ownerB.setId(UUID.randomUUID());
        Apartment apt2 = new Apartment();
        apt2.setOwner(ownerB);
        apt2.setTantiemes(800);

        building.setApartment(Arrays.asList(apt1, apt2));

        // Dépense de 1000€
        Expense expense = new Expense();
        expense.setTotalAmount(BigDecimal.valueOf(1000));
        expense.setLabel("Entretien");

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));

        // Act
        invoicesService.generateInvoicesForBuilding(buildingId, expense);

        // Assert
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        // On s'attend à 2 sauvegardes (une par proprio)
        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());

        List<Invoice> capturedInvoices = invoiceCaptor.getAllValues();

        // Vérification Proprio A (20% de 1000€ = 200€)
        Invoice invoiceA = capturedInvoices.stream()
                .filter(i -> i.getDestinataire().equals(ownerA))
                .findFirst().orElseThrow();
        assertThat(invoiceA.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200.0));

        // Vérification Proprio B (80% de 1000€ = 800€)
        Invoice invoiceB = capturedInvoices.stream()
                .filter(i -> i.getDestinataire().equals(ownerB))
                .findFirst().orElseThrow();
        assertThat(invoiceB.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(800.0));
    }

    @Test
    @DisplayName("Doit ignorer les appartements sans propriétaire")
    void generateInvoicesForBuilding_IgnoreUnowned() {
        // Arrange
        UUID buildingId = UUID.randomUUID();
        Building building = new Building();

        // Proprio A : 500 tantièmes
        Proprietaire ownerA = new Proprietaire();
        Apartment apt1 = new Apartment();
        apt1.setOwner(ownerA);
        apt1.setTantiemes(500);

        // Appartement vide (pas de proprio) : 500 tantièmes
        Apartment aptEmpty = new Apartment();
        aptEmpty.setOwner(null);
        aptEmpty.setTantiemes(500);

        building.setApartment(Arrays.asList(apt1, aptEmpty));

        // Dépense de 1000€
        Expense expense = new Expense();
        expense.setTotalAmount(BigDecimal.valueOf(1000));

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));

        // Act
        invoicesService.generateInvoicesForBuilding(buildingId, expense);

        // Assert
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository, times(1)).save(invoiceCaptor.capture());

        // Comme l'appart vide est ignoré, le total des tantièmes "possédés" est 500.
        // OwnerA possède 500/500 = 100% des parts actives. Il paie tout.
        Invoice invoice = invoiceCaptor.getValue();
        assertThat(invoice.getDestinataire()).isEqualTo(ownerA);
        assertThat(invoice.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.0));
    }

    // --- markAsPaid ---

    @Test
    @DisplayName("markAsPaid : Doit passer la facture à PAID")
    void markAsPaid_Simple() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setStatut(InvoiceStatut.PENDING);

        Expense expense = new Expense();
        expense.setStatut(ExpenseStatut.PENDING);
        // Liste modifiable pour que le stream fonctionne
        expense.setInvoices(new ArrayList<>(List.of(invoice)));
        invoice.setExpense(expense);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        invoicesService.markAsPaid(invoiceId);

        assertThat(invoice.getStatut()).isEqualTo(InvoiceStatut.PAID);
        // Une seule facture, elle est payée -> Dépense payée
        assertThat(expense.getStatut()).isEqualTo(ExpenseStatut.PAID);
    }

    @Test
    @DisplayName("markAsPaid : Ne doit pas passer la dépense à PAID si d'autres factures sont impayées")
    void markAsPaid_PartialPayment() {
        UUID invoiceId1 = UUID.randomUUID();

        Invoice invoice1 = new Invoice(); // Celle qu'on paie
        invoice1.setId(invoiceId1);
        invoice1.setStatut(InvoiceStatut.PENDING);

        Invoice invoice2 = new Invoice(); // L'autre qui reste impayée
        invoice2.setStatut(InvoiceStatut.PENDING);

        Expense expense = new Expense();
        expense.setStatut(ExpenseStatut.PENDING);
        expense.setInvoices(Arrays.asList(invoice1, invoice2));

        invoice1.setExpense(expense);
        invoice2.setExpense(expense);

        when(invoiceRepository.findById(invoiceId1)).thenReturn(Optional.of(invoice1));

        // Act
        invoicesService.markAsPaid(invoiceId1);

        // Assert
        assertThat(invoice1.getStatut()).isEqualTo(InvoiceStatut.PAID);
        assertThat(expense.getStatut()).isEqualTo(ExpenseStatut.PENDING); // Reste en attente car invoice2 pas payée
    }

    // --- getRefundInfos ---

    @Test
    @DisplayName("getRefundInfos : Ne retourne que les factures payées")
    void getRefundInfos_OnlyPaid() {
        UUID expenseId = UUID.randomUUID();
        Expense expense = new Expense();

        Proprietaire p1 = new Proprietaire(); p1.setEmail("paid@test.com");
        Invoice invPaid = new Invoice();
        invPaid.setStatut(InvoiceStatut.PAID);
        invPaid.setAmount(BigDecimal.valueOf(100));
        invPaid.setDestinataire(p1);
        invPaid.setId(UUID.randomUUID());

        Proprietaire p2 = new Proprietaire(); p2.setEmail("pending@test.com");
        Invoice invPending = new Invoice();
        invPending.setStatut(InvoiceStatut.PENDING);
        invPending.setDestinataire(p2);

        expense.setInvoices(Arrays.asList(invPaid, invPending));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // Act
        List<RefundInfo> refunds = invoicesService.getRefundInfos(expenseId);

        // Assert
        assertThat(refunds).hasSize(1);
        RefundInfo info = refunds.get(0);
        assertThat(info.email()).isEqualTo("paid@test.com");
        assertThat(info.amount()).isEqualTo(100.0);
    }

    // --- finalizeCancellation ---

    @Test
    @DisplayName("finalizeCancellation : Annule la dépense et toutes ses factures")
    void finalizeCancellation_Success() {
        UUID expenseId = UUID.randomUUID();
        Expense expense = new Expense();
        expense.setStatut(ExpenseStatut.PENDING);

        Invoice inv1 = new Invoice(); inv1.setStatut(InvoiceStatut.PENDING);
        Invoice inv2 = new Invoice(); inv2.setStatut(InvoiceStatut.PAID);
        expense.setInvoices(Arrays.asList(inv1, inv2));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // Act
        invoicesService.finalizeCancellation(expenseId);

        // Assert
        assertThat(expense.getStatut()).isEqualTo(ExpenseStatut.CANCELLED);
        assertThat(inv1.getStatut()).isEqualTo(InvoiceStatut.CANCELLED);
        assertThat(inv2.getStatut()).isEqualTo(InvoiceStatut.CANCELLED);

        verify(expenseRepository).save(expense);
    }

    // --- getInvoices ---

    @Test
    @DisplayName("getInvoices : Appelle le repository")
    void getInvoices_Success() {
        UUID ownerId = UUID.randomUUID();
        Pageable pageable = Pageable.unpaged();
        Page<Invoice> page = new PageImpl<>(List.of(new Invoice()));

        when(invoiceRepository.findByProprietaire(ownerId, pageable)).thenReturn(page);

        Page<Invoice> result = invoicesService.getInvoices(ownerId, pageable);

        assertThat(result).isNotEmpty();
    }
}