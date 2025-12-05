export const JWT_SECRET = process.env.JWT_SECRET || 'secret-key';
export const REDIS_HOST = process.env.REDIS_HOST || "127.0.0.1";
export const REDIS_PORT = Number(process.env.REDIS_PORT) || 10000;
export const BACKEND_HOST = process.env.BACKEND_HOST || "127.0.0.1";
export const BACKEND_PORT = Number(process.env.BACKEND_PORT) || 8084;
