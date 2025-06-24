import { Directive, ElementRef, Input, OnChanges, Renderer2, SimpleChanges, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
// Import as a namespace
import * as NSpellNamespace from 'nspell';

// Define the type for the NSpell instance
// It's likely NSpellNamespace.NSpell if @types/nspell defines NSpell within the module's export scope.
type NSpellInstance = NSpellNamespace.NSpell;

@Directive({
  selector: '[appSpellcheckDisplay]',
  standalone: true
})
export class SpellcheckDisplayDirective implements OnChanges, OnInit {
  @Input() htmlContent: string = '';
  private spell: NSpellInstance | null = null;
  private dictionaryLoaded: boolean = false;

  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadDictionary();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['htmlContent'] && this.dictionaryLoaded) {
      this.updateContent();
    }
  }

  private async loadDictionary(): Promise<void> {
    try {
      const affPromise = this.http.get('assets/en_US.aff', { responseType: 'text' }).toPromise();
      const dicPromise = this.http.get('assets/en_US.dic', { responseType: 'text' }).toPromise();
      const [affData, dicData] = await Promise.all([affPromise, dicPromise]);

      if (affData && dicData) {
        // Try to access the nspell function, assuming it might be on .default or the namespace itself
        const nspellFunction = (NSpellNamespace as any).default || NSpellNamespace;
        this.spell = nspellFunction(affData, dicData);
        this.dictionaryLoaded = true;
        if (this.htmlContent) {
          this.updateContent();
        }
      } else {
        console.error('Failed to load dictionary files.');
        this.dictionaryLoaded = false;
      }
    } catch (error) {
      console.error('Error loading dictionary files:', error);
      this.dictionaryLoaded = false;
    }
  }

  private updateContent(): void {
    if (!this.spell || !this.htmlContent) {
      this.renderer.setProperty(this.el.nativeElement, 'innerHTML', this.htmlContent || '');
      return;
    }

    const spellChecker = this.spell;
    const tempDiv = this.renderer.createElement('div');
    this.renderer.setProperty(tempDiv, 'innerHTML', this.htmlContent);

    this.walkAndSpellcheck(tempDiv, spellChecker);

    this.renderer.setProperty(this.el.nativeElement, 'innerHTML', tempDiv.innerHTML);
  }

  private walkAndSpellcheck(node: Node, spellChecker: NSpellInstance): void {
    if (node.nodeType === Node.TEXT_NODE) {
      const textNode = node as Text;
      let newHtml = '';
      // Regex to split by words, keeping spaces and punctuation as delimiters
      const wordsAndDelimiters = textNode.nodeValue?.split(/(\b\w+\b|[^\w\s]+|\s+)/) || [];

      wordsAndDelimiters.forEach(segment => {
        if (segment) {
          // Check if the segment is a word (contains at least one letter)
          if (/\w/.test(segment) && !/^\d+$/.test(segment) && !spellChecker.correct(segment)) {
            newHtml += `<span style="text-decoration: red wavy underline;">${segment}</span>`;
          } else {
            newHtml += segment;
          }
        }
      });

      if (textNode.parentNode && newHtml !== textNode.nodeValue) {
        const span = this.renderer.createElement('span');
        this.renderer.setProperty(span, 'innerHTML', newHtml);
        // Replace the text node with the new span containing potentially highlighted words
        try {
            textNode.parentNode.replaceChild(span, textNode);
        } catch (e) {
            console.warn("Could not replace text node during spellcheck:", e);
        }
      }
    } else if (node.nodeType === Node.ELEMENT_NODE) {
      // Do not traverse into script or style tags
      if (node.nodeName === 'SCRIPT' || node.nodeName === 'STYLE') {
        return;
      }
      // Create a static list of child nodes before iterating,
      // as DOM manipulation can change the live NodeList.
      const children = Array.from(node.childNodes);
      for (let i = 0; i < children.length; i++) {
        this.walkAndSpellcheck(children[i], spellChecker);
      }
    }
  }
}
