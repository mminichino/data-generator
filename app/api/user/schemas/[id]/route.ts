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

export async function GET(request: NextRequest, { params }: { params: { id: string } }) {
  const userId = await getUserIdFromRequest(request);
  if (!userId) return unauthorized();
  const key = `user:${userId}:schemas`;
  const data = await redis.get(key);
  const list = data ? (JSON.parse(data) as any[]) : [];
  const item = list.find((s) => s.id === params.id);
  if (!item) return NextResponse.json({ error: 'Not found' }, { status: 404 });
  return NextResponse.json({ schema: item });
}

export async function PUT(request: NextRequest, { params }: { params: { id: string } }) {
  const userId = await getUserIdFromRequest(request);
  if (!userId) return unauthorized();
  try {
    const { schema } = await request.json();
    if (!schema || !schema.id || schema.id !== params.id) {
      return NextResponse.json({ error: 'Invalid schema payload' }, { status: 400 });
    }
    const key = `user:${userId}:schemas`;
    const data = await redis.get(key);
    const list = data ? (JSON.parse(data) as any[]) : [];
    const idx = list.findIndex((s) => s.id === params.id);
    if (idx === -1) list.push(schema); else list[idx] = schema;
    await redis.set(key, JSON.stringify(list));
    return NextResponse.json({ ok: true });
  } catch (e) {
    console.error('Failed to update schema', e);
    return NextResponse.json({ error: 'Failed to update schema' }, { status: 500 });
  }
}

export async function DELETE(request: NextRequest, { params }: { params: { id: string } }) {
  const userId = await getUserIdFromRequest(request);
  if (!userId) return unauthorized();
  const key = `user:${userId}:schemas`;
  const data = await redis.get(key);
  const list = data ? (JSON.parse(data) as any[]) : [];
  const filtered = list.filter((s) => s.id !== params.id);
  await redis.set(key, JSON.stringify(filtered));
  return NextResponse.json({ ok: true });
}
