import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
})
export class Shell {
  auth = inject(AuthService);
  private router = inject(Router);

  initial(): string {
    return (this.auth.username() ?? '?').charAt(0);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
