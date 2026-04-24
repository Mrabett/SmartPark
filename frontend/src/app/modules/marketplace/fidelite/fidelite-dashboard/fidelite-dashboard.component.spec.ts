import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { FideliteDashboardComponent } from './fidelite-dashboard.component';

describe('FideliteDashboardComponent', () => {
  let component: FideliteDashboardComponent;
  let fixture: ComponentFixture<FideliteDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FideliteDashboardComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FideliteDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
