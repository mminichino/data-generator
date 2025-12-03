'use client';

import AuthGate from './AuthGate';
import UserMenu from './UserMenu';
import { usePathname } from 'next/navigation';
import { useAuthStore } from '../store/auth/AuthStore';
import React from 'react';
import Script from 'next/script';

export default function LayoutChrome({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const currentUser = useAuthStore((s) => s.currentUser);

  if (!currentUser && pathname !== '/login') {
    return (
      <div style={{ minHeight: '100vh' }}>
        <AuthGate>
          <></>
        </AuthGate>
      </div>
    );
  }

  if (pathname === '/login') {
    return (
      <div style={{ minHeight: '100vh' }} className="d-flex align-items-center justify-content-center bg-light">
        <div className="container">
          <AuthGate>
            {children}
          </AuthGate>
        </div>
      </div>
    );
  }

  return (
    <div id="main-wrapper">
      {/* Nav Header */}
      <div className="nav-header">
        <a href="/" className="brand-logo">
          <img className="logo-abbr" src="/images/logo.png" alt="Redis" />
          <img className="logo-compact" src="/images/logo-text.png" alt="Data Generator" />
          <img className="brand-title" src="/images/logo-text.png" alt="Data Generator" />
        </a>
        <div className="nav-control">
          <div className="hamburger">
            <span className="line"></span>
            <span className="line"></span>
            <span className="line"></span>
          </div>
        </div>
      </div>

      {/* Header */}
      <div className="header">
        <div className="header-content">
          <nav className="navbar navbar-expand">
            <div className="collapse navbar-collapse justify-content-between">
              <div className="header-left">
                <div className="dashboard_bar">Data Generator</div>
              </div>
              <div className="header-right">
                <ul className="navbar-nav header-right">
                  <li className="nav-item">
                    {/* Current user menu */}
                    <UserMenu />
                  </li>
                </ul>
              </div>
            </div>
          </nav>
        </div>
      </div>

      {/* Sidebar */}
      <div className="quixnav">
        <div className="quixnav-scroll">
          <ul className="metismenu" id="menu">
            <li className="nav-label first">Main Menu</li>
            <li>
              <a href="/" aria-expanded="false">
                <i className="flaticon-381-networking"></i>
                <span className="nav-text">Schemas</span>
              </a>
            </li>
            <li>
              <a href="/generate" aria-expanded="false">
                <i className="flaticon-381-television"></i>
                <span className="nav-text">Generate Data</span>
              </a>
            </li>
            <li>
              <a href="/connection" aria-expanded="false">
                <i className="flaticon-381-controls-3"></i>
                <span className="nav-text">Connection</span>
              </a>
            </li>
            <li>
              <a href="/users" aria-expanded="false">
                <i className="flaticon-381-user-7"></i>
                <span className="nav-text">User Administration</span>
              </a>
            </li>
          </ul>
        </div>
      </div>

      {/* Content Body */}
      <div className="content-body">
        <div className="container-fluid">
          <AuthGate>
            {children}
          </AuthGate>
        </div>
      </div>

      {/* Footer */}
      <div className="footer">
        <div className="copyright">
          <p>Copyright © Data Generator 2025</p>
        </div>
      </div>
      <Script id="jquery-core" src="/theme/assets/js/lib/data-table/jquery-3.6.0.min.js" strategy="afterInteractive"/>
      <Script id="jquery-global-bridge" strategy="afterInteractive">
        {`window.$ = window.$ || window.jQuery;`}
      </Script>
      <Script src="/theme/vendor/global/global.min.js" strategy="afterInteractive" />
      <Script src="/theme/vendor/metismenu/js/metisMenu.min.js" strategy="afterInteractive" />
      <Script src="/theme/vendor/bootstrap-select/dist/js/bootstrap-select.min.js" strategy="afterInteractive" />
      <Script src="/theme/js/custom.min.js" strategy="afterInteractive" />
      <Script src="/theme/js/quixnav-init.js" strategy="afterInteractive" />
    </div>
  );
}
