import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import Link from "next/link";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Database Schema Generator",
  description: "Create and generate data for database schemas",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <nav className="bg-gray-800 text-white p-4">
          <div className="container mx-auto flex gap-6">
            <Link href="/" className="hover:text-gray-300">
              Schemas
            </Link>
            <Link href="/connection" className="hover:text-gray-300">
              Database Connection
            </Link>
            <Link href="/generate" className="hover:text-gray-300">
              Generate Data
            </Link>
          </div>
        </nav>
        <main className="container mx-auto p-6">{children}</main>
      </body>
    </html>
  );
}
