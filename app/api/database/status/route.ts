import { NextResponse } from 'next/server';
import { BACKEND_HOST, BACKEND_PORT } from "@/app/config";

export async function GET() {
    try {
        const backendResponse = await fetch(`http://${BACKEND_HOST}:${BACKEND_PORT}/api/database/status`);
        if (!backendResponse.ok) {
            console.log(`Status responded with: ${backendResponse.status}`);
            return NextResponse.json(
                { response: backendResponse.json() },
                { status: backendResponse.status }
            );
        }
        const result = await backendResponse.json();
        return NextResponse.json(result);
    } catch (error) {
        console.error('Error calling status endpoint:', error);
        return NextResponse.json(
            { error: 'Failed to call status API' },
            { status: 500 }
        );
    }
}
