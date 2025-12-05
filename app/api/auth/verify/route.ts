import { NextResponse } from 'next/server';
import jwt from 'jsonwebtoken';
import redis from '@/app/lib/redis';
import { JWT_SECRET } from '@/app/config';

export async function POST(request: Request) {
    try {
        const { token } = await request.json();

        const decoded = jwt.verify(token, JWT_SECRET) as { userId: string; email: string };

        const sessionKey = `session:${token}`;
        const session = await redis.get(sessionKey);

        if (!session) {
            return NextResponse.json(
                { error: 'Invalid or expired session' },
                { status: 401 }
            );
        }

        return NextResponse.json({
            user: decoded,
            valid: true,
        });
    } catch (error) {
        return NextResponse.json(
            { error: 'Invalid token', valid: false },
            { status: 401 }
        );
    }
}
