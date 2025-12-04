import { NextResponse } from 'next/server';
import jwt from 'jsonwebtoken';
import redis from '@/app/lib/redis';

const JWT_SECRET = process.env.JWT_SECRET || 'secret-key';

export async function GET(request: Request) {
    try {
        const authHeader = request.headers.get('authorization');
        const token = authHeader?.replace('Bearer ', '');

        if (!token) {
            return NextResponse.json(
                { error: 'Not authenticated' },
                { status: 401 }
            );
        }

        const decoded = jwt.verify(token, JWT_SECRET) as { userId: string; username: string };

        if (decoded.username !== 'admin') {
            return NextResponse.json(
                { error: 'Unauthorized - admin access required' },
                { status: 403 }
            );
        }

        const userKeys = await redis.keys('user:*');

        const emailKeys = userKeys.filter(key => !key.startsWith('user:id:'));

        const userPromises = emailKeys.map(async (key) => {
            const userData = await redis.get(key);
            if (!userData) return null;

            const user = JSON.parse(userData);
            return {
                id: user.id,
                username: user.username,
                createdAt: user.createdAt,
                isDefaultAdmin: user.isDefaultAdmin || false,
            };
        });

        const users = (await Promise.all(userPromises)).filter(Boolean);

        return NextResponse.json({ users });
    } catch (error) {
        console.error('List users error:', error);
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}
