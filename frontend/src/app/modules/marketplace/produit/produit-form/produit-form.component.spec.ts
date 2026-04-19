import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ProduitFormComponent } from './produit-form.component';

describe('ProduitFormComponent', () => {
  let component: ProduitFormComponent;
  let fixture: ComponentFixture<ProduitFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProduitFormComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProduitFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
