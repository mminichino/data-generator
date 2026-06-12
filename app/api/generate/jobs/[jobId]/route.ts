import { NextRequest, NextResponse } from 'next/server';
import { BACKEND_HOST, BACKEND_PORT } from '@/app/config';

type RouteContext = {
  params: Promise<{ jobId: string }>;
};

export async function GET(
  request: NextRequest,
  { params }: RouteContext
) {
  try {
    const { jobId } = await params;
    const userId = request.headers.get('x-user-id') || '';
    const backendResponse = await fetch(
      `http://${BACKEND_HOST}:${BACKEND_PORT}/api/generate/jobs/${jobId}`,
      {
        headers: {
          ...(userId ? { 'X-User-Id': userId } : {}),
        },
      }
    );

    const result = await backendResponse.json().catch(() => ({}));
    return NextResponse.json(result, { status: backendResponse.status });
  } catch (error) {
    console.error('Error calling generation status endpoint:', error);
    return NextResponse.json({ error: 'Failed to fetch generation status' }, { status: 503 });
  }
}
