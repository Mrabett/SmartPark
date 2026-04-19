import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { timeout } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';

export interface AiGeneratedProduct {
  nom: string;
  description: string;
  prix: number;
  categorie: string;
  image: string;
  actif: boolean;
  stock?: {
    quantiteDisponible: number;
    quantiteMin: number;
    quantiteMax: number;
  };
}

export interface AiSuggestionResult {
  produit: AiGeneratedProduct;
  score?: number;
  pertinence: number;
  raison: string;
  motsClesCorrespondants: string[];
  attributes?: { [key: string]: string };
  source?: string;
  isGenerated: boolean;
}

@Injectable({ providedIn: 'root' })
export class AiSuggestService {
  private readonly apiUrl = `${environment.apiBaseUrl}/produits/ai/suggest`;
  private readonly productsApiUrl = `${environment.apiBaseUrl}/produits`;

  constructor(private http: HttpClient) {}

  suggest(description: string): Observable<AiSuggestionResult[]> {
    return this.http.post<AiSuggestionResult[]>(this.apiUrl, { description }).pipe(
      timeout(120_000) // 120 secondes de timeout pour Ollama
    );
  }

  /** Ajoute un produit généré par l'IA au catalogue réel */
  addToCatalog(product: AiGeneratedProduct): Observable<any> {
    return this.http.post(this.productsApiUrl, product);
  }
}
