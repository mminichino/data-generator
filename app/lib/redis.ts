import Redis from 'ioredis';
import bcrypt from 'bcryptjs';

const redis = new Redis({
    host: 'localhost',
    port: 10000,
    retryStrategy: (times) => {
        return Math.min(times * 50, 2000);
    },
});

redis.on('connect', () => {
    console.log('Connected to Redis on port 10000');
});

redis.on('error', (err) => {
    console.error('Redis connection error:', err);
});

async function bootstrapAdmin() {
    try {
        const adminKey = 'user:admin';
        const existingAdmin = await redis.get(adminKey);

        if (!existingAdmin) {
            console.log('Creating default admin user');

            const adminId = 'admin-default-id';
            const passwordHash = await bcrypt.hash('admin', 10);

            const adminUser = {
                id: adminId,
                username: 'admin',
                passwordHash,
                createdAt: new Date().toISOString(),
                isDefaultAdmin: true,
            };

            await redis.set(adminKey, JSON.stringify(adminUser));
            await redis.set(`user:id:${adminId}`, JSON.stringify(adminUser));

            console.log('Default admin user created (username: admin, password: admin)');
            console.log('Please change the admin password after first login');
        }
    } catch (error) {
        console.error('Failed to bootstrap admin user:', error);
    }
}

redis.on('ready', () => {
    bootstrapAdmin().then(() => {});
});

export default redis;
