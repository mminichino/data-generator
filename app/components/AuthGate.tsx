'use client';

import { ReactNode, useEffect, useState } from 'react';
import { useAuthStore } from '../store/auth/AuthStore';
import { usePathname, useRouter } from 'next/navigation';

interface Props {
  children: ReactNode;
}

export default function AuthGate({ children }: Props) {
  const currentUser = useAuthStore((s) => s.currentUser);
  const token = useAuthStore((s) => s.token);
  const hydrated = useAuthStore((s) => s.hydrated);
  const verifySession = useAuthStore((s) => s.verifySession);

  const [isVerifying, setIsVerifying] = useState(true);
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    if (!hydrated) return;

    const checkSession = async () => {
      if (token && currentUser) {
        const isValid = await verifySession();

        if (!isValid && pathname !== '/login') {
          router.replace('/login');
        }
      }

      setIsVerifying(false);
    };

    checkSession().then(() => {});
  }, [hydrated, token, currentUser, pathname, router, verifySession]);

  useEffect(() => {
    if (!hydrated || isVerifying) return;

    if (!currentUser && pathname !== '/login') {
      router.replace('/login');
      return;
    }

    if (currentUser && pathname === '/login') {
      router.replace('/');
    }
  }, [currentUser, pathname, router, hydrated, isVerifying]);

  if (!hydrated || isVerifying) {
    return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto"></div>
            <p className="mt-4 text-gray-600">Loading...</p>
          </div>
        </div>
    );
  }

  return <>{children}</>;
}
