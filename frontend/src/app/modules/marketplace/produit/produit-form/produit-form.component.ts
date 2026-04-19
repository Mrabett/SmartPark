import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from "@angular/forms";
import { FormsModule } from "@angular/forms";
import { HttpClient } from "@angular/common/http";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { Produit, Stock } from "../../models/marketplace.models";
import { OnInit } from "@angular/core";
import { environment } from '../../../../../environments/environment';

@Component({
  selector: "app-produit-form",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
  templateUrl: "./produit-form.component.html",
  styleUrl: "./produit-form.component.css"
})
export class ProduitFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  submitted = false;
  editMode = false;
  produitId: string | null = null;
  previewHeight = 225;
  previewFit: 'cover' | 'contain' = 'cover';
  private readonly apiUrl = `${environment.apiBaseUrl}/produits`;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      if (params["id"]) {
        this.editMode = true;
        this.produitId = params["id"];
        this.loadProduit(params["id"]);
      }
    });

    this.route.queryParams.subscribe((queryParams) => {
      if (this.editMode) {
        return;
      }

      const values: Partial<Produit> = {};

      if (queryParams['nom']) values.nom = queryParams['nom'];
      if (queryParams['description']) values.description = queryParams['description'];
      if (queryParams['prix']) values.prix = Number(queryParams['prix']);
      if (queryParams['categorie']) values.categorie = queryParams['categorie'];
      if (queryParams['image']) values.image = queryParams['image'];
      if (queryParams['actif'] !== undefined) values.actif = queryParams['actif'] === 'true' || queryParams['actif'] === true;

      if (Object.keys(values).length > 0) {
        this.form.patchValue(values);
      }
    });
  }

  initForm(): void {
    this.form = this.fb.group({
      nom: ["", [Validators.required, Validators.minLength(3)]],
      description: ["", Validators.required],
      prix: ["", [Validators.required, Validators.min(0)]],
      categorie: ["", Validators.required],
      dateExpiration: [""],
      image: [""],
      actif: [true],
      stock: this.fb.group({
        quantiteDisponible: ["", [Validators.required, Validators.min(0)]],
        quantiteMin: ["", [Validators.required, Validators.min(0)]],
        quantiteMax: ["", [Validators.required, Validators.min(0)]]
      })
    });
  }

  loadProduit(id: string): void {
    this.loading = true;
    this.http.get<Produit>(`${this.apiUrl}/${id}`).subscribe({
      next: (produit: Produit) => {
        this.form.patchValue(produit);
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error("Error loading produit:", err);
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.form.invalid) return;

    this.loading = true;
    const produit = this.form.value;

    const request =
      this.editMode && this.produitId
        ? this.http.put<Produit>(`${this.apiUrl}/${this.produitId}`, produit)
        : this.http.post<Produit>(this.apiUrl, produit);

    request.subscribe({
      next: () => {
        this.router.navigate(["/marketplace/admin/produits"]);
      },
      error: (err: unknown) => {
        console.error("Error saving produit:", err);
        this.loading = false;
        alert("Erreur lors de l'enregistrement du produit.");
      }
    });
  }
}
