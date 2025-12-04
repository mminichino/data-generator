import { NextResponse } from 'next/server';
import jwt from 'jsonwebtoken';
import redis from '@/app/lib/redis';

const JWT_SECRET = process.env.JWT_SECRET || 'secret-key';

export async function POST(request: Request) {
    try {
        const authHeader = request.headers.get('authorization');
        const token = authHeader?.replace('Bearer ', '');

        if (!token) {
            return NextResponse.json(
                { error: 'Not authenticated' },
                { status: 401 }
            );
        }

        jwt.verify(token, JWT_SECRET);
        const { username } = await request.json();

        if (username === 'admin') {
            return NextResponse.json(
                { error: 'Cannot delete admin user' },
                { status: 403 }
            );
        }

        const userKey = `user:${username}`;
        const userData = await redis.get(userKey);

        if (!userData) {
            return NextResponse.json(
                { error: 'User not found' },
                { status: 404 }
            );
        }

        const user = JSON.parse(userData);

        await redis.del(userKey);
        await redis.del(`user:id:${user.id}`);

        return NextResponse.json({ message: 'User deleted successfully' });
    } catch (error) {
        console.error('Delete user error:', error);
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}
