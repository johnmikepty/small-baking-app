import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, NavbarComponent, SidebarComponent],
  template: `
    <div class="min-h-screen bg-gray-100">
      @if (auth.isAuthenticated()) {
        <app-navbar />
        <div class="flex">
          <app-sidebar />
          <main class="flex-1 p-6 ml-64 mt-16">
            <router-outlet />
          </main>
        </div>
      } @else {
        <router-outlet />
      }
    </div>
  `,
})
export class AppComponent {
  protected auth = inject(AuthService);
}
