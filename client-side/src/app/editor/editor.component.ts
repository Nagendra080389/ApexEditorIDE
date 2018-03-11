import { Component, OnInit } from '@angular/core';
import {EditorService} from './editor.service';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.css']
})
export class EditorComponent implements OnInit {

  constructor(private editorService: EditorService) { }

  ngOnInit() {
  }

}
