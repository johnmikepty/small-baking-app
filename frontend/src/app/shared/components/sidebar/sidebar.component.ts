import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface NavItem {
  label: string;
  path: string;
  icon: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <aside class="fixed left-0 top-16 h-[calc(100vh-4rem)] w-64 bg-navbar-dark text-gray-300 flex flex-col shadow-lg">
      <nav class="flex-1 py-6">
        <ul class="space-y-1 px-3">
          @for (item of navItems; track item.path) {
            <li>
              <a
                [routerLink]="item.path"
                routerLinkActive="bg-primary text-white"
                [routerLinkActiveOptions]="{ exact: true }"
                class="flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium
                       hover:bg-navbar hover:text-white transition-colors duration-200">
                <span class="text-lg">{{ item.icon }}</span>
                {{ item.label }}
              </a>
            </li>
          }
        </ul>
      </nav>

      <div class="px-6 py-4 border-t border-gray-700 text-xs text-gray-500">
        SmallBank v0.1.0
      </div>
    </aside>
  `,
})
export class SidebarComponent {
  navItems: NavItem[] = [
    { label: 'Dashboard',    path: '/dashboard',    icon: '🏠' },
    { label: 'Transactions', path: '/transactions', icon: '💸' },
    { label: 'History',      path: '/history',      icon: '📋' },
  ];
}
