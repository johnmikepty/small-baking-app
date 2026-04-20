import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  template: `
    <header class="fixed top-0 left-0 right-0 z-50 bg-navbar h-16 flex items-center justify-between px-6 shadow-md">
      <!-- Logo -->
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 bg-primary rounded-full flex items-center justify-center">
          <span class="text-white font-bold text-sm">B</span>
        </div>
        <span class="text-white font-semibold text-lg tracking-wide">SmallBank</span>
      </div>

      <!-- User info + logout -->
      <div class="flex items-center gap-4">
        <span class="text-gray-300 text-sm">
          {{ auth.currentUser()?.firstName }} {{ auth.currentUser()?.lastName }}
        </span>
        <button
          (click)="auth.logout()"
          class="text-sm text-gray-300 hover:text-white border border-gray-500 hover:border-white
                 px-3 py-1 rounded transition-colors duration-200">
          Logout
        </button>
      </div>
    </header>
  `,
})
export class NavbarComponent {
  protected auth = inject(AuthService);
}
