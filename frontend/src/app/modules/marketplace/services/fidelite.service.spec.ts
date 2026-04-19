import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { FideliteService } from './fidelite.service';

describe('FideliteService', () => {
  let service: FideliteService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(FideliteService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
