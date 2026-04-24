import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { CommandeFormComponent } from './commande-form.component';

describe('CommandeFormComponent', () => {
  let component: CommandeFormComponent;
  let fixture: ComponentFixture<CommandeFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommandeFormComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CommandeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
