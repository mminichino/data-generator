import {NextResponse} from 'next/server';
import bcrypt from 'bcryptjs';
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

        const decoded = jwt.verify(token, JWT_SECRET) as { userId: string; username: string };
        const { username, oldPassword, newPassword } = await request.json();

        if (decoded.username !== 'admin' && decoded.username !== username) {
            return NextResponse.json(
                { error: 'Unauthorized' },
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

        if (decoded.username === username && oldPassword) {
            const isValid = await bcrypt.compare(oldPassword, user.passwordHash);
            if (!isValid) {
                return NextResponse.json(
                    { error: 'Current password is incorrect' },
                    { status: 401 }
                );
            }
        }

        const saltRounds = 10;
        user.passwordHash = await bcrypt.hash(newPassword, saltRounds);
        user.updatedAt = new Date().toISOString();
        if (user.isDefaultAdmin && newPassword !== 'admin') {
            user.isDefaultAdmin = false;
        }

        await redis.set(userKey, JSON.stringify(user));
        await redis.set(`user:id:${user.id}`, JSON.stringify(user));

        return NextResponse.json({ message: 'Password changed successfully' });
    } catch (error) {
        console.error('Change password error:', error);
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}
