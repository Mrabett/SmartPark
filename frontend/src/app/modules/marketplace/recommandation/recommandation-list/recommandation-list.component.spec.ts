import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { RecommandationListComponent } from './recommandation-list.component';

describe('RecommandationListComponent', () => {
  let component: RecommandationListComponent;
  let fixture: ComponentFixture<RecommandationListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecommandationListComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecommandationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
