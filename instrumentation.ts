export async function register() {
    if (process.env.NEXT_RUNTIME === 'nodejs') {
        const { default: redis } = await import('./app/lib/redis');

        console.log('Server starting - initializing Redis');

        try {
            await redis.ping();
            console.log('Redis connected successfully');
        } catch (error) {
            console.error('Failed to connect to Redis:', error);
        }
    }
}
