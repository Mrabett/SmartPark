import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Produit, Review, ReviewStats, ReviewInsight } from '../../models/marketplace.models';
import { OnInit } from '@angular/core';
import { environment } from '../../../../../environments/environment';
import { AuthService } from '../../../../services/auth.service';
import { PanierService } from '../../services/panier.service';
import { ReviewService } from '../../services/review.service';

@Component({
  selector: 'app-produit-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './produit-detail.component.html',
  styleUrl: './produit-detail.component.css'
})
export class ProduitDetailComponent implements OnInit {
  produit: Produit | null = null;
  loading = false;
  quantity = 1;
  reviews: Review[] = [];
  reviewStats: ReviewStats | null = null;
  reviewInsight: ReviewInsight | null = null;
  reviewInsightLoading = false;
  reviewSubmitting = false;
  reviewMessage = '';
  reviewError = '';
  reviewForm = {
    note: 5,
    commentaire: ''
  };
  private readonly apiUrl = `${environment.apiBaseUrl}/produits`;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private panierService: PanierService,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.loadProduit(params['id']);
      }
    });
  }

  loadProduit(id: string): void {
    this.loading = true;
    this.http.get<Produit>(`${this.apiUrl}/${id}`).subscribe({
      next: (data: Produit) => {
        this.produit = data;
        this.loadReviews(data.id || id);
        if (this.isAdmin) {
          this.loadReviewInsights(data.id || id);
        }
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error('Error loading produit:', err);
        this.loading = false;
      }
    });
  }

  addToCart(): void {
    if (!this.produit || this.isAdmin) {
      return;
    }
    this.panierService.add(this.produit, this.quantity);
  }

  deleteProduit(): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce produit?')) {
      if (this.produit?.id) {
        this.http.delete<void>(`${this.apiUrl}/${this.produit.id}`).subscribe({
          next: () => {
            this.router.navigate(['/marketplace/admin/produits']);
          },
          error: (err: unknown) => console.error('Error deleting:', err)
        });
      }
    }
  }

  loadReviews(produitId: string): void {
    this.reviewService.getReviewsByProduit(produitId).subscribe({
      next: (reviews) => {
        this.reviews = reviews;
      },
      error: (err) => {
        console.error('Erreur chargement reviews:', err);
      }
    });

    this.reviewService.getReviewStatsByProduit(produitId).subscribe({
      next: (stats) => {
        this.reviewStats = stats;
      },
      error: (err) => {
        console.error('Erreur chargement stats reviews:', err);
      }
    });
  }

  loadReviewInsights(produitId: string): void {
    if (!this.isAdmin) {
      this.reviewInsight = null;
      return;
    }

    this.reviewInsightLoading = true;
    this.reviewService.getReviewInsightsByProduit(produitId).subscribe({
      next: (insight) => {
        this.reviewInsight = insight;
        this.reviewInsightLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement insights reviews:', err);
        this.reviewInsight = null;
        this.reviewInsightLoading = false;
      }
    });
  }

  submitReview(): void {
    if (this.isAdmin || !this.produit?.id) {
      return;
    }

    const commentaire = this.reviewForm.commentaire.trim();
    if (!commentaire) {
      this.reviewError = 'Le commentaire est obligatoire.';
      this.reviewMessage = '';
      return;
    }

    this.reviewSubmitting = true;
    this.reviewError = '';
    this.reviewMessage = '';

    const client = this.authService.getClientInfo();
    const payload = {
      utilisateurId: client.id,
      utilisateurNom: `${client.prenom || ''} ${client.nom || ''}`.trim() || 'Client',
      note: this.reviewForm.note,
      commentaire
    };

    this.reviewService.createOrUpdateReview(this.produit.id, payload).subscribe({
      next: () => {
        this.reviewSubmitting = false;
        this.reviewMessage = 'Votre avis a été enregistré.';
        this.reviewForm.commentaire = '';
        this.loadReviews(this.produit!.id!);
      },
      error: (err) => {
        this.reviewSubmitting = false;
        if (err?.status === 403) {
          this.reviewError = 'il faut acheter le produit avant de faire un avis';
        } else {
          this.reviewError = err?.error?.message || 'il faut acheter le produit avant de faire un avis';
        }
      }
    });
  }

  removeReview(review: Review): void {
    if (!review.id || !this.produit?.id) {
      return;
    }

    const userId = this.authService.getCurrentUserId();
    if (!this.isAdmin && review.utilisateurId !== userId) {
      this.reviewError = 'Vous ne pouvez supprimer que votre propre avis.';
      this.reviewMessage = '';
      return;
    }

    this.reviewService.deleteReview(review.id, userId).subscribe({
      next: () => {
        this.reviewMessage = 'Avis supprimé avec succès.';
        this.reviewError = '';
        this.loadReviews(this.produit!.id!);
      },
      error: (err) => {
        this.reviewError = err?.error?.message || 'Erreur lors de la suppression de l\'avis.';
        this.reviewMessage = '';
      }
    });
  }

  starsFor(note: number): string {
    return '★'.repeat(Math.max(0, note)) + '☆'.repeat(Math.max(0, 5 - note));
  }

  get currentUserId(): string {
    return this.authService.getCurrentUserId();
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get routePrefix(): string {
    return this.isAdmin ? '/marketplace/admin' : '/marketplace/client';
  }
}
