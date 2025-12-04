import { NextResponse } from 'next/server';
import bcrypt from 'bcryptjs';
import redis from '@/app/lib/redis';
import { randomUUID } from 'crypto';

export async function POST(request: Request) {
    try {
        const { username, password } = await request.json();

        const userKey = `user:${username}`;
        const existingUser = await redis.get(userKey);

        if (existingUser) {
            return NextResponse.json(
                { error: 'User already exists' },
                { status: 409 }
            );
        }

        const saltRounds = 10;
        const passwordHash = await bcrypt.hash(password, saltRounds);

        const user = {
            id: randomUUID(),
            username,
            passwordHash,
            createdAt: new Date().toISOString(),
        };

        await redis.set(userKey, JSON.stringify(user));

        await redis.set(`user:id:${user.id}`, JSON.stringify(user));

        return NextResponse.json({
            user: { id: user.id, username: user.username },
            message: 'User created successfully',
        });
    } catch (error) {
        console.error('Registration error:', error);
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}
