import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component'; // Corrected import path
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig) // Corrected component name
  .catch((err) => console.error(err));
