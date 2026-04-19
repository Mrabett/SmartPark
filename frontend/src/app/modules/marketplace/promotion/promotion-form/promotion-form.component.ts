import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { environment } from '../../../../../environments/environment';
import { Produit, Promotion } from '../../models/marketplace.models';

@Component({
  selector: 'app-promotion-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './promotion-form.component.html',
  styles: [`
    .view-subtitle {
      color: #e2e8f0;
      margin-top: 0.35rem;
      font-size: 1rem;
      line-height: 1.5;
    }

    .admin-form-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1.1rem;
    }

    .field-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1rem;
    }

    .products-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 0.85rem;
    }

    .helper-text {
      color: #e8f1fb;
      font-size: 0.96rem;
      line-height: 1.45;
      font-weight: 500;
      margin-bottom: 0.9rem;
    }

    @media (min-width: 860px) {
      .field-grid {
        grid-template-columns: repeat(3, minmax(0, 1fr));
      }

      .products-grid {
        grid-template-columns: repeat(3, minmax(0, 1fr));
      }
    }

    @media (min-width: 1120px) {
      .admin-form-grid {
        grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
        gap: 1.5rem;
      }
    }
  `]
})
export class PromotionFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  submitted = false;
  editMode = false;
  promotionId: string | null = null;
  produits: Produit[] = [];
  errorMessage = "";
  private readonly apiUrl = `${environment.apiBaseUrl}/promotions`;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadProduits();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.editMode = true;
        this.promotionId = params['id'];
        this.loadPromotion(params['id']);
      }
    });

    // Pré-sélectionner un produit si fourni en query param
    this.route.queryParams.subscribe(queryParams => {
      if (queryParams['produitId']) {
        // On attend que les produits soient chargés
        setTimeout(() => {
          this.toggleProduit(queryParams['produitId']);
        }, 500);
      }
    });
  }

  initForm(): void {
    this.form = this.fb.group({
      titre: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', Validators.required],
      pourcentageReduction: [10, [Validators.required, Validators.min(1), Validators.max(100)]],
      dateDebut: [this.formatDate(new Date()), Validators.required],
      dateFin: [this.formatDate(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)), Validators.required],
      active: [true],
      produitIds: [[]]
    }, { validators: this.dateRangeValidator });
  }

  dateRangeValidator(control: AbstractControl): ValidationErrors | null {
    const dateDebut = control.get('dateDebut')?.value;
    const dateFin = control.get('dateFin')?.value;

    if (!dateDebut || !dateFin) {
      return null;
    }

    const debut = new Date(dateDebut).getTime();
    const fin = new Date(dateFin).getTime();

    if (fin <= debut) {
      control.get('dateFin')?.setErrors({ 'dateInvalide': true });
      return { 'dateInvalide': true };
    }

    control.get('dateFin')?.setErrors(null);
    return null;
  }

  loadProduits(): void {
    this.http.get<Produit[]>(`${environment.apiBaseUrl}/produits`).subscribe({
      next: (data) => this.produits = data,
      error: (err) => console.error('Erreur chargement produits:', err)
    });
  }

  loadPromotion(id: string): void {
    this.loading = true;
    this.http.get<Promotion>(`${this.apiUrl}/${id}`).subscribe({
      next: (promo) => {
        const formattedPromo = {
          ...promo,
          dateDebut: promo.dateDebut ? promo.dateDebut.split('T')[0] : '',
          dateFin: promo.dateFin ? promo.dateFin.split('T')[0] : ''
        };
        this.form.patchValue(formattedPromo);
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement promo:', err);
        this.loading = false;
      }
    });
  }

  toggleProduit(produitId: string | undefined): void {
    if (!produitId) return;
    const currentIds = [...(this.form.get('produitIds')?.value || [])];
    const index = currentIds.indexOf(produitId);
    if (index > -1) {
      currentIds.splice(index, 1);
    } else {
      currentIds.push(produitId);
    }
    this.form.patchValue({ produitIds: currentIds });
  }

  isProduitSelected(produitId: string | undefined): boolean {
    if (!produitId) return false;
    const currentIds = this.form.get('produitIds')?.value || [];
    return currentIds.includes(produitId);
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.form.invalid) {
      if (this.form.hasError('dateInvalide')) {
        this.errorMessage = "La date de fin doit être après la date de début";
      }
      return;
    }

    this.loading = true;
    this.errorMessage = "";
    const promo = this.form.value;

    const request = this.editMode && this.promotionId
      ? this.http.put<Promotion>(`${this.apiUrl}/${this.promotionId}`, promo)
      : this.http.post<Promotion>(this.apiUrl, promo);

    request.subscribe({
      next: () => {
        this.router.navigate(['/marketplace/admin/promotions']);
      },
      error: (err) => {
        console.error('Erreur sauvegarde promo:', err);
        this.loading = false;
        this.errorMessage = err?.error?.message || "Erreur lors de l'enregistrement de la promotion.";
      }
    });
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
