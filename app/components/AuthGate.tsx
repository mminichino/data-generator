'use client';

import { ReactNode, useEffect } from 'react';
import { useAuthStore } from '../store/auth/AuthStore';
import { usePathname, useRouter } from 'next/navigation';

interface Props {
  children: ReactNode;
}

export default function AuthGate({ children }: Props) {
  const currentUser = useAuthStore((s) => s.currentUser);
  const hydrated = useAuthStore((s) => (s as any).hydrated ?? false);
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    if (!hydrated) return;
    if (!currentUser && pathname !== '/login') {
      router.replace('/login');
      return;
    }
    if (currentUser && pathname === '/login') {
      router.replace('/');
    }
  }, [currentUser, pathname, router, hydrated]);

  return <>{children}</>;
}
