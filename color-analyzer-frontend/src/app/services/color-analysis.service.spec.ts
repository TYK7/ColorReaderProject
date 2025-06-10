import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ColorAnalysisService } from './color-analysis.service';

describe('ColorAnalysisService', () => {
  let service: ColorAnalysisService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ColorAnalysisService]
    });
    service = TestBed.inject(ColorAnalysisService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  afterEach(() => {
    httpMock.verify(); // Verify that no unmatched requests are outstanding
  });

  // Add more tests here
  it('should retrieve colors for a valid URL', () => {
    const testUrl = 'http://example.com';
    const topN = 5;
    const mockResponse = ['#FF0000', '#00FF00'];

    service.extractColors(testUrl, topN).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`/api/extract-colors?url=${testUrl}&topN=${topN}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

   it('should handle empty URL string', (done) => {
    service.extractColors('', 5).subscribe({
      next: () => done.fail('should have failed with an empty URL'),
      error: (error: Error) => {
        expect(error.message).toContain('URL cannot be empty.');
        done();
      }
    });
  });

});
