package com.smartpark.backend1.service;

import com.smartpark.backend1.dto.EvenementCreateRequest;
import com.smartpark.backend1.dto.EvenementUpdateRequest;
import com.smartpark.backend1.dto.EvenementResponse;
import com.smartpark.backend1.model.Evenement;
import com.smartpark.backend1.repository.EvenementRepository;
import com.smartpark.backend1.repository.InscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private InscriptionRepository inscriptionRepository;

    // ── GET ALL ────────────────────────────────────────────────
    public List<EvenementResponse> getAll() {
        return evenementRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── GET BY ID ──────────────────────────────────────────────
    public Optional<EvenementResponse> getById(String id) {
        return evenementRepository.findById(id)
                .map(this::toResponse);
    }

    // ── CREATE ─────────────────────────────────────────────────
    public EvenementResponse create(EvenementCreateRequest request) {

        // Vérifier conflit de lieu (null = pas d'ID à exclure)
        verifierConflitLieu(
                request.getLieu(),
                request.getDateDebut(),
                request.getDateFin(),
                null
        );

        Evenement evenement = toEntity(request);
        evenement.setStatut("PLANIFIE");
        evenement.setNbInscrits(0);
        evenement.setRevenus(0);
        return toResponse(evenementRepository.save(evenement));
    }

    // ── UPDATE ─────────────────────────────────────────────────
    public EvenementResponse update(String id, EvenementUpdateRequest request) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement introuvable : " + id));

        // Vérifier conflit de lieu (en excluant l'événement lui-même)
        verifierConflitLieu(
                request.getLieu(),
                request.getDateDebut(),
                request.getDateFin(),
                id
        );

        applyUpdate(evenement, request);
        return toResponse(evenementRepository.save(evenement));
    }

    // ── DELETE ─────────────────────────────────────────────────
    public void delete(String id) {
        evenementRepository.deleteById(id);
    }

    // ── FILTRES ────────────────────────────────────────────────
    public List<EvenementResponse> getByType(String type) {
        return evenementRepository.findByType(type)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<EvenementResponse> getByStatut(String statut) {
        return evenementRepository.findByStatut(statut)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── DASHBOARD ──────────────────────────────────────────────
    public Map<String, Object> getDashboardStats() {
        List<Evenement> all = evenementRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("total",         all.size());
        stats.put("planifies",     all.stream().filter(e -> "PLANIFIE".equals(e.getStatut())).count());
        stats.put("enCours",       all.stream().filter(e -> "EN_COURS".equals(e.getStatut())).count());
        stats.put("termines",      all.stream().filter(e -> "TERMINE".equals(e.getStatut())).count());
        stats.put("totalRevenus",  all.stream().mapToDouble(Evenement::getRevenus).sum());
        stats.put("totalInscrits", all.stream().mapToInt(Evenement::getNbInscrits).sum());
        return stats;
    }

    // ── UPDATE STATS ───────────────────────────────────────────
    public void updateStats(String evenementId) {
        Evenement e = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable"));
        long nbInscrits = inscriptionRepository.countByEvenementId(evenementId);
        double revenus  = inscriptionRepository.findByEvenementId(evenementId)
                .stream()
                .filter(i -> "CONFIRMEE".equals(i.getStatut()))
                .mapToDouble(i -> i.getMontantPaye())
                .sum();
        e.setNbInscrits((int) nbInscrits);
        e.setRevenus(revenus);
        evenementRepository.save(e);
    }

    // ══════════════════════════════════════════════════════════
    // VÉRIFICATION CONFLIT DE LIEU — 100% côté Java
    // ══════════════════════════════════════════════════════════

    /**
     * Vérifie qu'aucun événement actif n'utilise déjà ce lieu
     * sur une période qui chevauche [dateDebut, dateFin].
     *
     * Deux périodes [A,B] et [C,D] se chevauchent si : A <= D  ET  C <= B
     *
     * @param lieu       lieu à vérifier (comparaison insensible à la casse)
     * @param dateDebut  date de début du nouvel événement
     * @param dateFin    date de fin du nouvel événement
     * @param excludeId  ID de l'événement à ignorer (null pour une création)
     */
    private void verifierConflitLieu(
            String lieu,
            LocalDate dateDebut,
            LocalDate dateFin,
            String excludeId) {

        // 1. Récupérer tous les événements du même lieu (ignorer la casse)
        List<Evenement> memeElieu = evenementRepository.findByLieuIgnoreCase(lieu);

        // 2. Filtrer en Java
        Optional<Evenement> conflit = memeElieu.stream()
                .filter(e -> {
                    // Ignorer l'événement lui-même lors d'une mise à jour
                    if (excludeId != null && excludeId.equals(e.getId())) {
                        return false;
                    }
                    // Ignorer les événements annulés
                    if ("ANNULE".equals(e.getStatut())) {
                        return false;
                    }
                    // Vérifier le chevauchement de dates
                    // Chevauchement : dateDebut <= e.dateFin  ET  e.dateDebut <= dateFin
                    boolean chevauche = !dateDebut.isAfter(e.getDateFin())
                            && !e.getDateDebut().isAfter(dateFin);
                    return chevauche;
                })
                .findFirst();

        // 3. Si conflit trouvé → lancer une exception avec message clair
        if (conflit.isPresent()) {
            Evenement e = conflit.get();
            throw new RuntimeException(String.format(
                    "Le lieu \"%s\" est déjà réservé du %s au %s pour l'événement \"%s\" (statut : %s). " +
                            "Veuillez choisir un autre lieu ou modifier les dates.",
                    lieu,
                    e.getDateDebut(),
                    e.getDateFin(),
                    e.getTitre(),
                    e.getStatut()
            ));
        }
    }

    // ══════════════════════════════════════════════════════════
    // CONVERSIONS PRIVÉES
    // ══════════════════════════════════════════════════════════

    private Evenement toEntity(EvenementCreateRequest req) {
        Evenement e = new Evenement();
        e.setTitre(req.getTitre());
        e.setType(req.getType());
        e.setDescription(req.getDescription());
        e.setLieu(req.getLieu());
        e.setDateDebut(req.getDateDebut());
        e.setDateFin(req.getDateFin());
        e.setHeureDebut(req.getHeureDebut());
        e.setHeureFin(req.getHeureFin());
        e.setCapaciteMax(req.getCapaciteMax());
        e.setPrixBillet(req.getPrixBillet());
        e.setOrganisateur(req.getOrganisateur());
        e.setTags(req.getTags());
        e.setAvecPlacement(req.isAvecPlacement());
        e.setNbPlacesZoneA(req.getNbPlacesZoneA());
        e.setNbPlacesZoneB(req.getNbPlacesZoneB());
        e.setNbPlacesZoneC(req.getNbPlacesZoneC());
        e.setPrixZoneA(req.getPrixZoneA());
        e.setPrixZoneB(req.getPrixZoneB());
        e.setPrixZoneC(req.getPrixZoneC());
        return e;
    }

    private void applyUpdate(Evenement e, EvenementUpdateRequest req) {
        e.setTitre(req.getTitre());
        e.setType(req.getType());
        e.setDescription(req.getDescription());
        e.setLieu(req.getLieu());
        e.setDateDebut(req.getDateDebut());
        e.setDateFin(req.getDateFin());
        e.setHeureDebut(req.getHeureDebut());
        e.setHeureFin(req.getHeureFin());
        e.setCapaciteMax(req.getCapaciteMax());
        e.setPrixBillet(req.getPrixBillet());
        e.setStatut(req.getStatut());
        e.setOrganisateur(req.getOrganisateur());
        e.setTags(req.getTags());
        e.setAvecPlacement(req.isAvecPlacement());
        e.setNbPlacesZoneA(req.getNbPlacesZoneA());
        e.setNbPlacesZoneB(req.getNbPlacesZoneB());
        e.setNbPlacesZoneC(req.getNbPlacesZoneC());
        e.setPrixZoneA(req.getPrixZoneA());
        e.setPrixZoneB(req.getPrixZoneB());
        e.setPrixZoneC(req.getPrixZoneC());
    }

    private EvenementResponse toResponse(Evenement e) {
        int placesRestantes = Math.max(0, e.getCapaciteMax() - e.getNbInscrits());
        double taux = e.getCapaciteMax() > 0
                ? Math.min(100.0, (e.getNbInscrits() * 100.0) / e.getCapaciteMax())
                : 0.0;

        return EvenementResponse.builder()
                .id(e.getId())
                .titre(e.getTitre())
                .type(e.getType())
                .description(e.getDescription())
                .lieu(e.getLieu())
                .dateDebut(e.getDateDebut())
                .dateFin(e.getDateFin())
                .heureDebut(e.getHeureDebut())
                .heureFin(e.getHeureFin())
                .capaciteMax(e.getCapaciteMax())
                .prixBillet(e.getPrixBillet())
                .statut(e.getStatut())
                .organisateur(e.getOrganisateur())
                .tags(e.getTags())
                .nbInscrits(e.getNbInscrits())
                .revenus(e.getRevenus())
                .placesRestantes(placesRestantes)
                .tauxOccupation(Math.round(taux * 10.0) / 10.0)
                .avecPlacement(e.isAvecPlacement())
                .nbPlacesZoneA(e.getNbPlacesZoneA())
                .nbPlacesZoneB(e.getNbPlacesZoneB())
                .nbPlacesZoneC(e.getNbPlacesZoneC())
                .prixZoneA(e.getPrixZoneA())
                .prixZoneB(e.getPrixZoneB())
                .prixZoneC(e.getPrixZoneC())
                .build();
    }
}