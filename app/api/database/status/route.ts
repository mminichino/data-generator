import { NextRequest, NextResponse } from 'next/server';
import { BACKEND_HOST, BACKEND_PORT } from "@/app/config";

export async function GET(request: NextRequest) {
    try {
        const userId = request.headers.get('x-user-id') || '';
        const { searchParams } = new URL(request.url);
        const target = searchParams.get('target');
        const backendResponse = await fetch(`http://${BACKEND_HOST}:${BACKEND_PORT}/api/database/connect/${target}/status`, {
            headers: {
                ...(userId ? { 'X-User-Id': userId } : {}),
            }
        });
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
