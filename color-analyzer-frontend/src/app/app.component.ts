import { Component } from '@angular/core';
import { ColorAnalysisService, ColorAnalysisResponse, ColorFrequencyMap } from './services/color-analysis.service';
import { CommonModule } from '@angular/common'; // Required for *ngIf, *ngFor etc.
import { FormsModule } from '@angular/forms'; // Required for ngModel
import { SpellcheckDisplayDirective } from './directives/spellcheck-display.directive'; // Import the directive

@Component({
  selector: 'app-root',
  standalone: true, // Assuming Angular 17+ standalone components by default from ng new
  imports: [CommonModule, FormsModule, SpellcheckDisplayDirective], // Add FormsModule and SpellcheckDisplayDirective here
  templateUrl: './app.component.html',  // Corrected to conventional name
  styleUrls: ['./app.component.css']    // Corrected to conventional name
})
export class AppComponent {
  title = 'Color Analyzer';
  urlToAnalyze: string = 'https://www.google.com'; // Default or empty
  topNColors: number = 10;
  analysisResult: ColorAnalysisResponse | null = null;
  isLoading: boolean = false;
  errorMessage: string | null = null;

  // For displaying map results
  isResultMap = false;
  resultMap: ColorFrequencyMap | null = null;
  resultArray: string[] | null = null;

  // Content for spellchecking directive
  htmlContent: string = `
    <p>This is a paragarph with some deliberatly mispelled words.</p>
    <p>Another sentance with wrrds like <strong>fantestic</strong> and <em>amazzing</em> to test the spellchekr.</p>
    <p>Hopefully, it corectly identifies these errrors.</p>
    <p>Numbers like 12345 should not be spellchecked, nor should punctuation like ! or ?.</p>
    <p>Let's also try some HTML entities like &amp;amp; &lt; &gt;.</p>
    <div>Nested <span>elements with more <del>txet</del></span> inside.</div>
  `;

  constructor(private colorAnalysisService: ColorAnalysisService) {}

  analyzeUrl(): void {
    if (!this.urlToAnalyze) {
      this.errorMessage = 'Please enter a URL.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    this.analysisResult = null;
    this.isResultMap = false;
    this.resultMap = null;
    this.resultArray = null;

    this.colorAnalysisService.extractColors(this.urlToAnalyze, this.topNColors)
      .subscribe({
        next: (response) => {
          this.analysisResult = response;
          if (Array.isArray(response)) {
            this.isResultMap = false;
            this.resultArray = response;
          } else {
            this.isResultMap = true;
            this.resultMap = response as ColorFrequencyMap;
          }
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = err.message || 'Failed to analyze URL.';
          this.isLoading = false;
        }
      });
  }

  // Helper to get entries from map for *ngFor
  getMapEntries(map: ColorFrequencyMap | null): [string, number][] {
    return map ? Object.entries(map) : [];
  }
}
