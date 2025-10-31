import type { Metadata } from "next";
import "./globals.css";
import Script from "next/script";

export const metadata: Metadata = {
  title: "Data Generator",
  description: "Generate test data for your applications",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <head>
        <meta charSet="utf-8" />
        <meta httpEquiv="X-UA-Compatible" content="IE=edge" />
        <meta name="viewport" content="width=device-width,initial-scale=1" />
        
        {/* Favicon */}
        <link rel="icon" type="image/png" sizes="16x16" href="/theme/images/favicon.png" />
        
        {/* Stylesheets */}
        <link href="/theme/vendor/bootstrap-select/dist/css/bootstrap-select.min.css" rel="stylesheet" />
        <link href="/theme/css/style.css" rel="stylesheet" />
        
        {/* Google Fonts */}
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@100;200;300;400;500;600;700;800;900&family=Roboto:wght@100;300;400;500;700;900&display=swap" rel="stylesheet" />
      </head>
      <body>
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
                    <div className="dashboard_bar">
                      Data Generator
                    </div>
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
              </ul>
            </div>
          </div>
          
          {/* Content Body */}
          <div className="content-body">
            <div className="container-fluid">
              {children}
            </div>
          </div>
          
          {/* Footer */}
          <div className="footer">
            <div className="copyright">
              <p>Copyright © Data Generator 2025</p>
            </div>
          </div>
        </div>
        
        {/* Scripts */}
        <Script src="/theme/vendor/global/global.min.js" strategy="afterInteractive" />
        <Script src="/theme/vendor/bootstrap-select/dist/js/bootstrap-select.min.js" strategy="afterInteractive" />
        <Script src="/theme/js/custom.min.js" strategy="afterInteractive" />
        <Script src="/theme/js/quixnav-init.js" strategy="afterInteractive" />
      </body>
    </html>
  );
}
