import { NextResponse } from 'next/server';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import redis from '@/app/lib/redis';
import { JWT_SECRET } from '@/app/config';

export async function POST(request: Request) {
    try {
        const { username, password } = await request.json();

        const userKey = `user:${username}`;
        const userData = await redis.get(userKey);

        if (!userData) {
            return NextResponse.json(
                { error: 'Invalid credentials' },
                { status: 401 }
            );
        }

        const user = JSON.parse(userData);

        const isValid = await bcrypt.compare(password, user.passwordHash);

        if (!isValid) {
            return NextResponse.json(
                { error: 'Invalid credentials' },
                { status: 401 }
            );
        }

        const token = jwt.sign(
            { userId: user.id, username: user.username },
            JWT_SECRET,
            { expiresIn: '7d' }
        );

        const sessionKey = `session:${token}`;
        await redis.setex(
            sessionKey,
            7 * 24 * 60 * 60,
            JSON.stringify({ userId: user.id, username: user.username })
        );

        return NextResponse.json({
            user: { id: user.id, username: user.username },
            token,
        });
    } catch (error) {
        console.error('Login error:', error);
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}
