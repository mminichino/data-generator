import { NextRequest, NextResponse } from 'next/server';
import { BACKEND_HOST, BACKEND_PORT } from '@/app/config';

function backendHeaders(userId: string): HeadersInit {
  return {
    'Content-Type': 'application/json',
    ...(userId ? { 'X-User-Id': userId } : {}),
  };
}

export async function POST(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const target = searchParams.get('target') || 'samples';
    const body = await request.json();
    const userId = request.headers.get('x-user-id') || '';

    const backendResponse = await fetch(
      `http://${BACKEND_HOST}:${BACKEND_PORT}/api/generate/${target}`,
      {
        method: 'POST',
        headers: backendHeaders(userId),
        body: JSON.stringify(body),
      }
    );

    const result = await backendResponse.json().catch(() => ({}));
    return NextResponse.json(result, { status: backendResponse.status });
  } catch (error) {
    console.error('Error calling generate endpoint:', error);
    return NextResponse.json({ error: 'Failed to call generate API' }, { status: 500 });
  }
}
