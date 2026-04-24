export interface Produit {
  id?: string;
  nom: string;
  description: string;
  prix: number;
  categorie: string;
  image: string;
  actif: boolean;
  dateCreation?: string;
  dateExpiration?: string;
  stock?: Stock;
  promotionIds?: string[];
}

export interface ProduitExpirationAlert {
  produitId: string;
  nomProduit: string;
  categorie: string;
  dateExpiration: string;
  joursRestants: number;
  quantiteDisponible?: number | null;
}

export interface Stock {
  id?: string;
  quantiteDisponible: number;
  quantiteMin: number;
  quantiteMax: number;
  dernierReapprovisionnement?: string;
  produitId?: string;
}

export interface Commande {
  id?: string;
  numeroCommande?: string;
  dateCommande?: string;
  montantTotal: number;
  statut: StatutCommande;
  utilisateurId: string;
  utilisateurNom?: string;
  utilisateurPrenom?: string;
  livraisonDemandee?: boolean;
  lieuLivraison?: string;
  statutLivraison?: StatutLivraison;
  agentLivraisonId?: string;
  agentLivraisonNom?: string;
  dateAffectationLivraison?: string;
  dateLivraison?: string;
  lignes: LigneCommande[];
}

export interface UtilisateurLite {
  id: string;
  nom?: string;
  prenom?: string;
  email?: string;
  role?: string;
}

export interface LigneCommande {
  produitId: string;
  nomProduit: string;
  quantite: number;
  prixUnitaire: number;
  sousTotal: number;
  image?: string; // 👈 AJOUTÉ
}

export interface Promotion {
  id?: string;
  titre: string;
  description: string;
  pourcentageReduction: number;
  dateDebut: string;
  dateFin: string;
  heureDebut?: string;
  heureFin?: string;
  active: boolean;
  produitIds?: string[];
}

export interface PointsFidelite {
  id?: string;
  pointsTotal: number;
  pointsUtilises: number;
  pointsDisponibles: number;
  pointsAvailable?: number; // 👈 AJOUTÉ POUR COMPATIBILITÉ
  niveau: NiveauFidelite;
  abonnementActifCodeProchaineCommande?: string | null;
  abonnementActifLabelProchaineCommande?: string | null;
  abonnementActifReductionProchaineCommande?: number;
  dateDerniereMiseAJour?: string;
  utilisateurId: string;
}

export interface AbonnementConfig {
  id?: string;
  code: string;
  label: string;
  pointsRequis: number;
  pourcentageReduction: number;
  actif: boolean;
}

export interface HistoriquePoints {
  id?: string;
  pointsGagnes: number;
  pointsUtilises: number;
  motif: string;
  date: string;
  clientId: string;
  commandeId: string;
}

export interface Recommandation {
  id?: string;
  score: number;
  dateGeneree: string;
  raison: string;
  vue: boolean;
  achetee: boolean;
  utilisateurId: string;
  produitId: string;
}

export interface ProduitRecommande {
  id: string;
  nom: string;
  description: string;
  prix: number;
  categorie: string;
  image: string;
  actif: boolean;
}

export interface Review {
  id?: string;
  produitId: string;
  utilisateurId: string;
  utilisateurNom: string;
  note: number;
  commentaire: string;
  dateCreation?: string;
  dateMiseAJour?: string;
}

export interface ReviewStats {
  averageRating: number;
  totalReviews: number;
  ratingDistribution: { [key: number]: number };
}

export interface ReviewInsight {
  produitId: string;
  resumeAdmin: string;
  noteConfiance: number;
  nbAvisAnalyses: number;
  dateGeneration: string;
}

export interface Objectif {
  id?: string;
  titre: string;
  description: string;
  icon?: string;
  actif: boolean;
  dateCreation?: string;
  produitIds: string[];
}

export interface ChatUser {
  id: string;
  nom: string;
  prenom: string;
  role: string;
}

export interface ChatMessage {
  id?: string;
  senderId: string;
  senderName: string;
  receiverId?: string;
  receiverName?: string;
  content: string;
  createdAt?: string;
  updatedAt?: string;
}

export enum StatutCommande {
  EN_COURS = 'EN_COURS',
  PAYEE = 'PAYEE',
  LIVREE = 'LIVREE',
  ANNULEE = 'ANNULEE'
}

export enum StatutLivraison {
  EN_ATTENTE_AFFECTATION = 'EN_ATTENTE_AFFECTATION',
  AFFECTEE = 'AFFECTEE',
  EN_COURS = 'EN_COURS',
  LIVREE = 'LIVREE',
  ECHEC = 'ECHEC',
  ANNULEE = 'ANNULEE'
}

export enum NiveauFidelite {
  BRONZE = 'BRONZE',
  SILVER = 'SILVER',
  GOLD = 'GOLD',
  PLATINUM = 'PLATINUM'
}

export const MARKETPLACE_MODELS_VERSION = 1;
