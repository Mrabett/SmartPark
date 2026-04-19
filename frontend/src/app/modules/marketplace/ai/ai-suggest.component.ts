import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AiSuggestService, AiSuggestionResult } from './ai-suggest.service';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-ai-suggest',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-suggest.component.html',
  styleUrl: './ai-suggest.component.css'
})
export class AiSuggestComponent implements OnDestroy {

  description = '';
  suggestions: AiSuggestionResult[] = [];
  loading = false;
  analyzed = false;
  errorMsg = '';
  successMsg = '';
  lastDescription = '';
  textareaFocused = false;
  loadingStep = 0;
  addingIndex = -1;
  addedIndexes = new Set<number>();

  private loadingTimer: ReturnType<typeof setInterval> | null = null;

  /** Exemples de descriptions */
  readonly examples = [
    'une boisson énergétique pour les sportifs',
    'équipement de protection pour le football',
    'chaussures de running légères et confortables',
    'supplément protéiné pour la musculation',
    'tenue de yoga pour femme, respirante et flexible',
    'accessoires de natation pour piscine'
  ];

  constructor(private aiService: AiSuggestService, private router: Router) {}

  ngOnDestroy(): void {
    this.stopLoadingAnimation();
  }

  useExample(example: string): void {
    this.description = example;
  }

  clearAll(): void {
    this.description = '';
    this.suggestions = [];
    this.analyzed = false;
    this.errorMsg = '';
    this.successMsg = '';
    this.lastDescription = '';
    this.loadingStep = 0;
    this.addedIndexes.clear();
    this.addingIndex = -1;
  }

  onEnterKey(event: Event): void {
    const ke = event as KeyboardEvent;
    if (!ke.shiftKey && this.description.trim().length >= 10 && !this.loading) {
      event.preventDefault();
      this.analyze();
    }
  }

  /** Lance la génération IA */
  analyze(): void {
    const trimmed = this.description.trim();
    if (trimmed.length < 10 || this.loading) return;

    this.loading = true;
    this.analyzed = false;
    this.suggestions = [];
    this.errorMsg = '';
    this.successMsg = '';
    this.lastDescription = trimmed;
    this.loadingStep = 0;
    this.addedIndexes.clear();
    this.addingIndex = -1;

    this.startLoadingAnimation();

    this.aiService.suggest(trimmed).pipe(
      finalize(() => {
        this.loading = false;
        this.analyzed = true;
        this.stopLoadingAnimation();
        this.loadingStep = 4;
      })
    ).subscribe({
      next: (results: AiSuggestionResult[]) => {
        setTimeout(() => {
          this.suggestions = results;
        }, 300);
      },
      error: (err: any) => {
        console.error('[AiSuggest] Erreur:', err);
        const rawMsg = err?.error?.message || err?.message || '';
        if (rawMsg.includes('429') || rawMsg.includes('quota') || rawMsg.includes('Too Many')) {
          this.errorMsg = '⏳ L\'IA est temporairement surchargée. Veuillez patienter 30 secondes puis réessayer.';
        } else {
          this.errorMsg = rawMsg || 'Impossible de générer les produits. Vérifiez que le serveur est démarré.';
        }
      }
    });
  }

  /** Ajoute un produit généré au vrai catalogue */
  addToCatalog(result: AiSuggestionResult, index: number): void {
    if (this.addedIndexes.has(index) || this.addingIndex >= 0) return;

    this.addingIndex = index;
    this.errorMsg = '';
    this.successMsg = '';

    this.router.navigate(['/marketplace/admin/produits/nouveau'], {
      queryParams: {
        nom: result.produit.nom,
        description: result.produit.description,
        prix: result.produit.prix,
        categorie: result.produit.categorie,
        image: result.produit.image,
        actif: result.produit.actif
      }
    }).then(() => {
      this.addedIndexes.add(index);
      this.addingIndex = -1;
      this.successMsg = `Le formulaire produit a été ouvert pour "${result.produit.nom}". Vous pouvez ajuster le stock manuellement.`;
    }).catch((err: any) => {
      this.addingIndex = -1;
      this.errorMsg = err?.message || 'Impossible d\'ouvrir le formulaire produit.';
    });
  }

  private startLoadingAnimation(): void {
    this.loadingTimer = setInterval(() => {
      if (this.loadingStep < 3) {
        this.loadingStep++;
      }
    }, 1200);
  }

  private stopLoadingAnimation(): void {
    if (this.loadingTimer) {
      clearInterval(this.loadingTimer);
      this.loadingTimer = null;
    }
  }

  getScoreColor(pertinence: number): string {
    if (pertinence >= 70) return '#00c97f';
    if (pertinence >= 40) return '#f59e0b';
    return '#f87171';
  }

  getKeywordsArray(motsCles: any): string[] {
    if (!motsCles) return [];
    if (Array.isArray(motsCles)) return (motsCles as string[]).slice(0, 10);
    if (typeof motsCles === 'object') {
      return (Object.values(motsCles) as string[]).slice(0, 10);
    }
    return [];
  }

  getAttributeEntries(attributes: any): { key: string, value: string }[] {
    if (!attributes) return [];
    return Object.entries(attributes).map(([key, value]) => ({ 
      key: key === 'color' ? 'Couleur' : key === 'material' ? 'Matière' : 'Cible', 
      value: value as string 
    }));
  }
}
