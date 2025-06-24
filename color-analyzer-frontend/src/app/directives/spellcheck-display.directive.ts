import { Directive, ElementRef, Input, OnChanges, Renderer2, SimpleChanges, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import * as NSpell from 'nspell';

@Directive({
  selector: '[appSpellcheckDisplay]',
  standalone: true
})
export class SpellcheckDisplayDirective implements OnChanges, OnInit {
  @Input() htmlContent: string = '';

  private spell: NSpell.NSpell | null = null;
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
        this.spell = NSpell(affData, dicData);
        this.dictionaryLoaded = true;
        // If htmlContent was already set, update it now
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
      // If spell is not loaded or no content, just set original HTML
      this.renderer.setProperty(this.el.nativeElement, 'innerHTML', this.htmlContent || '');
      return;
    }

    const spellChecker = this.spell;
    const tempDiv = this.renderer.createElement('div');
    this.renderer.setProperty(tempDiv, 'innerHTML', this.htmlContent);

    this.walkAndSpellcheck(tempDiv, spellChecker);

    this.renderer.setProperty(this.el.nativeElement, 'innerHTML', tempDiv.innerHTML);
  }

  private walkAndSpellcheck(node: Node, spellChecker: NSpell.NSpell): void {
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
        // This needs to be done carefully to avoid issues if the parent is not an element or if the node is already removed.
        try {
            textNode.parentNode.replaceChild(span, textNode);
        } catch (e) {
            // Fallback or error logging if replaceChild fails
            // This can happen if the text node was part of a larger replacement
            // For simplicity, we'll log and it might mean some deeply nested text isn't spellchecked
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
