export function generateUUID(): string {
    // Check if crypto.randomUUID is available (modern browsers and Node 16.7+)
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        return crypto.randomUUID();
    }

    // Fallback implementation
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0;
        const v = c === 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
}

export function getUserId(): string {
    if (typeof window === 'undefined') return 'server';
    try {
        const key = 'dg_user_id';
        let id = localStorage.getItem(key);
        if (!id) {
            id = generateUUID();
            localStorage.setItem(key, id);
        }
        return id;
    } catch (_e) {
        // Fallback when localStorage is unavailable
        return generateUUID();
    }
}
