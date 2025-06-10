import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface ColorFrequencyMap {
  [color: string]: number;
}

// Can be string[] if only topN is returned, or the map, or a custom interface
export type ColorAnalysisResponse = string[] | ColorFrequencyMap;

@Injectable({
  providedIn: 'root'
})
export class ColorAnalysisService {
  // Adjust the backend URL as necessary.
  // This will be proxied during development to avoid CORS issues initially.
  private backendUrl = '/api/extract-colors'; // Using proxy, will be http://localhost:8080/api/extract-colors

  constructor(private http: HttpClient) { }

  extractColors(url: string, topN: number = 10): Observable<ColorAnalysisResponse> {
    if (!url || url.trim() === '') {
      return throwError(() => new Error('URL cannot be empty.'));
    }

    let params = new HttpParams();
    params = params.append('url', url);
    params = params.append('topN', topN.toString());

    return this.http.get<ColorAnalysisResponse>(this.backendUrl, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: any) {
    console.error('An error occurred:', error);
    // Could be a HttpErrorResponse
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else if (error.status) {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message || error.body?.error || error.error}`;
      if (error.error && typeof error.error === 'string') {
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.error}`;
      } else if (error.error && error.error.message) {
         errorMessage = `Error Code: ${error.status}\nMessage: ${error.error.message}`;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}
