import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';


import {AppComponent} from './app.component';
import {HomeComponent} from './home/home.component';
import {EditorComponent} from './editor/editor.component';
import {EditorService} from './editor/editor.service';
import {LoginComponent} from './login/login.component';
import {FormsModule} from '@angular/forms';
import {LoginService} from './login/login.service';
import {AlertComponent} from './alert/alert.component';
import {AlertService} from './alert/alert.service';
import {HttpClientModule} from "@angular/common/http";
import {routing} from "./app.routing";
import {AuthGuard} from "./auth.guard";
import { LogoutComponent } from './logout/logout.component';


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    EditorComponent,
    LoginComponent,
    AlertComponent,
    LogoutComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    routing
  ],
  providers: [
    AuthGuard,
    EditorService,
    LoginService,
    AlertService],
  bootstrap: [AppComponent]
})

export class AppModule { }
