import type { Metadata } from "next";
import "./globals.css";
import LayoutChrome from "./components/LayoutChrome";

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
        <LayoutChrome>
          {children}
        </LayoutChrome>
      </body>
      </html>
  );
}
