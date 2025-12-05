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

  const key = `user:${userId}:schemas`;
  const data = await redis.get(key);
  const schemas = data ? JSON.parse(data) : [];
  return NextResponse.json({ schemas });
}

export async function POST(request: NextRequest) {
  const userId = await getUserIdFromRequest(request);
  if (!userId) return unauthorized();
  try {
    const { schema } = await request.json();
    if (!schema || !schema.id) {
      return NextResponse.json({ error: 'Invalid schema payload' }, { status: 400 });
    }
    const key = `user:${userId}:schemas`;
    const existingRaw = await redis.get(key);
    const list = existingRaw ? JSON.parse(existingRaw) as any[] : [];
    const idx = list.findIndex((s) => s.id === schema.id);
    if (idx >= 0) list[idx] = schema; else list.push(schema);
    await redis.set(key, JSON.stringify(list));
    return NextResponse.json({ ok: true });
  } catch (e) {
    console.error('Failed to save schema', e);
    return NextResponse.json({ error: 'Failed to save schema' }, { status: 500 });
  }
}
