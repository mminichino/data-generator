import { NextRequest, NextResponse } from 'next/server';
import { BACKEND_HOST, BACKEND_PORT } from '@/app/config';

type RouteContext = {
  params: Promise<{ jobId: string }>;
};

export async function POST(
  request: NextRequest,
  { params }: RouteContext
) {
  try {
    const { jobId } = await params;
    const userId = request.headers.get('x-user-id') || '';
    const backendResponse = await fetch(
      `http://${BACKEND_HOST}:${BACKEND_PORT}/api/generate/jobs/${jobId}/cancel`,
      {
        method: 'POST',
        headers: {
          ...(userId ? { 'X-User-Id': userId } : {}),
        },
      }
    );

    const result = await backendResponse.json().catch(() => ({}));
    return NextResponse.json(result, { status: backendResponse.status });
  } catch (error) {
    console.error('Error calling generation cancel endpoint:', error);
    return NextResponse.json({ error: 'Failed to cancel generation' }, { status: 503 });
  }
}
