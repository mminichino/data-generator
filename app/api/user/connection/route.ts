import { NextRequest, NextResponse } from 'next/server';
import jwt from 'jsonwebtoken';
import redis from '@/app/lib/redis';
import { JWT_SECRET } from '@/app/config';

function unauthorized(msg = 'Not authenticated') {
  return NextResponse.json({ error: msg }, { status: 401 });
}

async function getUserIdFromRequest(request: NextRequest): Promise<string | null> {
  const authHeader = request.headers.get('authorization');
  const token = authHeader?.replace('Bearer ', '');
  if (!token) return null;
  try {
    const decoded = jwt.verify(token, JWT_SECRET) as { userId: string };
    const sessionKey = `session:${token}`;
    const session = await redis.get(sessionKey);
    if (!session) return null;
    return decoded.userId;
  } catch {
    return null;
  }
}

export async function GET(request: NextRequest) {
  const userId = await getUserIdFromRequest(request);
  if (!userId) return unauthorized();
  const key = `user:${userId}:connection`;
  const data = await redis.get(key);
  if (!data) return NextResponse.json({ error: 'Not found' }, { status: 404 });
  return NextResponse.json({ connection: JSON.parse(data) });
}

export async function PUT(request: NextRequest) {
  const userId = await getUserIdFromRequest(request);
  if (!userId) return unauthorized();
  try {
    const { connection } = await request.json();
    if (!connection) return NextResponse.json({ error: 'Invalid payload' }, { status: 400 });
    const key = `user:${userId}:connection`;
    await redis.set(key, JSON.stringify(connection));
    return NextResponse.json({ ok: true });
  } catch (e) {
    console.error('Failed to save connection', e);
    return NextResponse.json({ error: 'Failed to save connection' }, { status: 500 });
  }
}

export async function DELETE(request: NextRequest) {
  const userId = await getUserIdFromRequest(request);
  if (!userId) return unauthorized();
  const key = `user:${userId}:connection`;
  await redis.del(key);
  return NextResponse.json({ ok: true });
}
