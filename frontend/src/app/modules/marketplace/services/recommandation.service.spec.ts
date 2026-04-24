import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { RecommandationService } from './recommandation.service';

describe('RecommandationService', () => {
  let service: RecommandationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(RecommandationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
