import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Review, ReviewStats, ReviewInsight } from '../models/marketplace.models';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly apiUrl = `${environment.apiBaseUrl}`;

  constructor(private http: HttpClient) {}

  getReviewsByProduit(produitId: string): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/produits/${produitId}/reviews`);
  }

  getReviewStatsByProduit(produitId: string): Observable<ReviewStats> {
    return this.http.get<ReviewStats>(`${this.apiUrl}/produits/${produitId}/reviews/stats`);
  }

  getReviewInsightsByProduit(produitId: string): Observable<ReviewInsight> {
    return this.http.get<ReviewInsight>(`${this.apiUrl}/produits/${produitId}/reviews/insights`);
  }

  createOrUpdateReview(
    produitId: string,
    payload: { utilisateurId: string; utilisateurNom: string; note: number; commentaire: string }
  ): Observable<Review> {
    return this.http.post<Review>(`${this.apiUrl}/produits/${produitId}/reviews`, payload);
  }

  deleteReview(reviewId: string, utilisateurId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/reviews/${reviewId}?utilisateurId=${encodeURIComponent(utilisateurId)}`);
  }
}
