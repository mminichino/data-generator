import { NextResponse } from 'next/server';
import redis from '@/app/lib/redis';

export async function POST(request: Request) {
    try {
        const { token } = await request.json();

        if (token) {
            const sessionKey = `session:${token}`;
            await redis.del(sessionKey);
        }

        return NextResponse.json({ message: 'Logged out successfully' });
    } catch (error) {
        console.error('Logout error:', error);
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}
